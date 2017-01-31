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
    private static final String UNASSIGNED = "unassigned";
    private static final String MESSAGE_NAMEOFCERT = "variable.nameOfCert";
    private static final String MESSAGE_UNASSIGNED = "variable.unassigned";

    public AwardVariableResolver()
    {
        String name = getMessages().getString(MESSAGE_NAMEOFCERT);
        String unassigned = getMessages().getString(MESSAGE_UNASSIGNED);
        addVariable(CERT_NAME, name);
        addVariable (UNASSIGNED, unassigned);
    }

    public String getValue(CertificateDefinition certDef, String varLabel, String userId) throws VariableResolutionException
    {
        if (CERT_NAME.equals(varLabel))
        {
            return certDef.getName();
        }
        else if (UNASSIGNED.equals(varLabel))
        {
            return "";
        }

        throw new VariableResolutionException("could not resolve variable: \"" + varLabel + "\"");
    }
}
