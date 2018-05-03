package org.sakaiproject.certification.api.criteria;

import java.util.Date;

/**
 * Represents information regarding a user's progress toward a criterion
 */
public class UserProgress
{
    private String userId = null;
    private Criterion criterion = null;
    private String progress = null;
    private boolean passed = false;
    private Date dateAwarded = null;

    public UserProgress() {}

    public UserProgress(String userId, Criterion criterion, String progress, boolean passed, Date dateAwarded)
    {
        this.userId = userId;
        this.criterion = criterion;
        this.progress = progress;
        this.passed = passed;
        this.dateAwarded = dateAwarded;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public Criterion getCriterion()
    {
        return criterion;
    }

    public void setCriterion(Criterion criterion)
    {
        this.criterion = criterion;
    }

    public String getProgress()
    {
        return progress;
    }

    public void setProgress(String progress)
    {
        this.progress = progress;
    }

    public boolean isPassed()
    {
        return passed;
    }

    public void setPassed(boolean passed)
    {
        this.passed = passed;
    }

    public Date getDateAwarded()
    {
        return dateAwarded;
    }

    public void setDateAwarded(Date dateAwarded)
    {
        this.dateAwarded = dateAwarded;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("UserProgress with UserId: ");
        sb.append(userId)
                .append(" Criterion: ").append(criterion)
                .append(" Progress: ").append(progress)
                .append(" Passed: ").append(passed)
                .append(" DateAwarded: ").append(dateAwarded);
        return sb.toString();
    }
}
