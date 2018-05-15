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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.sakaiproject.certification.api.criteria.Criterion;
import org.sakaiproject.certification.api.criteria.gradebook.DueDatePassedCriterion;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.util.ResourceLoader;

public class DueDatePassedCriteriaTemplate extends GradebookItemCriteriaTemplate {

    private static final String MESSAGE_DUEDATE = "duedate";
    private static final String MESSAGE_DUEDATE_NONE = "duedate.none";
    private static final String MESSAGE_NOITEMS_DUEDATE = "message.noitems.duedate";

    private final String EXPRESSION_KEY = "due.date.has.passed.criteria.expression";

    public DueDatePassedCriteriaTemplate(final GradebookCriteriaFactory factory) {
        super(factory,
            new AssignmentFilter() {
                public boolean include(Assignment assignment) {
                    return assignment.getDueDate() != null;
                }
            },
            new AssignmentLabeler() {
                public String getLabel(Assignment assignment) {
                    StringBuilder assnLabel = new StringBuilder();
                    ResourceLoader rl = factory.getResourceLoader();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date due = assignment.getDueDate();
                    String dateStr;

                    if (due != null)
                    {
                        dateStr = rl.getFormattedMessage(MESSAGE_DUEDATE, new Object[] { sdf.format(due) });
                    }
                    else
                    {
                        dateStr = rl.getString(MESSAGE_DUEDATE_NONE);
                    }

                    assnLabel.append(assignment.getName()).append(" (").append(dateStr).append(')');
                    return assnLabel.toString();
                }
            }
        );
    }

    public String getId() {
        return DueDatePassedCriteriaTemplate.class.getName();
    }

    public String getExpression() {
        return getExpression(null);
    }

    public String getExpression (Criterion criterion) {
        if (criterion == null) {
            return getResourceLoader().getFormattedMessage(EXPRESSION_KEY, new Object[]{});
        }

        Object vars[] = new String[2];
        DueDatePassedCriterion gischi = (DueDatePassedCriterion)criterion;

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, getResourceLoader().getLocale());
        vars[0] = gischi.getItemName();
        vars[1] = dateFormat.format(gischi.getDueDate());

        return getResourceLoader().getFormattedMessage(DueDatePassedCriteriaTemplate.class.getName(), vars);
    }

    @Override
    public String getMessage() {
        return getResourceLoader().getString(MESSAGE_NOITEMS_DUEDATE);
    }
}
