package com.rsmart.certification.mock;

import com.rsmart.certification.api.CertificateAward;
import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.CertificateDefinitionStatus;
import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.DocumentTemplate;
import com.rsmart.certification.api.DocumentTemplateException;
import com.rsmart.certification.api.DocumentTemplateService;
import com.rsmart.certification.api.IncompleteCertificateDefinitionException;
import com.rsmart.certification.api.UnmetCriteriaException;

import com.rsmart.certification.api.UnmodifiableCertificateDefinitionException;
import com.rsmart.certification.api.criteria.UnknownCriterionTypeException;
import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.mock.criteria.MockCriteriaFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.user.api.UserDirectoryService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    private HashMap <String, HashMap<String, MockCertificateAward>>
        certAwards = new HashMap<String, HashMap<String, MockCertificateAward>>();
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

    public CertificateDefinition createCertificateDefinition(String name, String description, String siteId)
        throws IdUsedException
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

    public DocumentTemplate setDocumentTemplate(String certificateDefinitionId, String mimeType, InputStream template)
        throws IdUnusedException, DocumentTemplateException
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

    public DocumentTemplate setDocumentTemplate(String certificateDefinitionId, InputStream template)
        throws IdUnusedException, DocumentTemplateException
    {
        return setDocumentTemplate(certificateDefinitionId, "text/plain", template);
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

    public Set<Criterion> getUnmetAwardConditions(String certificateDefinitionId)
            throws IdUnusedException, UnknownCriterionTypeException
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

    public Set<Criterion> getUnmetAwardConditionsForUser(String certificateDefinitionId, String userId)
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

    public CertificateAward awardCertificate(String certificateDefinitionId)
            throws IdUnusedException, UnmetCriteriaException, UnknownCriterionTypeException
    {
        return awardCertificate (certificateDefinitionId, uds.getCurrentUser().getId());
    }

    public CertificateAward awardCertificate(String certificateDefinitionId, String userId)
            throws IdUnusedException, UnmetCriteriaException, UnknownCriterionTypeException
    {
        return awardCert(certificateDefinitionId, userId);
    }

    private HashMap<String, MockCertificateAward> getAwardsForCert (String certDefId)
    {
        return certAwards.get(certDefId);
    }

    private void addAwardForCert (String certDefId, MockCertificateAward ca)
    {
        HashMap <String, MockCertificateAward>
            awards = getAwardsForCert(certDefId);

        if (awards == null)
        {
            awards = new HashMap<String, MockCertificateAward>();

            certAwards.put (certDefId, awards);
        }

        awards.put (ca.getUserId(), ca);
    }

    public CertificateAward getCertificateAward(String certificateDefinitionId)
        throws IdUnusedException
    {
        return getCertificateAwardForUser(certificateDefinitionId, uds.getCurrentUser().getId());
    }

    private MockCertificateAward awardCert (String cdId, String userId)
            throws IdUnusedException, UnmetCriteriaException, UnknownCriterionTypeException
    {
        //TODO - some real logic

        MockCertificateDefinition
            cd = getCD (cdId);

        Set<Criterion>
            conditions = cd.getAwardCriteria(),
            unmet = new HashSet<Criterion>();

        for (Criterion condition : conditions)
        {
            CriteriaFactory
                cFact = critFactsMap.get(condition.getClass());

            if (!cFact.isCriterionMet(condition))
            {
                unmet.add(condition);
            }
        }

        if (unmet.size() > 0)
        {
            UnmetCriteriaException
                uce = new UnmetCriteriaException();

            uce.setUnmetCriteria(unmet);

            throw uce;
        }

        MockCertificateAward
            award = new MockCertificateAward();

        award.setId("" + caId++);
        award.setCertificateDefinition(cd);
        award.setUserId(userId);

        addAwardForCert(cdId, award);

        return award;
    }

    public CertificateAward getCertificateAwardForUser(String certificateDefinitionId, String userId)
            throws IdUnusedException
    {
        HashMap<String, MockCertificateAward>
            awards = getAwardsForCert(certificateDefinitionId);
        MockCertificateAward
            award = null;

        if (awards != null)
        {
            award = awards.get(userId);

            if (award != null)
                return award;
        }

        return null;
    }

    public Set<CertificateAward> getCertificateAwards()
    {
        HashSet<CertificateAward>
            awards = new HashSet<CertificateAward>();

        for (HashMap<String, MockCertificateAward> awds4Cert : certAwards.values())
        {
            awards.addAll(awds4Cert.values());
        }

        return awards;
    }

    public Set<CertificateAward> getCertificateAwards(String certificateDefinitionId)
        throws IdUnusedException
    {
        HashMap<String, MockCertificateAward>
            awards = getAwardsForCert(certificateDefinitionId);

        if (awards == null)
            throw new IdUnusedException(certificateDefinitionId);

        HashSet<CertificateAward>
            results = new HashSet<CertificateAward>();

        results.addAll(awards.values());

        return results;
    }

    public Map<String, CertificateAward> getCertificateAwardsForUser(String[] certificateDefinitionIds)
        throws IdUnusedException
    {
        String
            userId = getUserDirectoryService().getCurrentUser().getId();
        HashMap<String, CertificateAward>
            results = new HashMap<String, CertificateAward>();

        if (userId == null)
            return results;

        for (String id : certificateDefinitionIds)
        {
            Map<String, MockCertificateAward>
                awards = certAwards.get(id);

            MockCertificateAward
                award = awards.get(userId);

            if (award != null)
            {
                results.put(id, award);
            }
        }

        return results;
    }

    public Set<CertificateAward> getCertificateAwardsForUser(String userId)
    {
        HashSet<CertificateAward>
            results = new HashSet<CertificateAward>();

        if (userId == null)
            return results;

        for (CertificateAward award : getCertificateAwards())
        {
            MockCertificateAward
                mca = (MockCertificateAward)award;

            if (userId.equals(mca.getUserId()))
            {
                results.add(mca);
            }
        }

        return results;
    }

    public Map<String, String> getPredefinedTemplateVariables()
    {
        HashMap<String, String>
            vars = new HashMap<String, String>();

        vars.put ("foo", "this is foo");
        return vars;
    }

    public InputStream getPrintableCertificateRendering(String certificateAwardId)
        throws IdUnusedException
    {
        Set<CertificateAward>
            awards = getCertificateAwards();

        for (CertificateAward award : awards)
        {
            if (award.getId().equals(certificateAwardId))
            {
                byte[]
                    arr = {'h','e','l','l','o',' ','w','o','r','l','d'};

                return new ByteArrayInputStream(arr);
            }
        }
        throw new IdUnusedException(certificateAwardId);
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
