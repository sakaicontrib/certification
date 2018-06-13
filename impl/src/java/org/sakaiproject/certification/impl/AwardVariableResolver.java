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

package org.sakaiproject.certification.impl;

import org.sakaiproject.certification.api.CertificateDefinition;
import org.sakaiproject.certification.api.VariableResolutionException;

public class AwardVariableResolver extends AbstractVariableResolver {

    private static final String MESSAGE_NAMEOFCERT = "variable.nameOfCert";
    private static final String MESSAGE_UNASSIGNED = "variable.unassigned";

    public AwardVariableResolver() {
        String name = getMessages().getString(MESSAGE_NAMEOFCERT);
        String unassigned = getMessages().getString(MESSAGE_UNASSIGNED);
        addVariable(CERT_NAME, name);
        addVariable (UNASSIGNED, unassigned);
    }

    public String getValue(CertificateDefinition certDef, String varLabel, String userId, boolean useCaching) throws VariableResolutionException {
        if (CERT_NAME.equals(varLabel)) {
            return certDef.getName();
        } else if (UNASSIGNED.equals(varLabel)) {
            return "";
        }

        throw new VariableResolutionException("could not resolve variable: \"" + varLabel + "\"");
    }
}
