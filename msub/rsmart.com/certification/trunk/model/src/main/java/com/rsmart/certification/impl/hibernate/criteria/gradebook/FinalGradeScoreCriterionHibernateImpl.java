package com.rsmart.certification.impl.hibernate.criteria.gradebook;

/**
 * User: duffy
 * Date: Jul 5, 2011
 * Time: 9:59:47 AM
 */
public class FinalGradeScoreCriterionHibernateImpl
    extends GradebookItemCriterionHibernateImpl
{
    public String getScore()
    {
        return getVariableBindings().get("score");
    }

    public void setScore(String score)
    {
        getVariableBindings().put("score", score);
    }
}
