package com.rsmart.certification.tool;

import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.DocumentTemplateService;
import com.rsmart.certification.tool.validator.CertificateDefinitionValidator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

/**
 * User: duffy
 * Date: Jul 7, 2011
 * Time: 3:15:05 PM
 */
public class BaseCertificateController
{
    protected final Log logger = LogFactory.getLog(getClass());
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
    protected ResourceLoader messages = new ResourceLoader("com.rsmart.certification.tool.Messages");

    protected static final String EXPIRY_ONLY_CRITERION_ERROR_MSG_KEY = "form.expiry.onlyCriterionError";
    final DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");

    public UserDirectoryService getUserDirectoryService()
    {
        return (UserDirectoryService) ComponentManager.get(UserDirectoryService.class);
    }

    public ToolManager getToolManager()
    {
        return (ToolManager) ComponentManager.get(ToolManager.class);
    }

    public CertificateService getCertificateService()
    {
        return (CertificateService) ComponentManager.get(CertificateService.class);
    }

    public DocumentTemplateService getDocumentTemplateService()
    {
        return (DocumentTemplateService) ComponentManager.get(DocumentTemplateService.class);
    }

    public SecurityService getSecurityService()
    {
        return (SecurityService) ComponentManager.get(SecurityService.class);
    }

    public ServerConfigurationService getServerConfigurationService()
    {
        return (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
    }

    protected String userId()
    {
        User user = getUserDirectoryService().getCurrentUser();
        if (user == null)
        {
            return null;
        }

        return user.getId();
    }

    protected String siteId()
    {
        return getToolManager().getCurrentPlacement().getContext();
    }

    protected boolean isAdministrator(String userId)
    {
        String siteId = siteId();
        String fullId = siteId;

        if(getSecurityService().isSuperUser(userId))
        {
            //stand aside, it's admin
            return true;
        }
        if(siteId != null && !siteId.startsWith(SiteService.REFERENCE_ROOT))
        {
            fullId = SiteService.REFERENCE_ROOT + Entity.SEPARATOR + siteId;
        }
        if(getSecurityService().unlock(userId, ADMIN_FN, fullId))
        {
            //user has certificate.admin
            return true;
        }

        return false;
    }

    protected boolean isAdministrator()
    {
        return isAdministrator(userId());
    }

    protected boolean isAwardable(String userId)
    {
        String siteId = siteId();
        String fullId = siteId;

        if (getSecurityService().isSuperUser(userId))
        {
            //ha! Take that, admin!
            return false;
        }
        if (siteId != null && !siteId.startsWith(SiteService.REFERENCE_ROOT))
        {
            fullId = SiteService.REFERENCE_ROOT + Entity.SEPARATOR + siteId;
        }
        if (getSecurityService().unlock(userId, AWARDABLE_FN, fullId))
        {
            //user has certificate.be.awarded
            return true;
        }
        return false;
    }

    protected boolean isAwardable()
    {
        return isAwardable(userId());
    }

    protected SiteService getSiteService()
    {
        return (SiteService) ComponentManager.get(SiteService.class);
    }

    protected Site getCurrentSite()
    {
        try
        {
            return getSiteService().getSite(siteId());
        }
        catch (Exception e)
        {
            //Should never happen
            RuntimeException re = new RuntimeException ("BaseCertificateController can't get the current Site");
            re.initCause(e);
            throw re;
        }
    }

    /**
     *
     * @return a list of userIds for members of the current site who can be awarded a certificate
     */
    public List<String> getAwardableUserIds()
    {
        //return value
        List<String> userIds = new ArrayList<String>();

        Site currentSite = getCurrentSite();
        if (currentSite == null)
        {
            return null;
        }

        Set<Member> members = currentSite.getMembers();
        if (members == null)
        {
            //impossible, a site must always have at least one instructor/maintainer
            return null;
        }

        Iterator<Member> itMembers = members.iterator();
        while (itMembers.hasNext())
        {
            Member currentMember = itMembers.next();
            String userId = currentMember.getUserId();

            if (isAwardable(userId))
            {
                //user can't add/edit a certificate, hence this person is awardable
                userIds.add(userId);
            }
        }

        return userIds;
    }

    /**
     * Returns all users who have ever had a grade in the site
     * @return
     */
    public Set<String> getHistoricalGradedUserIds()
    {
        return new HashSet<String> (getCertificateService().getGradedUserIds(siteId()));
    }

    public String getRole(String userId)
    {
        Role role = getCurrentSite().getUserRole(userId);
        if (role != null)
        {
            return role.getId();
        }
        else
        {
            return messages.getString(REPORT_TABLE_NOT_A_MEMBER);
        }
    }

    /**
     * Gets the tool instance's url. Helps resolve issues in the PDA view
     * @return
     */
    public String getToolUrl()
    {
        /**
         * Fixes an issue with the PDA view.
         * For example, simply linking to print.form?certId=${cert.id} caused a download of the tool's markup
         */
        StringBuilder urlPrefix = new StringBuilder();
        String toolId = getToolManager().getCurrentPlacement().getId();
        String toolUrl = getServerConfigurationService().getToolUrl();
        urlPrefix.append(toolUrl);
        urlPrefix.append("/");
        urlPrefix.append(toolId);

        return urlPrefix.toString();
    }

    public ResourceLoader getMessages()
    {
        return messages;
    }
}
