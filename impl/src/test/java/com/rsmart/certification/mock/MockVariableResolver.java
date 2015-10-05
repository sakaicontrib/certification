package com.rsmart.certification.mock;

import com.rsmart.certification.api.CertificateAward;
import com.rsmart.certification.api.VariableResolver;

import java.util.HashSet;
import java.util.Set;

/**
 * User: duffy
 * Date: Jul 5, 2011
 * Time: 1:43:00 PM
 */
public class MockVariableResolver
    implements VariableResolver
{
    public Set<String> getVariableLabels()
    {
        HashSet<String>
            vars = new HashSet<String>();

        vars.add("mockVariable");
        return vars;
    }

    public String getVariableDescription(String key)
    {
        return "this is a mock variable for testing purposes";
    }

    public String getValue(CertificateAward award, String key)
    {
        return "test";
    }
}
