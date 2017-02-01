package com.rsmart.certification.mock;

import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.CertificateDefinitionStatus;
import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.DocumentTemplate;
import com.rsmart.certification.api.DocumentTemplateException;
import com.rsmart.certification.api.DocumentTemplateService;
import com.rsmart.certification.api.IncompleteCertificateDefinitionException;
import com.rsmart.certification.api.ReportRow;
import com.rsmart.certification.api.TemplateReadException;
import com.rsmart.certification.api.UnmodifiableCertificateDefinitionException;
import com.rsmart.certification.api.UnsupportedTemplateTypeException;
import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.api.criteria.UnknownCriterionTypeException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * User: duffy
 * Date: Jun 10, 2011
 * Time: 10:28:06 AM
 */
public class MockCertificateService
    implements CertificateService
{
    private DocumentTemplateService
        dts = null;
    private HashMap <String, MockCertificateDefinition>
        certDefs = new HashMap<String, MockCertificateDefinition>();
    private HashMap <String, MockDocumentTemplate>
        docTemps = new HashMap<String, MockDocumentTemplate> ();
    int
        id = 1,
        dtId = 1,
        caId = 1;
    private UserDirectoryService
        uds = null;
    private HashSet <CriteriaFactory>
        critFacts = new HashSet<CriteriaFactory>();
    private HashMap <Class, CriteriaFactory>
        critFactsMap = new HashMap<Class, CriteriaFactory>();

    public CertificateDefinition createCertificateDefinition(CertificateDefinition certDef)
    {
        MockCertificateDefinition
            myCertDef = new MockCertificateDefinition();

        myCertDef.setId("" + id++);
        myCertDef.setAwardCriteria(certDef.getAwardCriteria());
        myCertDef.setCreateDate(new Date());
        myCertDef.setCreatorUserId(getUserDirectoryService().getCurrentUser().getId());
        myCertDef.setDescription(certDef.getDescription());
        myCertDef.setDocumentTemplate(certDef.getDocumentTemplate());
        myCertDef.setFieldValues(certDef.getFieldValues());
        myCertDef.setSiteId(certDef.getSiteId());
        myCertDef.setName(certDef.getName());
        myCertDef.setStatus(CertificateDefinitionStatus.UNPUBLISHED);

        certDefs.put(myCertDef.getId(), myCertDef);

        return myCertDef;
    }

    @Override
    public void deleteCertificateDefinition( String certificateDefinitionId ) throws IdUnusedException, DocumentTemplateException
    {
        // Unimplemented
    }

    @Override
    public Map<Long, Date> getAssignmentDatesRecorded( String gradebookId, String studentId )
    {
        return null;
    }

    @Override
    public Map<Long, Double> getAssignmentPoints( String gradebookId )
    {
        return null;
    }

    @Override
    public Map<Long, Double> getAssignmentScores( String gradebookId, String studentId )
    {
        return null;
    }

    @Override
    public Map<Long, Double> getAssignmentWeights( String gradebookId )
    {
        return null;
    }

    @Override
    public Map<Long, Double> getCatOnlyAssignmentPoints( String gradebookId )
    {
        return null;
    }

    @Override
    public int getCategoryType( String gradebookId )
    {
        return 0;
    }

    @Override
    public Map<Long, Double> getCategoryWeights( String gradebookId )
    {
        return null;
    }

    @Override
    public CertificateDefinition getCertificateDefinitionByName( String siteId, String name ) throws IdUnusedException
    {
        return null;
    }

    @Override
    public List<Map.Entry<String, String>> getCertificateRequirementsForUser( String certId, String userId, String siteId, boolean useCaching ) throws IdUnusedException
    {
        return null;
    }

    @Override
    public ContentHostingService getContentHostingService()
    {
        return null;
    }

    @Override
    public CriteriaFactory getCriteriaFactory( String criteriaTemplateId )
    {
        return null;
    }

    @Override
    public String getFormattedMessage( String key, Object[] values )
    {
        return "";
    }

    @Override
    public Collection<String> getGradedUserIds( String siteId )
    {
        return null;
    }

    @Override
    public String getMimeType( byte[] toCheck ) throws DocumentTemplateException
    {
        return "application/pdf";
    }

    @Override
    public List<ReportRow> getReportRows( List<String> userIds, CertificateDefinition definition, String filterType, String filterDateType, Date startDate, Date endDate, List<Criterion> orderedCriteria )
    {
        return null;
    }

    @Override
    public String getString( String key )
    {
        return "";
    }

    @Override
    public InputStream getTemplateFileInputStream( String resourceId ) throws TemplateReadException
    {
        return null;
    }

    @Override
    public void removeAwardCriterion( String certificateDefinitionId, String criterionId ) throws IdUnusedException, UnmodifiableCertificateDefinitionException
    {
        // Unimplemented
    }

    public CertificateDefinition updateCertificateDefinition(CertificateDefinition certDef)
        throws IdUnusedException
    {
        MockCertificateDefinition
            myCertDef = getCD(certDef.getId());

        myCertDef.setAwardCriteria(certDef.getAwardCriteria());
        myCertDef.setCreateDate(certDef.getCreateDate());
        myCertDef.setCreatorUserId(certDef.getCreatorUserId());
        myCertDef.setDescription(certDef.getDescription());
        myCertDef.setDocumentTemplate(certDef.getDocumentTemplate());
        myCertDef.setFieldValues(certDef.getFieldValues());
        myCertDef.setSiteId(certDef.getSiteId());
        myCertDef.setName(certDef.getName());
        myCertDef.setStatus(certDef.getStatus());

        return myCertDef;
    }

    public void setDocumentTemplateService(DocumentTemplateService dts)
    {
        this.dts = dts;
    }

    public DocumentTemplateService getDocumentTemplateService()
    {
        return dts;
    }

    public void setUserDirectoryService (UserDirectoryService uds)
    {
        this.uds = uds;
    }

    public UserDirectoryService getUserDirectoryService()
    {
        return uds;
    }

    private MockCertificateDefinition getCD (String id)
        throws IdUnusedException
    {
        MockCertificateDefinition
            cd = certDefs.get(id);

        if (cd == null)
            throw new IdUnusedException("id: " + id);

        return cd;
    }

    public CertificateDefinition createCertificateDefinition( String name, String description, String siteId, Boolean progressHidden,
                                                                String fileName, String mimeType, InputStream template )
            throws IdUsedException, UnsupportedTemplateTypeException, DocumentTemplateException
    {
        MockCertificateDefinition
            certDef = new MockCertificateDefinition();

        certDef.setName(name);
        certDef.setDescription(description);
        certDef.setSiteId(siteId);

        certDef.setId("" + id++);

        certDefs.put(certDef.getId(), certDef);

        return certDef;
    }

    private final void setDataForTemplate (MockDocumentTemplate template, InputStream input)
        throws IOException
    {
        int
            alloc_size = 65536,
            chunk_size = 1024,
            offset = 0,
            numread = -1,
            read_size = chunk_size;
        byte
            buffer[] = new byte[alloc_size],
            chunk[] = new byte[read_size];

        while ((numread = input.read(chunk)) > -1)
        {
            if (numread + offset >= buffer.length)
            {
                byte
                    newBuff[] = new byte[buffer.length + alloc_size];

                System.arraycopy(buffer, 0, newBuff, 0, buffer.length);

                buffer = newBuff;
            }

            System.arraycopy (chunk, 0, buffer, offset, numread);

            offset += numread;
        }

        byte
            finalBuffer[] = new byte[offset];

        System.arraycopy(buffer, 0, finalBuffer, 0, offset);

        template.setData(finalBuffer);
    }

    public DocumentTemplate setDocumentTemplate( String certificateDefinitionId, String name, String mimeType, InputStream template )
            throws IdUnusedException, UnsupportedTemplateTypeException, DocumentTemplateException
    {
        MockCertificateDefinition
            cd = getCD(certificateDefinitionId);

        MockDocumentTemplate
            dt = new MockDocumentTemplate();

        dt.setId("" + dtId++);
        dt.setOutputMimeType(mimeType);

        try
        {
            setDataForTemplate(dt, template);
        }
        catch (IOException e)
        {
            throw new DocumentTemplateException ("Error reading template data", e);
        }

        docTemps.put(dt.getId(), dt);

        cd.setDocumentTemplate(dt);

        return dt;
    }

    public DocumentTemplate setDocumentTemplate(String certificateDefinitionId, String name, InputStream template)
        throws IdUnusedException, DocumentTemplateException
    {
        return setDocumentTemplate(certificateDefinitionId, name, "text/plain", template);
    }

    public void setFieldValues(String certificateDefinitionId, Map<String, String> fieldValues)
        throws IdUnusedException
    {
        MockCertificateDefinition
            cd = getCD(certificateDefinitionId);

        cd.setFieldValues(fieldValues);
    }

    public void activateCertificateDefinition(String certificateDefinitionId, boolean active)
            throws IncompleteCertificateDefinitionException, IdUnusedException
    {
        MockCertificateDefinition
            cd = getCD(certificateDefinitionId);

        if (cd.getDocumentTemplate() == null ||
            cd.getName() == null ||
            cd.getAwardCriteria() == null)
        {
            throw new IncompleteCertificateDefinitionException ("incomplete certificate definition");
        }

        cd.setStatus (active ? CertificateDefinitionStatus.ACTIVE : CertificateDefinitionStatus.INACTIVE);
    }

    public CertificateDefinition getCertificateDefinition(String id)
        throws IdUnusedException
    {
        return getCD(id);
    }

    public Set<CertificateDefinition> getCertificateDefinitions()
    {
        return new HashSet(certDefs.values());
    }

    public Set<CertificateDefinition> getCertificateDefinitionsForSite(String siteId)
    {
        HashSet<CertificateDefinition>
            cdSet = new HashSet<CertificateDefinition> ();
        MockCertificateDefinition mcd = new MockCertificateDefinition();
        certDefs.put("1", mcd);
        for (MockCertificateDefinition cd : certDefs.values())
        {
            if (cd.getId().equals(siteId))
                cdSet.add(cd);
        }

        return cdSet;
    }

    public Set<CertificateDefinition> getCertificateDefinitionsForSite(String siteId, CertificateDefinitionStatus[] statuses)
    {
        HashSet<CertificateDefinition>
            cdSet = new HashSet<CertificateDefinition> ();

        for (MockCertificateDefinition cd : certDefs.values())
        {
            CertificateDefinitionStatus
                status = cd.getStatus();

            if (cd.getId().equals(siteId))
            {
                for (CertificateDefinitionStatus cds : statuses)
                {
                    if (status.equals(cds))
                    {
                        cdSet.add(cd);
                        break;
                    }
                }
            }
        }

        return cdSet;
    }

    public Set<Criterion> getUnmetAwardConditions( String certificateDefinitionId, boolean useCaching ) throws IdUnusedException, UnknownCriterionTypeException
    {
        MockCertificateDefinition
            cd = getCD (certificateDefinitionId);
        Set<Criterion>
            unmet = new HashSet<Criterion>();
        Set<Criterion>
            criteria = cd.getAwardCriteria();

        for (Criterion criterion : criteria)
        {
            CriteriaFactory
                cFact = critFactsMap.get(criterion.getClass());

            if (!cFact.isCriterionMet(criterion))
                unmet.add(criterion);
        }

        return unmet;
    }

    public Set<Criterion> getUnmetAwardConditionsForUser( String certificateDefinitionId, String userId, boolean useCaching )
            throws IdUnusedException, UnknownCriterionTypeException
    {
        Set<Criterion>
            unmet = new HashSet<Criterion>();
        MockCertificateDefinition
            cd = getCD(certificateDefinitionId);
        Set<Criterion>
            criteria = cd.getAwardCriteria();

        String
            contextId = cd.getSiteId();

        for (Criterion criterion : criteria)
        {
            CriteriaFactory
                cFact = critFactsMap.get(criterion.getClass());

            if (!cFact.isCriterionMet(criterion))
                unmet.add(criterion);
        }

        return unmet;
    }

    public Map<String, String> getPredefinedTemplateVariables()
    {
        HashMap<String, String>
            vars = new HashMap<String, String>();

        vars.put ("foo", "this is foo");
        return vars;
    }

    public void registerCriteriaFactory(CriteriaFactory cFact)
    {
        for (Class critClass : cFact.getCriterionTypes())
        {
            critFactsMap.put(critClass, cFact);
        }
        critFacts.add(cFact);
    }

    public Set<CriteriaTemplate> getCriteriaTemplates()
    {
        Set<CriteriaTemplate>
            templates = new HashSet<CriteriaTemplate>();

        for (CriteriaFactory cf : critFacts)
        {
            templates.addAll(cf.getCriteriaTemplates());
        }

        return templates;
    }

    public CertificateDefinition duplicateCertificateDefinition(String certificateDefinitionId)
        throws IdUnusedException
    {
        MockCertificateDefinition
            cd = getCD(certificateDefinitionId),
            newCD = (MockCertificateDefinition) createCertificateDefinition(cd);

        newCD.setName("Copy of " + cd.getName());

        return newCD;
    }

    public void setAwardCriteria(String certificateDefinitionId, Set<Criterion> conditions)
            throws IdUnusedException, UnmodifiableCertificateDefinitionException
    {
        MockCertificateDefinition
            mcd = getCD(certificateDefinitionId);

        if (!CertificateDefinitionStatus.UNPUBLISHED.equals(mcd.getStatus()))
            throw new UnmodifiableCertificateDefinitionException("Attemted to modify the conditions for a certificate which has already been published");

        mcd.setAwardCriteria(conditions);
    }

    public Criterion addAwardCriterion(String certificateDefinitionId, Criterion criterion)
            throws IdUnusedException, UnmodifiableCertificateDefinitionException
    {
        CertificateDefinition
            cd = getCD(certificateDefinitionId);

        if (!CertificateDefinitionStatus.UNPUBLISHED.equals(cd.getStatus()))
            throw new UnmodifiableCertificateDefinitionException("Attemted to modify the conditions for a certificate which has already been published");

        Set<Criterion>
            criteria = cd.getAwardCriteria();

        criteria.add(criterion);

        return criterion;
    }
}
