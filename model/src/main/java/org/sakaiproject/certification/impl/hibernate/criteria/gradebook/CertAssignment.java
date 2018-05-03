package org.sakaiproject.certification.impl.hibernate.criteria.gradebook;

/**
 * User: duffy
 * Date: Aug 3, 2011
 * Time: 1:18:17 PM
 */
public class CertAssignment extends CertGradebookObject
{
    private double pointsPossible;
    private double assignmentWeighting;
    private boolean notCounted;
    private CertCategory category;
    private CertGradebook gradebook;

    public double getAssignmentWeighting()
    {
        return assignmentWeighting;
    }

    public void setAssignmentWeighting(double assignmentWeighting)
    {
        this.assignmentWeighting = assignmentWeighting;
    }

    public boolean isNotCounted()
    {
        return notCounted;
    }

    public void setNotCounted(boolean notCounted)
    {
        this.notCounted = notCounted;
    }

    public double getPointsPossible()
    {
        return pointsPossible;
    }

    public void setPointsPossible(double pointsPossible)
    {
        this.pointsPossible = pointsPossible;
    }

    public CertCategory getCategory()
    {
        return category;
    }

    public void setCategory(CertCategory category)
    {
        this.category = category;
    }

    public CertGradebook getGradebook()
    {
        return gradebook;
    }

    public void setGradebook(CertGradebook gradebook)
    {
        this.gradebook = gradebook;
    }
}
