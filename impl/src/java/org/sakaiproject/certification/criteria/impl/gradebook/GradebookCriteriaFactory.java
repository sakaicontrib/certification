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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.certification.api.CertificateDefinition;
import org.sakaiproject.certification.api.CertificateService;
import org.sakaiproject.certification.api.criteria.CriteriaFactory;
import org.sakaiproject.certification.api.criteria.CriteriaTemplate;
import org.sakaiproject.certification.api.criteria.CriteriaTemplateVariable;
import org.sakaiproject.certification.api.criteria.Criterion;
import org.sakaiproject.certification.api.criteria.CriterionCreationException;
import org.sakaiproject.certification.api.criteria.InvalidBindingException;
import org.sakaiproject.certification.api.criteria.UnknownCriterionTypeException;
import org.sakaiproject.certification.api.criteria.UserProgress;
import org.sakaiproject.certification.api.criteria.gradebook.DueDatePassedCriterion;
import org.sakaiproject.certification.api.criteria.gradebook.FinalGradeScoreCriterion;
import org.sakaiproject.certification.api.criteria.gradebook.GreaterThanScoreCriterion;
import org.sakaiproject.certification.api.criteria.gradebook.WillExpireCriterion;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.certification.impl.util.FormatHelper;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class GradebookCriteriaFactory implements CriteriaFactory {

    private CertificateService certService = null;
    private GradebookService gbService = null;
    private ToolManager toolManager = null;
    private UserDirectoryService userDirectoryService = null;
    private SecurityService securityService = null;
    private SessionManager sessionManager = null;

    private final HashMap<String, CriteriaTemplate> criteriaTemplates = new HashMap<>();
    private final HashSet<Class<? extends Criterion>> criterionClasses = new HashSet<>();

    private GreaterThanScoreCriteriaTemplate gbItemScoreTemplate = null;
    private DueDatePassedCriteriaTemplate gbDueDatePassedTemplate = null;
    private FinalGradeScoreCriteriaTemplate gbFinalGradeScoreTemplate = null;
    private WillExpireCriteriaTemplate gbWillExpireTemplate = null;

    //Caces a map of gradebook item ids to gradebook items so they don't have to be queried multiple times (speeds up performance). Stored as map(gradebook id, gradebook item)
    private final Map<Long, Assignment> cachedAssignments = new HashMap<>();

    //Caches grades. Stored as map(gradebook item ids, map(user ids, grades))
    private final Map<Long, Map<String, Double>> itemToUserToGradeMap = new HashMap<>();
    private final Map<Long, Map<String, Date>> itemToUserToDateRecordedMap = new HashMap<>();

    private ResourceLoader resourceLoader = null;

    private static final String PERM_VIEW_OWN_GRADES = "gradebook.viewOwnGrades";
    private static final String PERM_EDIT_ASSIGNMENTS = "gradebook.editAssignments";

    private static final String ERROR_NO_GRADEBOOK = "value.noGradebook";
    private static final String ERROR_EMPTY_GRADEBOOK = "value.emptyGradebook";
    private static final String ERROR_NO_DUE_DATES = "value.noDueDates";
    private static final String ERROR_MIN_REQUIRED = "value.minRequired";
    private static final String ERROR_EXPIRY_OFFSET_REQUIRED = "value.expiryOffsetRequired";
    private static final String ERROR_NAN = "value.notanumber";
    private static final String ERROR_WHOLE_NUMBER_REQUIRED = "value.wholeNumberRequired";
    private static final String ERROR_NEGATIVE_NUMBER = "value.negativenumber";
    private static final String ERROR_TOO_HIGH = "value.toohigh";

    public void init() {
        gbItemScoreTemplate = new GreaterThanScoreCriteriaTemplate(this);
        gbItemScoreTemplate.setResourceLoader(resourceLoader);

        gbFinalGradeScoreTemplate = new FinalGradeScoreCriteriaTemplate(this);
        gbFinalGradeScoreTemplate.setResourceLoader(resourceLoader);

        gbDueDatePassedTemplate = new DueDatePassedCriteriaTemplate(this);
        gbDueDatePassedTemplate.setResourceLoader(resourceLoader);

        gbFinalGradeScoreTemplate = new FinalGradeScoreCriteriaTemplate(this);
        gbFinalGradeScoreTemplate.setResourceLoader(resourceLoader);

        gbWillExpireTemplate = new WillExpireCriteriaTemplate(this);
        gbWillExpireTemplate.setResourceLoader(resourceLoader);

        criteriaTemplates.put(gbItemScoreTemplate.getId(), gbItemScoreTemplate);
        criteriaTemplates.put(gbDueDatePassedTemplate.getId(), gbDueDatePassedTemplate);
        criteriaTemplates.put(gbFinalGradeScoreTemplate.getId(), gbFinalGradeScoreTemplate);
        criteriaTemplates.put(gbWillExpireTemplate.getId(), gbWillExpireTemplate);

        criterionClasses.add(GreaterThanScoreCriterion.class);
        criterionClasses.add(DueDatePassedCriterion.class);
        criterionClasses.add(FinalGradeScoreCriterion.class);
        criterionClasses.add(WillExpireCriterion.class);

        if (certService != null) {
            certService.registerCriteriaFactory(this);
        }
    }

    public CertificateService getCertificateService() {
        return certService;
    }

    public void setCertificateService(CertificateService certService) {
        this.certService = certService;
    }

    public void setGradebookService(GradebookService gbs) {
        gbService = gbs;
    }

    public GradebookService getGradebookService() {
        return gbService;
    }

    public void setToolManager(ToolManager tm) {
        toolManager = tm;
    }

    public ToolManager getToolManager() {
        return toolManager;
    }

    public void setUserDirectoryService(UserDirectoryService uds) {
        userDirectoryService = uds;
    }

    public UserDirectoryService getUserDirectoryService() {
        return userDirectoryService;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    protected final String contextId() {
        return getToolManager().getCurrentPlacement().getContext();
    }

    protected final String userId() {
        return getUserDirectoryService().getCurrentUser().getId();
    }

    public Set<CriteriaTemplate> getCriteriaTemplates() {
        HashSet<CriteriaTemplate> values = new HashSet<>();
        values.addAll(criteriaTemplates.values());
        return values;
    }

    public CriteriaTemplate getCriteriaTemplate(Criterion criterion) throws UnknownCriterionTypeException {
        if (GreaterThanScoreCriterion.class.isAssignableFrom(criterion.getClass())) {
            return gbItemScoreTemplate;

        } else if (FinalGradeScoreCriterion.class.isAssignableFrom(criterion.getClass())) {
            return gbFinalGradeScoreTemplate;

        } else if (DueDatePassedCriterion.class.isAssignableFrom(criterion.getClass())) {
            return gbDueDatePassedTemplate;

        } else if (WillExpireCriterion.class.isAssignableFrom(criterion.getClass())) {
            return gbWillExpireTemplate;
        }

        throw new UnknownCriterionTypeException(criterion.getClass().getName());
    }

    public Set<Class<? extends Criterion>> getCriterionTypes() {
        return criterionClasses;
    }

    public boolean isCriterionMet(Criterion criterion) throws UnknownCriterionTypeException {
        if (!criterionClasses.contains(criterion.getClass())) {
            throw new UnknownCriterionTypeException(criterion.getClass().getName());
        }

        return isCriterionMet(criterion, userId(), contextId(), false);
    }

    protected Object doSecureGradebookAction(SecureGradebookActionCallback callback) throws Exception {
        final String contextId = contextId();

        SecurityAdvisor yesMan = new SecurityAdvisor() {
            public SecurityAdvice isAllowed(String userId, String function, String reference) {
                String compTo;
                if (contextId.startsWith("/site/")) {
                    compTo = contextId;
                } else {
                    compTo = "/site/" + contextId;
                }

                if (reference.equals(compTo) && (PERM_VIEW_OWN_GRADES.equals(function) ||
                                                 PERM_EDIT_ASSIGNMENTS.equals(function))) {
                    return SecurityAdvice.ALLOWED;
                } else {
                    return SecurityAdvice.PASS;
                }
            }
        };

        try {
            securityService.pushAdvisor(yesMan);
            return callback.doSecureAction();
        } finally {
            securityService.popAdvisor();
        }
    }

    public boolean isCriterionMet(final Criterion criterion, final String userId, final String contextId, final boolean useCaching)
        throws UnknownCriterionTypeException {
        if (!criterionClasses.contains(criterion.getClass())) {
            throw new UnknownCriterionTypeException (criterion.getClass().getName());
        }

        if (GreaterThanScoreCriterion.class.isAssignableFrom(criterion.getClass())) {
            GreaterThanScoreCriterion gischi = (GreaterThanScoreCriterion)criterion;
            final GradebookService gbs = getGradebookService();
            final Long itemId = gischi.getItemId();

            if (itemId == null) {
                log.error("isCriterionMet called on GreaterThanScoreCriterion whose gradebook itemId is null");
                return false;
            }

            Double score = null;
            //grab from the cache if available
            Map<String, Double> userToGradeMap;
            boolean cached = false;
            if (useCaching) {
                userToGradeMap = itemToUserToGradeMap.get(itemId);
                if (userToGradeMap != null && userToGradeMap.containsKey(userId)) {
                    cached = true;
                    score = userToGradeMap.get(userId);
                } else {
                    userToGradeMap = new HashMap<>();
                    itemToUserToGradeMap.put(itemId, userToGradeMap);
                }
            }

            if (!useCaching || !cached) {
                try {
                    score = (Double) doSecureGradebookAction (new SecureGradebookActionCallback() {
                        public Object doSecureAction() {
                            // pull the assignment from the gradebook to check the score
                            Assignment assn = cachedAssignments.get(itemId);
                            if (assn == null) {
                                assn = gbs.getAssignment(contextId, itemId);
                                if (useCaching) {
                                    cachedAssignments.put(itemId, assn);
                                }
                            }

                            if (assn == null) {
                                log.error("isCriterionMet couldn't retrieve the assignment; itemId = {}", itemId);

                                //if we returned null, the cache would think we missed this entry. -1 means we hit the entry, but there's no grade
                                return null;

                            } else if (!assn.isReleased()) {
                                return null;
                            }

                            String assignmentScoreString = gbs.getAssignmentScoreString (contextId, itemId, userId);
                            if (assignmentScoreString == null) {
                                return null;
                            }

                            return Double.parseDouble(FormatHelper.inputStringToFormatString(assignmentScoreString));
                        }
                    });

                } catch (Exception e) {
                    log.error("isCriterionMet on GreatherThanScoreCriterion - An exception was thrown while retrieving {}'s score for itemId: {}.", userId, itemId);
                    return false;
                }

                if (useCaching) {
                    //cache the score
                    itemToUserToGradeMap.get(itemId).put(userId, score);
                }
            }

            return (score != null && score >= Double.parseDouble(gischi.getScore()));

        } else if (FinalGradeScoreCriterion.class.isAssignableFrom(criterion.getClass())) {
            FinalGradeScoreCriterion fgschi = (FinalGradeScoreCriterion) criterion;
            double score;

            try {
                score = (Double) doSecureGradebookAction(new SecureGradebookActionCallback() {
                    public Object doSecureAction() {
                        //get gradebook for the site
                        //check category type
                        // if category type is CATEGORY_TYPE_WEIGHTED_CATEGORY than it is weighted category
                        //loop through category definitions
                        //get assignments for each category and multiply weight of category to weight of assignment to possible points

                        //if category type is CATEGORY_TYPE_NO_CATEGORY it does not have category
                        //get all assignments and add possible points

                        //if category type is CATEGORY_TYPE_ONLY_CATEGORY than loop through category definitions
                        //get assignments for each category and add assignments possible points

                        Map<Long,Double> catWeights = certService.getCategoryWeights(contextId);
                        Map<Long,Double> assgnWeights = certService.getAssignmentWeights(contextId);
                        Map<Long,Double> assgnScores = certService.getAssignmentScores(contextId, userId);
                        Map<Long,Double> assgnPoints = certService.getAssignmentPoints(contextId);

                        double studentTotalScore = 0;
                        int categoryType = certService.getCategoryType(contextId);

                        switch(categoryType) {
                            case GradebookService.CATEGORY_TYPE_NO_CATEGORY: {
                                for(Map.Entry<Long, Double> assgnScore : assgnScores.entrySet()) {
                                    Double score = assgnScore.getValue();
                                    studentTotalScore += score == null ? 0:score;
                                }

                                break;
                            }
                            case GradebookService.CATEGORY_TYPE_ONLY_CATEGORY: {
                                for(Map.Entry<Long, Double> assgnScore : assgnScores.entrySet()) {
                                    if(catWeights.containsKey(assgnScore.getKey())) {
                                        Double score = assgnScore.getValue();
                                        studentTotalScore += score == null ? 0:score;
                                    }
                                }

                                break;
                            }
                            case GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY: {
                                for(Map.Entry<Long, Double> assgnScore : assgnScores.entrySet()) {
                                    if(catWeights.containsKey(assgnScore.getKey())) {
                                        Double score = assgnScore.getValue(),
                                               points = assgnPoints.get(assgnScore.getKey()),
                                               catWeight = catWeights.get(assgnScore.getKey()),
                                               assgnWeight = assgnWeights.get(assgnScore.getKey());

                                        studentTotalScore += 100 * (((score == null) ? 0 : score) /
                                                             ((points == null) ? 1 : points)) *
                                                             ((catWeight == null ? 1 : catWeight)) *
                                                             ((assgnWeight == null ? 1 : assgnWeight));
                                    }
                                }

                                break;
                            }
                        }

                        return studentTotalScore;
                    }
                });

            } catch (Exception e) {
                log.error("isCriterionMet - Exception thrown while retrieving the final course grade for {} in contextId: {}.", userId, contextId);
                return false;
            }

            return (score >= Double.parseDouble(fgschi.getScore()));

        } else if (DueDatePassedCriterion.class.isAssignableFrom(criterion.getClass())) {
            DueDatePassedCriterion ddpchi = (DueDatePassedCriterion)criterion;
            final GradebookService gbs = getGradebookService();
            final Long itemId = ddpchi.getItemId();

            if (itemId == null) {
                log.error("isCriterionMet called with DueDatePassedCriterion where itemId is null");
                return false;
            }

            Assignment assn;
            try {
                assn = (Assignment) doSecureGradebookAction(() -> gbs.getAssignment(contextId, itemId));
            } catch (Exception e) {
                log.error("isCriterionMet on DueDatePassedCriterion - An exception was thrown while retrieving a gradebook item; itemId: {}.", itemId);
                return false;
            }

            return (assn != null && (new Date()).compareTo(assn.getDueDate()) > 0);

        } else if (WillExpireCriterion.class.isAssignableFrom(criterion.getClass())) {
            //we don't want to enforce this one
            return true;

        } else {
            throw new UnknownCriterionTypeException(criterion.getClass().getName());
        }
    }

    public Criterion createCriterion(CriteriaTemplate template, Map<String, String> bindings)
            throws InvalidBindingException, CriterionCreationException, UnknownCriterionTypeException {
        List<CriteriaTemplateVariable> variables = template.getTemplateVariables();
        final ResourceLoader rl = getResourceLoader();
        GradebookService gbs = getGradebookService();
        String contextId = getToolManager().getCurrentPlacement().getContext();

        for (CriteriaTemplateVariable variable : variables) {
            String value = bindings.get(variable.getVariableKey());
            if (variable instanceof ScoreTemplateVariable) {
                value = FormatHelper.inputStringToFormatString(value);
            }

            if (value == null || !variable.isValid(value)) {
                if (template instanceof DueDatePassedCriteriaTemplate) {
                    InvalidBindingException ibe = new InvalidBindingException();
                    ibe.setBindingKey(variable.getVariableKey());
                    ibe.setBindingValue(value);

                    if (!gbs.isGradebookDefined(contextId)) {
                        //This site does not have a gradebook
                        ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_NO_GRADEBOOK, new Object[] {value}));
                        throw ibe;

                    } else if (gbs.getAssignments(contextId).isEmpty()) {
                        //This is an empty gradebook
                        ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_EMPTY_GRADEBOOK, new Object[] {value}));
                        throw ibe;

                    } else {
                        //This gradebook is not empty, but there are no items with due dates
                        ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_NO_DUE_DATES, new Object[] {value}));
                        throw ibe;
                    }

                } else if (variable.getVariableKey().equals(KEY_SCORE)) {
                    if (StringUtils.isNotEmpty(value)) {
                        try {
                            Double.parseDouble(value);
                        } catch (NumberFormatException nfe) {
                            InvalidBindingException ibe = new InvalidBindingException();
                            ibe.setBindingKey(KEY_SCORE);
                            ibe.setBindingValue(value);
                            ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_NAN, new Object[] {value}));
                            throw ibe;
                        }
                    }

                    InvalidBindingException ibe = new InvalidBindingException ();
                    ibe.setBindingKey(variable.getVariableKey());
                    ibe.setBindingValue(value);
                    ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_MIN_REQUIRED, new Object[] {value}));
                    throw ibe;

                } else if (variable.getVariableKey().equals(KEY_EXPIRY_OFFSET)) {
                    if (StringUtils.isNotEmpty(value)) {
                        try {
                            Double.parseDouble(value);
                        } catch (NumberFormatException nfe) {
                            InvalidBindingException ibe = new InvalidBindingException();
                            ibe.setBindingKey(KEY_EXPIRY_OFFSET);
                            ibe.setBindingValue(value);
                            ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_NAN, new Object[] {value}));
                            throw ibe;
                        }

                        try {
                            Integer.parseInt(value);
                        } catch (NumberFormatException nfe) {
                            InvalidBindingException ibe = new InvalidBindingException();
                            ibe.setBindingKey(KEY_EXPIRY_OFFSET);
                            ibe.setBindingValue(value);
                            ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_WHOLE_NUMBER_REQUIRED, new Object[] {value}));
                            throw ibe;
                        }
                    }

                    InvalidBindingException ibe = new InvalidBindingException ();
                    ibe.setBindingKey(variable.getVariableKey());
                    ibe.setBindingValue(value);
                    ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_EXPIRY_OFFSET_REQUIRED, new Object[] {value}));
                    throw ibe;

                } else if (!gbs.isGradebookDefined(contextId)) {
                    //This site does not have a gradebook
                    InvalidBindingException ibe = new InvalidBindingException ();
                    ibe.setBindingKey(variable.getVariableKey());
                    ibe.setBindingValue(value);
                    ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_NO_GRADEBOOK, new Object[] {value}));
                    throw ibe;

                } else {
                    InvalidBindingException ibe = new InvalidBindingException ();
                    ibe.setBindingKey(variable.getVariableKey());
                    ibe.setBindingValue(value);
                    ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_EMPTY_GRADEBOOK, new Object[] {value}));
                    throw ibe;
                }
            }
        }

        if (GreaterThanScoreCriteriaTemplate.class.isAssignableFrom(template.getClass())) {
            GreaterThanScoreCriterion criterion = new GreaterThanScoreCriterion();
            criterion.setCriteriaFactory(this);
            Long itemId = new Long(bindings.get(KEY_GRADEBOOK_ITEM));
            Assignment assn = gbs.getAssignment(contextId, itemId);
            String scoreStr = FormatHelper.inputStringToFormatString(bindings.get(KEY_SCORE));

            criterion.setAssignment(assn);

            double score = -1;

            try {
                score = Double.parseDouble(scoreStr);
            } catch (NumberFormatException nfe) {
                InvalidBindingException ibe = new InvalidBindingException();
                ibe.setBindingKey(KEY_SCORE);
                ibe.setBindingValue(scoreStr);
                ibe.setLocalizedMessage (rl.getFormattedMessage(ERROR_NAN, new Object[] {scoreStr} ));
                throw ibe;
            }

            if (score < 0) {
                InvalidBindingException ibe = new InvalidBindingException();
                ibe.setBindingKey(KEY_SCORE);
                ibe.setBindingValue(scoreStr);
                ibe.setLocalizedMessage (rl.getFormattedMessage(ERROR_NEGATIVE_NUMBER, new Object[] {scoreStr}));
                throw ibe;
            }

            if (score > assn.getPoints()) {
                InvalidBindingException ibe = new InvalidBindingException("" + assn.getPoints());
                ibe.setBindingKey(KEY_SCORE);
                ibe.setBindingValue(scoreStr);

                if (assn.getPoints()==0) {
                    ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_EMPTY_GRADEBOOK, new Object[] {scoreStr} ));
                } else {
                    ibe.setLocalizedMessage (rl.getFormattedMessage(ERROR_TOO_HIGH, new Object[] {scoreStr}));
                }

                throw ibe;
            }

            criterion.setScore(scoreStr);
            criterion.setId(Long.toString(System.currentTimeMillis()));
            return criterion;

        } else if (FinalGradeScoreCriteriaTemplate.class.isAssignableFrom(template.getClass())) {
            if (!gbs.isGradebookDefined(contextId)) {
                //This site does not have a gradebook
                InvalidBindingException ibe = new InvalidBindingException ();
                ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_EMPTY_GRADEBOOK, new Object[] {} ));
                throw ibe;
            }

            FinalGradeScoreCriterion criterion = new FinalGradeScoreCriterion();
            criterion.setCriteriaFactory(this);
            String scoreStr = FormatHelper.inputStringToFormatString(bindings.get(KEY_SCORE));

            Map<Long,Double> catWeights = certService.getCategoryWeights(contextId);
            Map<Long,Double> assgnPoints = certService.getAssignmentPoints(contextId);

            double totalAvailable = 0;

            int categoryType = certService.getCategoryType(contextId);

            switch(categoryType) {
                case GradebookService.CATEGORY_TYPE_NO_CATEGORY: {
                    for(Map.Entry<Long, Double> assgnPoint : assgnPoints.entrySet()) {
                        Double point = assgnPoint.getValue();
                        totalAvailable += point == null ? 0:point;
                    }
                    break;
                }
                case GradebookService.CATEGORY_TYPE_ONLY_CATEGORY: {
                    for(Map.Entry<Long, Double> assgnPoint : assgnPoints.entrySet()) {
                        if(catWeights.containsKey(assgnPoint.getKey())) {
                            Double point = assgnPoint.getValue();
                            totalAvailable += point == null ? 0:point;
                        }
                    }
                    break;
                }
                case GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY: {
                    totalAvailable = 100;
                    break;
                }
            }

            double score = -1;
            InvalidBindingException ibe = new InvalidBindingException();
            ibe.setBindingKey(KEY_SCORE);
            ibe.setBindingValue(scoreStr);

            try {
                score = Double.parseDouble(scoreStr);
            } catch (NumberFormatException nfe) {
                ibe.setLocalizedMessage (rl.getFormattedMessage(ERROR_NAN, new Object[] {scoreStr}));
                throw ibe;
            }

            if (score < 0) {
                ibe.setLocalizedMessage (rl.getFormattedMessage(ERROR_NEGATIVE_NUMBER, new Object[] {scoreStr}));
                throw ibe;
            }

            if (score > totalAvailable) {
                ibe.setLocalizedMessage (rl.getFormattedMessage(ERROR_TOO_HIGH, new Object[] {scoreStr}));
                throw ibe;
            }

            criterion.setScore(scoreStr);
            criterion.setId(Long.toString(System.currentTimeMillis()));
            return criterion;

        } else if (DueDatePassedCriteriaTemplate.class.isAssignableFrom(template.getClass())) {
            DueDatePassedCriterion criterion = new DueDatePassedCriterion();
            criterion.setCriteriaFactory(this);
            Long itemId = new Long(bindings.get(KEY_GRADEBOOK_ITEM));
            Assignment assn = gbs.getAssignment(contextId, itemId);
            criterion.setId(Long.toString(System.currentTimeMillis()));
            criterion.setAssignment(assn);
            return criterion;

        } else if (WillExpireCriteriaTemplate.class.isAssignableFrom(template.getClass())) {
            WillExpireCriterion criterion = new WillExpireCriterion();
            criterion.setCriteriaFactory(this);
            criterion.setId(Long.toString(System.currentTimeMillis()));
            String strExpiryOffset = bindings.get(KEY_EXPIRY_OFFSET);

            InvalidBindingException ibe = new InvalidBindingException();
            ibe.setBindingKey(KEY_EXPIRY_OFFSET);
            ibe.setBindingValue(strExpiryOffset);
            int expiryOffset = -1;

            try {
                expiryOffset = Integer.parseInt(strExpiryOffset);
            } catch (NumberFormatException e) {
                ibe.setLocalizedMessage (rl.getFormattedMessage(ERROR_NAN, new Object[] {strExpiryOffset} ));
                throw ibe;
            }

            if (expiryOffset < 0) {
                ibe.setLocalizedMessage (rl.getFormattedMessage(ERROR_NEGATIVE_NUMBER, new Object[] {strExpiryOffset} ));
                throw ibe;
            }

            criterion.setExpiryOffset(strExpiryOffset);
            return criterion;
        }
        throw new UnknownCriterionTypeException (template.getClass().getName());
    }

    public CriteriaTemplate getCriteriaTemplate(String id) throws UnknownCriterionTypeException {
        CriteriaTemplate template = criteriaTemplates.get(id);
        if (template == null) {
            throw new UnknownCriterionTypeException (id);
        }

        return template;
    }

    public Double getScore(final Long itemId, final String userId, final String contextId, boolean useCaching) throws NumberFormatException {
        //grab from the cache if available
        Map<String, Double> userToGradeMap = null;
        if (useCaching) {
            userToGradeMap = itemToUserToGradeMap.get(itemId);
            if (userToGradeMap != null && userToGradeMap.containsKey(userId)) {
                return userToGradeMap.get(userId);
            } else {
                userToGradeMap = new HashMap<>();
                itemToUserToGradeMap.put(itemId, userToGradeMap);
            }
        }

        final GradebookService gbs = getGradebookService();

        try {
            Double score = (Double) doSecureGradebookAction (new SecureGradebookActionCallback() {
                public Object doSecureAction() {
                    // pull the assignment from the gradebook to check the score
                    Assignment assn = gbs.getAssignment(contextId, itemId);
                    if (assn == null || !assn.isReleased()) {
                        log.error("getScore - could not retrieve assignment for {}; itemId: {}", userId, itemId);
                        return null;
                    }

                    return Double.parseDouble(FormatHelper.inputStringToFormatString(gbs.getAssignmentScoreString(contextId, itemId, userId)));
                }
            });

            if (useCaching) {
                userToGradeMap.put(userId, score);
            }

            return score;

        } catch (Exception e) {
            log.error("getScore - an exception occurred while retrieving the score for {}; itemId: {}.", userId, itemId);
            return null;
        }
    }

    public Double getFinalScore(final String userId, final String contextId) throws NumberFormatException {
        try {
            return (Double)doSecureGradebookAction(new SecureGradebookActionCallback() {
                public Object doSecureAction() {
                    GradebookService gs = getGradebookService();
                    CourseGrade courseGrade = gs.getCourseGradeForStudent( contextId, userId );

                    /* The certification tool works with points and not with percentages */
                    /*
                    String calculatedGrade = courseGrade.getCalculatedGrade();

                    // If the string is null or empty, infer this as a zero
                    if( StringUtils.isBlank( calculatedGrade ) ) {
                        return 0.0;
                    }

                    // Otherwise, parse the string to a Double leaving exceptions to be caught by the caller
                    else {
                        return Double.parseDouble( calculatedGrade );
                    }*/

                    return courseGrade.getPointsEarned() ;
                }
            });

        } catch (Exception e) {
            log.error("getFinalScore - an exception occured while retrieving the final score for {} in {}.", userId, contextId);
            return null;
        }
    }

    public Date getDateRecorded(final Long itemId, final String userId, final String contextId, boolean useCaching) {
        boolean cached = false;
        Date dateRecorded = null;
        Map<String, Date> userToDateRecorded = null;
        if (useCaching) {
            userToDateRecorded = itemToUserToDateRecordedMap.get(itemId);
            if (userToDateRecorded == null) {
                userToDateRecorded = new HashMap<>();
                itemToUserToDateRecordedMap.put(itemId, userToDateRecorded);

            } else {
                if (userToDateRecorded.containsKey(userId)) {
                    cached = true;
                    dateRecorded = userToDateRecorded.get(userId);
                }
            }
        }

        //retrieve it every time if we're not using caching. If we are using caching, we retrieve it if it's not cached
        if (!useCaching || !cached) {
            final GradebookService gbs = getGradebookService();

            try {
                GradeDefinition gradeDefn = gbs.getGradeDefinitionForStudentForItem(contextId, itemId, userId);
                dateRecorded = gradeDefn.getDateRecorded();
            } catch(GradebookNotFoundException | AssessmentNotFoundException e) {
                dateRecorded = null;
            }

            if (useCaching) {
                userToDateRecorded.put(userId, dateRecorded);
            }
        }

        return dateRecorded;
    }

    public Date getFinalGradeDateRecorded(final String userId,final String contextId) {
        try {
            return (Date) doSecureGradebookAction(new SecureGradebookActionCallback() {
                public Object doSecureAction() {
                    //Just following the getFinalScore code, but ignoring grades and looking at dates
                    Map<Long,Double> catWeights = certService.getCategoryWeights(contextId);
                    Map<Long,Date> assgnDates = certService.getAssignmentDatesRecorded(contextId, userId);
                    Date lastDate = null;
                    int categoryType = certService.getCategoryType(contextId);

                    switch(categoryType) {
                        case GradebookService.CATEGORY_TYPE_NO_CATEGORY: {
                            for(Map.Entry<Long, Date> assgnDate : assgnDates.entrySet()) {
                                if (lastDate==null) {
                                    lastDate = assgnDate.getValue();
                                } else if (assgnDate.getValue() != null) {
                                    if (assgnDate.getValue().after(lastDate)) {
                                        lastDate = assgnDate.getValue();
                                    }
                                }
                            }

                            break;
                        }
                        case GradebookService.CATEGORY_TYPE_ONLY_CATEGORY: {
                            for(Map.Entry<Long, Date> assgnDate : assgnDates.entrySet()) {
                                if(catWeights.containsKey(assgnDate.getKey())) {
                                    if (lastDate==null) {
                                        lastDate = assgnDate.getValue();
                                    } else if (assgnDate.getValue() != null) {
                                        if (assgnDate.getValue().after(lastDate)) {
                                            lastDate = assgnDate.getValue();
                                        }
                                    }
                                }
                            }

                            break;
                        }
                        case GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY: {
                            for(Map.Entry<Long, Date> assgnDate : assgnDates.entrySet()) {
                                if(catWeights.containsKey(assgnDate.getKey())) {
                                    if (lastDate == null) {
                                        lastDate = assgnDate.getValue();
                                    } else if (assgnDate.getValue() != null) {
                                        if (assgnDate.getValue().after(lastDate)) {
                                            lastDate = assgnDate.getValue();
                                        }
                                    }
                                }
                            }

                            break;
                        }
                    }

                    return lastDate;
                }
            });

        } catch (Exception e) {
            return null;
        }
    }

    public Date getDateIssued(final String userId, final String contextId, CertificateDefinition certDef, boolean useCaching) {
        Set<Criterion> criteria = certDef.getAwardCriteria();

        //The last date in chronological order will be selected
        Date lastDate = null;

        Iterator<Criterion> itCriteria = criteria.iterator();
        while (itCriteria.hasNext()) {
            Criterion crit = itCriteria.next();
            try {
                if (!isCriterionMet(crit, userId, contextId, useCaching)) {
                    return null;
                }
            } catch (UnknownCriterionTypeException e) {
                return null;
            }

            Date date = crit.getDateMet(userId, contextId, useCaching);
            if (lastDate == null) {
                lastDate = date;
            } else if (date != null && date.after(lastDate)) {
                lastDate = date;
            }
        }

        return lastDate;
    }

    public void clearCaches() {
        cachedAssignments.clear();
        itemToUserToGradeMap.clear();
        itemToUserToDateRecordedMap.clear();
    }

    @Override
    public Map<String, Map<Criterion, UserProgress>> getProgressForUsers(String contextId, List<String> userIds, Class type, List<Criterion> critCollection)
            throws NumberFormatException {
        Map<String, Map<Criterion, UserProgress>> progressForUsers = new HashMap<>();
        if (type == null) {
            throw new IllegalArgumentException("criteria type cannot be null");
        }

        if (type.equals(DueDatePassedCriterion.class)) {
            return getDueDatePassedProgressForUsers(contextId, userIds, critCollection);

        } else if (type.equals(FinalGradeScoreCriterion.class)) {
            return getFinalGradeScoreProgressForUsers(contextId, userIds, critCollection);

        } else if (type.equals(GreaterThanScoreCriterion.class)) {
            return getGreaterThanScoreProgressForUsers(contextId, userIds, critCollection);

        } else if (type.equals(WillExpireCriterion.class)) {
            return getWillExpireProgressForUsers(userIds, critCollection);
        }

        return progressForUsers;
    }

    /**
     * Compares the criterion's class against type. Throws IllegalArgumentException if they are not equal
     */
    private void validateCriterionType(Criterion criterion, Class type) {
        if (criterion == null || !criterion.getClass().equals(type)) {
            throw new IllegalArgumentException("Expected " + type + ". Got: " + criterion);
        }
    }

    private Map<String, Map<Criterion, UserProgress>> getDueDatePassedProgressForUsers(String contextId, List<String> userIds, List<Criterion> critCollection) {
        Map<String, Map<Criterion, UserProgress>> userCritProgress = new HashMap<>();
        for (Criterion criterion : critCollection) {
            validateCriterionType(criterion, DueDatePassedCriterion.class);

            // Get the due date.

            // Start with the gradable object associated with this criterion
            DueDatePassedCriterion castedCriterion = (DueDatePassedCriterion) criterion;
            Long itemId = castedCriterion.getItemId();

            // get the due date from the assignment in gradebook service
            GradebookService gbs = getGradebookService();
            Assignment assn = gbs.getAssignment(contextId, itemId);
            Date dueDate = assn.getDueDate();

            // Due date passed - all students pass if the assignment's due date is in the past
            boolean everybodyPasses = dueDate.before(new Date());
            Date dateAwarded = everybodyPasses ? dueDate : null;

            for (String userId : userIds) {
                // We need to show the user's progress toward the criterion regardless of whether they passed.
                Map<Criterion, UserProgress> critProgress = getCritProgressMapForUser(userCritProgress, userId);

                // Their progress is simply the due date.
                ResourceLoader resourceLoader = new ResourceLoader();
                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, resourceLoader.getLocale());
                String strProgress = dateFormat.format(dueDate);
                UserProgress progress = new UserProgress(userId, criterion, strProgress, everybodyPasses, dateAwarded);
                critProgress.put(criterion, progress);
            }
        }

        return userCritProgress;
    }

    private Map<String, Map<Criterion, UserProgress>> getFinalGradeScoreProgressForUsers(String contextId, List<String> userIds, List<Criterion> critCollection)
            throws NumberFormatException {
        Map<String, Map<Criterion, UserProgress>> userCritProgress = new HashMap<>();

        /*
         * It doesn't matter how many FinalGradeScore criteria there are,
         * the calculations only need to be done once.
         * Eventually, the number of FinalGradeScore criteria will be limited to one,
         * but that's not implemented at the time of writing.
         */
        // TODO: we'd like to use the gb code's calculations (GradebookManager.getPointsEarnedCourseGradeRecords) and use that logic everywhere that calculates final grades
        Map<String, Double> userToScoreMap= new HashMap<>();
        Map<String, Date> userToDateRecordedMap = new HashMap<>();
        if (critCollection != null && !critCollection.isEmpty()) {
            for (String userId : userIds) {
                // TODO: if using the GradebookManager's final course grade calculation is too much to ask for, then we should create getFinalScores(final List<String> userIds, final String contextId);
                userToScoreMap.put(userId, getFinalScore(userId, contextId));

                // TODO: if 'getFinalScores' is too much to ask for, getting this date may be duplicating queries in 'getFinalGradeScore'
                userToDateRecordedMap.put(userId, getFinalGradeDateRecorded(userId, contextId));
            }
        }

        for (Criterion criterion : critCollection) {
            validateCriterionType(criterion, FinalGradeScoreCriterion.class);
            FinalGradeScoreCriterion castedCriterion = (FinalGradeScoreCriterion) criterion;

            // get the required score
            String strReqScore = castedCriterion.getScore();
            Double reqScore = null;

            try {
                reqScore = Double.parseDouble(strReqScore);
            } catch (NumberFormatException e) {
                log.error("Could not parse the required score as a double. Criterion is: {}", criterion);
            }

            for (String userId : userIds) {
                // Prepare the UserProgress object
                Double score = userToScoreMap.get(userId);
                String strScore = score == null ? null : score.toString();
                Date dateRecorded = userToDateRecordedMap.get(userId);
                UserProgress progress = new UserProgress(userId, criterion, strScore, score >= reqScore, dateRecorded);

                // add progress to the user's map of criteria to UserProgress (creates an empty map for the user if it doesn't exist)
                Map critProgressMap = getCritProgressMapForUser(userCritProgress, userId);
                critProgressMap.put(criterion, progress);
            }
        }

        return userCritProgress;
    }

    private Map<String, Map<Criterion, UserProgress>> getGreaterThanScoreProgressForUsers(String contextId, List<String> userIds, List<Criterion> critCollection) {
        // The return value. If we have a user and a criterion to look up, we can call userCritProgress.get(userId).get(criterion) to get the user's progress.
        Map<String, Map<Criterion, UserProgress>> userCritProgress = new HashMap<>();

        // The list of gradable object ids associated with the GreaterThanScore criteria in critCollection
        List<Long> gradableObjectIds = new ArrayList<>();

        // Maps gradableObjectIds back to the criteria they came from
        // Note: if we have the two GreaterThanScore criteria on the same gradebook item, the result of invoking .get(gboId) will be undefined. But we'll disallow this in the future.
        Map<Long, GreaterThanScoreCriterion> gradableObjectToCriterionMap = new HashMap<>();
        for (Criterion criterion : critCollection) {
            validateCriterionType(criterion, GreaterThanScoreCriterion.class);

            GreaterThanScoreCriterion castedCriterion = (GreaterThanScoreCriterion) criterion;
            Long gboId = castedCriterion.getItemId();
            gradableObjectIds.add(gboId);

            gradableObjectToCriterionMap.put(gboId, castedCriterion);
        }

        // Get a mapping of gradableObjectIds to lists grade definitions on their respective gradable objects.
        GradebookService gbs = getGradebookService();
        Map<Long, List<GradeDefinition>> gradesMap = gbs.getGradesWithoutCommentsForStudentsForItems(contextId, gradableObjectIds, userIds);

        // Iterate over the results to get the users' progress
        for (Map.Entry<Long, List<GradeDefinition>> gboGradeDef : gradesMap.entrySet()) {
            long gboId = gboGradeDef.getKey();
            List<GradeDefinition> gradeDefs = gboGradeDef.getValue();

            // Get the criterion that originally pointed at this gradable object ID
            GreaterThanScoreCriterion criterion = gradableObjectToCriterionMap.get(gboId);

            /*
             * Note we're only iterating over entires where the grades exist.
             * If the grade's not there, that's okay. The javadoc explains if a UserProgress is missing,
             * it is to be treated as though the user has no progress
             */
            for (GradeDefinition gradeDef : gradeDefs) {
                String userId = gradeDef.getStudentUid();
                // Gets the user's mapping of criteria to UserProgress from the map; creates and adds an empty map if it's not there
                Map<Criterion, UserProgress> critProgress = getCritProgressMapForUser(userCritProgress, userId);

                // Get the require score, the score and the date; populate UserProgress and add put it in critProgress
                Double scoreRequired = null;
                try {
                    scoreRequired = Double.parseDouble(criterion.getScore());
                } catch (NumberFormatException e) {
                    log.error("Could not parse the required score as a double. Criterion is: {}", criterion);
                }

                String progress = gradeDef.getGrade();
                Double grade;
                boolean passed = false;
                Date dateAwarded = null;

                try {
                    if (progress != null) {
                        grade = Double.parseDouble(progress);
                        if (grade >= scoreRequired) {
                            passed = true;
                            dateAwarded = gradeDef.getDateRecorded();
                        }
                    }

                } catch (NumberFormatException e) {
                    log.error("Could not parse student's progress toward this gradebook item as a double. Progress is: {}", progress);
                }

                UserProgress userProgress = new UserProgress(userId, criterion, progress, passed, dateAwarded);
                critProgress.put(criterion, userProgress);
            }
        }

        return userCritProgress;
    }

    private Map<String, Map<Criterion, UserProgress>> getWillExpireProgressForUsers(List<String> userIds, List<Criterion> critCollection) {
        // Everybody passes. Missing criteria are treated as though there's no progress which would result in failures, so we need to populate all of these as passed.
        Map<String, Map<Criterion, UserProgress>> userCritProgress = new HashMap<>();
        for (Criterion criterion : critCollection) {
            validateCriterionType(criterion, WillExpireCriterion.class);
            for (String userId : userIds) {
                Map<Criterion, UserProgress> critProgressMap = getCritProgressMapForUser(userCritProgress, userId);
                UserProgress progress = new UserProgress(userId, criterion, null, true, null);
                critProgressMap.put(criterion, progress);
            }
        }

        return userCritProgress;
    }

    /**
     * Helper method - returns userCritProgress.get(userId), but if it doesn't exist then a new HashMap<Criterion, UserProgress> is created for that user and then returned.
     */
    private Map<Criterion, UserProgress> getCritProgressMapForUser(Map<String, Map<Criterion, UserProgress>> userCritProgress, String userId) {
        Map<Criterion, UserProgress> critProgressMap = userCritProgress.get(userId);
        if (critProgressMap == null) {
            critProgressMap = new HashMap<>();
            userCritProgress.put(userId, critProgressMap);
        }

        return critProgressMap;
    }
}
