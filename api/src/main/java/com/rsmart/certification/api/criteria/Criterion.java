package com.rsmart.certification.api.criteria;

import java.util.Map;

/**
 * User: duffy
 * Date: Jun 21, 2011
 * Time: 10:19:22 AM
 */
public interface Criterion
{
    public String getId();

    public CriteriaFactory getCriteriaFactory();
    
    public String  getCurrentCriteriaTemplate();

    public String getExpression();
    
    public Map<String, String> getVariableBindings();
 
}
