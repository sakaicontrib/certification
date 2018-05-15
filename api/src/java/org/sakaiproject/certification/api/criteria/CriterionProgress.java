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

/**
 * When reporting on a criterion, every item of reportable data can be represented by this class.
 * The progress String indicates the progress towards the criterion.
 * The met boolean indicates whether the datum represesnted by this class meets the criterion.
 *
 * An example: FinalGradeScoreCriterion - progress would be a String representation of
 * the user's final grade (ie. 89), and if the minimum score is 80, then met would be true
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CriterionProgress {

    private String progress = "";
    private boolean met = false;
}
