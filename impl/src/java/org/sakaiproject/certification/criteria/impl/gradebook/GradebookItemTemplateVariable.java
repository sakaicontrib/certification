/**
 * Copyright (c) 2003-2018 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.certification.criteria.impl.gradebook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.certification.api.criteria.CriteriaTemplateVariable;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;

public class GradebookItemTemplateVariable implements CriteriaTemplateVariable {

    private GradebookCriteriaFactory criteriaFactory = null;

    private AssignmentFilter filter = null;
    private final AssignmentFilter dftFilter = (Assignment assignment) -> true;

    private AssignmentLabeler labeler = null;
    private final AssignmentLabeler dftLabeler = (Assignment assignment) -> (assignment == null) ? null : assignment.getName();

    private static final String KEY_GRADEBOOK_ITEM = "gradebook.item";

    public GradebookItemTemplateVariable(GradebookCriteriaFactory fact, AssignmentFilter filter, AssignmentLabeler adapter) {
        criteriaFactory = fact;
        this.filter = (filter != null) ? filter : dftFilter;
        labeler = (adapter != null) ? adapter : dftLabeler;
    }

    public ResourceLoader getResourceLoader() {
        return criteriaFactory.getResourceLoader();
    }

    public GradebookService getGradebookService() {
        return criteriaFactory.getGradebookService();
    }

    public ToolManager getToolManager() {
        return criteriaFactory.getToolManager();
    }

    public String getVariableKey() {
        return KEY_GRADEBOOK_ITEM;
    }

    public String getVariableLabel() {
        return getResourceLoader().getString(getVariableKey());
    }

    public boolean isMultipleChoice() {
        return true;
    }

    public Map<String, String> getValues() {
        GradebookService gbs = getGradebookService();
        ToolManager tm = getToolManager();
        HashMap<String, String> items = new HashMap<>();
        String contextId = tm.getCurrentPlacement().getContext();

        if (!gbs.isGradebookDefined(contextId)) {
            return items;
        }

        List<Assignment> assignments = gbs.getAssignments(contextId);
        for (Assignment asn : assignments) {
            if (filter.include(asn)) {
                items.put(Long.toString(asn.getId()), labeler.getLabel(asn));
            }
        }

        return items;
    }

    public boolean isValid(String value) {
        return getValues().keySet().contains(value);
    }
}
