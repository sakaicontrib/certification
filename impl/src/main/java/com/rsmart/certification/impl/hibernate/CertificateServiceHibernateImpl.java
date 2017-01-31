package com.rsmart.certification.impl.hibernate;

import com.rsmart.certification.api.BaseCertificateDefinition;
import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.CertificateDefinitionStatus;
import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.DocumentTemplate;
import com.rsmart.certification.api.DocumentTemplateException;
import com.rsmart.certification.api.DocumentTemplateService;
import com.rsmart.certification.api.IncompleteCertificateDefinitionException;
import com.rsmart.certification.api.ReportRow;
import com.rsmart.certification.api.TemplateReadException;
import com.rsmart.certification.api.UnsupportedTemplateTypeException;
import com.rsmart.certification.api.VariableResolver;
import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.api.criteria.CriterionProgress;
import com.rsmart.certification.api.criteria.UnknownCriterionTypeException;
import com.rsmart.certification.api.criteria.UserProgress;
import com.rsmart.certification.impl.hibernate.criteria.AbstractCriterionHibernateImpl;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.WillExpireCriterionHibernateImpl;
import com.rsmart.certification.impl.security.AllowMapSecurityAdvisor;
import com.rsmart.certification.impl.util.ExtraUserPropertyUtilityImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * User: duffy
 * Date: Jun 30, 2011
 * Time: 9:21:59 AM
 */
public class CertificateServiceHibernateImpl extends HibernateDaoSupport implements CertificateService
{
    private static Log LOG = LogFactory.getLog(CertificateServiceHibernateImpl.class);

    //managers and services
    private DocumentTemplateService documentTemplateService = null;
    private UserDirectoryService userDirectoryService = null;
    private ToolManager toolManager = null;
    private SessionManager sessionManager = null;
    private SecurityService securityService = null;
    private SiteService siteService = null;
    private AuthzGroupService authzGroupService= null;
    private ContentHostingService contentHostingService = null;

    private String templateDirectory = null;
    private HashMap<String, CriteriaFactory> criteriaTemplateMap = new HashMap<String, CriteriaFactory>();
    private HashMap<Class, CriteriaFactory> criteriaFactoryMap = new HashMap<Class, CriteriaFactory>();
    private HashSet<CriteriaFactory> criteriaFactories = new HashSet<CriteriaFactory>();
    private HashMap<String, VariableResolver> variableResolvers = new HashMap<String, VariableResolver>();

    private ResourceLoader messages = new ResourceLoader("com.rsmart.certification.Messages");

    //For resource properties
    private final String PUBVIEW_FALSE = "false";
    private final String REPORT_TABLE_NOT_A_MEMBER = "report.table.notamember";
    private final String MESSAGE_NO = "report.table.no";
    private final String MESSAGE_YES = "report.table.yes";

    //Hibernate named queries
    private static final String QUERY_CERTIFICATE_DEFINITION_BY_NAME = "getCertificateDefinitionByName";
    private static final String QUERY_CERTIFICATE_DEFINITIONS_BY_SITE = "getCertificateDefinitionsBySite";
    private static final String QUERY_CERTIFICATE_DEFINITIONS_BY_SITE_AND_STATUS = "getCertificateDefinitionsBySiteAndStatus";

    //Hibernate named query parameters
    private static final String PARAM_SITE_ID = "siteId";
    private static final String PARAM_STATUSES = "statuses";
    private static final String PARAM_GBID = "gbid";
    private static final String PARAM_GRADEBOOK_ID = "gradebookId";
    private static final String PARAM_STUDENT_ID = "studentId";

    private final DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");

    public void setAuthzGroupService(AuthzGroupService authzGroupService)
    {
        this.authzGroupService = authzGroupService;
    }

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    public String getTemplateDirectory()
    {
        return templateDirectory;
    }

    public void setTemplateDirectory(String templateDirectory)
    {
        this.templateDirectory = templateDirectory;
    }

    public ContentHostingService getContentHostingService()
    {
        return contentHostingService;
    }

    public void setContentHostingService(ContentHostingService contentHostingService)
    {
        this.contentHostingService = contentHostingService;
    }

    public String getString(String key)
    {
        return messages.getString(key);
    }

    public String getFormattedMessage(String key, Object[] values)
    {
        return messages.getFormattedMessage(key, values);
    }

    public void setDocumentTemplateService(DocumentTemplateService dts)
    {
        documentTemplateService = dts;
    }

    public DocumentTemplateService getDocumentTemplateService()
    {
        return documentTemplateService;
    }

    public ToolManager getToolManager()
    {
        return toolManager;
    }

    public void setToolManager(ToolManager toolManager)
    {
        this.toolManager = toolManager;
    }

    public UserDirectoryService getUserDirectoryService()
    {
        return userDirectoryService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService)
    {
        this.userDirectoryService = userDirectoryService;
    }

    public SessionManager getSessionManager()
    {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager)
    {
        this.sessionManager = sessionManager;
    }

    public SecurityService getSecurityService()
    {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService)
    {
        this.securityService = securityService;
    }

    public void init()
    {
        LOG.info("init");

        if (templateDirectory == null)
        {
            throw new IllegalStateException ("templateDirectory for CertificateService is not configured");
        }

        File dirFile = new File (templateDirectory);

        if ((!dirFile.exists() && !dirFile.mkdir()) || !dirFile.canWrite())
        {
            throw new IllegalStateException ("templateDirectory \"" + templateDirectory + "\" is unreadable");
        }
        if (documentTemplateService == null)
        {
            throw new IllegalStateException ("DocumentTemplateService not provided to CertificateService");
        }
        if (userDirectoryService == null)
        {
            throw new IllegalStateException ("UserDirectoryService not provided to CertificateService");
        }
        if (toolManager == null)
        {
            throw new IllegalStateException ("ToolManager not provided to CertificateService");
        }

        for (VariableResolver resolver : documentTemplateService.getVariableResolvers())
        {
            for (String label : resolver.getVariableLabels())
            {
                variableResolvers.put(label, resolver);
            }
        }
    }

    private String userId()
    {
        return getUserDirectoryService().getCurrentUser().getId();
    }

    private String contextId()
    {
        return getToolManager().getCurrentPlacement().getContext();
    }

    private String toolId()
    {
        return getToolManager().getCurrentTool().getId();
    }

    private String portalUrl()
    {
        return ServerConfigurationService.getPortalUrl();
    }

    public void deleteCertificateDefinition(final String certificateDefinitionId) throws IdUnusedException, DocumentTemplateException
    {
        CertificateDefinition cd = (CertificateDefinition) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                CertificateDefinitionHibernateImpl cd = (CertificateDefinitionHibernateImpl) session.load(CertificateDefinitionHibernateImpl.class, certificateDefinitionId);
                cd.getFieldValues().clear();
                cd.getAwardCriteria().clear();

                session.update(cd);
                session.flush();

                Query q = session.createQuery("delete from DocumentTemplateHibernateImpl where id=?");
                q.setString(0, certificateDefinitionId);
                q.executeUpdate();
                q = session.getNamedQuery("deleteCertificateDefinition");
                q.setString(0, certificateDefinitionId);
                q.executeUpdate();

                return cd;
            }
        });

        deleteTemplateFile(cd.getDocumentTemplate().getResourceId());
    }

    public CertificateDefinition createCertificateDefinition(CertificateDefinition certificateDefinition) throws IdUsedException
    {
        BaseCertificateDefinition cd = (BaseCertificateDefinition)certificateDefinition;
        CertificateDefinitionHibernateImpl myCertDefn = new CertificateDefinitionHibernateImpl();

        myCertDefn.setAwardCriteria(cd.getAwardCriteria());
        myCertDefn.setCreateDate(new Date());
        myCertDefn.setCreatorUserId(userId());
        myCertDefn.setDescription(cd.getDescription());
        myCertDefn.setDocumentTemplate(cd.getDocumentTemplate());
        myCertDefn.setFieldValues(cd.getFieldValues());
        myCertDefn.setSiteId(cd.getSiteId());
        myCertDefn.setName(cd.getName());
        myCertDefn.setStatus(CertificateDefinitionStatus.UNPUBLISHED);

        try
        {
            getHibernateTemplate().save(myCertDefn);
        }
        catch (DataIntegrityViolationException dive)
        {
            throw new IdUsedException("name: " + cd.getName() + " siteId: " + cd.getSiteId());
        }

        return myCertDefn;
    }

    public CertificateDefinition updateCertificateDefinition(final CertificateDefinition cd) throws IdUnusedException
    {
        CertificateDefinitionHibernateImpl retVal = null;
        if (cd instanceof CertificateDefinitionHibernateImpl)
        {
            retVal = (CertificateDefinitionHibernateImpl) cd;
        }

        try
        {
            retVal = (CertificateDefinitionHibernateImpl) getHibernateTemplate().execute(new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Query q = session.createQuery ("from "+ CertificateDefinitionHibernateImpl.class.getName() + " where id = :id ");
                    q.setParameter("id", cd.getId());
                    CertificateDefinitionHibernateImpl cdhi = (CertificateDefinitionHibernateImpl) q.list().get(0);
                    cdhi.setName(cd.getName());
                    cdhi.setDescription(cd.getDescription());
                    session.update(cdhi);
                    return cdhi;
                }
            });
        }
        catch (ObjectNotFoundException onfe)
        {
            throw new IdUnusedException(cd.getId());
        }
        catch (HibernateObjectRetrievalFailureException horfe)
        {
            throw new IdUnusedException(cd.getId());
        }

        return retVal;
    }

    public CertificateDefinition createCertificateDefinition (final String name, final String description,
                                                              final String siteId, final Boolean progressHidden, final String fileName,
                                                              final String mimeType, final InputStream template)
        throws IdUsedException, UnsupportedTemplateTypeException, DocumentTemplateException
    {
        CertificateDefinitionHibernateImpl cd = null;
        try
        {
            cd = (CertificateDefinitionHibernateImpl) getHibernateTemplate().execute(new HibernateCallback()
            {
                public Object doInHibernate(Session session) throws HibernateException, SQLException
                {
                    CertificateDefinitionHibernateImpl certificateDefinition = new CertificateDefinitionHibernateImpl();

                    certificateDefinition.setCreateDate(new Date());
                    certificateDefinition.setCreatorUserId(userId());
                    certificateDefinition.setDescription(description);
                    certificateDefinition.setName(name);
                    certificateDefinition.setSiteId(siteId);
                    certificateDefinition.setProgressHidden(progressHidden);
                    certificateDefinition.setStatus(CertificateDefinitionStatus.UNPUBLISHED);
                    session.save(certificateDefinition);

                    DocumentTemplateHibernateImpl documentTemplate = new DocumentTemplateHibernateImpl();
                    documentTemplate.setCertificateDefinition(certificateDefinition);

                    try
                    {
                        documentTemplate = processFile(documentTemplate, fileName, mimeType, template);
                    }
                    catch (DocumentTemplateException e)
                    {
                        throw new RuntimeException(e);
                    }

                    session.save(documentTemplate);
                    session.flush();
                    return certificateDefinition;
                }
            });
        }
        catch (RuntimeException re)
        {
            Throwable t = re.getCause();
            if (t != null)
            {
                if (t instanceof IdUsedException)
                {
                    throw (IdUsedException) t;
                }
                if (t instanceof UnsupportedTemplateTypeException)
                {
                    throw (UnsupportedTemplateTypeException) t;
                }
                if (t instanceof DocumentTemplateException)
                {
                    throw (DocumentTemplateException) t;
                }
            }
            else
            {
                t = re;
            }

            throw new DocumentTemplateException ("Unhandled exception creating new certificate definition", t);
        }

        return cd;
    }

    private void deleteTemplateFile(String resourceId)
    {
        try
        {
            String certDefCId = contentHostingService.getContainingCollectionId(resourceId);
            contentHostingService.removeResource(resourceId);
            contentHostingService.removeCollection(certDefCId);
        }
        catch (PermissionException e)
        {
            //TODO: Should these be thrown? Should this method 'throws DocumentTemplateException'?
            new DocumentTemplateException(e);
        }
        catch (IdUnusedException e)
        {
            new DocumentTemplateException(e);
        }
        catch (TypeException e)
        {
            new DocumentTemplateException(e);
        }
        catch (InUseException e)
        {
            new DocumentTemplateException(e);
        }
        catch (ServerOverloadException e)
        {
            new DocumentTemplateException(e);
        }
    }

    private ContentResourceEdit storeTemplateFile (String siteId, String certificateId, InputStream templateStream, String fileName, String mimeType, String resourceId)
        throws DocumentTemplateException
    {
        ContentResourceEdit resourceEdit = null;
        boolean resourceExist = false;

        try
        {
            try
            {

                if(authzGroupService.getAuthzGroup(siteService.siteReference(siteId)).isAllowed(sessionManager.getCurrentSessionUserId(), "certificate.admin"))
                {
                    getSecurityService().pushAdvisor(new AllowMapSecurityAdvisor(ContentHostingService.EVENT_RESOURCE_READ,
                                 contentHostingService.getReference(resourceId)));
                    getSecurityService().pushAdvisor(new AllowMapSecurityAdvisor(ContentHostingService.EVENT_RESOURCE_ADD,
                                 contentHostingService.getReference(resourceId)));
                }
            }
            catch(Exception e){}

            contentHostingService.checkResource(resourceId);
            resourceExist = true;
        }
        catch(IdUnusedException iue)
        {
            resourceExist = false;
        }
        catch (PermissionException e)
        {
            throw new DocumentTemplateException ("(PermissionException) Error storing template", e);
        }
        catch (TypeException e)
        {
            throw new DocumentTemplateException ("(TypeException) Error storing template", e);
        }

        try
        {
            if(resourceExist)
            {
                resourceEdit = contentHostingService.editResource(resourceId);
                ResourcePropertiesEdit props = resourceEdit.getPropertiesEdit();
                props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fileName);
                props.addProperty(ResourceProperties.PROP_PUBVIEW, PUBVIEW_FALSE);
                resourceEdit.setContent(templateStream);
                resourceEdit.setContentType(mimeType);
                contentHostingService.commitResource(resourceEdit);
            }
            else
            {
                resourceEdit = contentHostingService.addResource(resourceId);
                ResourcePropertiesEdit props = resourceEdit.getPropertiesEdit();
                props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fileName);
                props.addProperty(ResourceProperties.PROP_PUBVIEW, PUBVIEW_FALSE);
                resourceEdit.setContent(templateStream);
                resourceEdit.setContentType(mimeType);
                contentHostingService.commitResource(resourceEdit);
            }
        }
        catch (PermissionException e)
        {
            throw new DocumentTemplateException ("(PermissionException) Error storing template", e);
        }
        catch (IdUsedException e)
        {
            throw new DocumentTemplateException ("(IdUsedException) Error storing template", e);
        }
        catch (IdInvalidException e)
        {
            throw new DocumentTemplateException ("(IdInvalidException) Error storing template", e);
        }
        catch (InconsistentException e)
        {
            throw new DocumentTemplateException ("(InconsistentException) Error storing template", e);
        }
        catch (OverQuotaException e)
        {
            throw new DocumentTemplateException ("(OverQuotaException) Error storing template", e);
        }
        catch (ServerOverloadException e)
        {
            throw new DocumentTemplateException ("(ServerOverloadException) Error storing template", e);
        }
        catch (VirusFoundException e)
        {
            throw new DocumentTemplateException ("(VirusFoundException) Error storing template", e);
        }
        catch (IdUnusedException e)
        {
            throw new DocumentTemplateException ("(IdUnusedException) Error storing template", e);
        }
        catch (TypeException e)
        {
            throw new DocumentTemplateException ("(TypeException) Error storing template", e);
        }
        catch (InUseException e)
        {
            throw new DocumentTemplateException ("(InUseException) Error storing template", e);
        }

        finally
        {
            getSecurityService().popAdvisor();
            getSecurityService().popAdvisor();
        }
        return resourceEdit;
    }

    public String getMimeType (byte[] toCheck) throws DocumentTemplateException
    {
        Magic mimeMagicParser = new Magic();
        try
        {
            //mimeMagicParser.getMagicMatch(arg0, arg1)
            MagicMatch mimeTypeMatch = mimeMagicParser.getMagicMatch(toCheck, true);
            return mimeTypeMatch.getMimeType();
        }
        catch (MagicParseException e)
        {
            throw new DocumentTemplateException (e);
        }
        catch (MagicMatchNotFoundException e)
        {
            throw new DocumentTemplateException (e);
        }
        catch (MagicException e)
        {
            throw new DocumentTemplateException (e);
        }
    }

    private DocumentTemplateHibernateImpl processFile (DocumentTemplateHibernateImpl docTemp, final String fileName,
                                          final String mimeType, final InputStream template)
        throws DocumentTemplateException, UnsupportedTemplateTypeException
    {
        final CertificateDefinition cd = docTemp.getCertificateDefinition();

        if (cd == null)
        {
            throw new DocumentTemplateException("No CertificateDefinition set");
        }

        docTemp.setName(fileName);
        final String resourceId = DocumentTemplate.COLLECTION_ID + cd.getSiteId() + "/" + cd.getId() + "/" + fileName;
        ContentResourceEdit templateFile = null;
        try
        {
            templateFile = (ContentResourceEdit) doSecureCertificateService(new SecureCertificateServiceCallback()
            {
                public Object doSecureAction() throws Exception
                {
                    return storeTemplateFile(cd.getSiteId(), cd.getId(), template, fileName, mimeType, resourceId);
                }
            });
        }
        catch(Exception e)
        {
            throw new TemplateReadException ("Could not write Document Template with id: " + resourceId, e);
        }

        docTemp.setResourceId(resourceId);
        String newMimeType = mimeType;

        if (newMimeType == null)
        {
            try
            {
                newMimeType = getMimeType(templateFile.getContent());
            }
            catch (ServerOverloadException e)
            {
                throw new DocumentTemplateException ("Error storing template", e);
            }
        }

        if (null == getDocumentTemplateService().getRenderEngineForMimeType(newMimeType))
        {
            deleteTemplateFile(resourceId);
            throw new UnsupportedTemplateTypeException(newMimeType);
        }

        docTemp.setOutputMimeType(newMimeType);

        return docTemp;
    }

    public DocumentTemplate setDocumentTemplate(String certificateDefinitionId, String name, InputStream template) throws IdUnusedException, DocumentTemplateException
    {
        return setDocumentTemplate(certificateDefinitionId, name, null, template);
    }

    public DocumentTemplate setDocumentTemplate(final String certificateDefinitionId, final String name,
                                                final String mimeType, final InputStream template)
        throws IdUnusedException, UnsupportedTemplateTypeException, DocumentTemplateException
    {
        try
        {
            return (DocumentTemplate) getHibernateTemplate().execute(new HibernateCallback()
            {
                public Object doInHibernate(Session session) throws HibernateException, SQLException
                {
                    boolean updating = false;
                    CertificateDefinitionHibernateImpl cd = (CertificateDefinitionHibernateImpl)session.load(CertificateDefinitionHibernateImpl.class,
                                                                              certificateDefinitionId);
                    DocumentTemplateHibernateImpl dthi = (DocumentTemplateHibernateImpl) cd.getDocumentTemplate();

                    updating = (dthi != null);

                    if (!updating)
                    {
                        dthi = new DocumentTemplateHibernateImpl();
                        cd.setDocumentTemplate(dthi);
                        dthi.setCertificateDefinition(cd);
                    }

                    try
                    {
                        dthi = processFile (dthi, name, mimeType, template);
                    }
                    catch (DocumentTemplateException e)
                    {
                        throw new RuntimeException (e);
                    }

                    if (!updating)
                    {
                        session.save(dthi);
                    }
                    else
                    {
                        session.update(dthi);
                    }

                    return dthi;
                }
            });
        }
        catch (ObjectNotFoundException onfe)
        {
            throw new IdUnusedException (certificateDefinitionId);
        }
        catch (HibernateObjectRetrievalFailureException horfe)
        {
            throw new IdUnusedException (certificateDefinitionId);
        }
        catch (RuntimeException re)
        {
            Throwable t = re.getCause();
            if (t instanceof DocumentTemplateException)
            {
                throw (DocumentTemplateException)t;
            }

            throw re;
        }
    }

    public InputStream getTemplateFileInputStream(final String resourceId) throws TemplateReadException
    {
        FileInputStream fis = null;

        try
        {
            fis = (FileInputStream) doSecureCertificateService(new SecureCertificateServiceCallback()
            {
                public Object doSecureAction() throws Exception
                {
                    ContentResource resource = contentHostingService.getResource(resourceId);
                    return resource.streamContent();
                }
            });
        }
        catch(Exception e)
        {
            throw new TemplateReadException ("Could not read Document Template with id: " + resourceId, e);
        }

        return fis;
    }

    private Object doSecureCertificateService(SecureCertificateServiceCallback callback) throws Exception
    {
        final SessionManager sessionManager = getSessionManager();

        try
        {
            securityService.pushAdvisor(new SecurityAdvisor()
            {
                public SecurityAdvice isAllowed(String userId, String function, String reference)
                {
                    return SecurityAdvice.ALLOWED;
                }
            });

            return callback.doSecureAction();
        }
        finally
        {
           securityService.popAdvisor();
        }
    }

    public void setFieldValues(String certificateDefinitionId, Map<String, String> fieldValues) throws IdUnusedException
    {
        CertificateDefinitionHibernateImpl cd = (CertificateDefinitionHibernateImpl)getCertificateDefinition(certificateDefinitionId);
        cd.setFieldValues(fieldValues);
        getHibernateTemplate().update(cd);
    }

    public void activateCertificateDefinition(String certificateDefinitionId, boolean active) throws IncompleteCertificateDefinitionException, IdUnusedException
    {
        CertificateDefinitionHibernateImpl cd = (CertificateDefinitionHibernateImpl)getCertificateDefinition(certificateDefinitionId);

        if (cd.getDocumentTemplate() == null || cd.getName() == null || cd.getAwardCriteria() == null || cd.getFieldValues() == null)
        {
            throw new IncompleteCertificateDefinitionException ("incomplete certificate definition");
        }

        cd.setStatus (active ? CertificateDefinitionStatus.ACTIVE : CertificateDefinitionStatus.INACTIVE);
        getHibernateTemplate().update(cd);
    }

    private void setCriteriaFactoryOnCriteria(CertificateDefinition certDef)
    {
        Set<Criterion> criteria = certDef.getAwardCriteria();

        if (criteria != null)
        {
            for (Criterion crit : criteria)
            {
                AbstractCriterionHibernateImpl criterion = (AbstractCriterionHibernateImpl)crit;
                criterion.setCriteriaFactory(criteriaFactoryMap.get(criterion.getClass()));
            }
        }
    }

    private void setCertificateServiceOnCriteria(CertificateDefinition certDef)
    {
        Set<Criterion> criteria = certDef.getAwardCriteria();
        if (criteria != null)
        {
            for (Criterion crit : criteria)
            {
                AbstractCriterionHibernateImpl criterion = (AbstractCriterionHibernateImpl)crit;
                criterion.setCertificateService(this);
            }
        }
    }

    public CertificateDefinition getCertificateDefinitionByName (String siteId, String name) throws IdUnusedException
    {
        List results = getHibernateTemplate().findByNamedQuery(QUERY_CERTIFICATE_DEFINITION_BY_NAME, new Object[] { siteId, name });

        if (results == null || results.isEmpty())
        {
            throw new IdUnusedException ("site: " + siteId + " name: " + name);
        }

        return (CertificateDefinition) results.get(0);
    }

    public CertificateDefinition getCertificateDefinition(String id) throws IdUnusedException
    {
        try
        {
            CertificateDefinitionHibernateImpl certDef = (CertificateDefinitionHibernateImpl) getHibernateTemplate().load(CertificateDefinitionHibernateImpl.class, id);
            setCriteriaFactoryOnCriteria(certDef);
            setCertificateServiceOnCriteria(certDef);
            return certDef;
        }
        catch (ObjectNotFoundException onfe)
        {
            throw new IdUnusedException (id);
        }
        catch (HibernateObjectRetrievalFailureException horfe)
        {
            throw new IdUnusedException (id);
        }
    }

    public Set<CertificateDefinition> getCertificateDefinitions()
    {
        HashSet<CertificateDefinition> cds = new HashSet<CertificateDefinition>();
        cds.addAll(getHibernateTemplate().loadAll(CertificateDefinitionHibernateImpl.class));

        for (CertificateDefinition certDef : cds)
        {
            CertificateDefinitionHibernateImpl cert = (CertificateDefinitionHibernateImpl) certDef;
            setCriteriaFactoryOnCriteria(cert);
            setCertificateServiceOnCriteria(cert);
        }
        return cds;
    }

    public Set<CertificateDefinition> getCertificateDefinitionsForSite(final String siteId)
    {
        HashSet<CertificateDefinition> cds = new HashSet<CertificateDefinition>();
        List<CertificateDefinition> result = null;

        result = (List<CertificateDefinition>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                Query q = session.getNamedQuery(QUERY_CERTIFICATE_DEFINITIONS_BY_SITE).setString(PARAM_SITE_ID, siteId);
                return q.list();
            }
        });

        cds.addAll(result);
        for (CertificateDefinition certDef : cds)
        {
            CertificateDefinitionHibernateImpl cert = (CertificateDefinitionHibernateImpl) certDef;
            setCriteriaFactoryOnCriteria(cert);
            setCertificateServiceOnCriteria(cert);
        }

        return cds;
    }

    public Set<CertificateDefinition> getCertificateDefinitionsForSite(final String siteId, final CertificateDefinitionStatus[] statuses)
    {
        HashSet<CertificateDefinition> cds = new HashSet<CertificateDefinition>();
        List<CertificateDefinition> result = null;

        result = (List<CertificateDefinition>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                Query q = session.getNamedQuery(QUERY_CERTIFICATE_DEFINITIONS_BY_SITE_AND_STATUS);
                q.setString(PARAM_SITE_ID, siteId);
                q.setParameterList(PARAM_STATUSES, statuses);
                return q.list();
            }
        });

        cds.addAll(result);
        for (CertificateDefinition certDef : cds)
        {
            CertificateDefinitionHibernateImpl cert = (CertificateDefinitionHibernateImpl) certDef;
            setCriteriaFactoryOnCriteria(cert);
            setCertificateServiceOnCriteria(cert);
        }

        return cds;
    }

    public void setAwardCriteria(final String certificateDefinitionId, final Set<Criterion> conditions) throws IdUnusedException
    {
        try
        {
            getHibernateTemplate().execute(new HibernateCallback()
            {
                public Object doInHibernate(Session session) throws HibernateException, SQLException
                {
                    CertificateDefinitionHibernateImpl cd = null;

                    try
                    {
                        cd = (CertificateDefinitionHibernateImpl)getCertificateDefinition(certificateDefinitionId);
                    }
                    catch (IdUnusedException e)
                    {
                        throw new RuntimeException (e);
                    }

                    Set<Criterion> existingConditions = cd.getAwardCriteria();
                    for (Criterion condition : conditions)
                    {
                        if (existingConditions.contains(condition))
                        {
                            //do nothing, a criterion's bindings never change
                        }
                        else
                        {
                            session.save (condition);
                        }
                    }

                    cd.setAwardCriteria(conditions);
                    session.merge(cd);
                    return null;
                }
            });
        }
        catch (RuntimeException re)
        {
            Throwable t = re.getCause();

            if (t == null)
            {
                throw re;
            }

            if (t instanceof IdUnusedException)
            {
                throw (IdUnusedException) t;
            }

            throw re;
        }
    }

    public Criterion addAwardCriterion(final String certificateDefinitionId, final Criterion criterion) throws IdUnusedException
    {
        try
        {
            return (Criterion) getHibernateTemplate().execute(new HibernateCallback()
            {
                public Object doInHibernate(Session session) throws HibernateException, SQLException
                {
                    CertificateDefinitionHibernateImpl cd = (CertificateDefinitionHibernateImpl)session.load(CertificateDefinitionHibernateImpl.class,
                                                                                                             certificateDefinitionId);
                    Set<Criterion> criteria = cd.getAwardCriteria();
                    session.save(criterion);
                    criteria.add(criterion);
                    session.update(cd);
                    return criterion;
                }
            });
        }
        catch (RuntimeException e)
        {
            Throwable t = e.getCause();
            if (t != null)
            {
                if (t instanceof IdUnusedException)
                {
                    throw (IdUnusedException)t;
                }
            }

            throw e;
        }
    }

    public void removeAwardCriterion(String certificateDefinitionId, String criterionId) throws IdUnusedException
    {
        CertificateDefinitionHibernateImpl cd = (CertificateDefinitionHibernateImpl)getCertificateDefinition(certificateDefinitionId);
        Set<Criterion> criterions = cd.getAwardCriteria();

        Criterion removeThis = null;
        for(Criterion criterion : criterions)
        {
            if(criterionId.equals(criterion.getId()))
            {
                removeThis = criterion;
                break;
            }
        }

        cd.getAwardCriteria().remove(removeThis);
        HibernateTemplate ht = getHibernateTemplate();
        ht.update(cd);
    }

    public Set<Criterion> getUnmetAwardConditions(String certificateDefinitionId, boolean useCaching) throws IdUnusedException, UnknownCriterionTypeException
    {
        return getUnmetAwardConditionsForUser(certificateDefinitionId, userId(), useCaching);
    }

    public Set<Criterion> getUnmetAwardConditionsForUser(String certificateDefinitionId, String userId, boolean useCaching)
            throws IdUnusedException, UnknownCriterionTypeException
    {
        String contextId = contextId();
        CertificateDefinitionHibernateImpl cd = (CertificateDefinitionHibernateImpl)getCertificateDefinition(certificateDefinitionId);
        Set<Criterion> criteria = cd.getAwardCriteria();
        Set<Criterion> unmetCriteria = new HashSet<Criterion>();

        for (Criterion criterion : criteria)
        {
            CriteriaFactory cFact = criteriaFactoryMap.get(criterion.getClass());

            if (!cFact.isCriterionMet(criterion, userId, contextId, useCaching))
            {
                unmetCriteria.add(criterion);
            }
        }

        return unmetCriteria;
    }

    public Map<String, String> getPredefinedTemplateVariables()
    {
        HashMap<String, String> predefined = new HashMap<String, String>();

        for (String key : variableResolvers.keySet())
        {
            VariableResolver vr = variableResolvers.get(key);

            for (String label : vr.getVariableLabels())
            {
                predefined.put(label, vr.getVariableDescription(label));
            }
        }

        return predefined;
    }

    public void registerCriteriaFactory(CriteriaFactory cFact)
    {
        Set<Class<? extends Criterion>> critClasses = cFact.getCriterionTypes();
        for (Class critClass : critClasses)
        {
            criteriaFactoryMap.put(critClass, cFact);
        }

        Set<CriteriaTemplate> templates = cFact.getCriteriaTemplates();
        for (CriteriaTemplate template : templates)
        {
            criteriaTemplateMap.put (template.getId(), cFact);
        }

        criteriaFactories.add(cFact);
    }

    public CriteriaFactory getCriteriaFactory (String criteriaTemplateId)
    {
        return criteriaTemplateMap.get(criteriaTemplateId);
    }

    public Set<CriteriaTemplate> getCriteriaTemplates()
    {
        HashSet<CriteriaTemplate> criteriaTemplates = new HashSet<CriteriaTemplate>();
        for (CriteriaFactory factory : criteriaFactories)
        {
            criteriaTemplates.addAll(factory.getCriteriaTemplates());
        }

        return criteriaTemplates;
    }

    @SuppressWarnings("unchecked")
    public int getCategoryType(final String gradebookId)
    {
        return (Integer) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Integer doInHibernate(Session session) throws HibernateException, SQLException
            {
                List<Integer> list = session.createQuery("select gb.category_type from CertGradebook as gb where gb.uid=:gbid")
                        .setParameter(PARAM_GBID, gradebookId).list();
                return (Integer) list.get(0);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public Map<Long,Double> getCategoryWeights(final String gradebookId)
    {
        return (Map<Long, Double>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                List<Object[]> results = session.createQuery("select assn.id, cat.weight from CertCategory as cat, CertAssignment as assn " +
                        "where cat.gradebook.uid=:gbid and cat.removed=false " +
                        "and cat.id = assn.category.id and assn.notCounted=false and assn.removed=false"
                    ).setParameter(PARAM_GBID, gradebookId).list();

                Map<Long,Double> catWeightMap = new HashMap<Long,Double>();
                for(Object[] row : results)
                {
                    catWeightMap.put((Long) row[0], (Double) row[1]);
                }
                return catWeightMap;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public Map<Long, Double> getAssignmentWeights(final String gradebookId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                List<Object[]> results =  session.createQuery("select assn.id, assn.assignmentWeighting from CertAssignment as assn " +
                        "where assn.notCounted=false and assn.removed=false and " +
                        "assn.gradebook.uid=:gradebookId"
                    ).setParameter(PARAM_GRADEBOOK_ID, gradebookId).list();

                Map<Long, Double> assnWeights = new HashMap<Long, Double>();
                for(Object[] row : results)
                {
                    assnWeights.put((Long) row[0], (Double) row[1]);
                }

                return assnWeights;
            }
        };

        return (Map<Long,Double>)getHibernateTemplate().execute(callback);
    }

    @SuppressWarnings("unchecked")
    public Map<Long, Double> getCatOnlyAssignmentPoints(final String gradebookId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                List<Object[]> results = session.createQuery("select assn.id, assn.pointsPossible from CertCategory as cat, CertAssignment as assn " +
                        "where cat.gradebook.uid=:gradebookId and cat.removed=false " +
                        "and cat.id = assn.category.id and assn.notCounted=false " +
                        "and assn.removed=false"
                    ).setParameter(PARAM_GRADEBOOK_ID, gradebookId).list();

                HashMap<Long, Double> assnPoints = new HashMap<Long, Double>();
                for(Object[] row : results)
                {
                    assnPoints.put((Long) row[0], (Double) row[1]);
                }

                return assnPoints;
            }
        };

         return (HashMap<Long, Double>)getHibernateTemplate().execute(callback);
    }

    @SuppressWarnings("unchecked")
    public Map<Long, Double> getAssignmentPoints(final String gradebookId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                List<Object[]> results = session.createQuery("select assn.id, assn.pointsPossible from CertAssignment as assn " +
                        "where assn.removed=false and assn.notCounted=false and " +
                        "assn.gradebook.uid=:gradebookId"
                     ).setParameter(PARAM_GRADEBOOK_ID, gradebookId).list();

                HashMap<Long, Double> assnPoints = new HashMap<Long, Double>();
                for(Object[] row : results)
                {
                    assnPoints.put((Long) row[0], (Double) row[1]);
                }

                return assnPoints;
            }
        };

         return (HashMap<Long, Double>)getHibernateTemplate().execute(callback);
    }

    public Map<Long, Double> getAssignmentScores(final String gradebookId, final String studentId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Iterator results = session.createQuery("select agr.gradableObject.id, agr.pointsEarned from CertAssignmentScore as agr " +
                        "where agr.gradableObject.removed=false " +
                        "and agr.gradableObject.released=true " +
                        "and agr.gradableObject.gradebook.uid=:gradebookId and agr.studentId = :studentId"
                     ).setParameter(PARAM_GRADEBOOK_ID, gradebookId).setParameter(PARAM_STUDENT_ID, studentId).list().iterator();

                HashMap<Long, Double> assnScores = new HashMap<Long, Double>();
                while(results.hasNext())
                {
                    Object[] row = (Object[]) results.next();
                    assnScores.put((Long) row[0],(Double) row[1]);
                }

                return assnScores;
            }
        };

        return (HashMap<Long, Double>)getHibernateTemplate().execute(callback);
    }

    public Map<Long, Date> getAssignmentDatesRecorded (final String gradebookId, final String studentId)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Iterator results = session.createQuery("select agr.gradableObject.id, agr.dateRecorded from CertAssignmentScore as agr " +
                         "where agr.gradableObject.removed=false " +
                        "and agr.gradableObject.released=true " +
                        "and agr.gradableObject.gradebook.uid=:gradebookId and agr.studentId = :studentId"
                    ).setParameter(PARAM_GRADEBOOK_ID, gradebookId).setParameter(PARAM_STUDENT_ID, studentId).list().iterator();

                HashMap<Long, Date> assnDates = new HashMap<Long, Date>();
                while(results.hasNext())
                {
                    Object[] row = (Object[]) results.next();
                    assnDates.put((Long) row[0],(Date) row[1]);
                }

                return assnDates;
            }
        };

        return (HashMap<Long, Date>)getHibernateTemplate().execute(callback);
    }

    @Override
    public List<Map.Entry<String, String>> getCertificateRequirementsForUser (String certId, String userId, String siteId, boolean useCaching) throws IdUnusedException
    {
        CertificateDefinition certDef = getCertificateDefinition(certId);
        Map requirements = new HashMap<String, String>();

        Set<Criterion> criteria = certDef.getAwardCriteria();
        Iterator<Criterion> itCriteria = criteria.iterator();
        while (itCriteria.hasNext())
        {
            Criterion crit = itCriteria.next();
            CriteriaFactory factory = crit.getCriteriaFactory();
            String expression = crit.getExpression();
            String progress = crit.getProgress(userId, siteId, useCaching);

            //progress is "" if it's irrelevant (ie. WillExpire criterion)
            if ( !"".equals(progress) )
            {
                requirements.put(expression, progress);
            }
        }

        return new ArrayList<Map.Entry<String, String>>(requirements.entrySet());
    }

    public Collection<String> getGradedUserIds(final String siteId)
    {
        /* Gets all users who have earned grades in the site - regardless of whether they are still enrolled
         * (for historical purposes)*/

        /*
        The query would translate to this (minus the the sakai_user_id_map part):
        SELECT map.eid from sakai_user_id_map map
        WHERE map.user_id in (
            SELECT distinct gr.student_id from gb_grade_record_t gr
            WHERE gr.gradable_object_id in (
            SELECT gbo.id from gb_gradable_object_t gbo
            WHERE gbo.gradebook_id in (
                SELECT gb.id from gb_gradebook_t gb
                WHERE gb.gradebook_uid = '<siteId>')));
        */

        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                //We need to get people from gb_grade_record_t, grading event only gives us people graded from the gradebook tool
                String query = 	"select distinct gr.studentId from CertGradeRecordObject as gr " +
                        "where gr.gradableObject in ( " +
                            "select gbo.id from CertGradebookObject as gbo " +
                            "where gbo.gradebook.uid = :siteId " +
                        ")";
                return session.createQuery(query).setParameter(PARAM_SITE_ID, siteId).list();
            }
        };

        return (Collection<String>) getHibernateTemplate().execute(callback);
    }

    @Override
    public List<ReportRow> getReportRows(List<String> userIds, CertificateDefinition definition, String filterType, String filterDateType, Date startDate, Date endDate, List<Criterion> orderedCriteria)
    {
        if (definition == null)
        {
            return null;
        }

        boolean showUnawarded = false;
        if ("all".equals(filterType))
        {
            showUnawarded = true;
        }
        else if ("unawarded".equals(filterType))
        {
            showUnawarded = true;
        }

        List<ReportRow> reportRows = new ArrayList<ReportRow>();

        //we'll need this to get additional user properties
        ExtraUserPropertyUtilityImpl extraPropsUtil = new ExtraUserPropertyUtilityImpl();
        //determines if the current user has permission to view extra properties
        boolean canShowUserProps = extraPropsUtil.isExtraUserPropertiesEnabled() && extraPropsUtil.isExtraPropertyViewingAllowedForCurrentUser();

        //Get the headers for the additional user properties
        //keeps track of the order of the keys so that we know that the headers and the cells line up
        Map<String, String> propKeysTitles = extraPropsUtil.getExtraUserPropertiesKeyAndTitleMap();
        List<String> propKeys = new ArrayList<String>(propKeysTitles.keySet());

        //Get the criteria in the order of the displayed columns
        WillExpireCriterionHibernateImpl wechi = null;
        Iterator<Criterion> itOrderedCriteria = orderedCriteria.iterator();
        while (itOrderedCriteria.hasNext())
        {
            Criterion crit = itOrderedCriteria.next();
            if (crit instanceof WillExpireCriterionHibernateImpl)
            {
                wechi = (WillExpireCriterionHibernateImpl) crit;
                break;
            }
        }

        Iterator<CriteriaFactory> itCritFactories = criteriaFactories.iterator();
        while (itCritFactories.hasNext())
        {
            CriteriaFactory critFact = itCritFactories.next();
            critFact.clearCaches();
        }

        /*
         * Focus on retriving data per column rather than per row, and try to combine multiple columns into single queries when possible
         * For instance, we can get all the scores and all the dates for the greater than score criteria using something like:
         *
         * select student_id, points_earned, date_recorded, gradable_object_id from gb_grade_record_t
         * where gradable_object_id in (<gradableObject1.id>, <gradableObject2.id>, ... <gradableObjectN.id>) order by student_id;
         *
         * This reduces the number of queries from:
         * #students * 2 * #greater than score critieria
         * to 1 query. So if your site has 5000 users, then we're reducing 10,000 queries to 1
         */
       // If we have all criteria, here are the headers:
       // Name (sorts by lname; default sort), userId, Role, Employee Number, Issue Date, Expires, Gb Item, Final Course Grade, Due Date for Gb Item2, Awarded
       List<User> users = getUserDirectoryService().getUsers(userIds);

       /*
        * Iterate over criteria; grab any that are of the same type
        * (ie. 5 different 'Greater than Score' criteria would go into the same collection).
        * Invoke a method in the factory for that type of criterion called:
        * getProgressForCriteriaForUsers(List<Criterion>, List<userId>) which returns Map<userId, Map<Criterion, UserProgress>>
        * UserProgress is a class contaning userId, criterion, String progress, boolean passed, Date datePassed.
        */

       // Maps users to their progress on all criteria
       Map<String, Map<Criterion, UserProgress>> allUserProgress = new HashMap<String, Map<Criterion, UserProgress>>();

       Set<Criterion> criteria = definition.getAwardCriteria();
       String siteId = definition.getSiteId();

       /*
        * To get the UserProgress towards the criteria later, we'll be invoking the CriteriaFactories once for each criterion type
        * (this is how we'll minimize the number of queries).
        * So we need a mapping of CriteriaFactories to the criterion types, and the criterion types need to be mapped to the criteria that match each criterion type.
        * Ie. CriteriaFactory -> (Criterion type managed by CriteriaFactory -> Criteria of key'd Criterion type)
        */
       Map<CriteriaFactory, Map<Class, List<Criterion>>> critFactToCritCollectionMap = new HashMap<CriteriaFactory, Map<Class, List<Criterion>>>();
       for (Criterion criterion : criteria)
       {
           CriteriaFactory critFact = criterion.getCriteriaFactory();
           // Get the mapping of Criterion Type -> List<Criteria> that is associated with the current criterion's CriteriaFactory
           Map<Class, List<Criterion>> critTypeToCollectionMap = critFactToCritCollectionMap.get(critFact);
           if (critTypeToCollectionMap == null)
           {
               // The mapping of Criterion Type -> List<Criteria> for this CriterionFactory doesn't exist yet, so create one
               critTypeToCollectionMap = new HashMap<Class, List<Criterion>>();
               critFactToCritCollectionMap.put(critFact, critTypeToCollectionMap);
           }

           // Get the collection of criteria associated with this criterion's type
           Class criterionType = criterion.getClass();
           List<Criterion> critCollection = critTypeToCollectionMap.get(criterionType);
           if (critCollection == null)
           {
               // There is no collection of criteria associated with this criterion's type yet. Create one.
               critCollection = new ArrayList<Criterion>();
               critTypeToCollectionMap.put(criterionType, critCollection);
           }

           // associate this criterion with the criterion's type which is associated with the criterion's CriteriaFactory
           critCollection.add(criterion);
       }

       // Now execute the minimum number of queries to calculate each user's progress toward the criteria
       // Upon executing the queries, populate allUserProgress, which maps users -> (criteria -> user's progress on key'd criterion)
       for (Map.Entry<CriteriaFactory, Map<Class, List<Criterion>>> critFactToCritCollectionEntry: critFactToCritCollectionMap.entrySet())
       {
           CriteriaFactory critFact = critFactToCritCollectionEntry.getKey();
           Map<Class, List<Criterion>> critTypeToCollectionMap = critFactToCritCollectionEntry.getValue();
           for (Map.Entry<Class, List<Criterion>> classToCritListEntry : critTypeToCollectionMap.entrySet())
           {
               Class type = classToCritListEntry.getKey();
               List<Criterion> critCollection = classToCritListEntry.getValue();
               Map<String, Map<Criterion, UserProgress>> currentUserProgress = critFact.getProgressForUsers(siteId, userIds, type, critCollection);

               /*
                * The key is User, so invoking putAll would replace the values of the parent map.
                * For instance, if allUserProgress.get(userId) contains mappings of DueDatePassed -> UserProgress,
                * and we are currently looking at GreaterThanScore, then if we invoked
                * allUserProgress.putAll(currentUserProgress), this would not merge in the GreaterThanScore -> UserProgress mappings,
                * but rather it would replace the entire sub map, and we would lose our DueDatePassed -> UserProgress.
                * So we need to iterate through the userIds and merge the currentUserProgress.get(userId) results into allUserProgress.get(userId)
                */

               for (String userId : userIds)
               {
                   Map<Criterion, UserProgress> currentCriterionMap = currentUserProgress.get(userId);
                   if (currentCriterionMap != null)
                   {
                       Map<Criterion, UserProgress> allCriteriaMap = allUserProgress.get(userId);
                       if (allCriteriaMap == null)
                       {
                           // the current user doesn't have a mapping of Criterion -> UserProgress yet. Create one.
                           allCriteriaMap = new HashMap<Criterion, UserProgress>();
                           allUserProgress.put(userId, allCriteriaMap);
                       }

                       allCriteriaMap.putAll(currentCriterionMap);
                   }
               }
           }
       }

       // populate the report rows
       for (User user : users)
       {
           ReportRow row = new ReportRow();

           // populate the name fields
           String firstName = user.getFirstName();
           String lastName = user.getLastName();
           setNameFieldForReportRow(row, firstName, lastName);

           // userId for later use with our maps
           String userId = user.getId();
           // populate the userEid
           String userEid = user.getEid();
           // row's userId is the eid
           row.setUserId(userEid);

           // populate the role
           String role = getRole(userId, siteId);
           row.setRole(role);

           // populate the extra user properties
           ArrayList<String> extraProps = new ArrayList<String>();
           if (canShowUserProps)
           {
               Map<String, String> extraPropsMap = extraPropsUtil.getExtraPropertiesMapForUser(user);
               Iterator<String> itKeys = propKeys.iterator();
               while (itKeys.hasNext())
               {
                   String key = itKeys.next();
                   extraProps.add(extraPropsMap.get(key));
               }
           }
           row.setExtraProps(extraProps);

           // Determine the awarded status and the issue date using the UserProgress objects we previously retrieved
           Map<Criterion, UserProgress> critProgressMap = allUserProgress.get(userId);
           // assume this user is awarded until we find a criterion on which the user has failed
           boolean awarded = true;
           Date dateAwarded = null;
           if (criteria.isEmpty())
           {
               //TODO: ??? they're awarded, but when?
           }
           else if (critProgressMap == null)
           {
               // There are criteria, but this user doesn't have any mappings of Criterion -> UserProgress.
               // This means they have not made progress toward any criteria, hence they have failed.
               awarded = false;
           }
           else
           {
               for (Criterion criterion : criteria)
               {
                   UserProgress progress = critProgressMap.get(criterion);
                   if (progress == null || !progress.isPassed())
                   {
                       // null progress implies failure
                       awarded = false;
                       // date could have been set in previous iterations, so clear it
                       dateAwarded = null;
                       break;
                   }
                   else
                   {
                       // The user has passed all criteria so far
                       // Update the dateAwarded if the date that this user has met this criterion is later than all previous criteria
                       Date dateCritAwarded = progress.getDateAwarded();
                       if (dateAwarded == null)
                       {
                           // This is the first iteration; initialize dateAwarded
                           dateAwarded = dateCritAwarded;
                       }
                       else if (dateCritAwarded != null && dateAwarded.before(dateCritAwarded))
                       {
                           // The user was awarded on this criterion later than all previous criteria. Update the date awarded
                           dateAwarded = dateCritAwarded;
                       }
                   }
               }
           }

           // populate the awarded status
           String strAwarded = awarded ? messages.getString(MESSAGE_YES) : messages.getString(MESSAGE_NO);
           row.setAwarded(strAwarded);

           // populate the issue date
           String strIssueDate = dateAwarded == null ? null : dateFormat.format(dateAwarded);
           row.setIssueDate(strIssueDate);

           // populate the criterion cells
           List<CriterionProgress> criterionCells = new ArrayList<CriterionProgress>();
           if (critProgressMap == null)
           {
               // User made no progress on any criteria. Create placeholders
               for (int i = 0; i < orderedCriteria.size(); i++)
               {
                   CriterionProgress critProg = new CriterionProgress("", false);
                   criterionCells.add(critProg);
               }
           }
           else
           {
               // add the criterion cells in order (orderedCriteria is the order of the headers)
               for (Criterion criterion : orderedCriteria)
               {
                   CriterionProgress critProg;
                   if (criterion instanceof WillExpireCriterionHibernateImpl)
                   {
                       // populate the expiry date
                       String strExpiryDate = "";
                       if (awarded)
                       {
                           // it's awarded. The WillExpireCriterionHibernateImpl instance can do the expiry date calculation for us
                           Date expiryDate = wechi.getExpiryDate(dateAwarded);
                           strExpiryDate = dateFormat.format(expiryDate);
                       }

                       // wechi always passes
                       critProg = new CriterionProgress(strExpiryDate, true);
                   }
                   else
                   {
                       UserProgress userProg = critProgressMap.get(criterion);
                       if (userProg == null)
                       {
                           // no progress here, create placeholder
                           critProg = new CriterionProgress("", false);
                       }
                       else
                       {
                           critProg = new CriterionProgress(userProg.getProgress(), userProg.isPassed());
                       }
                   }

                   criterionCells.add(critProg);
               }
           }

           row.setCriterionCells(criterionCells);
           reportRows.add(row);
       }

        return reportRows;
    }

    /**
     * Sets the name field on the row in an appropriate format ('lastname, firstname' unless a name is missing)
     *
     * @param row
     * @param firstName
     * @param lastName
     */
    private void setNameFieldForReportRow(ReportRow row, String firstName, String lastName)
    {
        if (lastName == null)
        {
            lastName = "";
        }

        if (firstName == null)
        {
            firstName = "";
        }

        //if one name is missing, use the opposite
        if ("".equals(lastName))
        {
            //use the opposite name or empty string if firstName is missing (both cases are covered here)
            row.setName(firstName);
        }
        else if ("".equals(firstName))
        {
            row.setName(lastName);
        }
        else
        {
            //both names present
            row.setName(lastName+", "+firstName);
        }
    }

    private Site getSite(String siteId)
    {
        try
        {
            return siteService.getSite(siteId);
        }
        catch (IdUnusedException e)
        {
            return null;
        }
    }

    private String getRole(String userId, String siteId)
    {
        Role role = getSite(siteId).getUserRole(userId);
        if (role != null)
        {
            return role.getId();
        }
        else
        {
            return messages.getString(REPORT_TABLE_NOT_A_MEMBER);
        }
    }
}
