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

import java.time.LocalDate;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.mapping.PersistentClass;
import org.junit.Test;

import org.sakaiproject.certification.api.CertificateDefinition;

public class CertificateDefinitionMappingTest {

    private static final String MAPPING_ROOT = "org/sakaiproject/certification/impl/";
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

    @Test
    public void courseEndDateIsMappedAsLocalDate() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                .applySetting("hibernate.connection.provider_class",
                        "org.hibernate.engine.jdbc.connections.internal.UserSuppliedConnectionProviderImpl")
                .applySetting("hibernate.temp.use_jdbc_metadata_defaults", "false")
                .build();

        try {
            MetadataSources metadataSources = new MetadataSources(registry);
            for (String resource : MAPPING_RESOURCES) {
                metadataSources.addResource(resource);
            }

            Metadata metadata = metadataSources.buildMetadata();
            PersistentClass mapping = metadata.getEntityBinding(CertificateDefinition.class.getName());

            assertEquals(LocalDate.class, mapping.getProperty("courseEndDate").getType().getReturnedClass());
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
