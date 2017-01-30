package com.rsmart.certification.criteria.impl.gradebook;

import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.CriteriaTemplateVariable;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.FinalGradeScoreCriterionHibernateImpl;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.util.ResourceLoader;

/**
 * User: duffy
 * Date: Jul 5, 2011
 * Time: 9:26:50 AM
 */
public class FinalGradeScoreCriteriaTemplate implements CriteriaTemplate
{
    private static final Log LOG = LogFactory.getLog(FinalGradeScoreCriteriaTemplate.class);
    ScoreTemplateVariable scoreVariable = null;
    ArrayList<CriteriaTemplateVariable> variables = new ArrayList<CriteriaTemplateVariable>(1);
    GradebookCriteriaFactory factory = null;
    CertificateService certificateService = null;
    GradebookService
        gbService = null;
    ResourceLoader rl = null;

    private final String EXPRESSION_KEY = "final.grade.score.criteria.expression";
    private final String VARIABLE_SCORE = "score";

    public FinalGradeScoreCriteriaTemplate(final GradebookCriteriaFactory factory)
    {
        this.factory = factory;
        gbService = factory.getGradebookService();
        certificateService = factory.getCertificateService();

        scoreVariable =  new ScoreTemplateVariable(VARIABLE_SCORE, factory);
        addVariable(scoreVariable);
    }

    public String getId()
    {
        return FinalGradeScoreCriteriaTemplate.class.getName();
    }

    protected void addVariable (CriteriaTemplateVariable variable)
    {
        variables.add(variable);
    }

    public void setResourceLoader (ResourceLoader rl)
    {
        this.rl = rl;
    }

    public ResourceLoader getResourceLoader()
    {
        return rl;
    }

    public CriteriaFactory getCriteriaFactory()
    {
        return factory;
    }

    public int getTemplateVariableCount()
    {
        return variables.size();
    }

    public List<CriteriaTemplateVariable> getTemplateVariables()
    {
        return variables;
    }

    public CriteriaTemplateVariable getTemplateVariable(int i)
    {
        return variables.get(i);
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

        SecureGradebookActionCallback
            typeCallback = new SecureGradebookActionCallback()
            {
                public Object doSecureAction()
                {
                    return certificateService.getCategoryType(factory.contextId());
                }
            },
            assnPointsCallback = new SecureGradebookActionCallback()
            {
                public Object doSecureAction()
                {
                    return certificateService.getAssignmentPoints(factory.contextId());
                }
            },
            catOnlyAssnPointsCallback = new SecureGradebookActionCallback()
            {
            	public Object doSecureAction()
            	{
            		return certificateService.getCatOnlyAssignmentPoints(factory.contextId());
            	}
            };

        Map<Long, Double>
            assnPoints = null;
        int
            categoryType = -1;

        try
        {
            categoryType = (Integer) factory.doSecureGradebookAction(typeCallback);
            if(categoryType == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY)
            {
            	assnPoints = (Map<Long, Double>)factory.doSecureGradebookAction(catOnlyAssnPointsCallback);
            }
            else
            {
            	assnPoints = (Map<Long, Double>)factory.doSecureGradebookAction(assnPointsCallback);
            }

        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return rl.getString("error.cannotEvaluate");
        }

        double total = 0;
        switch(categoryType)
        {
            case GradebookService.CATEGORY_TYPE_NO_CATEGORY:
            case GradebookService.CATEGORY_TYPE_ONLY_CATEGORY:
            {
                for (Double points : assnPoints.values())
                {
                    total += points;
                }
                break;
            }
            case GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY:
            {
                total = 100;
                break;
            }
        }

        DecimalFormat df = new DecimalFormat("#0.00");
        Object vars[] = new String[2];

        vars[0] = df.format(total);

        FinalGradeScoreCriterionHibernateImpl fgschi = (FinalGradeScoreCriterionHibernateImpl)criterion;

        vars[1] = fgschi.getScore();

        return rl.getFormattedMessage(FinalGradeScoreCriteriaTemplate.class.getName(), vars);
    }

    @Override
    public String getMessage()
    {
        return "";
    }
}
