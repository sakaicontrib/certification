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
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import org.junit.Test;

import org.sakaiproject.certification.api.CertificateDefinition;
import org.sakaiproject.certification.api.VariableResolver;
import org.sakaiproject.util.ResourceLoader;

public class AwardVariableResolverTest {

    private static final Locale TEST_LOCALE = Locale.US;

    private final AwardVariableResolver resolver = new AwardVariableResolver(new ResourceLoader() {
        @Override
        public Locale getLocale() {
            return TEST_LOCALE;
        }

        @Override
        public String getString(String key) {
            return key;
        }
    });

    @Test
    public void exposesCourseEndDateVariable() {
        assertTrue(resolver.getVariableLabels().contains(VariableResolver.CERT_ENDDATE));
    }

    @Test
    public void resolvesCourseEndDateUsingCurrentLocale() throws Exception {
        LocalDate courseEndDate = LocalDate.of(2026, 8, 27);
        CertificateDefinition definition = new CertificateDefinition();
        definition.setCourseEndDate(courseEndDate);
        String expected = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                .withLocale(resolver.getMessages().getLocale())
                .format(courseEndDate);

        assertEquals(expected, resolver.getValue(definition, VariableResolver.CERT_ENDDATE, "user", false));
    }

    @Test
    public void resolvesUnsetCourseEndDateToEmptyString() throws Exception {
        assertEquals("", resolver.getValue(new CertificateDefinition(), VariableResolver.CERT_ENDDATE, "user", false));
    }
}
