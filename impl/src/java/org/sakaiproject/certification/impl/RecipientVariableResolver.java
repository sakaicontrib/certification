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
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public class RecipientVariableResolver extends AbstractVariableResolver {

    private UserDirectoryService uds = null;

    private static final String MESSAGE_FULLNAME = "variable.fullname";
    private static final String MESSAGE_FIRSTNAME = "variable.firstname";
    private static final String MESSAGE_LASTNAME = "variable.lastname";

    public RecipientVariableResolver() {
        String fullName = getMessages().getString(MESSAGE_FULLNAME);
        String firstName = getMessages().getString(MESSAGE_FIRSTNAME);
        String lastName = getMessages().getString(MESSAGE_LASTNAME);
        addVariable(FULL_NAME, fullName);
        addVariable(FIRST_NAME, firstName);
        addVariable(LAST_NAME, lastName);
    }

    public void setUserDirectoryService(UserDirectoryService uds) {
        this.uds = uds;
    }

    public UserDirectoryService getUserDirectoryService() {
        return uds;
    }

    public String getValue(CertificateDefinition certDef, String key, String userId, boolean useCaching) throws VariableResolutionException {
        User user = null;
        try {
            user = getUserDirectoryService().getUser(userId);
        } catch (UserNotDefinedException e) {
            throw new VariableResolutionException("could not resolve variable \"" + key + "\" due to UserNotDefinedException. userId: " + userId, e);
        }

        if (FULL_NAME.equals(key)) {
            return user.getDisplayName();

        } else if (FIRST_NAME.equals(key)) {
            return user.getFirstName();

        } else if (LAST_NAME.equals(key)) {
            return user.getLastName();
        }

        throw new VariableResolutionException ("key \"" + key + "\" has not been resolved");
    }
}
