package com.rsmart.certification.tool;

import com.rsmart.certification.api.CertificateAward;
import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.DocumentTemplateService;
import com.rsmart.certification.tool.validator.CertificateDefinitionValidator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

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
    protected static final String STATUS_MESSAGE_KEY = "statusMessageKey";
    protected static final String ERROR_MESSAGE = "errorMessage";
    protected static final String FORM_ERR= "form.submit.error";
    protected static final String TEMPLATE_PROCESSING_ERR = "form.error.templateProcessingError";
    protected static final String DUPLICATE_NAME_ERR = "form.error.duplicateName";    
    protected static final String PREDEFINED_VAR_EXCEPTION = "form.error.predefinedVariableException";
    protected static final String CRITERION_EXCEPTION = "form.error.criterionException";
    protected static final String INVALID_TEMPLATE = "form.error.invalidTemplate";
    protected static final String SUCCESS= "form.submit.success";
    protected SecurityService securityService;
    protected CertificateService certificateService;
    protected DocumentTemplateService documentTemplateService;
    protected ToolManager toolManager;
    protected UserDirectoryService userDirectoryService;
    protected CertificateDefinitionValidator certificateDefinitionValidator = new CertificateDefinitionValidator();

    @Resource(name="org.sakaiproject.user.api.UserDirectoryService")
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
    {
		this.userDirectoryService = userDirectoryService;
	}

    public UserDirectoryService getUserDirectoryService() {
		return userDirectoryService;
	}


    public ToolManager getToolManager() {
        return toolManager;
    }

    @Resource(name="org.sakaiproject.tool.api.ToolManager")
    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    public CertificateService getCertificateService() {
		return certificateService;
	}

    @Resource(name="com.rsmart.certification.api.CertificateService")
	public void setCertificateService(
			CertificateService certificateService) {
		this.certificateService = certificateService;
	}

	public DocumentTemplateService getDocumentTemplateService() {
		return documentTemplateService;
	}

	@Autowired
	public void setDocumentTemplateService(
			DocumentTemplateService documentTemplateService) {
		this.documentTemplateService = documentTemplateService;
	}


    public SecurityService getSecurityService()
    {
        return securityService;
    }

    @Resource(name="org.sakaiproject.authz.api.SecurityService")
    public void setSecurityService(SecurityService securityService)
    {
        this.securityService = securityService;
    }

    protected String userId()
    {
        User
            user = getUserDirectoryService().getCurrentUser();

        if (user == null)
            return null;

        return user.getId();
    }

    protected String siteId()
    {
        return getToolManager().getCurrentPlacement().getContext();
    }

    protected boolean isAdministrator ()
    {
        String
            siteId = siteId(),
            fullId = siteId,
            userId = userId();

		if(securityService.isSuperUser()) {
			return true;
		}
		if(siteId != null && !siteId.startsWith(SiteService.REFERENCE_ROOT)) {
			fullId = SiteService.REFERENCE_ROOT + Entity.SEPARATOR + siteId;
		}
		if(securityService.unlock(userId, ADMIN_FN, fullId)) {
			return true;
		}
		return false;
    }

    protected boolean isAwardPrintable (CertificateAward award)
    {
        String
            siteId = siteId(),
            fullId = siteId,
            userId = userId();

        if(securityService.isSuperUser()) {
            return true;
        }
        if(siteId != null && !siteId.startsWith(SiteService.REFERENCE_ROOT)) {
            fullId = SiteService.REFERENCE_ROOT + Entity.SEPARATOR + siteId;
        }
        if(securityService.unlock(userId, ADMIN_FN, fullId)) {
            return true;
        }
        return false;
    }

}
