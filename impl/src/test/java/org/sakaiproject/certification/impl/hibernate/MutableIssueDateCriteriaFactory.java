/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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

package org.sakaiproject.certification.impl.hibernate;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.certification.api.CertificateDefinition;
import org.sakaiproject.certification.api.criteria.CriteriaFactory;
import org.sakaiproject.certification.api.criteria.CriteriaTemplate;
import org.sakaiproject.certification.api.criteria.Criterion;
import org.sakaiproject.certification.api.criteria.CriterionCreationException;
import org.sakaiproject.certification.api.criteria.InvalidBindingException;
import org.sakaiproject.certification.api.criteria.UnknownCriterionTypeException;
import org.sakaiproject.certification.api.criteria.UserProgress;
import org.sakaiproject.certification.api.criteria.gradebook.DueDatePassedCriterion;

class MutableIssueDateCriteriaFactory implements CriteriaFactory {

    private Date issueDate;

    void setIssueDate(Date issueDate) {
        this.issueDate = issueDate == null ? null : new Date(issueDate.getTime());
    }

    @Override
    public Set<CriteriaTemplate> getCriteriaTemplates() {
        return Collections.emptySet();
    }

    @Override
    public CriteriaTemplate getCriteriaTemplate(String id) throws UnknownCriterionTypeException {
        throw new UnknownCriterionTypeException(id);
    }

    @Override
    public CriteriaTemplate getCriteriaTemplate(Criterion criterion) throws UnknownCriterionTypeException {
        throw new UnknownCriterionTypeException(criterion.getClass().getName());
    }

    @Override
    public Set<Class<? extends Criterion>> getCriterionTypes() {
        return Collections.singleton(DueDatePassedCriterion.class);
    }

    @Override
    public boolean isCriterionMet(Criterion criterion) {
        return issueDate != null;
    }

    @Override
    public boolean isCriterionMet(Criterion criterion, String userId, String contextId, boolean useCaching) {
        return issueDate != null;
    }

    @Override
    public Criterion createCriterion(CriteriaTemplate template, Map<String, String> bindings)
            throws InvalidBindingException, CriterionCreationException, UnknownCriterionTypeException {
        throw new UnknownCriterionTypeException(template.getClass().getName());
    }

    @Override
    public Double getScore(Long itemId, String userId, String contextId, boolean useCaching) {
        return null;
    }

    @Override
    public Double getFinalScore(String userId, String contextId) {
        return null;
    }

    @Override
    public Date getDateRecorded(Long itemId, String userId, String contextId, boolean useCaching) {
        return null;
    }

    @Override
    public Date getFinalGradeDateRecorded(String userId, String contextId) {
        return null;
    }

    @Override
    public Date getDateIssued(String userId, String contextId, CertificateDefinition certDef,
                              boolean useCaching) {
        return issueDate == null ? null : new Date(issueDate.getTime());
    }

    @Override
    public void clearCaches() {
    }

    @Override
    public Map<String, Map<Criterion, UserProgress>> getProgressForUsers(String contextId,
            List<String> userIds, Class type, List<Criterion> critCollection) {
        return Collections.emptyMap();
    }
}
