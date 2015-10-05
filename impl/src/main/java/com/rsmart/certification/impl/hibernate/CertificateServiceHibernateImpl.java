package com.rsmart.certification.impl.hibernate;

import com.rsmart.certification.api.BaseCertificateDefinition;
import com.rsmart.certification.api.CertificateAward;
import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.CertificateDefinitionStatus;
import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.DocumentTemplate;
import com.rsmart.certification.api.DocumentTemplateException;
import com.rsmart.certification.api.DocumentTemplateService;
import com.rsmart.certification.api.IncompleteCertificateDefinitionException;
import com.rsmart.certification.api.TemplateReadException;
import com.rsmart.certification.api.UnmetCriteriaException;
import com.rsmart.certification.api.UnsupportedTemplateTypeException;
import com.rsmart.certification.api.VariableResolver;
import com.rsmart.certification.api.criteria.CriterionCreationException;
import com.rsmart.certification.api.criteria.UnknownCriterionTypeException;
import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.impl.hibernate.criteria.AbstractCriterionHibernateImpl;
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
import org.sakaiproject.metaobj.security.AllowMapSecurityAdvisor;
import org.sakaiproject.shortenedurl.api.ShortenedUrlService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: duffy
 * Date: Jun 30, 2011
 * Time: 9:21:59 AM
 */
public class CertificateServiceHibernateImpl
    extends HibernateDaoSupport
    implements CertificateService
{
    private static Log
        LOG = LogFactory.getLog(CertificateServiceHibernateImpl.class);
    private DocumentTemplateService
        documentTemplateService = null;
    private UserDirectoryService
        userDirectoryService = null;
    private ShortenedUrlService
    	shortenedUrlService = null;
    private ToolManager
        toolManager = null;
    private SessionManager
    	sessionManager = null;
    private SecurityService
    	securityService = null;
    private String
    	adminUser = null;
    private String
        templateDirectory = null;
    private HashMap<String, CriteriaFactory>
        criteriaTemplateMap = new HashMap<String, CriteriaFactory>();
    private HashMap<Class, CriteriaFactory>
        criteriaFactoryMap = new HashMap<Class, CriteriaFactory>();
    private HashSet<CriteriaFactory>
        criteriaFactories = new HashSet<CriteriaFactory>();
    private HashMap<String, VariableResolver>
        variableResolvers = new HashMap<String, VariableResolver>();
	private ContentHostingService contentHostingService = null;

	private AuthzGroupService authzGroupService= null;
	private SiteService siteService= null;
	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
	    this.authzGroupService = authzGroupService;
	}
	
	public void setSiteService(SiteService siteService) {
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

    public ContentHostingService getContentHostingService() {
		return contentHostingService;
	}

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

    public void setDocumentTemplateService(DocumentTemplateService dts)
    {
        documentTemplateService = dts;
    }

    public DocumentTemplateService getDocumentTemplateService()
    {
        return documentTemplateService;
    }


    public ShortenedUrlService getShortenedUrlService() {
		return shortenedUrlService;
	}

	public void setShortenedUrlService(ShortenedUrlService shortenedUrlService) {
		this.shortenedUrlService = shortenedUrlService;
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

	public String getAdminUser() {
		return adminUser;
	}

	public void setAdminUser(String adminUser) {
		this.adminUser = adminUser;
	}

    public void init()
    {
        LOG.info("init");

        if (templateDirectory == null)
        {
            throw new IllegalStateException ("templateDirectory for CertificateService is not configured");
        }

        File
            dirFile = new File (templateDirectory);

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


        if (shortenedUrlService == null)
        {
        	throw new IllegalStateException ("ShortenedUrlService not provided to CertificateService");
        }
 

        for (VariableResolver resolver : documentTemplateService.getVariableResolvers())
        {
            for (String label : resolver.getVariableLabels())
            {
                variableResolvers.put(label, resolver);
            }
        }
    }

    private final String userId()
    {
        return getUserDirectoryService().getCurrentUser().getId();
    }

    private final String contextId()
    {
        return getToolManager().getCurrentPlacement().getContext();
    }
    
    private final String toolId()
    {
    	return getToolManager().getCurrentTool().getId();
    }
    
    private final String portalUrl()
    {
    	return ServerConfigurationService.getPortalUrl();
    }
    
    public void deleteCertificateDefinition(final String certificateDefinitionId)
    throws IdUnusedException, DocumentTemplateException
	{
	    CertificateDefinition
	        cd = (CertificateDefinition) getHibernateTemplate().execute
	        (
	            new HibernateCallback()
	            {
	                public Object doInHibernate(Session session)
	                    throws HibernateException, SQLException
	                {
	                    CertificateDefinitionHibernateImpl
	                        cd = (CertificateDefinitionHibernateImpl) session.load(CertificateDefinitionHibernateImpl.class, certificateDefinitionId);
	
	                    cd.getFieldValues().clear();
	                    cd.getAwardCriteria().clear();
	
	                    session.update(cd);
	
	                    session.flush();
	
	                    Query
	                        q = session.createQuery("delete from DocumentTemplateHibernateImpl where id=?");
	
	                    q.setString(0, certificateDefinitionId);
	
	                    q.executeUpdate();
	
                        q = session.createQuery("delete from CertificateAwardHibernateImpl where certificateDefinition.id=?");

                        q.setString(0, certificateDefinitionId);

                        q.executeUpdate();

	                    q = session.getNamedQuery("deleteCertificateDefinition");
	
	                    q.setString(0, certificateDefinitionId);
	
	                    q.executeUpdate();
	
	                    return cd;
	                }
	            }
	        );
	
	    deleteTemplateFile (cd.getDocumentTemplate().getResourceId());
	}
    
    public CertificateDefinition createCertificateDefinition(CertificateDefinition certificateDefinition)
        throws IdUsedException
    {
        BaseCertificateDefinition
            cd = (BaseCertificateDefinition)certificateDefinition;
        CertificateDefinitionHibernateImpl
            myCertDefn = new CertificateDefinitionHibernateImpl();

        myCertDefn.setAwardCriteria(cd.getAwardCriteria());
        myCertDefn.setCreateDate(new Date());
        myCertDefn.setCreatorUserId(userId());
        myCertDefn.setDescription(cd.getDescription());
        myCertDefn.setDocumentTemplate(cd.getDocumentTemplate());
        myCertDefn.setFieldValues(cd.getFieldValues());
        myCertDefn.setSiteId(cd.getSiteId());
        myCertDefn.setShortUrl(cd.getShortUrl());
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

    public CertificateDefinition updateCertificateDefinition(CertificateDefinition cd)
        throws IdUnusedException
    {
        try
        {
            getHibernateTemplate().update(cd);
        }
        catch (ObjectNotFoundException onfe)
        {
            throw new IdUnusedException(cd.getId());
        }
        catch (HibernateObjectRetrievalFailureException horfe)
        {
            throw new IdUnusedException(cd.getId());
        }

        return cd;
    }

    public CertificateDefinition createCertificateDefinition(String name, String description, String siteId)
        throws IdUsedException
    {
        CertificateDefinitionHibernateImpl
            certificateDefinition = new CertificateDefinitionHibernateImpl();

        certificateDefinition.setCreateDate(new Date());
        certificateDefinition.setCreatorUserId(userId());
        certificateDefinition.setDescription(description);
        certificateDefinition.setName(name);
        certificateDefinition.setSiteId(siteId);
        certificateDefinition.setStatus(CertificateDefinitionStatus.UNPUBLISHED);

        try
        {
            getHibernateTemplate().save(certificateDefinition);
        }
        catch (DataIntegrityViolationException dive)
        {
            throw new IdUsedException("name: " + name + " siteId: " + siteId);
        }

        return certificateDefinition;
    }

    public CertificateDefinition createCertificateDefinition (final String name, final String description,
                                                              final String siteId, final String fileName, 
                                                              final String mimeType, final InputStream template)
        throws IdUsedException, UnsupportedTemplateTypeException, DocumentTemplateException
    {
        CertificateDefinitionHibernateImpl
            cd = null;
        try
        {
            cd = (CertificateDefinitionHibernateImpl) getHibernateTemplate().execute(
                new HibernateCallback()
                {
                    public Object doInHibernate(Session session)
                        throws HibernateException, SQLException
                    {
                        CertificateDefinitionHibernateImpl
                            certificateDefinition = new CertificateDefinitionHibernateImpl();

                        certificateDefinition.setCreateDate(new Date());
                        certificateDefinition.setCreatorUserId(userId());
                        certificateDefinition.setDescription(description);
                        certificateDefinition.setName(name);
                        certificateDefinition.setSiteId(siteId);
                        certificateDefinition.setStatus(CertificateDefinitionStatus.UNPUBLISHED);

                        session.save(certificateDefinition);

                        DocumentTemplateHibernateImpl
                            documentTemplate = new DocumentTemplateHibernateImpl();

                        documentTemplate.setCertificateDefinition(certificateDefinition);

                        try
                        {
                            documentTemplate = processFile (documentTemplate, fileName, mimeType, template);
                        }
                        catch (DocumentTemplateException e)
                        {
                            throw new RuntimeException(e);
                        }

                        session.save(documentTemplate);
                        
                        session.flush();
                        
                        return certificateDefinition;
                    }
                }
            );
        }
        catch (RuntimeException re)
        {
            Throwable
                t = re.getCause();

            if (t != null)
            {
                if (t instanceof IdUsedException)
                    throw (IdUsedException) t;
                if (t instanceof UnsupportedTemplateTypeException)
                    throw (UnsupportedTemplateTypeException) t;
                if (t instanceof DocumentTemplateException)
                    throw (DocumentTemplateException) t;
            }
            else
                t = re;

            throw new DocumentTemplateException ("Unhandled exception creating new certificate definition", t);
        }

        return cd;
    }

   /* private void streamFileToStorage (InputStream in, OutputStream out)
        throws IOException
    {
        BufferedInputStream
            bis = new BufferedInputStream(in);
        BufferedOutputStream
            bos = new BufferedOutputStream(out);
        int
            chunkSize = 64*1024,
            lastRead = 0;
        byte
            chunk[] = new byte[chunkSize];

        while ((lastRead = bis.read(chunk)) > 0)
        {
            out.write(chunk, 0, lastRead);
        }
    }*/

    private void deleteTemplateFile (String resourceId)
    {
    	try 
    	{
    		String certDefCId = contentHostingService.getContainingCollectionId(resourceId);
			contentHostingService.removeResource(resourceId);
			contentHostingService.removeCollection(certDefCId);
		}
    	catch (PermissionException e) 
    	{
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
       /* File
            templateDirectory = new File(getTemplateDirectory()),
            siteDirectory = new File (templateDirectory, siteId),
            templateFile = null;

        if (!siteDirectory.exists() && !siteDirectory.mkdir())
        {
            throw new DocumentTemplateException ("Cannot create template directory for site: " + siteId);
        }

        templateFile = new File (siteDirectory, certificateId);

        FileOutputStream
            fos = null;

        try
        {
            fos = new FileOutputStream(templateFile);

            streamFileToStorage(templateStream, fos);
        }
        catch (IOException ioe)
        {
            throw new DocumentTemplateException ("Error storing template", ioe);
        }
        finally
        {
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException e) {}
            }
        }*/
        
        ContentResourceEdit resourceEdit = null;
        boolean resourceExist = false;
        
        try
        {
        	try{
        		
        	if(authzGroupService.getAuthzGroup(siteService.siteReference(siteId)).isAllowed(sessionManager.getCurrentSessionUserId(), "certificate.admin")){
        		getSecurityService().pushAdvisor(
           	         new AllowMapSecurityAdvisor(ContentHostingService.EVENT_RESOURCE_READ,
           	        		 contentHostingService.getReference(resourceId)));
        		getSecurityService().pushAdvisor(
          	         new AllowMapSecurityAdvisor(ContentHostingService.EVENT_RESOURCE_ADD,
          	        		 contentHostingService.getReference(resourceId)));
        	}
        	}catch(Exception e){}
        	
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
        		props.addProperty(ResourceProperties.PROP_PUBVIEW, "false");
        		resourceEdit.setContent(templateStream);
        		resourceEdit.setContentType(mimeType);
        		contentHostingService.commitResource(resourceEdit);
        	}
        	else
        	{
        		resourceEdit = contentHostingService.addResource(resourceId);
        		ResourcePropertiesEdit props = resourceEdit.getPropertiesEdit();
        		props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fileName);
        		props.addProperty(ResourceProperties.PROP_PUBVIEW, "false");
        		resourceEdit.setContent(templateStream);
        		resourceEdit.setContentType(mimeType);
        		contentHostingService.commitResource(resourceEdit);
        	}
        		//edit.setContentType(arg0);
//        	}
//        	else
//        	{
//        		ContentCollectionEdit edit = contentHostingService.addCollection(DocumentTemplate.COLLECTION_ID);
//        		ResourcePropertiesEdit propsFolder = edit.getPropertiesEdit();
//        		propsFolder.addProperty(ResourceProperties.PROP_DISPLAY_NAME, "Certification");
//                propsFolder.addProperty(ResourceProperties.PROP_DESCRIPTION, "Certification Templates Collection");
//                propsFolder.addProperty(ResourceProperties.PROP_PUBVIEW, "false");
//                propsFolder.addProperty(ResourceProperties.PROP_RESOURCE_TYPE, "org.sakaiproject.content.types.folder&action=create");
//                propsFolder.addProperty(ResourceProperties.PROP_CREATION_DATE, new Date().toString());
//                propsFolder.addProperty(ResourceProperties.PROP_IS_COLLECTION, "true");
//                contentHostingService.commitCollection(edit);
//        	}
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
        
        finally{
    		getSecurityService().popAdvisor();
    		getSecurityService().popAdvisor();
    	}
        return resourceEdit;
    }

    public String getMimeType (byte[] toCheck)
        throws DocumentTemplateException
    {
        Magic
            mimeMagicParser = new Magic();
        try
        {
        	//mimeMagicParser.getMagicMatch(arg0, arg1)
            MagicMatch
                mimeTypeMatch = mimeMagicParser.getMagicMatch(toCheck, true);

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

    private DocumentTemplateHibernateImpl processFile (DocumentTemplateHibernateImpl docTemp, String fileName,
                                          String mimeType, InputStream template)
        throws DocumentTemplateException, UnsupportedTemplateTypeException
    {
        final CertificateDefinition
            cd = docTemp.getCertificateDefinition();

        if (cd == null)
        {
            throw new DocumentTemplateException("No CertificateDefinition set");
        }

        docTemp.setName(fileName);
        String resourceId = DocumentTemplate.COLLECTION_ID + cd.getSiteId() + "/" + cd.getId() + "/" + fileName;
        ContentResourceEdit
            templateFile = storeTemplateFile(cd.getSiteId(), cd.getId(), template, fileName, mimeType, resourceId);

        docTemp.setResourceId(resourceId);
        //docTemp.setPath(templateFile.getPath());

        if (mimeType == null)
        {
            try 
            {
				mimeType = getMimeType(templateFile.getContent());
			} 
            catch (ServerOverloadException e) 
            {
            	throw new DocumentTemplateException ("Error storing template", e);
			}
        }

        if (null == getDocumentTemplateService().getRenderEngineForMimeType(mimeType))
        {
        	deleteTemplateFile(resourceId);
            throw new UnsupportedTemplateTypeException(mimeType);
        }

        docTemp.setOutputMimeType(mimeType);

        return docTemp;
    }

    public DocumentTemplate setDocumentTemplate(String certificateDefinitionId, String name, InputStream template)
        throws IdUnusedException, DocumentTemplateException
    {
        return setDocumentTemplate(certificateDefinitionId, name, null, template);
    }

    public DocumentTemplate setDocumentTemplate(final String certificateDefinitionId, final String name,
                                                final String mimeType, final InputStream template)
        throws IdUnusedException, UnsupportedTemplateTypeException, DocumentTemplateException
    {
        try
        {
            return (DocumentTemplate) getHibernateTemplate().execute(
                new HibernateCallback()
                {
                    public Object doInHibernate(Session session)
                        throws HibernateException, SQLException
                    {
                        boolean
                            updating = false;
                        CertificateDefinitionHibernateImpl
                            cd = (CertificateDefinitionHibernateImpl)session.load(CertificateDefinitionHibernateImpl.class,
                                                                                  certificateDefinitionId);
                        DocumentTemplateHibernateImpl
                            dthi = (DocumentTemplateHibernateImpl) cd.getDocumentTemplate();

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
            Throwable
                t = re.getCause();

            if (t instanceof DocumentTemplateException)
            {
                throw (DocumentTemplateException)t;
            }

            throw re;
        }
    }

    public InputStream getTemplateFileInputStream(final String resourceId)
    	throws TemplateReadException
	{
	    FileInputStream
	        fis = null;
	
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
    
    private Object doSecureCertificateService(SecureCertificateServiceCallback callback)
    	throws Exception
    {

        final SessionManager
            sessionManager = getSessionManager();
        //final SecurityService
        //    securityService = getSecurityService();

        final org.sakaiproject.tool.api.Session
            sakaiSession = sessionManager.getCurrentSession();
        final String
            contextId = contextId(),
            currentUserId = userId(),
            currentUserEid = getUserDirectoryService().getCurrentUser().getEid(),
            adminUser = getAdminUser();

        try
        {
            sakaiSession.setUserId(adminUser);
            sakaiSession.setUserEid(adminUser);

            securityService.pushAdvisor
                (
                    new SecurityAdvisor ()
                    {
                        public SecurityAdvice isAllowed(String userId, String function, String reference)
                        {
                            String
                                compTo = null;

                            if (contextId.startsWith("/site/"))
                            {
                                compTo = contextId;
                            }
                            else
                            {
                                compTo = "/site/" + contextId;
                            }

                            if (reference.equals(compTo) && ("content.read".equals(function)))
                            {
                                return SecurityAdvice.ALLOWED;
                            }
                            else
                            {
                                return SecurityAdvice.PASS;
                            }
                        }
                    }
                );

            return callback.doSecureAction();
        }
        finally
        {
           securityService.popAdvisor();

            sakaiSession.setUserId(currentUserId);
            sakaiSession.setUserEid(currentUserEid);
        }
    
    }
    public void setFieldValues(String certificateDefinitionId, Map<String, String> fieldValues)
        throws IdUnusedException
    {
        CertificateDefinitionHibernateImpl
            cd = (CertificateDefinitionHibernateImpl)getCertificateDefinition(certificateDefinitionId);

        cd.setFieldValues(fieldValues);

        getHibernateTemplate().update(cd);
    }

    public void activateCertificateDefinition(String certificateDefinitionId, boolean active)
        throws IncompleteCertificateDefinitionException, IdUnusedException
    {
        CertificateDefinitionHibernateImpl
            cd = (CertificateDefinitionHibernateImpl)getCertificateDefinition(certificateDefinitionId);

        if (cd.getDocumentTemplate() == null ||
            cd.getName() == null ||
            cd.getAwardCriteria() == null)
        {
            throw new IncompleteCertificateDefinitionException ("incomplete certificate definition");
        }


        if (cd.getShortUrl() == null || cd.getShortUrl().length() < 1)
        {
        		cd.setShortUrl(shortenedUrlService.shorten(portalUrl() + "/directtool/" + toolId()
        				+ "/checkstatus.form?certId=" + certificateDefinitionId
        				+"&sakai.site=" + contextId()));
        }

        cd.setStatus (active ? CertificateDefinitionStatus.ACTIVE : CertificateDefinitionStatus.INACTIVE);
        
        getHibernateTemplate().update(cd);
    }

    private void setCriteriaFactoryOnCriteria (CertificateDefinition certDef)
    {
        Set<Criterion>
            criteria = certDef.getAwardCriteria();

        if (criteria != null)
        {
            for (Criterion crit : criteria)
            {
                AbstractCriterionHibernateImpl
                    criterion = (AbstractCriterionHibernateImpl)crit;

                criterion.setCriteriaFactory(criteriaFactoryMap.get(criterion.getClass()));
            }
        }
    }

    public CertificateDefinition getCertificateDefinitionByName (String siteId, String name)
        throws IdUnusedException
    {
        List
            results = getHibernateTemplate().findByNamedQuery("getCertificateDefinitionByName",
                                                              new String[] { siteId, name });

        if (results == null || results.isEmpty())
        {
            throw new IdUnusedException ("site: " + siteId + " name: " + name);
        }

        return (CertificateDefinition) results.get(0);
    }

    public CertificateDefinition getCertificateDefinition(String id)
        throws IdUnusedException
    {
        try
        {
            CertificateDefinitionHibernateImpl
                certDef = (CertificateDefinitionHibernateImpl) getHibernateTemplate().load(CertificateDefinitionHibernateImpl.class, id);

            setCriteriaFactoryOnCriteria(certDef);

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
        HashSet<CertificateDefinition>
            cds = new HashSet<CertificateDefinition>();

        cds.addAll(getHibernateTemplate().loadAll(CertificateDefinitionHibernateImpl.class));

        for (CertificateDefinition certDef : cds)
        {
            CertificateDefinitionHibernateImpl
                cert = (CertificateDefinitionHibernateImpl) certDef;

            setCriteriaFactoryOnCriteria(cert);
        }
        return cds;
    }

    public Set<CertificateDefinition> getCertificateDefinitionsForSite(final String siteId)
    {
        HashSet<CertificateDefinition>
            cds = new HashSet<CertificateDefinition>();
        List<CertificateDefinition>
            result = null;

        result = (List<CertificateDefinition>) getHibernateTemplate().execute
            (
                new HibernateCallback()
                {
                    public Object doInHibernate(Session session)
                        throws HibernateException, SQLException
                    {
                        Query
                            q = session.getNamedQuery("getCertificateDefinitionsBySite");

                        q.setString(0, siteId);

                        return q.list();
                    }
                }
            );

        cds.addAll(result);

        for (CertificateDefinition certDef : cds)
        {
            CertificateDefinitionHibernateImpl
                cert = (CertificateDefinitionHibernateImpl) certDef;

            setCriteriaFactoryOnCriteria(cert);
        }

        return cds;
    }

    public Set<CertificateDefinition> getCertificateDefinitionsForSite(final String siteId,
                                                                       final CertificateDefinitionStatus[] statuses)
    {
        HashSet<CertificateDefinition>
            cds = new HashSet<CertificateDefinition>();
        List<CertificateDefinition>
            result = null;

        result = (List<CertificateDefinition>) getHibernateTemplate().execute
            (
                new HibernateCallback()
                {
                    public Object doInHibernate(Session session)
                        throws HibernateException, SQLException
                    {
                        Query
                            q = session.getNamedQuery("getCertificateDefinitionsBySiteAndStatus");

                        q.setString("siteId", siteId);
                        q.setParameterList("statuses", statuses);

                        return q.list();
                    }
                }
            );

        cds.addAll(result);

        for (CertificateDefinition certDef : cds)
        {
            CertificateDefinitionHibernateImpl
                cert = (CertificateDefinitionHibernateImpl) certDef;

            setCriteriaFactoryOnCriteria(cert);
        }

        return cds;
    }

    public void setAwardCriteria(final String certificateDefinitionId, final Set<Criterion> conditions)
        throws IdUnusedException
    {
        try
        {
            getHibernateTemplate().execute
            (
                new HibernateCallback()
                {
                    public Object doInHibernate(Session session)
                        throws HibernateException, SQLException
                    {
                        CertificateDefinitionHibernateImpl
                            cd = null;

                        try
                        {
                            cd = (CertificateDefinitionHibernateImpl)getCertificateDefinition(certificateDefinitionId);
                        }
                        catch (IdUnusedException e)
                        {
                            throw new RuntimeException (e);
                        }

                        Set<Criterion>
                            existingConditions = cd.getAwardCriteria();

                        for (Criterion condition : conditions)
                        {
                            if (existingConditions.contains(condition))
                            {
                                session.update (condition);
                            }
                            else
                            {
                                session.save (condition);
                            }
                        }

                        cd.setAwardCriteria(conditions);

                        session.update(cd);

                        return null;
                    }
                }
            );
        }
        catch (RuntimeException re)
        {
            Throwable
                t = re.getCause();

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

    public Criterion addAwardCriterion(final String certificateDefinitionId, final Criterion criterion)
        throws IdUnusedException
    {
        try
        {
            return (Criterion) getHibernateTemplate().execute(
                new HibernateCallback()
                {
                    public Object doInHibernate(Session session)
                        throws HibernateException, SQLException
                    {
                        CertificateDefinitionHibernateImpl
                            cd = (CertificateDefinitionHibernateImpl)session.load(CertificateDefinitionHibernateImpl.class, certificateDefinitionId);

                        Set<Criterion>
                            criteria = cd.getAwardCriteria();

                        session.save(criterion);

                        criteria.add(criterion);

                        session.update(cd);

                        return criterion;
                    }
                }
            );
        }
        catch (RuntimeException e)
        {
            Throwable
                t = e.getCause();

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
    
    public void removeAwardCriterion(String certificateDefinitionId, String criterionId)
    	throws IdUnusedException
    {
    	CertificateDefinitionHibernateImpl
	        cd = (CertificateDefinitionHibernateImpl)getCertificateDefinition(certificateDefinitionId);
	
	    Set<Criterion>
	        criterions = cd.getAwardCriteria();
	    
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
	    
	    HibernateTemplate
        ht = getHibernateTemplate();

	    ht.update(cd);

    }

    public Set<Criterion> getUnmetAwardConditions(String certificateDefinitionId)
            throws IdUnusedException, UnknownCriterionTypeException
    {
        return getUnmetAwardConditionsForUser(certificateDefinitionId, userId());
    }

    public Set<Criterion> getUnmetAwardConditionsForUser(String certificateDefinitionId, String userId)
            throws IdUnusedException, UnknownCriterionTypeException
    {
        String
            contextId = contextId();
        CertificateDefinitionHibernateImpl
            cd = (CertificateDefinitionHibernateImpl)getCertificateDefinition(certificateDefinitionId);

        Set<Criterion>
            criteria = cd.getAwardCriteria(),
            unmetCriteria = new HashSet<Criterion>();

        for (Criterion criterion : criteria)
        {
            CriteriaFactory
                cFact = criteriaFactoryMap.get(criterion.getClass());

            if (!cFact.isCriterionMet(criterion, userId, contextId))
            {
                unmetCriteria.add(criterion);
            }
        }

        return unmetCriteria;
    }

    public CertificateAward awardCertificate(String certificateDefinitionId)
            throws IdUnusedException, UnmetCriteriaException, UnknownCriterionTypeException
    {
        CertificateAward
            ca = null;

        try
        {
            ca = getCertificateAward (certificateDefinitionId);
        }
        catch (IdUnusedException iue)
        {
            //do nothing... award not yet granted
        }

        if (ca != null)
            return ca;

        return awardCertificate (certificateDefinitionId, userId());
    }

    public CertificateAward awardCertificate(String certificateDefinitionId, String userId)
            throws IdUnusedException, UnmetCriteriaException, UnknownCriterionTypeException
    {
        CertificateDefinitionHibernateImpl
            cd = (CertificateDefinitionHibernateImpl)getCertificateDefinition(certificateDefinitionId);
        Set<Criterion>
            criteria = getUnmetAwardConditionsForUser(certificateDefinitionId, userId);

        if (criteria != null && criteria.size() > 0)
        {
            UnmetCriteriaException
                uce = new UnmetCriteriaException();

            uce.setUnmetCriteria(criteria);

            throw uce;
        }
        
        CertificateAwardHibernateImpl
            certificateAward = new CertificateAwardHibernateImpl();

        certificateAward.setCertificateDefinition(cd);
        certificateAward.setCertificationTimeStamp(new Date());
        certificateAward.setUserId(userId);

        getHibernateTemplate().save(certificateAward);

        return certificateAward;
    }

    public CertificateAward getCertificateAward(final String certificateDefinitionId)
        throws IdUnusedException
    {
        return getCertificateAwardForUser (certificateDefinitionId, userId());
    }

    public Set<CertificateAward> getCertificateAwards()
    {
        HashSet<CertificateAward>
            awards = new HashSet<CertificateAward>();

        List<CertificateAward>
            awardList = getHibernateTemplate().loadAll(CertificateAward.class);

        awards.addAll(awardList);

        return awards;
    }

    public CertificateAward getCertificateAwardForUser(final String certificateDefinitionId, final String userId)
        throws IdUnusedException
    {
        CertificateAwardHibernateImpl
            ca = null;

        List
            results = (List)getHibernateTemplate().execute
                (
                    new HibernateCallback()
                    {
                        public Object doInHibernate(Session session)
                            throws HibernateException, SQLException
                        {
                            Query
                                q = session.getNamedQuery("getCertificateAwardForUser");

                            q.setString(0, certificateDefinitionId);
                            q.setString(1, userId);

                            return q.list();
                        }
                    }
                );

        if (results == null || results.size() == 0)
        {
            throw new IdUnusedException ("userId: " + userId + " certificate definition id: " + certificateDefinitionId);
        }

        return (CertificateAward)results.get(0);
    }

    public Set<CertificateAward> getCertificateAwards(final String certificateDefinitionId)
        throws IdUnusedException
    {
        Set<CertificateAward>
            retSet = new HashSet<CertificateAward>();
        List<CertificateAward>
            results = null;
        final String
            userId = userId(),
            contextId = contextId();

        results = (List<CertificateAward>)getHibernateTemplate().execute
                (
                    new HibernateCallback()
                    {
                        public Object doInHibernate(Session session)
                            throws HibernateException, SQLException
                        {
                            Query
                                q = session.getNamedQuery("getCertificateAwardsForUser");

                            q.setString(0, certificateDefinitionId);
                            q.setString(1, userId);
                            q.setString(2, contextId);

                            return q.list();
                        }
                    }
                );

        retSet.addAll(results);

        return retSet;
    }

    public Map<String, CertificateAward> getCertificateAwardsForUser(final String[] certificateDefinitionIds)
        throws IdUnusedException
    {
        HashMap<String, CertificateAward>
            retSet = new HashMap<String, CertificateAward>();

        if (certificateDefinitionIds == null || certificateDefinitionIds.length == 0)
            return retSet;
        
        List<CertificateAward>
            results = null;
        final String
            userId = userId();

        results = (List<CertificateAward>)getHibernateTemplate().execute
                (
                    new HibernateCallback()
                    {
                        public Object doInHibernate(Session session)
                            throws HibernateException, SQLException
                        {
                            Query
                                q = session.getNamedQuery("getCertificateAwardsInListForUser");

                            q.setParameterList("certDefs", certificateDefinitionIds);
                            q.setString("userId", userId);

                            return q.list();
                        }
                    }
                );

        for (CertificateAward award : results)
        {
            retSet.put(((CertificateAwardHibernateImpl)award).getCertificateDefinition().getId(), award);
        }

        return retSet;
    }

    public Set<CertificateAward> getCertificateAwardsForUser(final String userId)
    {
        HashSet<CertificateAward>
            retSet = new HashSet<CertificateAward>();
        List<CertificateAward>
            results = null;
        final String
            contextId = contextId();

        results = (List<CertificateAward>)getHibernateTemplate().execute
                (
                    new HibernateCallback()
                    {
                        public Object doInHibernate(Session session)
                            throws HibernateException, SQLException
                        {
                            Query
                                q = session.getNamedQuery("getCertificateAwardsForUserInContext");

                            q.setString(0, userId);
                            q.setString(1, contextId);

                            return q.list();
                        }
                    }
                );

        retSet.addAll(results);

        return retSet;
    }

    public Map<String, String> getPredefinedTemplateVariables()
    {
        HashMap<String, String>
            predefined = new HashMap<String, String>();

        for (String key : variableResolvers.keySet())
        {
            VariableResolver
                vr = variableResolvers.get(key);

            for (String label : vr.getVariableLabels())
            {
                predefined.put(label, vr.getVariableDescription(label));
            }
        }
        return predefined;
    }

    public InputStream getPrintableCertificateRendering(String certificateAwardId)
        throws IdUnusedException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void registerCriteriaFactory(CriteriaFactory cFact)
    {
        Set<Class<? extends Criterion>>
            critClasses = cFact.getCriterionTypes();

        for (Class critClass : critClasses)
        {
            criteriaFactoryMap.put(critClass, cFact);
        }

        Set<CriteriaTemplate>
            templates = cFact.getCriteriaTemplates();

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
        HashSet<CriteriaTemplate>
            criteriaTemplates = new HashSet<CriteriaTemplate>();

        for (CriteriaFactory factory : criteriaFactories)
        {
            criteriaTemplates.addAll(factory.getCriteriaTemplates());
        }

        return criteriaTemplates;
    }

    public CertificateDefinition duplicateCertificateDefinition(String certificateDefinitionId)
            throws IdUnusedException, IdUsedException, DocumentTemplateException, UnknownCriterionTypeException, CriterionCreationException {
        CertificateDefinitionHibernateImpl
            oldCD = (CertificateDefinitionHibernateImpl)getCertificateDefinition(certificateDefinitionId),
            newCD = new CertificateDefinitionHibernateImpl();

        newCD.setName("Copy of " + oldCD.getName());
        newCD.setSiteId(oldCD.getSiteId());
        newCD.setCreateDate(new Date());
        newCD.setCreatorUserId(userId());
        newCD.setDescription(oldCD.getDescription());
        newCD.setStatus(CertificateDefinitionStatus.UNPUBLISHED);

        Map<String, String>
            oldFieldValues = oldCD.getFieldValues(),
            newFieldValues = null;

        if (oldFieldValues != null)
        {
            newFieldValues = new HashMap<String, String>();
            newFieldValues.putAll(oldCD.getFieldValues());
        }

        newCD.setFieldValues(newFieldValues);

        newCD = (CertificateDefinitionHibernateImpl) createCertificateDefinition (newCD);

        DocumentTemplateHibernateImpl
            oldDT = (DocumentTemplateHibernateImpl) oldCD.getDocumentTemplate(),
            newDT = (DocumentTemplateHibernateImpl) setDocumentTemplate (newCD.getId(), oldDT.getOutputMimeType(), getTemplateFileInputStream(oldDT.getResourceId()));

        Set<Criterion>
            oldCriteria = oldCD.getAwardCriteria(),
            newCriteria = new HashSet<Criterion>();

        for (Criterion criterion : oldCriteria)
        {
            CriteriaFactory
                factory = criteriaFactoryMap.get(criterion.getClass());
            CriteriaTemplate
                template = factory.getCriteriaTemplate(criterion);
            Criterion
                newCriterion = factory.createCriterion(template, criterion.getVariableBindings());

            newCriteria.add(newCriterion);
        }

        setAwardCriteria (newCD.getId(), newCriteria);

        return getCertificateDefinition(newCD.getId());
    }

    @SuppressWarnings("unchecked")
	public int getCategoryType(final String gradebookId)
    {
    	return (Integer) getHibernateTemplate().execute(
    			 new HibernateCallback()
                 {
                     public Integer doInHibernate(Session session)
                         throws HibernateException, SQLException
                     {
                    	 List<Integer> list = session.createQuery(
                    	  	"select gb.category_type from CertGradebook as gb where gb.uid=:gbid").
                    	  	setParameter("gbid", gradebookId).list();
                    	 return (Integer)list.get(0);
                     }
                 });
    }
    
    @SuppressWarnings("unchecked")
	public Map<Long,Double> getCategoryWeights(final String gradebookId)
    {
    	return (Map<Long, Double>) getHibernateTemplate().execute(
   			 new HibernateCallback()
                {
                    public Object doInHibernate(Session session)
                        throws HibernateException, SQLException
                    {
                   	 List<Object[]> results = session.createQuery(
                   	  	"select assn.id, cat.weight from CertCategory as cat, CertAssignment as assn " +
                   	  	"where cat.gradebook.uid=:gbid and cat.removed=false " +
                   	  	"and cat.id = assn.category.id and assn.notCounted=false and assn.removed=false").
                   	  	setParameter("gbid", gradebookId).list();
                   	 
                   	 Map<Long,Double> catWeightMap = new HashMap<Long,Double>();
                   	 for(Object[] row : results)
                   	 {
                   		catWeightMap.put((Long)row[0], (Double)row[1]);
                   	 }
                   	 return catWeightMap;
                    }
                });
    }

    @SuppressWarnings("unchecked")
	public Map<Long, Double> getAssignmentWeights(final String gradebookId)
    {

        HibernateCallback
            callback = new HibernateCallback()
        {
			public Object doInHibernate(Session session)
                throws HibernateException
            {
				List<Object[]> results =  session.createQuery
                     ("select assn.id, assn.assignmentWeighting from CertAssignment as assn " +
                        "where assn.notCounted=false and assn.removed=false and " +
					    "assn.gradebook.uid=:gradebookId").setParameter("gradebookId", gradebookId)
					    .list();

				Map<Long,Double> assnWeights = new HashMap<Long,Double>();
				for(Object[] row : results)
				{
					assnWeights.put((Long)row[0], (Double)row[1]);
				}
				return assnWeights;
            }
		};

		return (Map<Long,Double>)getHibernateTemplate().execute(callback);

    }

    @SuppressWarnings("unchecked")
    public Map<Long, Double> getCatOnlyAssignmentPoints(final String gradebookId)
    {
        HibernateCallback
            callback = new HibernateCallback()
        {
			public Object doInHibernate(Session session)
                throws HibernateException
            {
                List<Object[]> results = session.createQuery
                     ("select assn.id, assn.pointsPossible from CertCategory as cat, CertAssignment as assn " +
                      "where cat.gradebook.uid=:gradebookId and cat.removed=false " +
                      "and cat.id = assn.category.id and assn.notCounted=false " +
                      "and assn.removed=false").setParameter("gradebookId", gradebookId)
					    .list();
                HashMap<Long, Double>
                	assnPoints = new HashMap<Long, Double>();
                for(Object[] row : results)
                {
                	assnPoints.put((Long)row[0], (Double)row[1]);
                }
                return assnPoints;
            }
		};

         return (HashMap<Long,Double>)getHibernateTemplate().execute(callback);
    }

    @SuppressWarnings("unchecked")
    public Map<Long, Double> getAssignmentPoints(final String gradebookId)
    {
        HibernateCallback
            callback = new HibernateCallback()
        {
			public Object doInHibernate(Session session)
                throws HibernateException
            {
                List<Object[]> results = session.createQuery
                     ("select assn.id, assn.pointsPossible from CertAssignment as assn " +
                        "where assn.removed=false and assn.notCounted=false and " +
					    "assn.gradebook.uid=:gradebookId").setParameter("gradebookId", gradebookId)
					    .list();
                HashMap<Long, Double>
                	assnPoints = new HashMap<Long, Double>();
                for(Object[] row : results)
                {
                	assnPoints.put((Long)row[0], (Double)row[1]);
                }
                return assnPoints;
            }
		};

         return (HashMap<Long,Double>)getHibernateTemplate().execute(callback);
    }
    
    public Map<Long, Double> getAssignmentScores(final String gradebookId, final String studentId)
    {
        

        HibernateCallback
            callback = new HibernateCallback()
        {
			public Object doInHibernate(Session session)
                throws HibernateException
            {
                Iterator results = session.createQuery
                     ("select agr.gradableObject.id, agr.pointsEarned from CertAssignmentScore as agr " +
                        "where agr.gradableObject.removed=false " +
					    "and agr.gradableObject.gradebook.uid=:gradebookId and agr.studentId = :studentId").
					    setParameter("gradebookId", gradebookId).setParameter("studentId", studentId).list().iterator();

                HashMap<Long, Double>
                assnScores = new HashMap<Long, Double>();
                while(results.hasNext())
                {
                	Object[] row = (Object[])results.next();
                	assnScores.put((Long)row[0],(Double)row[1]);
                }
                return assnScores;
            }
		};

        return (HashMap<Long,Double>)getHibernateTemplate().execute(callback);

    }


}
