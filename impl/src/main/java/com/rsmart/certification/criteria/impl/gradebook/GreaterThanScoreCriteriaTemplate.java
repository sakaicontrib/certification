package com.rsmart.certification.criteria.impl.gradebook;

import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.GreaterThanScoreCriterionHibernateImpl;

import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.util.ResourceLoader;

/**
 * User: duffy
 * Date: Jul 5, 2011
 * Time: 9:26:50 AM
 */
public class GreaterThanScoreCriteriaTemplate extends GradebookItemCriteriaTemplate
{
    ScoreTemplateVariable scoreVariable = null;
    private final String EXPRESSION_KEY = "greater.than.score.criteria.expression";
    private static final String MESSAGE_POINT = "point";
    private static final String MESSAGE_POINTS = "points";
    private static final String MESSAGE_NOITEMS = "message.noitems.greaterthanscore";

    public GreaterThanScoreCriteriaTemplate(final GradebookCriteriaFactory factory)
    {
        super(factory,
                null,
                new AssignmentLabeler()
                {
                    public String getLabel(Assignment assignment)
                    {
                        StringBuilder assnLabel = new StringBuilder();
                        assnLabel.append(assignment.getName()).append(" (").append(assignment.getPoints().toString()).append(" ");
                        ResourceLoader rl = factory.getResourceLoader();

                        if (assignment.getPoints() == 1)
                        {
                            assnLabel.append(rl.getString(MESSAGE_POINT));
                        }
                        else
                        {
                            assnLabel.append(rl.getString(MESSAGE_POINTS));
                        }

                        assnLabel.append(')');
                        return assnLabel.toString();
                    }
                });

        scoreVariable =  new ScoreTemplateVariable(CriteriaFactory.KEY_SCORE, factory);
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

        Object vars[] = new String[2];

        GreaterThanScoreCriterionHibernateImpl gischi = (GreaterThanScoreCriterionHibernateImpl)criterion;

        String score = gischi.getScore();
        if (score != null)
        {
            Double dblScore = new Double (score);
            StringBuilder sbScore = new StringBuilder(score);
            if (dblScore == 1)
            {
                sbScore.append(" ").append(rl.getString(MESSAGE_POINT));
            }
            else
            {
                sbScore.append(" ").append(rl.getString(MESSAGE_POINTS));
            }
            score = sbScore.toString();
        }

        vars[0] = gischi.getItemName();
        vars[1] = score;

        return rl.getFormattedMessage(GreaterThanScoreCriteriaTemplate.class.getName(), vars);
    }

    @Override
    public String getMessage()
    {
        return getResourceLoader().getString(MESSAGE_NOITEMS);
    }
}
