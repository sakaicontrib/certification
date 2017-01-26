package com.rsmart.certification.api.criteria;

import com.rsmart.certification.api.CertificateDefinition;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * User: duffy
 * Date: Jun 23, 2011
 * Time: 11:43:32 AM
 */
public interface CriteriaFactory
{
    public Set<CriteriaTemplate> getCriteriaTemplates();

    public CriteriaTemplate getCriteriaTemplate(String id)
        throws UnknownCriterionTypeException;
    
    public CriteriaTemplate getCriteriaTemplate(Criterion criterion)
        throws UnknownCriterionTypeException;

    public Set<Class <? extends Criterion>> getCriterionTypes();

    public boolean isCriterionMet (Criterion criterion)
        throws UnknownCriterionTypeException;

    public boolean isCriterionMet (Criterion criterion, String userId, String contextId)
        throws UnknownCriterionTypeException;

    public Criterion createCriterion (CriteriaTemplate template, Map<String, String> bindings)
        throws InvalidBindingException, CriterionCreationException, UnknownCriterionTypeException;

    /**
     * 
     * @param itemId the gradebook item's id
     * @param userId the user's id
     * @param contextId
     * @return the score on a gradebook item (if applicable)
     */
    public Double getScore(Long itemId, String userId, String contextId);

    /**
     * not sure if this method will be needed, might be covered by getScore()
     * @param userId the user's id
     * @param contextId
     * @return the final score for the given user
     */
    public Double getFinalScore(String userId, String contextId);

    /**
     * 
     * @param itemId
     * @param userId
     * @param contextId
     * @return the date that the gradebook item's score was entered (if applicable)
     */
    public Date getDateRecorded(Long itemId, String userId, String contextId);

    /**
     * Looks at all the submission dates attached to all the criteria and returns the latest date
     * @param userId
     * @param contextId
     * @param certDef
     * @return
     */
    public Date getDateIssued(String userId, String contextId, CertificateDefinition certDef);
}
