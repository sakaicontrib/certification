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

/**
 * Thrown when creating a certificate definition, but some constraint isn't met
 */
@Data
public class InvalidCertificateDefinitionException extends CertificationException {

    public static final int REASON_TOO_LONG = 1;

    private int invalidField = 0;
    private int reason = 0;

    public InvalidCertificateDefinitionException() {
        super();
    }

    public InvalidCertificateDefinitionException(String message) {
        super(message);
    }

    public InvalidCertificateDefinitionException(String message, Throwable t) {
        super(message, t);
    }

    public InvalidCertificateDefinitionException(Throwable t) {
        super(t);
    }
}
