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

import java.util.Map;

import org.sakaiproject.certification.api.criteria.CriteriaTemplateVariable;
import org.sakaiproject.util.ResourceLoader;

public class ScoreTemplateVariable implements CriteriaTemplateVariable {

    private GradebookCriteriaFactory criteriaFactory = null;
    private String key = null;

    public ScoreTemplateVariable(String key, GradebookCriteriaFactory cFact) {
        criteriaFactory = cFact;
        setVariableKey(key);
    }

    public void setVariableKey(String key) {
        this.key = key;
    }

    public String getVariableKey() {
        return key;
    }

    public String getVariableLabel() {
        return getResourceLoader().getString(key);
    }

    public ResourceLoader getResourceLoader() {
        return criteriaFactory.getResourceLoader();
    }

    public boolean isMultipleChoice() {
        return false;
    }

    public Map<String, String> getValues() {
        return null;
    }

    public boolean isValid(String value) {
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }
}
