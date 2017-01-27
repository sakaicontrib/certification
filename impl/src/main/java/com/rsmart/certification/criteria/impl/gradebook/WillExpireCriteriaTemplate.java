package com.rsmart.certification.criteria.impl.gradebook;

import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.CriteriaTemplateVariable;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.impl.ExpiryOffsetTemplateVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.sakaiproject.util.ResourceLoader;

public class WillExpireCriteriaTemplate implements CriteriaTemplate
{
    ExpiryOffsetTemplateVariable expiryOffsetVariable = null;
    ArrayList<CriteriaTemplateVariable> variables = new ArrayList<CriteriaTemplateVariable>(1);
    GradebookCriteriaFactory factory = null;
    CertificateService certificateService = null;
    ResourceLoader rl = null;

    private final String EXPRESSION_KEY = "will.expire.criteria.expression";

    public WillExpireCriteriaTemplate(final GradebookCriteriaFactory factory)
    {
        this.factory = factory;
        certificateService = factory.getCertificateService();
        expiryOffsetVariable =  new ExpiryOffsetTemplateVariable("expiry.offset", factory);
        addVariable(expiryOffsetVariable);
    }

    public String getId()
    {
        return WillExpireCriteriaTemplate.class.getName();
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
            return getResourceLoader().getFormattedMessage(EXPRESSION_KEY, new Object[]{});
        }
        else
        {
            Map<String, String> bindings = criterion.getVariableBindings();
            String expiryOffset = bindings.get("expiry.offset");
            return getResourceLoader().getFormattedMessage(WillExpireCriteriaTemplate.class.getName(), new Object[]{expiryOffset});
        }
    }

    @Override
    public String getMessage()
    {
        return getResourceLoader().getString("message.noitems.willexpire");
    }
}
