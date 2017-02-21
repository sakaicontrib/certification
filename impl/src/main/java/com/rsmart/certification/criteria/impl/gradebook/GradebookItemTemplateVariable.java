package com.rsmart.certification.criteria.impl.gradebook;

import com.rsmart.certification.api.criteria.CriteriaTemplateVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * User: duffy
 * Date: Jun 23, 2011
 * Time: 3:06:49 PM
 */
public class GradebookItemTemplateVariable implements CriteriaTemplateVariable
{
    private GradebookCriteriaFactory criteriaFactory = null;

    private AssignmentFilter filter = null;
    private final AssignmentFilter dftFilter = (Assignment assignment) -> true;

    private AssignmentLabeler labeler = null;
    private final AssignmentLabeler dftLabeler = (Assignment assignment) -> (assignment == null) ? null : assignment.getName();

    private static final String KEY_GRADEBOOK_ITEM = "gradebook.item";

    public GradebookItemTemplateVariable(GradebookCriteriaFactory fact, AssignmentFilter filter, AssignmentLabeler adapter)
    {
        criteriaFactory = fact;
        this.filter = (filter != null) ? filter : dftFilter;
        labeler = (adapter != null) ? adapter : dftLabeler;
    }

    public ResourceLoader getResourceLoader()
    {
        return criteriaFactory.getResourceLoader();
    }

    public GradebookService getGradebookService()
    {
        return criteriaFactory.getGradebookService();
    }

    public ToolManager getToolManager()
    {
        return criteriaFactory.getToolManager();
    }

    public String getVariableKey()
    {
        return KEY_GRADEBOOK_ITEM;
    }

    public String getVariableLabel()
    {
        return getResourceLoader().getString(getVariableKey());
    }

    public boolean isMultipleChoice()
    {
        return true;
    }

    public Map<String, String> getValues()
    {
        GradebookService gbs = getGradebookService();
        ToolManager tm = getToolManager();
        HashMap<String, String> items = new HashMap<>();
        String contextId = tm.getCurrentPlacement().getContext();

        if (!gbs.isGradebookDefined(contextId))
        {
            return items;
        }

        List<Assignment> assignments = gbs.getAssignments(contextId);
        for (Assignment asn : assignments)
        {
            if (filter.include(asn))
            {
                items.put(Long.toString(asn.getId()), labeler.getLabel(asn));
            }
        }

        return items;
    }

    public boolean isValid(String value)
    {
        return getValues().keySet().contains(value);
    }
}
