package com.rsmart.certification.api;

import java.util.Date;

/**
 * This object represents the award of a certificate to a specific user. It is tied to a specific CertificateDefinition
 * and revision number to accurately represent which AwardCriteria were in effect at the time of award.
 *
 * User: duffy
 * Date: Jun 8, 2011
 * Time: 12:36:32 PM
 */
public interface CertificateAward
{
    public String getId();

    public String getUserId();

    public CertificateDefinition getCertificateDefinition();

    public Date getCertificationTimeStamp();

    public String getFormattedCertificationTimeStamp();
}