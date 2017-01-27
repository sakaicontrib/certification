package com.rsmart.certification.api;

import java.util.Set;

/**
 * User: duffy
 * Date: Jul 5, 2011
 * Time: 8:59:48 AM
 */
public interface VariableResolver
{
    public Set<String> getVariableLabels();

    public String getVariableDescription(String varLabel);

    public String getValue(CertificateDefinition certDef, String varLabel, String userId) throws VariableResolutionException;
}
