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

package org.sakaiproject.certification.tool.validator;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.sakaiproject.certification.tool.util.CertificateToolState;

public class CertificateDefinitionValidatorTest {

    private CertificateDefinitionValidator validator;
    private CertificateToolState state;

    @Before
    public void setUp() {
        validator = new CertificateDefinitionValidator();
        state = new CertificateToolState();

        Map<String, String> predefinedFields = new HashMap<>();
        predefinedFields.put("cert.date", "date of award");
        predefinedFields.put("unassigned", "unassigned");
        state.setPredifinedFields(predefinedFields);
    }

    @Test
    public void validateThirdPreservesLiteralValuesAndRestoresPredefinedVariables() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("Course end date", "August 27, 2026");
        fields.put("Award date", "{cert.date}");
        fields.put("Literal expression", "${not.a.variable}");
        fields.put("Unused field", "");
        state.setTemplateFields(fields);

        validator.validateThird(state, null);

        assertEquals("August 27, 2026", state.getTemplateFields().get("Course end date"));
        assertEquals("${cert.date}", state.getTemplateFields().get("Award date"));
        assertEquals("${not.a.variable}", state.getTemplateFields().get("Literal expression"));
        assertEquals("${unassigned}", state.getTemplateFields().get("Unused field"));
    }

    @Test
    public void escapedFieldValuesOnlyRemoveThePrefixFromKnownVariables() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("Course end date", "August 27, 2026");
        fields.put("Award date", "${cert.date}");
        fields.put("Literal expression", "${not.a.variable}");
        state.getCertificateDefinition().setFieldValues(fields);

        Map<String, String> escapedFields = state.getEscapedFieldValues();

        assertEquals("August 27, 2026", escapedFields.get("Course end date"));
        assertEquals("{cert.date}", escapedFields.get("Award date"));
        assertEquals("${not.a.variable}", escapedFields.get("Literal expression"));
    }

    @Test
    public void fieldDescriptionsShowLiteralValues() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("Course end date", "August 27, 2026");
        fields.put("Award date", "${cert.date}");
        state.setTemplateFields(fields);

        Map<String, String> descriptions = state.getFieldToDescription();

        assertEquals("August 27, 2026", descriptions.get("Course end date"));
        assertEquals("date of award", descriptions.get("Award date"));
    }
}
