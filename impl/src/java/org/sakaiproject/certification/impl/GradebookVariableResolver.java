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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.sakaiproject.certification.api.CertificateDefinition;
import org.sakaiproject.certification.api.VariableResolutionException;
import org.sakaiproject.certification.api.criteria.Criterion;
import org.sakaiproject.certification.api.criteria.gradebook.WillExpireCriterion;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

public class GradebookVariableResolver extends AbstractVariableResolver {

    private GradebookService gradebookService = null;
    private UserDirectoryService userDirectoryService = null;
    private ToolManager toolManager = null;
    private SecurityService securityService = null;
    private SessionManager sessionManager = null;

    private static final String MESSAGE_EXPIRATION = "variable.expiration";
    private static final String MESSAGE_ISSUEDATE = "variable.issuedate";

    private static final String PERM_VIEWOWNGRADES = "gradebook.viewOwnGrades";
    private static final String PERM_EDITASSIGNMENT = "gradebook.editAssignments";

    public GradebookVariableResolver() {
        String expirationDate = getMessages().getString(MESSAGE_EXPIRATION);
        String awardDate = getMessages().getString(MESSAGE_ISSUEDATE);
        addVariable(CERT_EXPIREDATE, expirationDate);
        addVariable(CERT_AWARDDATE, awardDate);
    }

    public String getValue(CertificateDefinition certDef, String varLabel, String userId, boolean useCaching) throws VariableResolutionException {
        ResourceLoader resourceLoader = new ResourceLoader();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, resourceLoader.getLocale());

        if (CERT_EXPIREDATE.equals(varLabel)) {
            Date issueDate = certDef.getIssueDate(userId, useCaching);
            if (issueDate == null) {
                //shouldn't happen unless new criteria are added where issue date is incalculable
                return "";
            }
            Set<Criterion> awardCriteria = certDef.getAwardCriteria();

            Iterator<Criterion> itAwardCriteria = awardCriteria.iterator();
            while (itAwardCriteria.hasNext()) {
                Criterion crit = itAwardCriteria.next();
                if (crit instanceof WillExpireCriterion) {
                    //get the offset
                    WillExpireCriterion wechi = (WillExpireCriterion) crit;
                    int expiryOffset = Integer.parseInt(wechi.getExpiryOffset());

                    //add the offset to the issue date
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(issueDate);
                    cal.add(Calendar.MONTH, expiryOffset);
                    Date expiryDate = cal.getTime();

                    //return
                    return dateFormat.format(expiryDate);
                }
            }

            return "";

        } else if (CERT_AWARDDATE.equals(varLabel)) {
            Date issueDate = certDef.getIssueDate(userId, useCaching);
            if (issueDate == null) {
                return "";
            }

            return dateFormat.format(issueDate);
        }

        throw new VariableResolutionException("could not resolve variable: \"" + varLabel + "\"");
    }

    public GradebookService getGradebookService() {
        return gradebookService;
    }

    public void setGradebookService(GradebookService gradebookService) {
        this.gradebookService = gradebookService;
    }

    public UserDirectoryService getUserDirectoryService() {
        return userDirectoryService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public ToolManager getToolManager() {
        return toolManager;
    }

    public void setToolManager (ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    protected final String contextId() {
        return getToolManager().getCurrentPlacement().getContext();
    }

    protected final String userId() {
        return getUserDirectoryService().getCurrentUser().getId();
    }

    protected Object doSecureGradebookAction(SecureGradebookActionCallback callback) throws Exception {
        final String contextId = contextId();

        try {
            securityService.pushAdvisor(new SecurityAdvisor () {
                public SecurityAdvice isAllowed(String userId, String function, String reference) {
                    String compTo;
                    if (contextId.startsWith("/site/")) {
                        compTo = contextId;
                    } else {
                        compTo = "/site/" + contextId;
                    }

                    if (reference.equals(compTo) && (PERM_VIEWOWNGRADES.equals(function) ||
                                                     PERM_EDITASSIGNMENT.equals(function))) {
                        return SecurityAdvice.ALLOWED;
                    } else {
                        return SecurityAdvice.PASS;
                    }
                }
            });

            return callback.doSecureAction();

        } finally {
            securityService.popAdvisor();
        }
    }
}
