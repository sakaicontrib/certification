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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.hsqldb.jdbc.JDBCDataSource;

import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import org.sakaiproject.certification.api.CertificateDefinition;
import org.sakaiproject.certification.api.CertificateDefinitionStatus;
import org.sakaiproject.certification.api.CertificateService;
import org.sakaiproject.certification.api.IncompleteCertificateDefinitionException;
import org.sakaiproject.certification.api.VariableResolver;

public class CertificateDefinitionPersistenceTest {

    private static final String MAPPING_ROOT = "org/sakaiproject/certification/impl/";
    private static final String COURSE_END_DATE_VARIABLE = "${" + VariableResolver.CERT_ENDDATE + "}";
    private static final String CERTIFICATE_NAME_VARIABLE = "${" + VariableResolver.CERT_NAME + "}";
    private static final String[] MAPPING_RESOURCES = {
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
    private CertificateService certificateService;

    @Before
    public void setUp() {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:certification-" + UUID.randomUUID());
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
        CertificateServiceHibernateImpl service = new CertificateServiceHibernateImpl();
        service.setSessionFactory(sessionFactory);
        certificateService = service;
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
    public void updatePersistsCourseEndDateAndFieldMappingsTogether() throws Exception {
        LocalDate originalDate = LocalDate.of(2026, 8, 27);
        String id = saveDefinition(originalDate, fieldValues(COURSE_END_DATE_VARIABLE));

        CertificateDefinition definition = getDefinition(id);
        definition.setCourseEndDate(null);
        definition.setFieldValues(fieldValues(CERTIFICATE_NAME_VARIABLE));
        assertNull(definition.getCourseEndDate());
        CertificateDefinition updated =
                inTransaction(() -> certificateService.updateCertificateDefinition(definition));
        assertNull(updated.getCourseEndDate());

        CertificateDefinition withoutCourseEndDate = getDefinition(id);
        assertNull(withoutCourseEndDate.getCourseEndDate());
        assertEquals(CERTIFICATE_NAME_VARIABLE, withoutCourseEndDate.getFieldValues().get("date"));

        LocalDate revisedDate = LocalDate.of(2026, 9, 30);
        withoutCourseEndDate.setCourseEndDate(revisedDate);
        withoutCourseEndDate.setFieldValues(fieldValues(COURSE_END_DATE_VARIABLE));
        inTransaction(() -> certificateService.updateCertificateDefinition(withoutCourseEndDate));

        CertificateDefinition withCourseEndDate = getDefinition(id);
        assertEquals(revisedDate, withCourseEndDate.getCourseEndDate());
        assertEquals(COURSE_END_DATE_VARIABLE, withCourseEndDate.getFieldValues().get("date"));
    }

    @Test
    public void setFieldValuesRejectsCourseEndDateVariableWithoutDate() throws Exception {
        String id = saveDefinition(null, Collections.emptyMap());

        assertIncomplete(() -> {
            certificateService.setFieldValues(id, fieldValues(COURSE_END_DATE_VARIABLE));
            return null;
        });

        assertEquals(Collections.emptyMap(), getDefinition(id).getFieldValues());
    }

    @Test
    public void setFieldValuesPersistsCourseEndDateVariableWhenDateIsConfigured() throws Exception {
        String id = saveDefinition(LocalDate.of(2026, 8, 27), Collections.emptyMap());

        inTransaction(() -> {
            certificateService.setFieldValues(id, fieldValues(COURSE_END_DATE_VARIABLE));
            return null;
        });

        assertEquals(COURSE_END_DATE_VARIABLE, getDefinition(id).getFieldValues().get("date"));
    }

    @Test
    public void updateRejectsAndRollsBackInvalidCourseEndDateConfiguration() throws Exception {
        LocalDate originalDate = LocalDate.of(2026, 8, 27);
        String id = saveDefinition(originalDate, fieldValues(COURSE_END_DATE_VARIABLE));

        CertificateDefinition definition = getDefinition(id);
        definition.setCourseEndDate(null);
        assertIncomplete(() -> certificateService.updateCertificateDefinition(definition));

        CertificateDefinition persisted = getDefinition(id);
        assertEquals(originalDate, persisted.getCourseEndDate());
        assertEquals(COURSE_END_DATE_VARIABLE, persisted.getFieldValues().get("date"));
    }

    @Test
    public void activationRejectsPersistedInvalidCourseEndDateConfiguration() throws Exception {
        String id = saveDefinition(null, fieldValues(COURSE_END_DATE_VARIABLE));

        assertIncomplete(() -> {
            certificateService.activateCertificateDefinition(id, true);
            return null;
        });
    }

    private String saveDefinition(LocalDate courseEndDate, Map<String, String> fieldValues) {
        CertificateDefinition definition = new CertificateDefinition();
        definition.setName("Certificate " + UUID.randomUUID());
        definition.setDescription("description");
        definition.setCreateDate(new Date());
        definition.setCreatorUserId("creator");
        definition.setSiteId("site");
        definition.setCourseEndDate(courseEndDate);
        definition.setProgressHidden(false);
        definition.setStatus(CertificateDefinitionStatus.UNPUBLISHED);
        definition.setFieldValues(fieldValues);

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.save(definition);
            transaction.commit();
        }
        return definition.getId();
    }

    private CertificateDefinition getDefinition(String id) throws Exception {
        return inTransaction(() -> certificateService.getCertificateDefinition(id));
    }

    private Map<String, String> fieldValues(String value) {
        return Collections.singletonMap("date", value);
    }

    private void assertIncomplete(TransactionalOperation<?> operation) throws Exception {
        try {
            inTransaction(operation);
            fail("Expected IncompleteCertificateDefinitionException");
        } catch (IncompleteCertificateDefinitionException expected) {
            // Expected.
        }
    }

    private <T> T inTransaction(TransactionalOperation<T> operation) throws Exception {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        T result;
        try {
            result = operation.execute();
        } catch (Exception | Error e) {
            transactionManager.rollback(status);
            throw e;
        }
        transactionManager.commit(status);
        return result;
    }

    @FunctionalInterface
    private interface TransactionalOperation<T> {
        T execute() throws Exception;
    }
}
