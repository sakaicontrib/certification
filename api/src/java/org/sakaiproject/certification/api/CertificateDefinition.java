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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.sakaiproject.certification.api.criteria.CriteriaFactory;
import org.sakaiproject.certification.api.criteria.Criterion;
import org.sakaiproject.certification.api.criteria.UnknownCriterionTypeException;

/**
 * CertificateDefinition represents the context and criteria for a certificate and maintains a DocumentTemplate that
 * can be used to render a printable version of the certificate.

 */
@Data
@EqualsAndHashCode(of = "id")
public class CertificateDefinition {

    /**
     * Integers to identify the fields in the certificate definition
     */
    public static final int FIELD_NAME = 1;
    public static final int FIELD_DESCRIPTION = 2;

    protected String id;
    protected String creatorUserId;
    protected String name;
    protected String description;
    protected String siteId;
    protected String expiryOffset;
    protected Date createDate;
    /**
     * The status of a CertificateDefinition is one of:
     *
     *      UNPUBLISHED - The CertificateDefinition has not yet been fully defined
     *      ACTIVE      - The CertificateDefinition is in use and can be used for awards
     *      INACTIVE    - The CertificateDefinition is not presently available for awards
     *
     * @return the current status
     */
    protected CertificateDefinitionStatus status = CertificateDefinitionStatus.UNPUBLISHED;
    protected Boolean hidden;
    protected DocumentTemplate documentTemplate;
    protected Map<String, String> fieldValues = new HashMap<>(0);
    protected Set<Criterion> awardCriteria = new HashSet<>();

    public void addAwardCriterion(Criterion criterion) {
        if (awardCriteria == null) {
            awardCriteria = new HashSet<>();
        }

        awardCriteria.add(criterion);
    }

    public Date getIssueDate(String userId, boolean useCaching) {
        if (awardCriteria.isEmpty()) {
            return null;
        }

        Iterator<Criterion> itAwardCriteria = awardCriteria.iterator();
        while (itAwardCriteria.hasNext()) {
            Criterion crit = itAwardCriteria.next();
            if (crit != null) {
                CriteriaFactory critFact = crit.getCriteriaFactory();
                if (critFact != null) {
                    return critFact.getDateIssued(userId, siteId, this, useCaching);
                }
            }
        }

        return null;
    }

    public boolean isAwarded(String userId, boolean useCaching) throws UnknownCriterionTypeException {
        Iterator<Criterion> itAwardCriteria = awardCriteria.iterator();
        boolean awarded = true;
        while (itAwardCriteria.hasNext()) {
            Criterion crit = itAwardCriteria.next();
            CriteriaFactory critFact = crit.getCriteriaFactory();
            if (!critFact.isCriterionMet(crit, userId, siteId, useCaching)) {
                awarded = false;
            }
        }

        return awarded;
    }

    public void setProgressShown(Boolean show) {
        if(show == null) {
            hidden = Boolean.TRUE;
        }

        this.hidden = !show;
    }

    public Boolean getProgressShown() {
        if(hidden == null) {
            return Boolean.FALSE;
        }

        return !hidden;
    }

    public void setProgressHidden(Boolean hidden) {
        if (hidden == null) {
            hidden = Boolean.FALSE;
        }

        this.hidden = hidden;
    }

    public Boolean getProgressHidden() {
        if (hidden == null) {
            hidden = Boolean.TRUE;
        }

        return hidden;
    }
}
