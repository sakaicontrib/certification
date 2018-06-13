package org.sakaiproject.certification.mock;

import org.sakaiproject.certification.api.CertificateDefinition;
import org.sakaiproject.certification.api.VariableResolutionException;
import org.sakaiproject.certification.api.VariableResolver;

import java.util.HashSet;
import java.util.Set;

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
