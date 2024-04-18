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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.certification.api.CertificateService;
import org.sakaiproject.certification.api.criteria.CriteriaFactory;
import org.sakaiproject.certification.api.criteria.CriteriaTemplate;
import org.sakaiproject.certification.api.criteria.CriteriaTemplateVariable;
import org.sakaiproject.certification.api.criteria.Criterion;
import org.sakaiproject.certification.api.criteria.gradebook.FinalGradeScoreCriterion;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class FinalGradeScoreCriteriaTemplate implements CriteriaTemplate {

    ScoreTemplateVariable scoreVariable = null;
    ArrayList<CriteriaTemplateVariable> variables = new ArrayList<>(1);
    GradebookCriteriaFactory factory = null;
    CertificateService certificateService = null;
    GradingService gradingService = null;
    ResourceLoader rl = null;

    private final String EXPRESSION_KEY = "final.grade.score.criteria.expression";
    private final String VARIABLE_SCORE = "score";

    // TODO: Once these are correct in GradingConstants, delete these
    public static final int CATEGORY_TYPE_NO_CATEGORY = 1;
    public static final int CATEGORY_TYPE_ONLY_CATEGORY = 2;
    public static final int CATEGORY_TYPE_WEIGHTED_CATEGORY = 3;

    public FinalGradeScoreCriteriaTemplate(final GradebookCriteriaFactory factory) {
        this.factory = factory;
        gradingService = factory.getGradingService();
        certificateService = factory.getCertificateService();

        scoreVariable = new ScoreTemplateVariable(VARIABLE_SCORE, factory);
        addVariable(scoreVariable);
    }

    public String getId() {
        return FinalGradeScoreCriteriaTemplate.class.getName();
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

    public String getExpression() {
        return getExpression(null);
    }

    public String getExpression (Criterion criterion) {
        if (criterion == null) {
            return rl.getFormattedMessage(EXPRESSION_KEY, new Object[]{});
        }

        SecureGradebookActionCallback typeCallback = () -> certificateService.getCategoryType(factory.contextId());
        SecureGradebookActionCallback assnPointsCallback = () -> certificateService.getAssignmentPoints(factory.contextId());
        SecureGradebookActionCallback catOnlyAssnPointsCallback = () -> certificateService.getCatOnlyAssignmentPoints(factory.contextId());

        Map<Long, Double> assnPoints;
        int categoryType;

        try {
            categoryType = (Integer) factory.doSecureGradebookAction(typeCallback);
            if(categoryType == CATEGORY_TYPE_ONLY_CATEGORY) {
                assnPoints = (Map<Long, Double>)factory.doSecureGradebookAction(catOnlyAssnPointsCallback);
            } else {
                assnPoints = (Map<Long, Double>)factory.doSecureGradebookAction(assnPointsCallback);
            }
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            return rl.getString("error.cannotEvaluate");
        }

        double total = 0;
        switch(categoryType) {
            case CATEGORY_TYPE_NO_CATEGORY:
            case CATEGORY_TYPE_WEIGHTED_CATEGORY:
            case CATEGORY_TYPE_ONLY_CATEGORY: {
                for (Double points : assnPoints.values()) {
                    total += points;
                }
                break;
            }
        }

        DecimalFormat df = new DecimalFormat("#0.00");
        Object vars[] = new String[2];

        vars[0] = df.format(total);

        FinalGradeScoreCriterion fgschi = (FinalGradeScoreCriterion) criterion;

        vars[1] = fgschi.getScore();

        return rl.getFormattedMessage(FinalGradeScoreCriteriaTemplate.class.getName(), vars);
    }

    @Override
    public String getMessage() {
        return "";
    }
}
