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

import java.util.Set;

import org.sakaiproject.certification.api.criteria.Criterion;

public class UnmetCriteriaException extends CertificationException {

    private Set<Criterion> unmetCriteria = null;

    public UnmetCriteriaException () {
        super ();
    }

    public UnmetCriteriaException(String message) {
        super(message);
    }

    public UnmetCriteriaException(String message, Throwable t) {
        super(message, t);
    }

    public UnmetCriteriaException(Throwable t) {
        super(t);
    }

    public void setUnmetCriteria (Set<Criterion> criteria) {
        unmetCriteria = criteria;
    }

    public Set<Criterion> getUnmetConditions () {
        return unmetCriteria;
    }
}
