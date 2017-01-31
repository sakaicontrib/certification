package com.rsmart.certification.api.criteria;

import com.rsmart.certification.api.CertificateDefinition;
import java.util.Date;
import java.util.List;
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

    /**
     * Gets the progress of multiple users toward each criterion in the specified collection of criteria.
     * The class of each Criterion in 'critCollection' must match the class specified by 'type'.
     * The progress will be stored as a UserProgress object;
     * all such UserProgress objects will be associated with their respective criterion in a map,
     * and all such maps will be associated to their respective users in a parent map.
     * Ie. getProgressForUsers(...).get(myUser.getId()).get(myCriterion) will give us myUser's progress on myCriterion
     * If a UserProgress is null, it means the user made no progress toward the criterion, and therefore has failed.
     * @param contextId the siteId/gradebookUid for this site
     * @param userIds list of userIds to get the progress for
     * @param type the implemented type of the collection
     * @param critCollection list of criteria on which to assess the users' progress
     * @return Mapping of userId -> (mapping of Criterion -> UserProgress)
     * @throws IllegalArgumentException if a member of criteria's type doesn't match type
     */
   public Map<String, Map<Criterion, UserProgress>> getProgressForUsers(String contextId, List<String> userIds, Class type, List<Criterion> critCollection);
}
