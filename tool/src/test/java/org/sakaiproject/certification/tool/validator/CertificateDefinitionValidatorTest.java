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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import org.springframework.validation.BeanPropertyBindingResult;

import org.sakaiproject.certification.api.CertificateDefinition;
import org.sakaiproject.certification.api.VariableResolver;
import org.sakaiproject.certification.tool.util.CertificateToolState;

public class CertificateDefinitionValidatorTest {

    private static final String COURSE_END_DATE_VARIABLE = "${" + VariableResolver.CERT_ENDDATE + "}";

    private final CertificateDefinitionValidator validator = new CertificateDefinitionValidator();

    @Test
    public void missingCourseEndDateIsRejectedWhenCertificateUsesVariable() {
        CertificateToolState state = stateWithCourseEndDate(null);
        state.getCertificateDefinition().getFieldValues().put("date", COURSE_END_DATE_VARIABLE);
        BeanPropertyBindingResult errors = validateFirst(state);

        assertTrue(errors.hasFieldErrors("certificateDefinition.courseEndDate"));
    }

    @Test
    public void missingCourseEndDateIsRejectedWhenEscapedVariableIsPosted() {
        CertificateToolState state = stateWithCourseEndDate(null);
        state.getCertificateDefinition().getFieldValues().put("date", COURSE_END_DATE_VARIABLE.substring(1));
        BeanPropertyBindingResult errors = validateFirst(state);

        assertTrue(errors.hasFieldErrors("certificateDefinition.courseEndDate"));
    }

    @Test
    public void configuredCourseEndDateAllowsVariable() {
        CertificateToolState state = stateWithCourseEndDate(LocalDate.of(2026, 8, 27));
        state.getCertificateDefinition().getFieldValues().put("date", COURSE_END_DATE_VARIABLE);
        BeanPropertyBindingResult errors = validateFirst(state);

        assertFalse(errors.hasFieldErrors("certificateDefinition.courseEndDate"));
    }

    @Test
    public void missingCourseEndDateIsRejectedWhenInProgressMappingUsesVariable() {
        CertificateToolState state = stateWithCourseEndDate(null);
        state.setTemplateFields(new LinkedHashMap<>());
        state.getTemplateFields().put("date", COURSE_END_DATE_VARIABLE.substring(1));
        BeanPropertyBindingResult errors = validateFirst(state);

        assertTrue(errors.hasFieldErrors("certificateDefinition.courseEndDate"));
    }

    @Test
    public void courseEndDateVariableIsHiddenUntilDateIsConfigured() {
        CertificateToolState state = stateWithCourseEndDate(null);
        state.setPredifinedFields(predefinedFields());

        assertFalse(state.getPredifinedFields().containsKey(COURSE_END_DATE_VARIABLE));

        state.getCertificateDefinition().setCourseEndDate(LocalDate.of(2026, 8, 27));
        state.setPredifinedFields(predefinedFields());

        assertTrue(state.getPredifinedFields().containsKey(COURSE_END_DATE_VARIABLE));
    }

    @Test
    public void selectedCourseEndDateVariableRemainsVisibleIfDateIsCleared() {
        CertificateToolState state = stateWithCourseEndDate(null);
        state.setTemplateFields(new LinkedHashMap<>());
        state.getTemplateFields().put("date", COURSE_END_DATE_VARIABLE.substring(1));
        state.setPredifinedFields(predefinedFields());

        assertTrue(state.getPredifinedFields().containsKey(COURSE_END_DATE_VARIABLE));
    }

    private BeanPropertyBindingResult validateFirst(CertificateToolState state) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(state, "certificateToolState");
        validator.validateFirst(state, errors, null);
        return errors;
    }

    private CertificateToolState stateWithCourseEndDate(LocalDate courseEndDate) {
        CertificateDefinition definition = new CertificateDefinition();
        definition.setCourseEndDate(courseEndDate);

        CertificateToolState state = new CertificateToolState();
        state.setCertificateDefinition(definition);
        return state;
    }

    private Map<String, String> predefinedFields() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put(VariableResolver.UNASSIGNED, "unassigned");
        fields.put(VariableResolver.CERT_AWARDDATE, "date of award");
        fields.put(VariableResolver.CERT_ENDDATE, "course end date");
        return fields;
    }
}
