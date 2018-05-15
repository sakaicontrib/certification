/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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

/**
 * CertificateDefinition objects may be in one of three states:
 *
 *      INCOMPLETE  - The CertificateDefinition has not yet been fully defined
 *      ACTIVE      - The CertificateDefinition is in use and can be used for awards
 *      UNPUBLISHED    - The CertificateDefinition is not presently available for awards
 */
public enum CertificateDefinitionStatus {
    UNPUBLISHED,
    ACTIVE,
    INACTIVE,
}
