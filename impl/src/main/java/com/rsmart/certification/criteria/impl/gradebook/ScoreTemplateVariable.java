package com.rsmart.certification.criteria.impl.gradebook;

import com.rsmart.certification.api.criteria.CriteriaTemplateVariable;
import java.util.Map;
import org.sakaiproject.util.ResourceLoader;

/**
 * User: duffy
 * Date: Jul 5, 2011
 * Time: 9:30:23 AM
 */
public class ScoreTemplateVariable implements CriteriaTemplateVariable
{
    private GradebookCriteriaFactory criteriaFactory = null;
    private String key = null;

    public ScoreTemplateVariable(String key, GradebookCriteriaFactory cFact)
    {
        criteriaFactory = cFact;
        setVariableKey(key);
    }

    public void setVariableKey(String key)
    {
        this.key = key;
    }

    public String getVariableKey()
    {
        return key;
    }

    public String getVariableLabel()
    {
        return getResourceLoader().getString(key);
    }

    public ResourceLoader getResourceLoader()
    {
        return criteriaFactory.getResourceLoader();
    }

    public boolean isMultipleChoice()
    {
        return false;
    }

    public Map<String, String> getValues()
    {
        return null;
    }

    public boolean isValid(String value)
    {
        try
        {
            Double.parseDouble(value);
        }
        catch (NumberFormatException nfe)
        {
            return false;
        }

        return true;
    }
}
