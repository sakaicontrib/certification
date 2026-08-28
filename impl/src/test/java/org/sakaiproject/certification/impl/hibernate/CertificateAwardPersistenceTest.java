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

import org.sakaiproject.certification.api.CertificateAward;
import org.sakaiproject.certification.api.CertificateDefinition;
import org.sakaiproject.certification.api.CertificateDefinitionStatus;
import org.sakaiproject.certification.api.DocumentTemplate;
import org.sakaiproject.certification.api.UnmetCriteriaException;
import org.sakaiproject.certification.api.criteria.gradebook.DueDatePassedCriterion;
import org.sakaiproject.content.api.ContentHostingService;

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
    public void awardIsNotCreatedWithoutACalculableIssueDate() throws Exception {
        String certificateDefinitionId = saveCertificateDefinition();
        criteriaFactory.setIssueDate(null);

        try {
            inTransaction(() -> certificateService.awardCertificate(certificateDefinitionId, "student"));
            fail("Expected UnmetCriteriaException");
        } catch (UnmetCriteriaException expected) {
            // Expected.
        }

        assertNull(inTransaction(() ->
            certificateService.getCertificateAwardForUser(certificateDefinitionId, "student")));
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
        CertificateDefinition definition = new CertificateDefinition();
        definition.setName("Certificate " + UUID.randomUUID());
        definition.setDescription("description");
        definition.setCreateDate(new Date());
        definition.setCreatorUserId("creator");
        definition.setSiteId("site");
        definition.setProgressHidden(false);
        definition.setStatus(CertificateDefinitionStatus.ACTIVE);
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

    private ContentHostingService createContentHostingService() {
        return (ContentHostingService) Proxy.newProxyInstance(
            ContentHostingService.class.getClassLoader(),
            new Class<?>[] {ContentHostingService.class},
            (proxy, method, args) -> {
                if ("getContainingCollectionId".equals(method.getName())) {
                    return "/";
                }
                Class<?> returnType = method.getReturnType();
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
            });
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
