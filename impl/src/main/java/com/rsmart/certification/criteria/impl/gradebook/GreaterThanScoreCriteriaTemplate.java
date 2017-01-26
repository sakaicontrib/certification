package com.rsmart.certification.criteria.impl.gradebook;

import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.GreaterThanScoreCriterionHibernateImpl;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.util.ResourceLoader;

/**
 * User: duffy
 * Date: Jul 5, 2011
 * Time: 9:26:50 AM
 */
public class GreaterThanScoreCriteriaTemplate
    extends GradebookItemCriteriaTemplate
{
    ScoreTemplateVariable
        scoreVariable = null;

    private final String EXPRESSION_KEY = "greater.than.score.criteria.expression";

    public GreaterThanScoreCriteriaTemplate(final GradebookCriteriaFactory factory)
    {
        super(factory,
                null,
                new AssignmentLabeler()
                {
                    public String getLabel(Assignment assignment)
                    {
                        StringBuffer
                            assnLabel = new StringBuffer();
                        ResourceLoader
                            rl = factory.getResourceLoader();

                        String
                            pointsStr = rl.getFormattedMessage("points", new String[] { assignment.getPoints().toString() });

                        assnLabel.append(assignment.getName()).append(" (").append(pointsStr).append(')');

                        return assnLabel.toString();
                    }
                });

        scoreVariable =  new ScoreTemplateVariable("score", factory);

        addVariable(scoreVariable);
    }

    public String getId()
    {
        return GreaterThanScoreCriteriaTemplate.class.getName();
    }

    public String getExpression()
    {
        return getExpression(null);
    }

    public String getExpression (Criterion criterion)
    {
        if (criterion == null)
        {
            return rl.getFormattedMessage(EXPRESSION_KEY, new Object[]{});
        }

        Object
            vars[] = new String[2];

        GreaterThanScoreCriterionHibernateImpl
           gischi = (GreaterThanScoreCriterionHibernateImpl)criterion;

        vars[0] = gischi.getItemName();
        vars[1] = gischi.getScore();

        return rl.getFormattedMessage(GreaterThanScoreCriteriaTemplate.class.getName(), vars);
    }

}
