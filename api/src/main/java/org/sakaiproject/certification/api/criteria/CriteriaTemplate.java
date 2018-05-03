package org.sakaiproject.certification.api.criteria;

import java.util.List;

/**
 * User: duffy
 * Date: Jun 21, 2011
 * Time: 2:00:39 PM
 */
public interface CriteriaTemplate
{
    public String getId();

    public String getExpression();

    public String getExpression(Criterion criterion);

    public int getTemplateVariableCount();

    public List<CriteriaTemplateVariable> getTemplateVariables();

    public CriteriaTemplateVariable getTemplateVariable(int i);

    public CriteriaFactory getCriteriaFactory();

    public String getMessage();
}
