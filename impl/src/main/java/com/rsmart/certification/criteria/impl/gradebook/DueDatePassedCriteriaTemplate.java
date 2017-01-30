package com.rsmart.certification.criteria.impl.gradebook;

import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.DueDatePassedCriterionHibernateImpl;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.util.ResourceLoader;

/**
 * User: duffy
 * Date: Jul 18, 2011
 * Time: 9:47:57 PM
 */
public class DueDatePassedCriteriaTemplate extends GradebookItemCriteriaTemplate
{
    private static final String MESSAGE_DUEDATE = "duedate";
    private static final String MESSAGE_DUEDATE_NONE = "duedate.none";
    private static final String MESSAGE_NOITEMS_DUEDATE = "message.noitems.duedate";

    private final String EXPRESSION_KEY = "due.date.has.passed.criteria.expression";
    private final DateFormat EXPRESSION_DATE_FORMAT = new SimpleDateFormat("MMMM dd, yyyy");

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
                            dateStr = rl.getFormattedMessage(MESSAGE_DUEDATE, new Object[] { sdf.format(due) });
                        }
                        else
                        {
                            dateStr = rl.getString(MESSAGE_DUEDATE_NONE);
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

        Object vars[] = new String[2];
        DueDatePassedCriterionHibernateImpl gischi = (DueDatePassedCriterionHibernateImpl)criterion;

        vars[0] = gischi.getItemName();
        vars[1] = EXPRESSION_DATE_FORMAT.format(gischi.getDueDate());

        return getResourceLoader().getFormattedMessage(DueDatePassedCriteriaTemplate.class.getName(), vars);
    }

    @Override
    public String getMessage()
    {
        return getResourceLoader().getString(MESSAGE_NOITEMS_DUEDATE);
    }
}
