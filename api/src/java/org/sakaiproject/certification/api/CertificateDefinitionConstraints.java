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

package org.sakaiproject.certification.api;

import java.time.LocalDate;
import java.util.Map;

/**
 * Cross-field constraints for certificate definitions.
 */
public final class CertificateDefinitionConstraints {

    private static final String COURSE_END_DATE_VARIABLE = "${" + VariableResolver.CERT_ENDDATE + "}";
    private static final String ESCAPED_COURSE_END_DATE_VARIABLE = COURSE_END_DATE_VARIABLE.substring(1);

    private CertificateDefinitionConstraints() {
    }

    public static boolean isCourseEndDateConfigurationValid(LocalDate courseEndDate,
                                                             Map<String, String> fieldValues) {
        return courseEndDate != null || !usesCourseEndDateVariable(fieldValues);
    }

    public static boolean usesCourseEndDateVariable(Map<String, String> fieldValues) {
        return fieldValues != null
                && (fieldValues.containsValue(COURSE_END_DATE_VARIABLE)
                        || fieldValues.containsValue(ESCAPED_COURSE_END_DATE_VARIABLE));
    }
}
