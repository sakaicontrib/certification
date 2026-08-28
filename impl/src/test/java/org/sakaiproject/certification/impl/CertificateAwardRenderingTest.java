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

package org.sakaiproject.certification.impl;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import org.sakaiproject.certification.api.CertificateAward;
import org.sakaiproject.certification.api.CertificateDefinition;
import org.sakaiproject.certification.api.DocumentTemplate;
import org.sakaiproject.certification.api.DocumentTemplateRenderEngine;
import org.sakaiproject.certification.api.TemplateReadException;
import org.sakaiproject.certification.api.VariableResolver;
import org.sakaiproject.certification.api.criteria.gradebook.WillExpireCriterion;
import org.sakaiproject.util.ResourceLoader;

public class CertificateAwardRenderingTest {

    @Test
    public void gradebookResolverUsesPersistedAwardDateInsteadOfRecalculatedDate() throws Exception {
        Date recalculatedDate = new Date(1_777_862_800_000L);
        Date persistedDate = new Date(1_777_776_400_000L);
        CertificateDefinition definition = new CertificateDefinition() {
            @Override
            public Date getIssueDate(String userId, boolean useCaching) {
                return recalculatedDate;
            }
        };
        ResourceLoader resourceLoader = new ResourceLoader() {
            @Override
            public String getString(String key) {
                return key;
            }

            @Override
            public Locale getLocale() {
                return Locale.US;
            }
        };
        GradebookVariableResolver resolver = new GradebookVariableResolver(resourceLoader);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, resourceLoader.getLocale());

        assertEquals(dateFormat.format(persistedDate), resolver.getValue(definition,
            VariableResolver.CERT_AWARDDATE, "student", false, persistedDate));
        assertEquals(dateFormat.format(recalculatedDate), resolver.getValue(definition,
            VariableResolver.CERT_AWARDDATE, "student", false));

        WillExpireCriterion expiryCriterion = new WillExpireCriterion();
        expiryCriterion.setExpiryOffset("1");
        definition.addAwardCriterion(expiryCriterion);
        Calendar expectedExpiry = Calendar.getInstance();
        expectedExpiry.setTime(persistedDate);
        expectedExpiry.add(Calendar.MONTH, 1);
        assertEquals(dateFormat.format(expectedExpiry.getTime()), resolver.getValue(definition,
            VariableResolver.CERT_EXPIREDATE, "student", false, persistedDate));
    }

    @Test
    public void awardRenderPassesPersistedDateToVariableResolvers() throws Exception {
        Date persistedDate = new Date(1_777_776_400_000L);
        CertificateDefinition definition = new CertificateDefinition();
        definition.setFieldValues(Collections.singletonMap("date", "${cert.date}"));
        DocumentTemplate template = new DocumentTemplate();
        template.setOutputMimeType("text/plain");
        CertificateAward award = new CertificateAward("student", definition, persistedDate);

        DocumentTemplateServiceImpl service = new DocumentTemplateServiceImpl();
        service.setVariableResolvers(Collections.singleton(new PersistedDateResolver()));
        service.setRendererMap(Collections.singletonMap("text/plain", new BindingRenderEngine()));

        try (InputStream rendered = service.render(template, award)) {
            assertEquals(Long.toString(persistedDate.getTime()),
                new String(rendered.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private static class PersistedDateResolver implements VariableResolver {

        @Override
        public Set<String> getVariableLabels() {
            return Collections.singleton(CERT_AWARDDATE);
        }

        @Override
        public String getVariableDescription(String varLabel) {
            return varLabel;
        }

        @Override
        public String getValue(CertificateDefinition certDef, String varLabel, String userId, boolean useCaching) {
            return "recalculated";
        }

        @Override
        public String getValue(CertificateDefinition certDef, String varLabel, String userId,
                               boolean useCaching, Date awardedAt) {
            return Long.toString(awardedAt.getTime());
        }
    }

    private static class BindingRenderEngine implements DocumentTemplateRenderEngine {

        @Override
        public String getOutputMimeType(DocumentTemplate template) {
            return template.getOutputMimeType();
        }

        @Override
        public Set<String> getTemplateFields(DocumentTemplate template) {
            return Collections.singleton("date");
        }

        @Override
        public Set<String> getTemplateFields(InputStream inputStream) {
            return Collections.singleton("date");
        }

        @Override
        public InputStream render(DocumentTemplate template, Map<String, String> bindings) {
            return new ByteArrayInputStream(bindings.get("date").getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public boolean supportsPreview(DocumentTemplate template) {
            return false;
        }

        @Override
        public String getPreviewMimeType(DocumentTemplate template) {
            return null;
        }

        @Override
        public InputStream renderPreview(DocumentTemplate template, Map<String, String> bindings)
                throws TemplateReadException {
            return render(template, bindings);
        }
    }
}
