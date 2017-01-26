package com.rsmart.certification.criteria.impl.gradebook;

import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.DueDatePassedCriterionHibernateImpl;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.GreaterThanScoreCriterionHibernateImpl;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.util.ResourceLoader;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: duffy
 * Date: Jul 18, 2011
 * Time: 9:47:57 PM
 */
public class DueDatePassedCriteriaTemplate
    extends GradebookItemCriteriaTemplate
{
    private final String EXPRESSION_KEY="due.date.has.passed.criteria.expression";

    public DueDatePassedCriteriaTemplate(final GradebookCriteriaFactory factory)
    {
        super(factory,
                new AssignmentFilter()
                {
                    public boolean include(Assignment assignment)
                    {
                        return assignment.getDueDate() != null;
                    }
                },
                new AssignmentLabeler()
                {
                    public String getLabel(Assignment assignment)
                    {
                        StringBuffer
                            assnLabel = new StringBuffer();
                        ResourceLoader
                            rl = factory.getResourceLoader();
                        SimpleDateFormat
                            sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date
                            due = assignment.getDueDate();
                        String
                            dateStr = null;

                        if (due != null)
                        {
                            dateStr = rl.getFormattedMessage("duedate", new Object[] { sdf.format(due) });
                        }
                        else
                        {
                            dateStr = rl.getString("duedate.none");
                        }

                        assnLabel.append(assignment.getName()).append(" (").append(dateStr).append(')');

                        return assnLabel.toString();
                    }
                });
    }

    public String getId()
    {
        return DueDatePassedCriteriaTemplate.class.getName();
    }

    public String getExpression()
    {
        return getExpression(null);
    }

    public String getExpression (Criterion criterion)
    {
        if (criterion == null)
        {
            return getResourceLoader().getFormattedMessage(EXPRESSION_KEY, new Object[]{});
        }

        Object
            vars[] = new String[1];

        DueDatePassedCriterionHibernateImpl
           gischi = (DueDatePassedCriterionHibernateImpl)criterion;

        vars[0] = gischi.getItemName();

        return getResourceLoader().getFormattedMessage(DueDatePassedCriteriaTemplate.class.getName(), vars);
    }
}
