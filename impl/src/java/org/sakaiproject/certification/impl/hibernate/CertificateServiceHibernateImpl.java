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

package org.sakaiproject.certification.impl.hibernate;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateObjectRetrievalFailureException;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.certification.api.CertificateDefinition;
import org.sakaiproject.certification.api.CertificateDefinitionStatus;
import org.sakaiproject.certification.api.CertificateService;
import org.sakaiproject.certification.api.DocumentTemplate;
import org.sakaiproject.certification.api.DocumentTemplateException;
import org.sakaiproject.certification.api.DocumentTemplateService;
import org.sakaiproject.certification.api.IncompleteCertificateDefinitionException;
import org.sakaiproject.certification.api.ReportRow;
import org.sakaiproject.certification.api.TemplateReadException;
import org.sakaiproject.certification.api.UnsupportedTemplateTypeException;
import org.sakaiproject.certification.api.VariableResolver;
import org.sakaiproject.certification.api.criteria.AbstractCriterion;
import org.sakaiproject.certification.api.criteria.CriteriaFactory;
import org.sakaiproject.certification.api.criteria.CriteriaTemplate;
import org.sakaiproject.certification.api.criteria.Criterion;
import org.sakaiproject.certification.api.criteria.CriterionProgress;
import org.sakaiproject.certification.api.criteria.UnknownCriterionTypeException;
import org.sakaiproject.certification.api.criteria.UserProgress;
import org.sakaiproject.certification.api.criteria.gradebook.WillExpireCriterion;
import org.sakaiproject.certification.impl.security.AllowMapSecurityAdvisor;
import org.sakaiproject.certification.impl.util.FormatHelper;
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
import org.sakaiproject.user.api.CandidateDetailProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class CertificateServiceHibernateImpl extends HibernateDaoSupport implements CertificateService {

    //managers and services
    private DocumentTemplateService documentTemplateService = null;
    private UserDirectoryService userDirectoryService = null;
    private ToolManager toolManager = null;
    private SessionManager sessionManager = null;
    private SecurityService securityService = null;
    private SiteService siteService = null;
    private AuthzGroupService authzGroupService= null;
    private ContentHostingService contentHostingService = null;
    private CandidateDetailProvider candidateDetailProvider = null;

    private String templateDirectory = null;
    private final HashMap<String, CriteriaFactory> criteriaTemplateMap = new HashMap<>();
    private final HashMap<Class, CriteriaFactory> criteriaFactoryMap = new HashMap<>();
    private final HashSet<CriteriaFactory> criteriaFactories = new HashSet<>();
    private final HashMap<String, VariableResolver> variableResolvers = new HashMap<>();

    private final ResourceLoader messages = new ResourceLoader("org.sakaiproject.certification.Messages");

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
    private static final String PARAM_ID = "id";
    private static final String PARAM_NAME = "name";

    private static final String PERMISSION_VIEW_STUDENT_NUMS = "certificate.extraprops.view";

    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public String getTemplateDirectory() {
        return templateDirectory;
    }

    public void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory = templateDirectory;
    }

    public ContentHostingService getContentHostingService() {
        return contentHostingService;
    }

    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    public String getString(String key) {
        return messages.getString(key);
    }

    public String getFormattedMessage(String key, Object[] values) {
        return messages.getFormattedMessage(key, values);
    }

    public Locale getLocale(){
        return messages.getLocale();
    }

    public void setDocumentTemplateService(DocumentTemplateService dts) {
        documentTemplateService = dts;
    }

    public DocumentTemplateService getDocumentTemplateService() {
        return documentTemplateService;
    }

    public ToolManager getToolManager() {
        return toolManager;
    }

    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    public UserDirectoryService getUserDirectoryService() {
        return userDirectoryService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
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

    public void setCandidateDetailProvider(CandidateDetailProvider candidateDetailProvider) {
        this.candidateDetailProvider = candidateDetailProvider;
    }

    public void init() {
        log.info("init");

        if (templateDirectory == null) {
            throw new IllegalStateException ("templateDirectory for CertificateService is not configured");
        }

        File dirFile = new File (templateDirectory);
        if ((!dirFile.exists() && !dirFile.mkdir()) || !dirFile.canWrite()) {
            throw new IllegalStateException ("templateDirectory \"" + templateDirectory + "\" is unreadable");
        }
        if (documentTemplateService == null) {
            throw new IllegalStateException ("DocumentTemplateService not provided to CertificateService");
        }
        if (userDirectoryService == null) {
            throw new IllegalStateException ("UserDirectoryService not provided to CertificateService");
        }
        if (toolManager == null) {
            throw new IllegalStateException ("ToolManager not provided to CertificateService");
        }

        for (VariableResolver resolver : documentTemplateService.getVariableResolvers()) {
            for (String label : resolver.getVariableLabels()) {
                variableResolvers.put(label, resolver);
            }
        }
    }

    private String userId() {
        return getUserDirectoryService().getCurrentUser().getId();
    }

    private String contextId() {
        return getToolManager().getCurrentPlacement().getContext();
    }

    public void deleteCertificateDefinition(final String certificateDefinitionId) throws IdUnusedException, DocumentTemplateException {
        CertificateDefinition cd = (CertificateDefinition) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                CertificateDefinition cd = (CertificateDefinition) session.load(CertificateDefinition.class, certificateDefinitionId);

                session.delete(cd);
                session.flush();

                return cd;
            }
        });

        deleteTemplateFile(cd.getDocumentTemplate().getResourceId());
    }

    public CertificateDefinition updateCertificateDefinition(final CertificateDefinition cd) throws IdUnusedException {
        CertificateDefinition retVal = null;
        if (cd instanceof CertificateDefinition) {
            retVal = (CertificateDefinition) cd;
        }

        try {
            retVal = (CertificateDefinition) getHibernateTemplate().execute(new HibernateCallback() {
                public Object doInHibernate(Session session) {
                    Query q = session.createQuery ("from "+ CertificateDefinition.class.getName() + " where id = :id ");
                    q.setParameter(PARAM_ID, cd.getId());
                    CertificateDefinition cdhi = (CertificateDefinition) q.list().get(0);
                    cdhi.setName(cd.getName());
                    cdhi.setDescription(cd.getDescription());
                    cdhi.setProgressHidden(cd.getProgressHidden());
                    session.update(cdhi);
                    return cdhi;
                }
            });
        } catch (ObjectNotFoundException | HibernateObjectRetrievalFailureException onfe) {
            throw new IdUnusedException(cd.getId());
        }

        return retVal;
    }

    public CertificateDefinition createCertificateDefinition (final String name, final String description,
                                                              final String siteId, final Boolean progressHidden, final String fileName,
                                                              final String mimeType, final InputStream template)
        throws IdUsedException, UnsupportedTemplateTypeException, DocumentTemplateException {
        CertificateDefinition cd = null;
        try {
            cd = (CertificateDefinition) getHibernateTemplate().execute(new HibernateCallback() {
                public Object doInHibernate(Session session) throws HibernateException {
                    CertificateDefinition certificateDefinition = new CertificateDefinition();

                    certificateDefinition.setCreateDate(new Date());
                    certificateDefinition.setCreatorUserId(userId());
                    certificateDefinition.setDescription(description);
                    certificateDefinition.setName(name);
                    certificateDefinition.setSiteId(siteId);
                    certificateDefinition.setProgressHidden(progressHidden);
                    certificateDefinition.setStatus(CertificateDefinitionStatus.UNPUBLISHED);
                    session.save(certificateDefinition);

                    DocumentTemplate documentTemplate = new DocumentTemplate();
                    documentTemplate.setCertificateDefinition(certificateDefinition);

                    try {
                        documentTemplate = processFile(documentTemplate, fileName, mimeType, template);
                    } catch (DocumentTemplateException e) {
                        throw new RuntimeException(e);
                    }

                    session.save(documentTemplate);
                    session.flush();
                    return certificateDefinition;
                }
            });

        } catch (RuntimeException re) {
            Throwable t = re.getCause();
            if (t != null) {
                if (t instanceof IdUsedException) {
                    throw (IdUsedException) t;
                }
                if (t instanceof UnsupportedTemplateTypeException) {
                    throw (UnsupportedTemplateTypeException) t;
                }
                if (t instanceof DocumentTemplateException) {
                    throw (DocumentTemplateException) t;
                }

            } else {
                t = re;
            }

            throw new DocumentTemplateException ("Unhandled exception creating new certificate definition", t);
        }

        return cd;
    }

    private void deleteTemplateFile(String resourceId) {
        try {
            String certDefCId = contentHostingService.getContainingCollectionId(resourceId);
            contentHostingService.removeResource(resourceId);
            contentHostingService.removeCollection(certDefCId);
        } catch (PermissionException | IdUnusedException | TypeException | InUseException | ServerOverloadException e) {
            //TODO: Should these be thrown? Should this method 'throws DocumentTemplateException'?
            new DocumentTemplateException(e);
        }
    }

    private ContentResourceEdit storeTemplateFile (String siteId, String certificateId, InputStream templateStream, String fileName, String mimeType, String resourceId)
        throws DocumentTemplateException {
        ContentResourceEdit resourceEdit = null;
        boolean resourceExist = false;

        try {
            try {
                if(authzGroupService.getAuthzGroup(siteService.siteReference(siteId)).isAllowed(sessionManager.getCurrentSessionUserId(), "certificate.admin")) {
                    getSecurityService().pushAdvisor(new AllowMapSecurityAdvisor(ContentHostingService.EVENT_RESOURCE_READ,
                        contentHostingService.getReference(resourceId)));
                    getSecurityService().pushAdvisor(new AllowMapSecurityAdvisor(ContentHostingService.EVENT_RESOURCE_ADD,
                        contentHostingService.getReference(resourceId)));
                }
            } catch(Exception e) { }

            contentHostingService.checkResource(resourceId);
            resourceExist = true;

        } catch(IdUnusedException iue) {
            resourceExist = false;

        } catch (PermissionException e) {
            throw new DocumentTemplateException ("(PermissionException) Error storing template", e);

        } catch (TypeException e) {
            throw new DocumentTemplateException ("(TypeException) Error storing template", e);
        }

        try {
            if(resourceExist) {
                resourceEdit = contentHostingService.editResource(resourceId);
                ResourcePropertiesEdit props = resourceEdit.getPropertiesEdit();
                props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fileName);
                props.addProperty(ResourceProperties.PROP_PUBVIEW, PUBVIEW_FALSE);
                resourceEdit.setContent(templateStream);
                resourceEdit.setContentType(mimeType);
                contentHostingService.commitResource(resourceEdit);

            } else {
                resourceEdit = contentHostingService.addResource(resourceId);
                ResourcePropertiesEdit props = resourceEdit.getPropertiesEdit();
                props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, fileName);
                props.addProperty(ResourceProperties.PROP_PUBVIEW, PUBVIEW_FALSE);
                resourceEdit.setContent(templateStream);
                resourceEdit.setContentType(mimeType);
                contentHostingService.commitResource(resourceEdit);
            }

        } catch (PermissionException e) {
            throw new DocumentTemplateException ("(PermissionException) Error storing template", e);

        } catch (IdUsedException e) {
            throw new DocumentTemplateException ("(IdUsedException) Error storing template", e);

        } catch (IdInvalidException e) {
            throw new DocumentTemplateException ("(IdInvalidException) Error storing template", e);

        } catch (InconsistentException e) {
            throw new DocumentTemplateException ("(InconsistentException) Error storing template", e);

        } catch (OverQuotaException e) {
            throw new DocumentTemplateException ("(OverQuotaException) Error storing template", e);

        } catch (ServerOverloadException e) {
            throw new DocumentTemplateException ("(ServerOverloadException) Error storing template", e);

        } catch (VirusFoundException e) {
            throw new DocumentTemplateException ("(VirusFoundException) Error storing template", e);

        } catch (IdUnusedException e) {
            throw new DocumentTemplateException ("(IdUnusedException) Error storing template", e);

        } catch (TypeException e) {
            throw new DocumentTemplateException ("(TypeException) Error storing template", e);

        } catch (InUseException e) {
            throw new DocumentTemplateException ("(InUseException) Error storing template", e);

        } finally {
            getSecurityService().popAdvisor();
            getSecurityService().popAdvisor();
        }

        return resourceEdit;
    }

    public String getMimeType (byte[] toCheck) throws DocumentTemplateException {
        try {
            MagicMatch mimeTypeMatch = Magic.getMagicMatch(toCheck, true);
            return mimeTypeMatch.getMimeType();
        } catch (MagicParseException | MagicMatchNotFoundException | MagicException e) {
            throw new DocumentTemplateException (e);
        }
    }

    private DocumentTemplate processFile (DocumentTemplate docTemp, final String fileName,
                                          final String mimeType, final InputStream template)
        throws DocumentTemplateException, UnsupportedTemplateTypeException {
        final CertificateDefinition cd = docTemp.getCertificateDefinition();

        if (cd == null) {
            throw new DocumentTemplateException("No CertificateDefinition set");
        }

        docTemp.setName(fileName);
        final String resourceId = DocumentTemplate.COLLECTION_ID + cd.getSiteId() + "/" + cd.getId() + "/" + fileName;
        ContentResourceEdit templateFile = null;
        try {
            templateFile = (ContentResourceEdit) doSecureCertificateService(() -> storeTemplateFile(cd.getSiteId(), cd.getId(), template, fileName, mimeType, resourceId));
        } catch(Exception e) {
            throw new TemplateReadException ("Could not write Document Template with id: " + resourceId, e);
        }

        docTemp.setResourceId(resourceId);
        String newMimeType = mimeType;

        if (newMimeType == null) {
            try {
                newMimeType = getMimeType(templateFile.getContent());
            } catch (ServerOverloadException e) {
                throw new DocumentTemplateException ("Error storing template", e);
            }
        }

        if (null == getDocumentTemplateService().getRenderEngineForMimeType(newMimeType)) {
            deleteTemplateFile(resourceId);
            throw new UnsupportedTemplateTypeException(newMimeType);
        }

        docTemp.setOutputMimeType(newMimeType);

        return docTemp;
    }

    public DocumentTemplate setDocumentTemplate(String certificateDefinitionId, String name, InputStream template) throws IdUnusedException, DocumentTemplateException {
        return setDocumentTemplate(certificateDefinitionId, name, null, template);
    }

    public DocumentTemplate setDocumentTemplate(final String certificateDefinitionId, final String name,
                                                final String mimeType, final InputStream template)
        throws IdUnusedException, UnsupportedTemplateTypeException, DocumentTemplateException {
        try {
            return (DocumentTemplate) getHibernateTemplate().execute(new HibernateCallback() {
                public Object doInHibernate(Session session) throws HibernateException {
                    boolean updating;
                    CertificateDefinition cd = (CertificateDefinition)session.load(CertificateDefinition.class,
                                                                              certificateDefinitionId);
                    DocumentTemplate dthi = (DocumentTemplate) cd.getDocumentTemplate();

                    updating = (dthi != null);
                    if (!updating) {
                        dthi = new DocumentTemplate();
                        cd.setDocumentTemplate(dthi);
                        dthi.setCertificateDefinition(cd);
                    }

                    try {
                        dthi = processFile (dthi, name, mimeType, template);
                    } catch (DocumentTemplateException e) {
                        throw new RuntimeException (e);
                    }

                    if (!updating) {
                        session.save(dthi);
                    } else {
                        session.update(dthi);
                    }

                    return dthi;
                }
            });

        } catch (ObjectNotFoundException | HibernateObjectRetrievalFailureException onfe) {
            throw new IdUnusedException (certificateDefinitionId);

        } catch (RuntimeException re) {
            Throwable t = re.getCause();
            if (t instanceof DocumentTemplateException) {
                throw (DocumentTemplateException)t;
            }

            throw re;
        }
    }

    public InputStream getTemplateFileInputStream(final String resourceId) throws TemplateReadException {
        InputStream is = null;

        try {
            is = (InputStream) doSecureCertificateService(new SecureCertificateServiceCallback() {
                public Object doSecureAction() throws Exception {
                    ContentResource resource = contentHostingService.getResource(resourceId);
                    return resource.streamContent();
                }
            });
        } catch(Exception e) {
            throw new TemplateReadException("Could not read Document Template with id: " + resourceId, e);
        }

        return is;
    }

    private Object doSecureCertificateService(SecureCertificateServiceCallback callback) throws Exception {
        try {
            securityService.pushAdvisor((String userId, String function, String reference) -> SecurityAdvice.ALLOWED);
            return callback.doSecureAction();
        }
        finally {
           securityService.popAdvisor();
        }
    }

    public void setFieldValues(String certificateDefinitionId, Map<String, String> fieldValues) throws IdUnusedException {
        CertificateDefinition cd = (CertificateDefinition)getCertificateDefinition(certificateDefinitionId);
        cd.setFieldValues(fieldValues);
        getHibernateTemplate().update(cd);
    }

    public void activateCertificateDefinition(String certificateDefinitionId, boolean active) throws IncompleteCertificateDefinitionException, IdUnusedException {
        CertificateDefinition cd = (CertificateDefinition)getCertificateDefinition(certificateDefinitionId);
        if (cd.getDocumentTemplate() == null || cd.getName() == null || cd.getAwardCriteria() == null || cd.getFieldValues() == null)
        {
            throw new IncompleteCertificateDefinitionException ("incomplete certificate definition");
        }

        cd.setStatus (active ? CertificateDefinitionStatus.ACTIVE : CertificateDefinitionStatus.INACTIVE);
        getHibernateTemplate().update(cd);
    }

    private void setCriteriaFactoryOnCriteria(CertificateDefinition certDef) {
        Set<Criterion> criteria = certDef.getAwardCriteria();
        if (criteria != null) {
            for (Criterion crit : criteria) {
                AbstractCriterion criterion = (AbstractCriterion) crit;
                criterion.setCriteriaFactory(criteriaFactoryMap.get(criterion.getClass()));
            }
        }
    }

    private void setCertificateServiceOnCriteria(CertificateDefinition certDef) {
        Set<Criterion> criteria = certDef.getAwardCriteria();
        if (criteria != null) {
            for (Criterion crit : criteria) {
                AbstractCriterion criterion = (AbstractCriterion) crit;
                criterion.setCertificateService(this);
            }
        }
    }

    public CertificateDefinition getCertificateDefinitionByName (String siteId, String name) throws IdUnusedException {
        List results = getHibernateTemplate().findByNamedQueryAndNamedParam(QUERY_CERTIFICATE_DEFINITION_BY_NAME, new String[] {PARAM_SITE_ID, PARAM_NAME}, new String[] {siteId, name});
        if (results == null || results.isEmpty()) {
            throw new IdUnusedException ("site: " + siteId + " name: " + name);
        }

        return (CertificateDefinition) results.get(0);
    }

    public CertificateDefinition getCertificateDefinition(String id) throws IdUnusedException {
        try {
            CertificateDefinition certDef = (CertificateDefinition) getHibernateTemplate().load(CertificateDefinition.class, id);
            setCriteriaFactoryOnCriteria(certDef);
            setCertificateServiceOnCriteria(certDef);
            return certDef;
        } catch (ObjectNotFoundException | HibernateObjectRetrievalFailureException onfe) {
            throw new IdUnusedException (id);
        }
    }

    public Set<CertificateDefinition> getCertificateDefinitions() {
        HashSet<CertificateDefinition> cds = new HashSet<>();
        cds.addAll(getHibernateTemplate().loadAll(CertificateDefinition.class));

        for (CertificateDefinition certDef : cds) {
            CertificateDefinition cert = (CertificateDefinition) certDef;
            setCriteriaFactoryOnCriteria(cert);
            setCertificateServiceOnCriteria(cert);
        }

        return cds;
    }

    public Set<CertificateDefinition> getCertificateDefinitionsForSite(final String siteId) {
        HashSet<CertificateDefinition> cds = new HashSet<>();
        List<CertificateDefinition> result;

        result = (List<CertificateDefinition>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.getNamedQuery(QUERY_CERTIFICATE_DEFINITIONS_BY_SITE).setString(PARAM_SITE_ID, siteId);
                return q.list();
            }
        });

        cds.addAll(result);
        for (CertificateDefinition certDef : cds) {
            CertificateDefinition cert = (CertificateDefinition) certDef;
            setCriteriaFactoryOnCriteria(cert);
            setCertificateServiceOnCriteria(cert);
        }

        return cds;
    }

    public Set<CertificateDefinition> getCertificateDefinitionsForSite(final String siteId, final CertificateDefinitionStatus[] statuses) {
        HashSet<CertificateDefinition> cds = new HashSet<>();
        List<CertificateDefinition> result;

        result = (List<CertificateDefinition>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.getNamedQuery(QUERY_CERTIFICATE_DEFINITIONS_BY_SITE_AND_STATUS);
                q.setString(PARAM_SITE_ID, siteId);
                q.setParameterList(PARAM_STATUSES, statuses);
                return q.list();
            }
        });

        cds.addAll(result);
        for (CertificateDefinition certDef : cds) {
            CertificateDefinition cert = (CertificateDefinition) certDef;
            setCriteriaFactoryOnCriteria(cert);
            setCertificateServiceOnCriteria(cert);
        }

        return cds;
    }

    public void setAwardCriteria(final String certificateDefinitionId, final Set<Criterion> conditions) throws IdUnusedException {
        try {
            getHibernateTemplate().execute(new HibernateCallback() {
                public Object doInHibernate(Session session) throws HibernateException {
                    CertificateDefinition cd = null;

                    try {
                        cd = (CertificateDefinition)getCertificateDefinition(certificateDefinitionId);
                    } catch (IdUnusedException e) {
                        throw new RuntimeException (e);
                    }

                    Set<Criterion> existingConditions = cd.getAwardCriteria();
                    for (Criterion condition : conditions) {
                        if (!existingConditions.contains(condition)) {
                            session.save(condition);
                        }
                    }

                    for (Criterion condition : existingConditions) {
                        if (!conditions.contains(condition)) {
                            session.delete(condition);
                        }
                    }

                    cd.setAwardCriteria(conditions);
                    session.merge(cd);
                    return null;
                }
            });

        } catch (RuntimeException re) {
            Throwable t = re.getCause();
            if (t == null) {
                throw re;
            }

            if (t instanceof IdUnusedException) {
                throw (IdUnusedException) t;
            }

            throw re;
        }
    }

    public Criterion addAwardCriterion(final String certificateDefinitionId, final Criterion criterion) throws IdUnusedException {
        try {
            return (Criterion) getHibernateTemplate().execute(new HibernateCallback() {
                public Object doInHibernate(Session session) throws HibernateException {
                    CertificateDefinition cd = (CertificateDefinition)session.load(CertificateDefinition.class, certificateDefinitionId);
                    Set<Criterion> criteria = cd.getAwardCriteria();
                    session.save(criterion);
                    criteria.add(criterion);
                    session.update(cd);
                    return criterion;
                }
            });

        } catch (RuntimeException e) {
            Throwable t = e.getCause();
            if (t != null) {
                if (t instanceof IdUnusedException) {
                    throw (IdUnusedException)t;
                }
            }

            throw e;
        }
    }

    public void removeAwardCriterion(String certificateDefinitionId, String criterionId) throws IdUnusedException {
        CertificateDefinition cd = (CertificateDefinition)getCertificateDefinition(certificateDefinitionId);
        Set<Criterion> criterions = cd.getAwardCriteria();

        Criterion removeThis = null;
        for(Criterion criterion : criterions) {
            if(criterionId.equals(criterion.getId())) {
                removeThis = criterion;
                break;
            }
        }

        cd.getAwardCriteria().remove(removeThis);
        HibernateTemplate ht = getHibernateTemplate();
        ht.update(cd);
    }

    public Set<Criterion> getUnmetAwardConditions(String certificateDefinitionId, boolean useCaching) throws IdUnusedException, UnknownCriterionTypeException {
        return getUnmetAwardConditionsForUser(certificateDefinitionId, userId(), useCaching);
    }

    public Set<Criterion> getUnmetAwardConditionsForUser(String certificateDefinitionId, String userId, boolean useCaching)
            throws IdUnusedException, UnknownCriterionTypeException {
        String contextId = contextId();
        CertificateDefinition cd = (CertificateDefinition)getCertificateDefinition(certificateDefinitionId);
        Set<Criterion> criteria = cd.getAwardCriteria();
        Set<Criterion> unmetCriteria = new HashSet<>();

        for (Criterion criterion : criteria) {
            CriteriaFactory cFact = criteriaFactoryMap.get(criterion.getClass());

            if (!cFact.isCriterionMet(criterion, userId, contextId, useCaching)) {
                unmetCriteria.add(criterion);
            }
        }

        return unmetCriteria;
    }

    public Map<String, String> getPredefinedTemplateVariables() {
        HashMap<String, String> predefined = new HashMap<>();

        for (String key : variableResolvers.keySet()) {
            VariableResolver vr = variableResolvers.get(key);
            for (String label : vr.getVariableLabels()) {
                predefined.put(label, vr.getVariableDescription(label));
            }
        }

        return predefined;
    }

    public void registerCriteriaFactory(CriteriaFactory cFact) {
        Set<Class<? extends Criterion>> critClasses = cFact.getCriterionTypes();
        for (Class critClass : critClasses) {
            criteriaFactoryMap.put(critClass, cFact);
        }

        Set<CriteriaTemplate> templates = cFact.getCriteriaTemplates();
        for (CriteriaTemplate template : templates) {
            criteriaTemplateMap.put (template.getId(), cFact);
        }

        criteriaFactories.add(cFact);
    }

    public CriteriaFactory getCriteriaFactory (String criteriaTemplateId) {
        return criteriaTemplateMap.get(criteriaTemplateId);
    }

    public Set<CriteriaTemplate> getCriteriaTemplates() {
        HashSet<CriteriaTemplate> criteriaTemplates = new HashSet<>();
        for (CriteriaFactory factory : criteriaFactories) {
            criteriaTemplates.addAll(factory.getCriteriaTemplates());
        }

        return criteriaTemplates;
    }

    @SuppressWarnings("unchecked")
    public int getCategoryType(final String gradebookId) {
        return (Integer) getHibernateTemplate().execute(new HibernateCallback() {
            public Integer doInHibernate(Session session) throws HibernateException {
                List<Integer> list = session.createQuery("select gb.category_type from CertGradebook as gb where gb.uid=:gbid")
                    .setParameter(PARAM_GBID, gradebookId).list();
                return list.get(0);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public Map<Long,Double> getCategoryWeights(final String gradebookId) {
        return (Map<Long, Double>) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                List<Object[]> results = session.createQuery("select assn.id, cat.weight from CertCategory as cat, CertAssignment as assn " +
                    "where cat.gradebook.uid=:gbid and cat.removed=false " +
                    "and cat.id = assn.category.id and assn.notCounted=false and assn.removed=false")
                .setParameter(PARAM_GBID, gradebookId).list();

                Map<Long,Double> catWeightMap = new HashMap<>();
                for(Object[] row : results) {
                    catWeightMap.put((Long) row[0], (Double) row[1]);
                }
                return catWeightMap;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public Map<Long, Double> getAssignmentWeights(final String gradebookId) {
        HibernateCallback callback = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                List<Object[]> results =  session.createQuery("select assn.id, assn.assignmentWeighting from CertAssignment as assn " +
                    "where assn.notCounted=false and assn.removed=false and " +
                    "assn.gradebook.uid=:gradebookId")
                .setParameter(PARAM_GRADEBOOK_ID, gradebookId).list();

                Map<Long, Double> assnWeights = new HashMap<>();
                for(Object[] row : results) {
                    assnWeights.put((Long) row[0], (Double) row[1]);
                }

                return assnWeights;
            }
        };

        return (Map<Long,Double>)getHibernateTemplate().execute(callback);
    }

    @SuppressWarnings("unchecked")
    public Map<Long, Double> getCatOnlyAssignmentPoints(final String gradebookId) {
        HibernateCallback callback = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                List<Object[]> results = session.createQuery("select assn.id, assn.pointsPossible from CertCategory as cat, CertAssignment as assn " +
                    "where cat.gradebook.uid=:gradebookId and cat.removed=false " +
                    "and cat.id = assn.category.id and assn.notCounted=false " +
                    "and assn.removed=false")
                .setParameter(PARAM_GRADEBOOK_ID, gradebookId).list();

                HashMap<Long, Double> assnPoints = new HashMap<>();
                for(Object[] row : results) {
                    assnPoints.put((Long) row[0], (Double) row[1]);
                }

                return assnPoints;
            }
        };

         return (HashMap<Long, Double>)getHibernateTemplate().execute(callback);
    }

    @SuppressWarnings("unchecked")
    public Map<Long, Double> getAssignmentPoints(final String gradebookId) {
        HibernateCallback callback = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                List<Object[]> results = session.createQuery("select assn.id, assn.pointsPossible from CertAssignment as assn " +
                    "where assn.removed=false and assn.notCounted=false and " +
                    "assn.gradebook.uid=:gradebookId")
                .setParameter(PARAM_GRADEBOOK_ID, gradebookId).list();

                HashMap<Long, Double> assnPoints = new HashMap<>();
                for(Object[] row : results) {
                    assnPoints.put((Long) row[0], (Double) row[1]);
                }

                return assnPoints;
            }
        };

         return (HashMap<Long, Double>)getHibernateTemplate().execute(callback);
    }

    public Map<Long, Double> getAssignmentScores(final String gradebookId, final String studentId) {
        HibernateCallback callback = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Iterator results = session.createQuery("select agr.gradableObject.id, agr.pointsEarned from CertAssignmentScore as agr " +
                    "where agr.gradableObject.removed=false " +
                    "and agr.gradableObject.released=true " +
                    "and agr.gradableObject.gradebook.uid=:gradebookId and agr.studentId = :studentId")
                .setParameter(PARAM_GRADEBOOK_ID, gradebookId).setParameter(PARAM_STUDENT_ID, studentId).list().iterator();

                HashMap<Long, Double> assnScores = new HashMap<>();
                while(results.hasNext()) {
                    Object[] row = (Object[]) results.next();
                    assnScores.put((Long) row[0],(Double) row[1]);
                }

                return assnScores;
            }
        };

        return (HashMap<Long, Double>)getHibernateTemplate().execute(callback);
    }

    public Map<Long, Date> getAssignmentDatesRecorded (final String gradebookId, final String studentId) {
        HibernateCallback callback = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Iterator results = session.createQuery("select agr.gradableObject.id, agr.dateRecorded from CertAssignmentScore as agr " +
                    "where agr.gradableObject.removed=false " +
                    "and agr.gradableObject.released=true " +
                    "and agr.gradableObject.gradebook.uid=:gradebookId and agr.studentId = :studentId")
                .setParameter(PARAM_GRADEBOOK_ID, gradebookId).setParameter(PARAM_STUDENT_ID, studentId).list().iterator();

                HashMap<Long, Date> assnDates = new HashMap<>();
                while(results.hasNext()) {
                    Object[] row = (Object[]) results.next();
                    assnDates.put((Long) row[0],(Date) row[1]);
                }

                return assnDates;
            }
        };

        return (HashMap<Long, Date>)getHibernateTemplate().execute(callback);
    }

    @Override
    public List<Map.Entry<String, String>> getCertificateRequirementsForUser (String certId, String userId, String siteId, boolean useCaching) throws IdUnusedException {
        CertificateDefinition certDef = getCertificateDefinition(certId);
        Map requirements = new HashMap<>();

        Set<Criterion> criteria = certDef.getAwardCriteria();
        Iterator<Criterion> itCriteria = criteria.iterator();
        while (itCriteria.hasNext()) {
            Criterion crit = itCriteria.next();
            String expression = crit.getExpression();
            String progress = crit.getProgress(userId, siteId, useCaching);

            //progress is "" if it's irrelevant (ie. WillExpire criterion)
            if ( StringUtils.isNotEmpty(progress) ) {
                requirements.put(expression, progress);
            }
        }

        return new ArrayList<>(requirements.entrySet());
    }

    public Collection<String> getGradedUserIds(final String siteId) {
        /* Gets all users who have earned grades in the site - regardless of whether they are still enrolled
         * (for historical purposes)*/

        HibernateCallback callback = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                //We need to get people from gb_grade_record_t, grading event only gives us people graded from the gradebook tool
                String query =  "select distinct gr.studentId from CertGradeRecordObject as gr " +
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
    public List<ReportRow> getReportRows(List<String> userIds, CertificateDefinition definition, String filterType, String filterDateType, Date startDate,
                                         Date endDate, List<Criterion> orderedCriteria) {
        if (definition == null) {
            return null;
        }

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, this.getLocale());

        boolean showUnawarded = false;
        if ("all".equals(filterType)) {
            showUnawarded = true;
        } else if ("unawarded".equals(filterType)) {
            showUnawarded = true;
        }

        List<ReportRow> reportRows = new ArrayList<>();

        //Get the criteria in the order of the displayed columns
        WillExpireCriterion wechi = null;
        Iterator<Criterion> itOrderedCriteria = orderedCriteria.iterator();
        while (itOrderedCriteria.hasNext()) {
            Criterion crit = itOrderedCriteria.next();
            if (crit instanceof WillExpireCriterion) {
                wechi = (WillExpireCriterion) crit;
                break;
            }
        }

        Iterator<CriteriaFactory> itCritFactories = criteriaFactories.iterator();
        while (itCritFactories.hasNext()) {
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
        Map<String, Map<Criterion, UserProgress>> allUserProgress = new HashMap<>();

        Set<Criterion> criteria = definition.getAwardCriteria();
        String siteId = definition.getSiteId();

        /*
         * To get the UserProgress towards the criteria later, we'll be invoking the CriteriaFactories once for each criterion type
         * (this is how we'll minimize the number of queries).
         * So we need a mapping of CriteriaFactories to the criterion types, and the criterion types need to be mapped to the criteria that match each criterion type.
         * Ie. CriteriaFactory -> (Criterion type managed by CriteriaFactory -> Criteria of key'd Criterion type)
         */
        Map<CriteriaFactory, Map<Class, List<Criterion>>> critFactToCritCollectionMap = new HashMap<>();
        for (Criterion criterion : criteria) {
            CriteriaFactory critFact = criterion.getCriteriaFactory();
            // Get the mapping of Criterion Type -> List<Criteria> that is associated with the current criterion's CriteriaFactory
            Map<Class, List<Criterion>> critTypeToCollectionMap = critFactToCritCollectionMap.get(critFact);
            if (critTypeToCollectionMap == null) {
                // The mapping of Criterion Type -> List<Criteria> for this CriterionFactory doesn't exist yet, so create one
                critTypeToCollectionMap = new HashMap<>();
                critFactToCritCollectionMap.put(critFact, critTypeToCollectionMap);
            }

            // Get the collection of criteria associated with this criterion's type
            Class criterionType = criterion.getClass();
            List<Criterion> critCollection = critTypeToCollectionMap.get(criterionType);
            if (critCollection == null) {
                // There is no collection of criteria associated with this criterion's type yet. Create one.
                critCollection = new ArrayList<>();
                critTypeToCollectionMap.put(criterionType, critCollection);
            }

            // associate this criterion with the criterion's type which is associated with the criterion's CriteriaFactory
            critCollection.add(criterion);
        }

        // Now execute the minimum number of queries to calculate each user's progress toward the criteria
        // Upon executing the queries, populate allUserProgress, which maps users -> (criteria -> user's progress on key'd criterion)
        for (Map.Entry<CriteriaFactory, Map<Class, List<Criterion>>> critFactToCritCollectionEntry: critFactToCritCollectionMap.entrySet()) {
            CriteriaFactory critFact = critFactToCritCollectionEntry.getKey();
            Map<Class, List<Criterion>> critTypeToCollectionMap = critFactToCritCollectionEntry.getValue();
            for (Map.Entry<Class, List<Criterion>> classToCritListEntry : critTypeToCollectionMap.entrySet()) {
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

                for (String userId : userIds) {
                    Map<Criterion, UserProgress> currentCriterionMap = currentUserProgress.get(userId);
                    if (currentCriterionMap != null) {
                        Map<Criterion, UserProgress> allCriteriaMap = allUserProgress.get(userId);
                        if (allCriteriaMap == null) {
                            // the current user doesn't have a mapping of Criterion -> UserProgress yet. Create one.
                            allCriteriaMap = new HashMap<>();
                            allUserProgress.put(userId, allCriteriaMap);
                        }

                        allCriteriaMap.putAll(currentCriterionMap);
                    }
                }
            }
        }

        // populate the report rows
        boolean canShowStudentNums = canUserViewStudentNumbers();
        Site currentSite = getCurrentSite();
        for (User user : users) {
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
            if (canShowStudentNums) {
                row.setStudentNumber(getStudentNumber(user, currentSite));
            }

            // Determine the awarded status and the issue date using the UserProgress objects we previously retrieved
            Map<Criterion, UserProgress> critProgressMap = allUserProgress.get(userId);

            // assume this user is awarded until we find a criterion on which the user has failed
            boolean awarded = true;
            Date dateAwarded = null;
            if (criteria.isEmpty()) {
                //TODO: ??? they're awarded, but when?
            } else if (critProgressMap == null) {
                // There are criteria, but this user doesn't have any mappings of Criterion -> UserProgress.
                // This means they have not made progress toward any criteria, hence they have failed.
                awarded = false;

            } else {
                for (Criterion criterion : criteria) {
                    UserProgress progress = critProgressMap.get(criterion);
                    if (progress == null || !progress.isPassed()) {
                        // null progress implies failure
                        awarded = false;

                        // date could have been set in previous iterations, so clear it
                        dateAwarded = null;
                        break;

                    } else {
                        // The user has passed all criteria so far
                        // Update the dateAwarded if the date that this user has met this criterion is later than all previous criteria
                        Date dateCritAwarded = progress.getDateAwarded();
                        if (dateAwarded == null) {
                            // This is the first iteration; initialize dateAwarded
                            dateAwarded = dateCritAwarded;
                        } else if (dateCritAwarded != null && dateAwarded.before(dateCritAwarded)) {
                            // The user was awarded on this criterion later than all previous criteria. Update the date awarded
                            dateAwarded = dateCritAwarded;
                        }
                    }
                }
            }

            Date expiryDate = null;
            if (wechi != null && dateAwarded != null) {
                expiryDate = wechi.getExpiryDate(dateAwarded);
            }

            // We now have enough information to filter this row if appropriate. To filter the row, we'll just 'continue' to the next user
            if (!awarded) {
                // User is not awarded
                if (!showUnawarded) {
                    continue;
                }

            } else {
                // User is awarded
                if ("unawarded".equals(filterType)) {
                    continue;
                } else {
                    if ("issueDate".equals(filterDateType)) {
                        if (dateAwarded == null
                            || (startDate != null && dateAwarded.before(startDate))
                            || (endDate != null && dateAwarded.after(endDate))) {
                            continue;
                        }
                    } else if ("expiryDate".equals(filterDateType) && wechi != null) {
                        if (expiryDate == null
                            || (startDate != null && expiryDate.before(startDate))
                            || (endDate != null && expiryDate.after(endDate))) {
                            continue;
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
            List<CriterionProgress> criterionCells = new ArrayList<>();
            if (critProgressMap == null) {
                // User made no progress on any criteria. Create placeholders
                for( Criterion orderedCriteria1 : orderedCriteria ) {
                    CriterionProgress critProg = new CriterionProgress("", false);
                    criterionCells.add(critProg);
                }

            } else {
                // add the criterion cells in order (orderedCriteria is the order of the headers)
                for (Criterion criterion : orderedCriteria) {
                    CriterionProgress critProg;
                    if (criterion instanceof WillExpireCriterion) {
                        // populate the expiry date
                        String strExpiryDate = "";
                        if (awarded && expiryDate != null) {
                            // it's awarded. The WillExpireCriterion instance can do the expiry date calculation for us
                            strExpiryDate = dateFormat.format(expiryDate);
                        }

                        // wechi always passes
                        critProg = new CriterionProgress(strExpiryDate, true);
                    } else {
                        UserProgress userProg = critProgressMap.get(criterion);
                        if (userProg == null) {
                            // no progress here, create placeholder
                            critProg = new CriterionProgress("", false);
                        } else {
                            critProg = new CriterionProgress(FormatHelper.formatGradeForDisplay(userProg.getProgress()), userProg.isPassed());
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

    private String getStudentNumber(User user, Site site) {
        if (site == null || user == null) {
            return "";
        }

        return candidateDetailProvider != null ? candidateDetailProvider.getInstitutionalNumericId(user, site).orElse("") : "";
    }

    /**
     * {@inheritDoc}
     */
    public boolean canUserViewStudentNumbers() {
        User currentUser = getCurrentUser();
        Site currentSite = getCurrentSite();

        if (currentUser != null && currentSite != null) {
            String siteRef = siteService.siteReference(currentSite.getId());
            boolean userHasSitePerm = securityService.unlock(currentUser.getId(), PERMISSION_VIEW_STUDENT_NUMS, siteRef);
            return userHasSitePerm && (candidateDetailProvider != null ? candidateDetailProvider.isInstitutionalNumericIdEnabled(currentSite) : false);
        } else {
            return false;
        }
    }

    /**
     * Utility method to get the current User
     * @return the current user
     */
    private User getCurrentUser() {
        return userDirectoryService.getCurrentUser();
    }

    /**
     * Utility method to get the current site ID
     * @return the current site ID, or null
     */
    private String getCurrentSiteID() {
        try {
            return this.toolManager.getCurrentPlacement().getContext();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Utility method to get the current Site
     * @return the current site, or null
     */
    private Site getCurrentSite() {
        String siteID = getCurrentSiteID();
        Site site = null;
        if (StringUtils.isNotBlank(siteID)) {
            try {
                site = siteService.getSite(siteID);
            } catch (IdUnusedException ex) {
                log.debug("Can't find site with ID = {}", siteID);
            }
        }

        return site;
    }

    /**
     * Sets the name field on the row in an appropriate format ('lastname, firstname' unless a name is missing)
     *
     * @param row
     * @param firstName
     * @param lastName
     */
    private void setNameFieldForReportRow(ReportRow row, String firstName, String lastName) {
        if (lastName == null) {
            lastName = "";
        }

        if (firstName == null) {
            firstName = "";
        }

        //if one name is missing, use the opposite
        if (StringUtils.isEmpty(lastName)) {
            //use the opposite name or empty string if firstName is missing (both cases are covered here)
            row.setName(firstName);
        } else if (StringUtils.isEmpty(firstName)) {
            row.setName(lastName);
        } else {
            //both names present
            row.setName(lastName+", "+firstName);
        }
    }

    private Site getSite(String siteId) {
        try {
            return siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            return null;
        }
    }

    private String getRole(String userId, String siteId) {
        Role role = getSite(siteId).getUserRole(userId);
        if (role != null) {
            return role.getId();
        } else {
            return messages.getString(REPORT_TABLE_NOT_A_MEMBER);
        }
    }
}
