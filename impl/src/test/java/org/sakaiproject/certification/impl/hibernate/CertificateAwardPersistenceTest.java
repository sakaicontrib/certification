/**
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.certification.impl.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.exception.ConstraintViolationException;

import org.hsqldb.jdbc.JDBCDataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.certification.api.AwardDateUnavailableException;
import org.sakaiproject.certification.api.CertificateAward;
import org.sakaiproject.certification.api.CertificateDefinition;
import org.sakaiproject.certification.api.CertificateDefinitionStatus;
import org.sakaiproject.certification.api.DocumentTemplate;
import org.sakaiproject.certification.api.UnmetCriteriaException;
import org.sakaiproject.certification.api.criteria.gradebook.DueDatePassedCriterion;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.SiteService;

public class CertificateAwardPersistenceTest {

    private static final String MAPPING_ROOT = "org/sakaiproject/certification/impl/";
    private static final String[] MAPPING_RESOURCES = {
        MAPPING_ROOT + "CertificateAward.hbm.xml",
        MAPPING_ROOT + "CertificateDefinition.hbm.xml",
        MAPPING_ROOT + "DocumentTemplate.hbm.xml",
        MAPPING_ROOT + "criteria/AbstractCriterion.hbm.xml",
        MAPPING_ROOT + "criteria/gradebook/GreaterThanScoreCriterion.hbm.xml",
        MAPPING_ROOT + "criteria/gradebook/DueDatePassedCriterion.hbm.xml",
        MAPPING_ROOT + "criteria/gradebook/FinalGradeScoreCriterion.hbm.xml",
        MAPPING_ROOT + "criteria/gradebook/WillExpireCriterion.hbm.xml",
        MAPPING_ROOT + "criteria/gradebook/CertAssignment.hbm.xml",
        MAPPING_ROOT + "criteria/gradebook/CertCategory.hbm.xml",
        MAPPING_ROOT + "criteria/gradebook/CertGradebook.hbm.xml",
        MAPPING_ROOT + "criteria/gradebook/CertGradeRecord.hbm.xml"
    };

    private StandardServiceRegistry registry;
    private SessionFactory sessionFactory;
    private HibernateTransactionManager transactionManager;
    private CertificateServiceHibernateImpl certificateService;
    private MutableIssueDateCriteriaFactory criteriaFactory;
    private Set<String> awardableSiteReferences;

    @Before
    public void setUp() {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:certificate-award-" + UUID.randomUUID());
        dataSource.setUser("sa");
        dataSource.setPassword("");

        registry = new StandardServiceRegistryBuilder()
            .applySetting("hibernate.dialect", "org.hibernate.dialect.HSQLDialect")
            .applySetting("hibernate.connection.datasource", dataSource)
            .applySetting("hibernate.current_session_context_class",
                "org.springframework.orm.hibernate5.SpringSessionContext")
            .applySetting("hibernate.hbm2ddl.auto", "create-drop")
            .applySetting("hibernate.show_sql", "false")
            .build();

        MetadataSources metadataSources = new MetadataSources(registry);
        for (String resource : MAPPING_RESOURCES) {
            metadataSources.addResource(resource);
        }
        sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
        transactionManager = new HibernateTransactionManager(sessionFactory);

        certificateService = new CertificateServiceHibernateImpl();
        certificateService.setSessionFactory(sessionFactory);
        certificateService.setContentHostingService(createContentHostingService());
        awardableSiteReferences = new HashSet<>();
        awardableSiteReferences.add("/site/site");
        certificateService.setSiteService(createSiteService());
        certificateService.setSecurityService(createSecurityService());
        criteriaFactory = new MutableIssueDateCriteriaFactory();
        certificateService.registerCriteriaFactory(criteriaFactory);
    }

    @After
    public void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    public void firstCalculatedAwardDateIsPersistedAndNeverOverwritten() throws Exception {
        String certificateDefinitionId = saveCertificateDefinition();
        Date firstCalculatedDate = new Date(1_777_776_400_000L);
        criteriaFactory.setIssueDate(firstCalculatedDate);

        CertificateAward first = inTransaction(() ->
            certificateService.awardCertificate(certificateDefinitionId, "student"));

        Date laterCalculatedDate = new Date(firstCalculatedDate.getTime() + 86_400_000L);
        criteriaFactory.setIssueDate(laterCalculatedDate);
        CertificateAward second = inTransaction(() ->
            certificateService.awardCertificate(certificateDefinitionId, "student"));

        assertNotNull(first.getId());
        assertEquals(first.getId(), second.getId());
        assertEquals(firstCalculatedDate, second.getAwardedAt());
        assertEquals(1L, countAwards());
    }

    @Test
    public void existingAwardIsReturnedAfterEligibilityDataChanges() throws Exception {
        String certificateDefinitionId = saveCertificateDefinition();
        Date awardedAt = new Date(1_777_776_400_000L);
        criteriaFactory.setIssueDate(awardedAt);
        inTransaction(() -> certificateService.awardCertificate(certificateDefinitionId, "student"));

        criteriaFactory.setIssueDate(null);
        CertificateAward persisted = inTransaction(() ->
            certificateService.awardCertificate(certificateDefinitionId, "student"));

        assertEquals(awardedAt, persisted.getAwardedAt());
        assertEquals(1L, countAwards());
    }

    @Test
    public void existingAwardIsReturnedAfterDefinitionBecomesInactive() throws Exception {
        String certificateDefinitionId = saveCertificateDefinition();
        Date awardedAt = new Date(1_777_776_400_000L);
        criteriaFactory.setIssueDate(awardedAt);
        CertificateAward issued = inTransaction(() ->
            certificateService.awardCertificate(certificateDefinitionId, "student"));

        inTransaction(() -> {
            CertificateDefinition definition = sessionFactory.getCurrentSession()
                .get(CertificateDefinition.class, certificateDefinitionId);
            definition.setStatus(CertificateDefinitionStatus.INACTIVE);
            return null;
        });

        CertificateAward persisted = inTransaction(() ->
            certificateService.awardCertificate(certificateDefinitionId, "student"));

        assertEquals(issued.getId(), persisted.getId());
        assertEquals(awardedAt, persisted.getAwardedAt());
        assertEquals(1L, countAwards());
    }

    @Test
    public void awardDateCannotBeMutatedThroughTheApi() throws Exception {
        String certificateDefinitionId = saveCertificateDefinition();
        Date awardedAt = new Date(1_777_776_400_000L);
        criteriaFactory.setIssueDate(awardedAt);
        CertificateAward award = inTransaction(() ->
            certificateService.awardCertificate(certificateDefinitionId, "student"));

        Date returnedDate = award.getAwardedAt();
        returnedDate.setTime(returnedDate.getTime() + 86_400_000L);

        assertEquals(awardedAt, award.getAwardedAt());
        CertificateAward reloaded = inTransaction(() ->
            certificateService.getCertificateAwardForUser(certificateDefinitionId, "student"));
        assertEquals(awardedAt, reloaded.getAwardedAt());
    }

    @Test
    public void unmetCriteriaAreReportedAndAwardIsNotCreated() throws Exception {
        String certificateDefinitionId = saveCertificateDefinition();
        criteriaFactory.setIssueDate(null);

        try {
            inTransaction(() -> certificateService.awardCertificate(certificateDefinitionId, "student"));
            fail("Expected UnmetCriteriaException");
        } catch (UnmetCriteriaException expected) {
            assertNotNull(expected.getUnmetConditions());
            assertEquals(1, expected.getUnmetConditions().size());
        }

        assertNull(inTransaction(() ->
            certificateService.getCertificateAwardForUser(certificateDefinitionId, "student")));
        assertEquals(0L, countAwards());
    }

    @Test
    public void awardDateFailureIsDistinctFromUnmetCriteria() throws Exception {
        String certificateDefinitionId = saveCertificateDefinition();
        criteriaFactory.setIssueDate(null);
        criteriaFactory.setCriterionMet(true);

        try {
            inTransaction(() -> certificateService.awardCertificate(certificateDefinitionId, "student"));
            fail("Expected AwardDateUnavailableException");
        } catch (AwardDateUnavailableException expected) {
            assertTrue(expected.getMessage().contains("award date"));
        }

        assertEquals(0L, countAwards());
    }

    @Test
    public void inactiveAndUnpublishedDefinitionsCannotCreateAwards() throws Exception {
        criteriaFactory.setIssueDate(new Date(1_777_776_400_000L));

        for (CertificateDefinitionStatus status : new CertificateDefinitionStatus[] {
                CertificateDefinitionStatus.INACTIVE, CertificateDefinitionStatus.UNPUBLISHED }) {
            String certificateDefinitionId = saveCertificateDefinition(status, "site");
            try {
                inTransaction(() -> certificateService.awardCertificate(certificateDefinitionId, "student"));
                fail("Expected UnmetCriteriaException for " + status);
            } catch (UnmetCriteriaException expected) {
                assertNotNull(expected.getUnmetConditions());
                assertTrue(expected.getUnmetConditions().isEmpty());
            }
        }

        assertEquals(0L, countAwards());
    }

    @Test
    public void awardPermissionIsCheckedAgainstTheDefinitionSite() throws Exception {
        String certificateDefinitionId = saveCertificateDefinition(CertificateDefinitionStatus.ACTIVE, "other-site");
        criteriaFactory.setIssueDate(new Date(1_777_776_400_000L));

        try {
            inTransaction(() -> certificateService.awardCertificate(certificateDefinitionId, "student"));
            fail("Expected PermissionException");
        } catch (PermissionException expected) {
            assertEquals("/site/other-site", expected.getResource());
        }

        assertEquals(0L, countAwards());
    }

    @Test
    public void databaseRejectsDuplicateAwardsForTheSameCertificateAndUser() throws Exception {
        String certificateDefinitionId = saveCertificateDefinition();

        try {
            inTransaction(() -> {
                Session session = sessionFactory.getCurrentSession();
                CertificateDefinition definition = session.get(CertificateDefinition.class, certificateDefinitionId);
                session.save(new CertificateAward("student", definition, new Date(1_777_776_400_000L)));
                session.save(new CertificateAward("student", definition, new Date(1_777_862_800_000L)));
                session.flush();
                return null;
            });
            fail("Expected the certificate/user unique key to reject a duplicate award");
        } catch (RuntimeException expected) {
            assertTrue(hasCause(expected, ConstraintViolationException.class));
        }
    }

    @Test
    public void concurrentIssuanceReturnsOnePersistedAward() throws Exception {
        String certificateDefinitionId = saveCertificateDefinition();
        criteriaFactory.setIssueDate(new Date(1_777_776_400_000L));
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<CertificateAward> first = executor.submit(() -> {
                ready.countDown();
                start.await();
                return inTransaction(() ->
                    certificateService.awardCertificate(certificateDefinitionId, "student"));
            });
            Future<CertificateAward> second = executor.submit(() -> {
                ready.countDown();
                start.await();
                return inTransaction(() ->
                    certificateService.awardCertificate(certificateDefinitionId, "student"));
            });

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            assertEquals(first.get(10, TimeUnit.SECONDS).getId(), second.get(10, TimeUnit.SECONDS).getId());
            assertEquals(1L, countAwards());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void deletingCertificateDefinitionAlsoDeletesItsAwards() throws Exception {
        String certificateDefinitionId = saveCertificateDefinition();
        criteriaFactory.setIssueDate(new Date(1_777_776_400_000L));
        inTransaction(() -> certificateService.awardCertificate(certificateDefinitionId, "student"));

        inTransaction(() -> {
            certificateService.deleteCertificateDefinition(certificateDefinitionId);
            return null;
        });

        assertEquals(0L, countAwards());
        assertNull(inTransaction(() ->
            sessionFactory.getCurrentSession().get(CertificateDefinition.class, certificateDefinitionId)));
    }

    private String saveCertificateDefinition() {
        return saveCertificateDefinition(CertificateDefinitionStatus.ACTIVE, "site");
    }

    private String saveCertificateDefinition(CertificateDefinitionStatus status, String siteId) {
        CertificateDefinition definition = new CertificateDefinition();
        definition.setName("Certificate " + UUID.randomUUID());
        definition.setDescription("description");
        definition.setCreateDate(new Date());
        definition.setCreatorUserId("creator");
        definition.setSiteId(siteId);
        definition.setProgressHidden(false);
        definition.setStatus(status);
        definition.addAwardCriterion(new DueDatePassedCriterion());
        DocumentTemplate template = new DocumentTemplate();
        template.setName("certificate.pdf");
        template.setOutputMimeType("application/pdf");
        template.setResourceId("/certificate.pdf");
        template.setCertificateDefinition(definition);
        definition.setDocumentTemplate(template);

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.save(definition);
            transaction.commit();
        }
        return definition.getId();
    }

    private SiteService createSiteService() {
        return (SiteService) Proxy.newProxyInstance(
            SiteService.class.getClassLoader(),
            new Class<?>[] {SiteService.class},
            (proxy, method, args) -> {
                if ("siteReference".equals(method.getName())) {
                    return "/site/" + args[0];
                }
                return defaultValue(method.getReturnType());
            });
    }

    private SecurityService createSecurityService() {
        return (SecurityService) Proxy.newProxyInstance(
            SecurityService.class.getClassLoader(),
            new Class<?>[] {SecurityService.class},
            (proxy, method, args) -> {
                if ("isSuperUser".equals(method.getName())) {
                    return false;
                }
                if ("unlock".equals(method.getName())) {
                    return awardableSiteReferences.contains(args[2]);
                }
                return defaultValue(method.getReturnType());
            });
    }

    private ContentHostingService createContentHostingService() {
        return (ContentHostingService) Proxy.newProxyInstance(
            ContentHostingService.class.getClassLoader(),
            new Class<?>[] {ContentHostingService.class},
            (proxy, method, args) -> {
                if ("getContainingCollectionId".equals(method.getName())) {
                    return "/";
                }
                return defaultValue(method.getReturnType());
            });
    }

    private Object defaultValue(Class<?> returnType) {
        if (returnType.equals(boolean.class)) {
            return false;
        }
        if (returnType.equals(int.class)) {
            return 0;
        }
        if (returnType.equals(long.class)) {
            return 0L;
        }
        if (returnType.equals(short.class)) {
            return (short) 0;
        }
        if (returnType.equals(byte.class)) {
            return (byte) 0;
        }
        if (returnType.equals(float.class)) {
            return 0.0F;
        }
        if (returnType.equals(double.class)) {
            return 0.0;
        }
        if (returnType.equals(char.class)) {
            return '\0';
        }
        return null;
    }

    private long countAwards() throws Exception {
        return inTransaction(() -> (Long) sessionFactory.getCurrentSession()
            .createQuery("select count(a.id) from CertificateAward a")
            .uniqueResult());
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private <T> T inTransaction(TransactionalOperation<T> operation) throws Exception {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            T result = operation.execute();
            transactionManager.commit(status);
            return result;
        } catch (Exception | Error e) {
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
            }
            throw e;
        }
    }

    @FunctionalInterface
    private interface TransactionalOperation<T> {
        T execute() throws Exception;
    }
}
