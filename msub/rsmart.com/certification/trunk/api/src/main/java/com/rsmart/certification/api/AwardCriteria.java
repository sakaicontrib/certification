package com.rsmart.certification.api;

import com.rsmart.certification.api.criteria.Criterion;

import java.util.Date;
import java.util.Set;

/**
 * AwardCriteria objects represent the conditions that must be met for a user to earn a certificate. Specific
 * conditions are represented by Condition objects from the org.sakaiproject.condition.api interfaces defined
 * in the sakai-kernel-api project.
 *
 * User: duffy
 * Date: Jun 9, 2011
 * Time: 10:47:03 AM
 */
public interface AwardCriteria
{
    public String getId();

    /**
     * @return the date this criteria became effective for award
     */
    public Date getEffectiveDate();

    /**
     * @return the date that this criteria was replaced by the next revision
     */
    public Date getObsoleteDate();

    public CertificateDefinition getCertificateDefinition ();

    /**
     * This revision number is incremented with each change to the Conditions for award. If the revision number of
     * the AwardCriteria matches getCertificateDefition().getRevision() this is the current AwardCriteria.
     *
     * @return revision number for this set of criteria
     */
    public int getRevision();

    /**
     * The Conditions which must be met to be awarded a certificate are reprsented by Condition objects as defined
     * by the org.sakaiproject.condition.api objects in the sakai-kernel-api project.
     *
     * @return the Set of Condition objects which must be met to award the certificate
     */
    public Set<Criterion> getCriteria();
}