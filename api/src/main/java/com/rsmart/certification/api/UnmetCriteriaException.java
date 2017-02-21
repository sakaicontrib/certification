package com.rsmart.certification.api;

import com.rsmart.certification.api.criteria.Criterion;

import java.util.Set;

/**
 * User: duffy
 * Date: Jun 9, 2011
 * Time: 11:18:07 AM
 */
public class UnmetCriteriaException extends CertificationException
{
    private Set<Criterion> unmetCriteria = null;

    public UnmetCriteriaException ()
    {
        super ();
    }

    public UnmetCriteriaException(String message)
    {
        super(message);
    }

    public UnmetCriteriaException(String message, Throwable t)
    {
        super(message, t);
    }

    public UnmetCriteriaException(Throwable t)
    {
        super(t);
    }

    public void setUnmetCriteria (Set<Criterion> criteria)
    {
        unmetCriteria = criteria;
    }

    public Set<Criterion> getUnmetConditions ()
    {
        return unmetCriteria;
    }
}
