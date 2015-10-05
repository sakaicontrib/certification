package com.rsmart.certification.impl.hibernate;

import com.rsmart.certification.api.AwardCriteria;
import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.criteria.Criterion;

import java.util.Date;
import java.util.HashSet;

/**
 * User: duffy
 * Date: Jun 27, 2011
 * Time: 2:20:46 PM
 */
public class AwardCriteriaHibernateImpl
    implements AwardCriteria
{
    private String
        id = null;
    private Date
        effectiveDate = new Date(),
        obsoleteDate = null;
    private CertificateDefinition
        certificateDefinition;
    private int
        revision = 0;
    private HashSet<Criterion>
        criteria;

    public CertificateDefinition getCertificateDefinition()
    {
        return certificateDefinition;
    }

    public void setCertificateDefinition(CertificateDefinition certificateDefinition)
    {
        this.certificateDefinition = certificateDefinition;
    }

    public HashSet<Criterion> getCriteria()
    {
        return criteria;
    }

    public void setCriteria(HashSet<Criterion> criteria)
    {
        this.criteria = criteria;
    }

    public Date getEffectiveDate()
    {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate)
    {
        this.effectiveDate = effectiveDate;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Date getObsoleteDate()
    {
        return obsoleteDate;
    }

    public void setObsoleteDate(Date obsoleteDate)
    {
        this.obsoleteDate = obsoleteDate;
    }

    public int getRevision()
    {
        return revision;
    }

    public void setRevision(int revision)
    {
        this.revision = revision;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof AwardCriteriaHibernateImpl)) return false;

        AwardCriteriaHibernateImpl that = (AwardCriteriaHibernateImpl) o;

        if (revision != that.revision) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + revision;
        return result;
    }
}
