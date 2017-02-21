package com.rsmart.certification.mock;

import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.VariableResolutionException;
import com.rsmart.certification.api.VariableResolver;

import java.util.HashSet;
import java.util.Set;

/**
 * User: duffy
 * Date: Jul 5, 2011
 * Time: 1:43:00 PM
 */
public class MockVariableResolver implements VariableResolver
{
    public Set<String> getVariableLabels()
    {
        HashSet<String> vars = new HashSet<>();
        vars.add("mockVariable");
        return vars;
    }

    public String getVariableDescription(String key)
    {
        return "this is a mock variable for testing purposes";
    }

    @Override
    public String getValue( CertificateDefinition certDef, String varLabel, String userId, boolean useCaching ) throws VariableResolutionException
    {
        return "test";
    }
}
