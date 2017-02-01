package com.rsmart.certification.api.criteria;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: duffy
 * Date: Jun 21, 2011
 * Time: 10:19:22 AM
 */
public interface Criterion
{
    public String getId();

    public CriteriaFactory getCriteriaFactory();

    public String getCurrentCriteriaTemplate();

    public String getExpression();

    public Map<String, String> getVariableBindings();

    /**
     * Returns all the headers that should be displayed on the reporting interface for this criterion.
     * For example, if this is a FinalGradeScore criterion, it will return a list with one item,
     * namely "Final Course Grade"
     * @return
     */
    public List<String> getReportHeaders();

    /**
     * Returns all the cell data that should be displayed on the reporting interface for this criterion.
     * For example if this is a WillExpireCriterion, it will return a list with one item,
     * namely the date of expiry
     *
     * @param userId the user we are grabbing report data for
     * @param siteId the site containing this criterion
     * @param issueDate
     * @return
     */
    public List<CriterionProgress> getReportData(String userId, String siteId, Date issueDate, boolean useCaching);

    /**
     * Returns the first date on which this criterion was met. For example, if this is a DueDatePassed criterion,
     * then the dateMet would be the gradebook item's due date, as this criterion is met as soon as the gradebook item's
     * due date has passed.
     * If this is a FinalGradeScore criterion, then the dateMet would be the last date recorded of the gradebook items
     * Returns null if the criterion is not met
     * @param userId
     * @param siteId
     * @return
     */
    public Date getDateMet(String userId, String siteId, boolean useCaching);

    /**
     * Returns a message for the UI to indicate the specified user's progress towards meeting this criterion.
     * For example, if this is a GreaterThanScore criterion, the user's progress would be their current score
     * on this criterion's gradebook item.
     * Returns "" if progress is undefined (ie. WillExpire)
     * @param userId
     * @param siteId
     * @return
     */
    public String getProgress(String userId, String siteId, boolean useCaching);
}
