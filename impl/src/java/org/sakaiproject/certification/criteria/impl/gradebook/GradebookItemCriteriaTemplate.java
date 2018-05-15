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

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.certification.api.criteria.CriteriaFactory;
import org.sakaiproject.certification.api.criteria.CriteriaTemplate;
import org.sakaiproject.certification.api.criteria.CriteriaTemplateVariable;
import org.sakaiproject.util.ResourceLoader;

public abstract class GradebookItemCriteriaTemplate implements CriteriaTemplate {

    GradebookItemTemplateVariable itemVariable = null;
    ArrayList<CriteriaTemplateVariable> variables = new ArrayList<>(1);
    GradebookCriteriaFactory factory = null;
    ResourceLoader rl = null;

    public GradebookItemCriteriaTemplate(GradebookCriteriaFactory factory, AssignmentFilter filter, AssignmentLabeler labeler) {
        this.factory = factory;
        itemVariable = new GradebookItemTemplateVariable(factory, filter, labeler);
        addVariable(itemVariable);
    }

    protected void addVariable (CriteriaTemplateVariable variable) {
        variables.add(variable);
    }

    public void setResourceLoader (ResourceLoader rl) {
        this.rl = rl;
    }

    public ResourceLoader getResourceLoader() {
        return rl;
    }

    public CriteriaFactory getCriteriaFactory() {
        return factory;
    }

    public int getTemplateVariableCount() {
        return variables.size();
    }

    public List<CriteriaTemplateVariable> getTemplateVariables() {
        return variables;
    }

    public CriteriaTemplateVariable getTemplateVariable(int i) {
        return variables.get(i);
    }
}
