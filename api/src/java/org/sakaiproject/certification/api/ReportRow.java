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

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import org.sakaiproject.certification.api.criteria.CriterionProgress;

/**
 * Represents information pertaining to one user in the reporting interface
 */
 
@Data
public class ReportRow {

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
     */
    private List<CriterionProgress> criterionCells = new ArrayList<>();

    /**
     * Whether the user was awarded or not (ie. yes/no)
     */
    private String awarded = "";
}
