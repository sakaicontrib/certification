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

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * This encapsulates a binary file containing a template that can be rendered into a printable certificate. The
 * template will have labeled fields that can be populated at rendering time. These fields can be used for elements
 * such as the time/date of award, the name of the recipient, and the name of the Site.
 *
*/
@Data
@EqualsAndHashCode(of = "id")
public class DocumentTemplate
{
    /**
     * The resource directory in which the templates get stored
     */
    public static final String COLLECTION_ID = "/certification/templates/";

    private String id;
    private String name;
    private String outputMimeType;
    private String resourceId;

    private CertificateDefinition certificateDefinition = null;
}
