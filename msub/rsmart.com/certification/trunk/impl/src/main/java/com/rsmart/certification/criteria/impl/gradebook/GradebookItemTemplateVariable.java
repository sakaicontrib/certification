package com.rsmart.certification.criteria.impl.gradebook;

import com.rsmart.certification.api.criteria.CriteriaTemplateVariable;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: duffy
 * Date: Jun 23, 2011
 * Time: 3:06:49 PM
 */
public class GradebookItemTemplateVariable
    implements CriteriaTemplateVariable
{
    private GradebookCriteriaFactory
        criteriaFactory = null;
    private AssignmentFilter
        filter = null,
        dftFilter = new AssignmentFilter()
                    {
                        public boolean include(Assignment assignment)
                        {
                            return true;
                        }
                    };
    private AssignmentLabeler
        labeler = null,
        dftLabeler = new AssignmentLabeler()
                    {
                        public String getLabel(Assignment assignment)
                        {
                            return (assignment == null) ? null : assignment.getName();
                        }
                    };

    public GradebookItemTemplateVariable(GradebookCriteriaFactory fact, AssignmentFilter filter,
                                         AssignmentLabeler adapter)
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
        return "gradebook.item";
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
        GradebookService
            gbs = getGradebookService();
        ToolManager
            tm = getToolManager();
        HashMap<String, String>
            items = new HashMap<String, String>();
        ResourceLoader
            rl = getResourceLoader();
        String
            contextId = tm.getCurrentPlacement().getContext();

        if (!gbs.isGradebookDefined(contextId))
        {
            return items;
        }

        List<Assignment>
            assignments = gbs.getAssignments(contextId);

        for (Assignment asn : assignments)
        {
            if (filter.include(asn))
                items.put(Long.toString(asn.getId()), labeler.getLabel(asn));
        }

        return items;
    }

    public boolean isValid(String value)
    {
        return getValues().keySet().contains(value);
    }


}
