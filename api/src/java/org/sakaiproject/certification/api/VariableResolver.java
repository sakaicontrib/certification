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

import java.util.Set;

public interface VariableResolver {

    public static final String CERT_NAME = "cert.name";
    public static final String UNASSIGNED = "unassigned";
    public static final String FULL_NAME = "recipient.fullname";
    public static final String FIRST_NAME = "recipient.firstname";
    public static final String LAST_NAME = "recipient.lastname";
    public static final String CERT_EXPIREDATE = "cert.expiredate";
    public static final String CERT_AWARDDATE = "cert.date";

    public Set<String> getVariableLabels();

    public String getVariableDescription(String varLabel);

    public String getValue(CertificateDefinition certDef, String varLabel, String userId, boolean useCaching) throws VariableResolutionException;
}
