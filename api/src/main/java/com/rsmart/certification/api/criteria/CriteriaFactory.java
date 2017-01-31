package com.rsmart.certification.api.criteria;

import com.rsmart.certification.api.CertificateDefinition;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Rename this to CriteriaManager.
 * This class has too many other responsibilities to be considered a factory class
*
 * User: duffy
 * Date: Jun 23, 2011
 * Time: 11:43:32 AM
 */
public interface CriteriaFactory
{
    public static final String KEY_SCORE = "score";
    public static final String KEY_GRADEBOOK_ITEM = "gradebook.item";
    public static final String KEY_EXPIRY_OFFSET = "expiry.offset";

    public Set<CriteriaTemplate> getCriteriaTemplates();

    public CriteriaTemplate getCriteriaTemplate(String id) throws UnknownCriterionTypeException;

    public CriteriaTemplate getCriteriaTemplate(Criterion criterion) throws UnknownCriterionTypeException;

    public Set<Class <? extends Criterion>> getCriterionTypes();

    public boolean isCriterionMet (Criterion criterion) throws UnknownCriterionTypeException;

    public boolean isCriterionMet (Criterion criterion, String userId, String contextId, boolean useCaching) throws UnknownCriterionTypeException;

    public Criterion createCriterion (CriteriaTemplate template, Map<String, String> bindings)
        throws InvalidBindingException, CriterionCreationException, UnknownCriterionTypeException;

    /**
     *
     * @param itemId the gradebook item's id
     * @param userId the user's id
     * @param contextId
     * @return the score on a gradebook item (if not applicable, returns null)
     */
    public Double getScore(Long itemId, String userId, String contextId, boolean useCaching);

    /**
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
    public Date getDateRecorded(Long itemId, String userId, String contextId, boolean useCaching);

     /**
     * @param userId
     * @param contextId
     * @return the date at which the final grade was recorded (last date of any relevant grade entries)
     */
    public Date getFinalGradeDateRecorded(String userId, String contextId);

    /**
     * The date of issue is the moment in time where this user has become eligible to download their certificate.
     * Returns null if the certificate is not awarded to this user
     *
     * For example, on a GreaterThanScore criterion, the date they met that criteria is the result of getDateRecorded()
     * on the criterion's gradebook item, whereas on a DueDatePassed criterion, the date at which this criterion is met
     * is the gradebook item's due date. To get the date of issue we evaluate the date that each criterion was met and
     * select the last one in chronological order
     * @param userId
     * @param contextId
     * @param certDef
     * @return
     */
    public Date getDateIssued(String userId, String contextId, CertificateDefinition certDef, boolean useCaching);

    /**
     * Data gets cached in the criteria factories to speed up performance, this method clears the cache
     */
    public void clearCaches();
}
