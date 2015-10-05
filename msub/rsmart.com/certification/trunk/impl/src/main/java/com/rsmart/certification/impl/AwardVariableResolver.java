package com.rsmart.certification.impl;

import com.rsmart.certification.api.CertificateAward;
import com.rsmart.certification.api.VariableResolutionException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * User: duffy
 * Date: Jul 7, 2011
 * Time: 8:28:13 AM
 */
public class AwardVariableResolver
    extends AbstractVariableResolver
{
    private static final String
        CERT_NAME                       = "cert.name",
        CERT_AWARDDATE                  = "cert.date";

    public AwardVariableResolver()
    {
        addVariable (CERT_NAME, "name of this certificate");
        addVariable (CERT_AWARDDATE, "date of award");
    }
    
    public String getValue(CertificateAward award, String varLabel)
        throws VariableResolutionException
    {
        if (CERT_NAME.equals(varLabel))
        {
            return award.getCertificateDefinition().getName();
        }
        else if (CERT_AWARDDATE.equals(varLabel))
        {
            DateFormat
                dateFormat = SimpleDateFormat.getDateInstance();

            return dateFormat.format(award.getCertificationTimeStamp());
        }

        throw new VariableResolutionException("could not resolve variable: \"" + varLabel + "\"");
    }
}
