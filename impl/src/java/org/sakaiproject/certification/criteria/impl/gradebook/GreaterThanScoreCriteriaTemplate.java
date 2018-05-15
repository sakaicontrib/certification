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

import org.sakaiproject.certification.api.criteria.CriteriaFactory;
import org.sakaiproject.certification.api.criteria.Criterion;
import org.sakaiproject.certification.api.criteria.gradebook.GreaterThanScoreCriterion;
import org.sakaiproject.certification.impl.util.FormatHelper;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.util.ResourceLoader;

public class GreaterThanScoreCriteriaTemplate extends GradebookItemCriteriaTemplate {

    ScoreTemplateVariable scoreVariable = null;
    private final String EXPRESSION_KEY = "greater.than.score.criteria.expression";
    private static final String MESSAGE_POINT = "point";
    private static final String MESSAGE_POINTS = "points";
    private static final String MESSAGE_NOITEMS = "message.noitems.greaterthanscore";

    public GreaterThanScoreCriteriaTemplate(final GradebookCriteriaFactory factory) {
        super(factory, null, new AssignmentLabeler() {
            public String getLabel(Assignment assignment) {
                StringBuilder assnLabel = new StringBuilder();
                assnLabel.append(assignment.getName()).append(" (").append(FormatHelper.formatGradeForDisplay(assignment.getPoints().toString())).append(" ");
                ResourceLoader rl = factory.getResourceLoader();

                if (assignment.getPoints() == 1) {
                    assnLabel.append(rl.getString(MESSAGE_POINT));
                } else {
                    assnLabel.append(rl.getString(MESSAGE_POINTS));
                }

                assnLabel.append(')');
                return assnLabel.toString();
            }
        });

        scoreVariable =  new ScoreTemplateVariable(CriteriaFactory.KEY_SCORE, factory);
        addVariable(scoreVariable);
    }

    public String getId() {
        return GreaterThanScoreCriteriaTemplate.class.getName();
    }

    public String getExpression() {
        return getExpression(null);
    }

    public String getExpression (Criterion criterion) {
        if (criterion == null) {
            return rl.getFormattedMessage(EXPRESSION_KEY, new Object[]{});
        }

        Object vars[] = new String[2];

        GreaterThanScoreCriterion gischi = (GreaterThanScoreCriterion)criterion;

        String score = gischi.getScore();
        if (score != null) {
            Double dblScore = new Double(score);
            StringBuilder sbScore = new StringBuilder(FormatHelper.formatGradeForDisplay(score));
            if (dblScore == 1) {
                sbScore.append(" ").append(rl.getString(MESSAGE_POINT));
            } else {
                sbScore.append(" ").append(rl.getString(MESSAGE_POINTS));
            }
            score = sbScore.toString();
        }

        vars[0] = gischi.getItemName();
        vars[1] = score;

        return rl.getFormattedMessage(GreaterThanScoreCriteriaTemplate.class.getName(), vars);
    }

    @Override
    public String getMessage() {
        return getResourceLoader().getString(MESSAGE_NOITEMS);
    }
}
