package com.rsmart.certification.criteria.impl.gradebook;

import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.CriteriaTemplateVariable;
import org.sakaiproject.util.ResourceLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * User: duffy
 * Date: Jul 18, 2011
 * Time: 10:38:10 PM
 */
public abstract class GradebookItemCriteriaTemplate
    implements CriteriaTemplate
{
    GradebookItemTemplateVariable
        itemVariable = null;
    ArrayList<CriteriaTemplateVariable>
        variables = new ArrayList<CriteriaTemplateVariable>(1);
    GradebookCriteriaFactory
        factory = null;
    ResourceLoader
        rl = null;

    public GradebookItemCriteriaTemplate(GradebookCriteriaFactory factory, AssignmentFilter filter,
                                         AssignmentLabeler labeler)
    {
        this.factory = factory;

        itemVariable = new GradebookItemTemplateVariable(factory, filter, labeler);

        addVariable(itemVariable);
    }

    protected void addVariable (CriteriaTemplateVariable variable)
    {
        variables.add(variable);
    }

    public void setResourceLoader (ResourceLoader rl)
    {
        this.rl = rl;
    }

    public ResourceLoader getResourceLoader()
    {
        return rl;
    }

    public CriteriaFactory getCriteriaFactory()
    {
        return factory;
    }

    public int getTemplateVariableCount()
    {
        return variables.size();
    }

    public List<CriteriaTemplateVariable> getTemplateVariables()
    {
        return variables;
    }

    public CriteriaTemplateVariable getTemplateVariable(int i)
    {
        return variables.get(i);
    }
}