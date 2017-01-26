package com.rsmart.certification.impl;

import com.rsmart.certification.api.criteria.CriteriaTemplateVariable;
import com.rsmart.certification.criteria.impl.gradebook.GradebookCriteriaFactory;

import java.util.Map;

import org.sakaiproject.util.ResourceLoader;
/**
 * Essentially the implementation as ScoreTemplateVariable, except it's an Integer
 * @author bbailla2
 *
 */
public class ExpiryOffsetTemplateVariable implements CriteriaTemplateVariable {

    private GradebookCriteriaFactory
    criteriaFactory = null;
    private String key = null;

    public ExpiryOffsetTemplateVariable(String key, GradebookCriteriaFactory cFact)
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
            Integer.parseInt(value);
        }
        catch (NumberFormatException nfe)
        {
            return false;
        }

        return true;
    }
}
