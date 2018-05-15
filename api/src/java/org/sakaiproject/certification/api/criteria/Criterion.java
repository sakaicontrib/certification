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

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Criterion {

    public String getId();

    public CriteriaFactory getCriteriaFactory();

    public String getCurrentCriteriaTemplate();

    public String getExpression();

    public Map<String, String> getVariableBindings();

    /**
     * Returns all the headers that should be displayed on the reporting interface for this criterion.
     * For example, if this is a FinalGradeScore criterion, it will return a list with one item,
     * namely "Final Course Grade"
     * @return
     */
    public List<String> getReportHeaders();

    /**
     * Returns all the cell data that should be displayed on the reporting interface for this criterion.
     * For example if this is a WillExpireCriterion, it will return a list with one item,
     * namely the date of expiry
     *
     * @param userId the user we are grabbing report data for
     * @param siteId the site containing this criterion
     * @param issueDate
     * @param useCaching
     * @return
     */
    public List<CriterionProgress> getReportData(String userId, String siteId, Date issueDate, boolean useCaching);

    /**
     * Returns the first date on which this criterion was met. For example, if this is a DueDatePassed criterion,
     * then the dateMet would be the gradebook item's due date, as this criterion is met as soon as the gradebook item's
     * due date has passed.
     * If this is a FinalGradeScore criterion, then the dateMet would be the last date recorded of the gradebook items
     * Returns null if the criterion is not met
     * @param userId
     * @param siteId
     * @param useCaching
     * @return
     */
    public Date getDateMet(String userId, String siteId, boolean useCaching);

    /**
     * Returns a message for the UI to indicate the specified user's progress towards meeting this criterion.
     * For example, if this is a GreaterThanScore criterion, the user's progress would be their current score
     * on this criterion's gradebook item.
     * Returns "" if progress is undefined (ie. WillExpire)
     * @param userId
     * @param siteId
     * @param useCaching
     * @return
     */
    public String getProgress(String userId, String siteId, boolean useCaching);
}