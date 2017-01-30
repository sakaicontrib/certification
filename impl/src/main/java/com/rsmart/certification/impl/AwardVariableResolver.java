package com.rsmart.certification.impl;

import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.VariableResolutionException;

/**
 * User: duffy
 * Date: Jul 7, 2011
 * Time: 8:28:13 AM
 */
public class AwardVariableResolver extends AbstractVariableResolver
{
    private static final String CERT_NAME = "cert.name";
    private static final String MESSAGE_NAMEOFCERT = "variable.nameOfCert";

    public AwardVariableResolver()
    {
        String name = getMessages().getString(MESSAGE_NAMEOFCERT);
        addVariable(CERT_NAME, name);
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
