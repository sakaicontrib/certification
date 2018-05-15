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

package org.sakaiproject.certification.tool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;

import org.sakaiproject.certification.api.CertificateService;
import org.sakaiproject.certification.api.DocumentTemplateService;
import org.sakaiproject.certification.tool.validator.CertificateDefinitionValidator;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

public class BaseCertificateController {

    protected static final String REQUEST_PARAMATER_SUBVAL = "submitValue";
    protected static final String MOD_ATTR = "certificateToolState";
    protected static final String ADMIN_FN = "certificate.admin";
    protected static final String AWARDABLE_FN = "certificate.be.awarded";
    protected static final String STATUS_MESSAGE_KEY = "statusMessageKey";
    protected static final String ERROR_MESSAGE = "errorMessage";
    protected static final String ERROR_ARGUMENTS = "errorArguments";
    protected static final String ERROR_BAD_ID = "error.bad.id";
    protected static final String FORM_ERR= "form.submit.error";
    protected static final String TEMPLATE_FIELD_ERR = "form.error.templateField";
    protected static final String ERROR_BAD_TEMPLATE_ID = "error.bad.template.id";
    protected static final String DUPLICATE_NAME_ERR = "form.error.duplicateName";
    protected static final String PREDEFINED_VAR_EXCEPTION = "form.error.predefinedVariableException";
    protected static final String CRITERION_EXCEPTION = "form.error.criterionException";
    protected static final String INVALID_TEMPLATE = "form.error.invalidTemplate";
    protected static final String SUCCESS= "form.submit.success";
    protected static final String REPORT_TABLE_NOT_A_MEMBER = "report.table.notamember";
    protected static final String MODEL_KEY_TOOL_URL = "toolUrl";
    public static final String REDIRECT = "redirect:";

    protected CertificateDefinitionValidator certificateDefinitionValidator = new CertificateDefinitionValidator();
    protected ResourceLoader messages = new ResourceLoader("org.sakaiproject.certification.tool.Messages");

    protected static final String EXPIRY_ONLY_CRITERION_ERROR_MSG_KEY = "form.expiry.onlyCriterionError";

    protected ToolManager                 toolManager;
    protected UserDirectoryService        userDirectoryService;
    protected SecurityService             securityService;
    protected SiteService                 siteService;
    protected ServerConfigurationService  serverConfigurationService;

    protected CertificateService          certificateService;
    protected DocumentTemplateService     documentTemplateService;

    @Resource(name="org.sakaiproject.tool.api.ToolManager")
    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    @Resource(name="org.sakaiproject.user.api.UserDirectoryService")
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    @Resource(name="org.sakaiproject.authz.api.SecurityService")
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Resource(name="org.sakaiproject.site.api.SiteService")
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    @Resource(name="org.sakaiproject.component.api.ServerConfigurationService")
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    @Resource(name="org.sakaiproject.certification.api.CertificateService")
    public void setCertificateService(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @Autowired
    public void setDocumentTemplateService(DocumentTemplateService documentTemplateService) {
        this.documentTemplateService = documentTemplateService;
    }

    protected String userId() {
        User user = userDirectoryService.getCurrentUser();
        if (user == null) {
            return null;
        }

        return user.getId();
    }

    protected String siteId() {
        return toolManager.getCurrentPlacement().getContext();
    }

    protected boolean isAdministrator(String userId) {
        String siteId = siteId();
        String fullId = siteId;

        if(securityService.isSuperUser(userId)) {
            //stand aside, it's admin
            return true;
        }
        if(siteId != null && !siteId.startsWith(SiteService.REFERENCE_ROOT)) {
            fullId = SiteService.REFERENCE_ROOT + Entity.SEPARATOR + siteId;
        }

        return securityService.unlock(userId, ADMIN_FN, fullId);
    }

    protected boolean isAdministrator() {
        return isAdministrator(userId());
    }

    protected boolean isAwardable(String userId) {
        String siteId = siteId();
        String fullId = siteId;

        if (securityService.isSuperUser(userId)) {
            //stand aside, it's admin
            return false;
        }
        if (siteId != null && !siteId.startsWith(SiteService.REFERENCE_ROOT)) {
            fullId = SiteService.REFERENCE_ROOT + Entity.SEPARATOR + siteId;
        }

        return securityService.unlock(userId, AWARDABLE_FN, fullId);
    }

    protected boolean isAwardable() {
        return isAwardable(userId());
    }

    protected Site getCurrentSite() {
        try {
            return siteService.getSite(siteId());
        } catch (Exception e) {
            //Should never happen
            throw new RuntimeException( "BaseCertificateController can't get the current Site", e );
        }
    }

    /**
     *
     * @return a list of userIds for members of the current site who can be awarded a certificate
     */
    public List<String> getAwardableUserIds() {
        //return value
        List<String> userIds = new ArrayList<>();

        Site currentSite = getCurrentSite();
        if (currentSite == null) {
            return null;
        }

        userIds.addAll(currentSite.getUsersIsAllowed(AWARDABLE_FN));
        return userIds;
    }

    /**
     * Returns all users who have ever had a grade in the site
     * @return
     */
    public Set<String> getHistoricalGradedUserIds() {
        return new HashSet<> (certificateService.getGradedUserIds(siteId()));
    }

    public String getRole(String userId) {
        Role role = getCurrentSite().getUserRole(userId);
        if (role != null) {
            return role.getId();
        } else {
            return messages.getString(REPORT_TABLE_NOT_A_MEMBER);
        }
    }

    /**
     * Gets the tool instance's url. Helps resolve issues in the PDA view
     * @return
     */
    public String getToolUrl() {
        /**
         * Fixes an issue with the PDA view.
         * For example, simply linking to print.form?certId=${cert.id} caused a download of the tool's markup
         */
        StringBuilder urlPrefix = new StringBuilder();
        String toolId = toolManager.getCurrentPlacement().getId();
        String toolUrl = serverConfigurationService.getToolUrl();
        urlPrefix.append(toolUrl);
        urlPrefix.append("/");
        urlPrefix.append(toolId);

        return urlPrefix.toString();
    }

    public ResourceLoader getMessages() {
        return messages;
    }
}
