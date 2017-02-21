package com.rsmart.certification.api;

import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.api.criteria.UnknownCriterionTypeException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Implementation for CertificateDefinition
 *
 * User: duffy
 * Date: Jun 29, 2011
 * Time: 2:54:55 PM
 */
public class BaseCertificateDefinition implements CertificateDefinition
{
    protected String id;
    protected String creatorUserId;
    protected String name;
    protected String description;
    protected String siteId;
    protected String expiryOffset;
    protected Date createDate;
    protected CertificateDefinitionStatus status = CertificateDefinitionStatus.UNPUBLISHED;
    protected Boolean hidden;
    protected DocumentTemplate documentTemplate;
    protected Map<String, String> fieldValues = new HashMap<>(0);
    protected Set<Criterion> awardCriteria = new HashSet<>();

    private final DateFormat DATE_FORMAT = new SimpleDateFormat("MMMM dd, yyyy hh:mm aa");

    public Date getCreateDate()
    {
        return createDate;
    }

    public void setCreateDate(Date createDate)
    {
        this.createDate = createDate;
    }

    public String getFormattedCreateDate()
    {
        return DATE_FORMAT.format(getCreateDate());
    }

    public String getCreatorUserId()
    {
        return creatorUserId;
    }

    public void setCreatorUserId(String creatorUserId)
    {
        this.creatorUserId = creatorUserId;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        if (description != null)
        {
            this.description = description.trim();
        }
    }

    public DocumentTemplate getDocumentTemplate()
    {
        return documentTemplate;
    }

    public void setDocumentTemplate(DocumentTemplate documentTemplate)
    {
        this.documentTemplate = documentTemplate;
    }

    public Map<String, String> getFieldValues()
    {
        return fieldValues;
    }

    public void setFieldValues(Map<String, String> fieldValues)
    {
        this.fieldValues = fieldValues;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id.trim();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name.trim();
    }

    public String getSiteId()
    {
        return siteId;
    }

    public void setSiteId(String siteId)
    {
        this.siteId = siteId;
    }

    public CertificateDefinitionStatus getStatus()
    {
        return status;
    }

    public void setStatus(CertificateDefinitionStatus status)
    {
        this.status = status;
    }

    public Set<Criterion> getAwardCriteria()
    {
        return awardCriteria;
    }

    public void addAwardCriterion(Criterion criterion)
    {
        if (awardCriteria == null)
        {
            awardCriteria = new HashSet<>();
        }

        awardCriteria.add(criterion);
    }

    public Date getIssueDate(String userId, boolean useCaching)
    {
        if (awardCriteria.isEmpty())
        {
            return null;
        }

        Iterator<Criterion> itAwardCriteria = awardCriteria.iterator();
        while (itAwardCriteria.hasNext())
        {
            Criterion crit = itAwardCriteria.next();
            if (crit != null)
            {
                CriteriaFactory critFact = crit.getCriteriaFactory();
                if (critFact != null)
                {
                    return critFact.getDateIssued(userId, siteId, this, useCaching);
                }
            }
        }

        return null;
    }

    public void setAwardCriteria(Set<Criterion> awardCriteria)
    {
        this.awardCriteria = awardCriteria;
    }

    public String getExpiryOffset()
    {
        return expiryOffset;
    }

    public void setExpiryOffset( String expiryOffset )
    {
        this.expiryOffset = expiryOffset;
    }

    public boolean isAwarded(String userId, boolean useCaching) throws UnknownCriterionTypeException
    {
        Iterator<Criterion> itAwardCriteria = awardCriteria.iterator();
        boolean awarded = true;
        while (itAwardCriteria.hasNext())
        {
            Criterion crit = itAwardCriteria.next();
            CriteriaFactory critFact = crit.getCriteriaFactory();
            if (!critFact.isCriterionMet(crit, userId, siteId, useCaching))
            {
                awarded = false;
            }
        }

        return awarded;
    }

    public void setProgressShown( Boolean show )
    {
        if( show == null )
        {
            hidden = Boolean.TRUE;
        }

        this.hidden = !show;
    }

    public Boolean getProgressShown()
    {
        if( hidden == null )
        {
            return Boolean.FALSE;
        }

        return !hidden;
    }

    public void setProgressHidden(Boolean hidden)
    {
        if (hidden == null)
        {
            hidden = Boolean.FALSE;
        }

        this.hidden = hidden;
    }

    public Boolean getProgressHidden()
    {
        if (hidden == null)
        {
            hidden = Boolean.TRUE;
        }

        return hidden;
    }
}
