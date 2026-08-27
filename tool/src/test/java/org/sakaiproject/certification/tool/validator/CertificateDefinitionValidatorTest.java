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
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;

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
    public void validateThirdResolvesCustomAndPredefinedValues() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("Course end date", state.getCustomFieldValue());
        fields.put("Award date", "{cert.date}");
        fields.put("Literal expression", state.getCustomFieldValue());
        fields.put("Unused field", state.getUnassignedValue());
        state.setTemplateFields(fields);

        Map<String, String> customFields = new LinkedHashMap<>();
        customFields.put("Course end date", "August 27, 2026");
        customFields.put("Award date", "");
        customFields.put("Literal expression", "${not.a.variable}");
        customFields.put("Unused field", "");
        state.setCustomTemplateFields(customFields);

        BeanPropertyBindingResult errors = errorsForState();
        validator.validateThird(state, errors);

        assertEquals(0, errors.getErrorCount());
        assertEquals("August 27, 2026", state.getTemplateFields().get("Course end date"));
        assertEquals("${cert.date}", state.getTemplateFields().get("Award date"));
        assertEquals("${not.a.variable}", state.getTemplateFields().get("Literal expression"));
        assertEquals("${unassigned}", state.getTemplateFields().get("Unused field"));
    }

    @Test
    public void prepareTemplateFieldsUsesSelectForVariablesAndCustomInputForLiterals() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("Course end date", "August 27, 2026");
        fields.put("Award date", "${cert.date}");
        fields.put("Literal expression", "${not.a.variable}");
        state.getCertificateDefinition().setFieldValues(fields);
        state.setTemplateFields(fields.keySet());

        state.prepareTemplateFieldsForEditing();

        assertEquals(state.getCustomFieldValue(), state.getTemplateFields().get("Course end date"));
        assertEquals("August 27, 2026", state.getCustomTemplateFields().get("Course end date"));
        assertEquals("{cert.date}", state.getTemplateFields().get("Award date"));
        assertEquals(state.getCustomFieldValue(), state.getTemplateFields().get("Literal expression"));
        assertEquals("${not.a.variable}", state.getCustomTemplateFields().get("Literal expression"));
    }

    @Test
    public void validateThirdRejectsCustomValuesOverPersistenceLimit() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("Course end date", state.getCustomFieldValue());
        state.setTemplateFields(fields);

        String overlongValue = repeat('x', state.getMaxFieldValueLength() + 1);
        Map<String, String> customFields = new LinkedHashMap<>();
        customFields.put("Course end date", overlongValue);
        state.setCustomTemplateFields(customFields);

        BeanPropertyBindingResult errors = errorsForState();
        validator.validateThird(state, errors);

        assertTrue(errors.hasFieldErrors("customTemplateFields"));
        assertEquals(state.getCustomFieldValue(), state.getTemplateFields().get("Course end date"));
        assertEquals(overlongValue, state.getCustomTemplateFields().get("Course end date"));
    }

    @Test
    public void validateFourthPreservesReviewStepOverwrite() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("Course end date", "${unassigned}");
        fields.put("Award date", "${cert.date}");
        state.setTemplateFields(fields);

        Map<String, String> customFields = new LinkedHashMap<>();
        customFields.put("Course end date", "August 27, 2026");
        customFields.put("Award date", "ignored");
        state.setCustomTemplateFields(customFields);

        BeanPropertyBindingResult errors = errorsForState();
        validator.validateFourth(state, errors);

        assertEquals(0, errors.getErrorCount());
        assertEquals("August 27, 2026", state.getTemplateFields().get("Course end date"));
        assertEquals("${cert.date}", state.getTemplateFields().get("Award date"));
    }

    @Test
    public void validateFourthRejectsOverlongReviewStepOverwrite() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("Course end date", "${unassigned}");
        state.setTemplateFields(fields);

        Map<String, String> customFields = new LinkedHashMap<>();
        customFields.put("Course end date", repeat('x', state.getMaxFieldValueLength() + 1));
        state.setCustomTemplateFields(customFields);

        BeanPropertyBindingResult errors = errorsForState();
        validator.validateFourth(state, errors);

        assertTrue(errors.hasFieldErrors("customTemplateFields"));
        assertEquals("${unassigned}", state.getTemplateFields().get("Course end date"));
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

    private BeanPropertyBindingResult errorsForState() {
        return new BeanPropertyBindingResult(state, "certificateToolState");
    }

    private String repeat(char value, int count) {
        char[] values = new char[count];
        java.util.Arrays.fill(values, value);
        return new String(values);
    }
}
