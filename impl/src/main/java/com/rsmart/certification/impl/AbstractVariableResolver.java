package com.rsmart.certification.impl;

import com.rsmart.certification.api.VariableResolver;

import java.util.HashMap;
import java.util.Set;

/**
 * User: duffy
 * Date: Jul 7, 2011
 * Time: 8:29:29 AM
 */
public abstract class AbstractVariableResolver
    implements VariableResolver
{
    private HashMap<String, String>
        descriptions = new HashMap<String, String>();

    public void addVariable (String variable, String description)
    {
        descriptions.put(variable, description);
    }
    
    public Set<String> getVariableLabels()
    {
        return descriptions.keySet();
    }

    public String getVariableDescription(String key)
    {
        return descriptions.get(key);
    }

}
