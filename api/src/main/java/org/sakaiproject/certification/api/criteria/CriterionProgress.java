package org.sakaiproject.certification.api.criteria;

/**
 * When reporting on a criterion, every item of reportable data can be represented by this class.
 * The progress String indicates the progress towards the criterion.
 * The met boolean indicates whether the datum represesnted by this class meets the criterion.
 *
 *  An example: FinalGradeScoreCriterion - progress would be a String representation of
 *  the user's final grade (ie. 89), and if the minimum score is 80, then met would be true
 */
public class CriterionProgress
{
    private String progress = "";
    private boolean met = false;

    public CriterionProgress() {}

    public CriterionProgress(String progress, boolean met)
    {
        this.progress = progress;
        this.met = met;
    }

    public String getProgress()
    {
        return progress;
    }

    public void setProgress(String progress)
    {
        this.progress = progress;
    }

    public boolean isMet()
    {
        return met;
    }

    public void setMet(boolean met)
    {
        this.met = met;
    }
}
