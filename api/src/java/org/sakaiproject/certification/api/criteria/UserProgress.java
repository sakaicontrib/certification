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

/**
 * Represents information regarding a user's progress toward a criterion
 */
public class UserProgress {

    private String userId = null;
    private Criterion criterion = null;
    private String progress = null;
    private boolean passed = false;
    private Date dateAwarded = null;

    public UserProgress() {
    }

    public UserProgress(String userId, Criterion criterion, String progress, boolean passed, Date dateAwarded) {
        this.userId = userId;
        this.criterion = criterion;
        this.progress = progress;
        this.passed = passed;
        this.dateAwarded = dateAwarded;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Criterion getCriterion() {
        return criterion;
    }

    public void setCriterion(Criterion criterion) {
        this.criterion = criterion;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public Date getDateAwarded() {
        return dateAwarded;
    }

    public void setDateAwarded(Date dateAwarded) {
        this.dateAwarded = dateAwarded;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("UserProgress with UserId: ");
        sb.append(userId)
            .append(" Criterion: ").append(criterion)
            .append(" Progress: ").append(progress)
            .append(" Passed: ").append(passed)
            .append(" DateAwarded: ").append(dateAwarded);
        return sb.toString();
    }
}
