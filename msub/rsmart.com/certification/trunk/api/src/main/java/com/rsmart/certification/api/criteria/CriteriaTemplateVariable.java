package com.rsmart.certification.api.criteria;

import java.util.Map;

/**
 * User: duffy
 * Date: Jun 21, 2011
 * Time: 2:22:33 PM
 */
public interface CriteriaTemplateVariable
{
    public String getVariableKey();

    public String getVariableLabel();

    public boolean isMultipleChoice();

    public Map<String, String> getValues();
    
    public boolean isValid (String value);
}
