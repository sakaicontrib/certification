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

package org.sakaiproject.certification.api.criteria;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.sakaiproject.certification.api.CertificateService;

@Data
@EqualsAndHashCode(of = "id")
public abstract class AbstractCriterion implements Criterion
{
    private String id = null;
    private Map<String, String> variableBindings = new HashMap<>();
    private CriteriaFactory criteriaFactory = null;
    private CertificateService certificateService = null;

    public String getExpression()
    {
        String expression = null;

        try
        {
            expression = getCriteriaFactory().getCriteriaTemplate(this).getExpression(this);
        }
        catch (UnknownCriterionTypeException e)
        {
            //well now that would just be weird if my own CriteriaFactory was not able
            // to find the right CriteriaTemplate for my type, right?

            //I'm swallowing this exception.
        }

        return expression;
    }

    public String getCurrentCriteriaTemplate()
    {
        CriteriaTemplate criteriaTemplate = null;
        try
        {
            criteriaTemplate = getCriteriaFactory().getCriteriaTemplate(this);
        }
        catch (UnknownCriterionTypeException e)
        {
            // TODO Auto-generated catch block
        }

        return criteriaTemplate.getClass().getName();
    }
}