package com.rsmart.certification.impl;

import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.VariableResolutionException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * User: duffy
 * Date: Jul 7, 2011
 * Time: 8:28:13 AM
 */
public class AwardVariableResolver extends AbstractVariableResolver
{
    private static final String CERT_NAME = "cert.name";

    public AwardVariableResolver()
    {
        //TODO: Internationalize
        addVariable(CERT_NAME, "name of this certificate");
    }

    public String getValue(CertificateDefinition certDef, String varLabel, String userId) throws VariableResolutionException
    {
        if (CERT_NAME.equals(varLabel))
        {
            return certDef.getName();
        }

        throw new VariableResolutionException("could not resolve variable: \"" + varLabel + "\"");
    }
}
