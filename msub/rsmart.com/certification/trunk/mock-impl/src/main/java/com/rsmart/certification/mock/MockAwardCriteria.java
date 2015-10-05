package com.rsmart.certification.mock;

import com.rsmart.certification.api.AwardCriteria;
import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.criteria.Criterion;
import org.sakaiproject.conditions.api.Condition;

import java.util.Date;
import java.util.Set;

/**
 * User: duffy
 * Date: Jun 10, 2011
 * Time: 2:03:57 PM
 */
public class MockAwardCriteria
    implements AwardCriteria
{
    private String
        id = null;
    private Date
        effectiveDate = null,
        obsoleteDate = null;
    private CertificateDefinition
        certDef = null;
    private int
        revision = 0;
    private Set<Criterion>
        criteria;

    public String getId()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public Date getEffectiveDate()
    {
        return effectiveDate;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setEffectiveDate (Date ed)
    {
        effectiveDate = ed;
    }

    public Date getObsoleteDate()
    {
        return obsoleteDate;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setObsoleteDate (Date od)
    {
        obsoleteDate = od;
    }

    public CertificateDefinition getCertificateDefinition()
    {
        return certDef;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setRevision (int rev)
    {
        revision = rev;
    }

    public int getRevision()
    {
        return revision;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Set<Criterion> getCriteria()
    {
        return criteria;
    }

    public void setCriteria (Set<Criterion> criteria)
    {
        this.criteria = criteria;
    }

    public void setCertificateDefinition(MockCertificateDefinition mcd) {
        certDef = mcd;
    }
}
