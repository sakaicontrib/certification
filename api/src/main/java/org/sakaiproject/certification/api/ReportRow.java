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

package org.sakaiproject.certification.api;

import org.sakaiproject.certification.api.criteria.CriterionProgress;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents information pertaining to one user in the reporting interface
 */
public class ReportRow
{
    /**
     * The user's name (formatted as "lastname, firstname")
     */
    private String name = "";

    /**
     * The user's display id
     */
    private String userId = "";

    /**
     * The user's site role
     */
    private String role = "";

    /**
     * The date the user was issued the certificate
     */
    private String issueDate = "";

    /**
     * The user's student number
     */
    private String studentNumber = "";

    /**
     * Cells representing the user's progress toward the criteria
     **/
    private List<CriterionProgress> criterionCells = new ArrayList<>();

    /**
     * Whether the user was awarded or not (ie. yes/no)
     **/
    private String awarded = "";

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public String getRole()
    {
        return role;
    }

    public void setStudentNumber(String studentNumber)
    {
        this.studentNumber = studentNumber;
    }

    public String getStudentNumber()
    {
        return studentNumber;
    }

    public void setIssueDate(String issueDate)
    {
        this.issueDate = issueDate;
    }

    public String getIssueDate()
    {
        return issueDate;
    }

    public void setCriterionCells(List<CriterionProgress> criterionCells)
    {
        this.criterionCells = criterionCells;
    }

    public List<CriterionProgress> getCriterionCells()
    {
        return criterionCells;
    }

    public void setAwarded(String awarded)
    {
        this.awarded = awarded;
    }

    public String getAwarded()
    {
        return awarded;
    }
}
