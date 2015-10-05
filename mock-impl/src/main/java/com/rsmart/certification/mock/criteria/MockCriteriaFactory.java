package com.rsmart.certification.mock.criteria;

import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.criteria.UnknownCriterionTypeException;
import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.CriteriaTemplateVariable;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.api.criteria.CriterionCreationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: duffy
 * Date: Jun 24, 2011
 * Time: 9:21:40 AM
 */
public class MockCriteriaFactory
    implements CriteriaFactory
{
    static int
        critId = 0;

    private MockCriteriaTemplate
        template = new MockCriteriaTemplate();
    private HashSet<CriteriaTemplate>
        templates = new HashSet<CriteriaTemplate>();
    private CertificateService
        cs = null;

    public MockCriteriaFactory()
    {
        template.criteriaFactory = this;
        templates.add(template);
    }

    public void init()
    {
        getCertificateService().registerCriteriaFactory(this);
    }

    public void setCertificateService (CertificateService cs)
    {
        this.cs = cs;
    }

    public CertificateService getCertificateService()
    {
        return cs;
    }
    
    public Set<CriteriaTemplate> getCriteriaTemplates()
    {
        return templates;
    }

    public Set<Class<? extends Criterion>> getCriterionTypes()
    {
        HashSet<Class<? extends Criterion>>
            types = new HashSet<Class<? extends Criterion>> ();

        types.add(MockCriterion.class);

        return types;
    }

    public CriteriaTemplate getCriteriaTemplate(Criterion criterion)
        throws UnknownCriterionTypeException
    {
        if (!(criterion instanceof MockCriterion))
            throw new UnknownCriterionTypeException (criterion.getClass().getName());
        
        return template;
    }

    public boolean isCriterionMet(Criterion criterion)
        throws UnknownCriterionTypeException
    {
        if (!MockCriterion.class.isAssignableFrom(criterion.getClass()))
            throw new UnknownCriterionTypeException();

        MockCriterion
            mc = (MockCriterion)criterion;
        
        return (mc.stringVar != null && mc.stringVar.trim().length() > 0 &&
                "rutebega".equals (mc.multipleChoiceVar));
    }

    public boolean isCriterionMet(Criterion criterion, String userId, String contextId)
        throws UnknownCriterionTypeException
    {
        if (!MockCriterion.class.isAssignableFrom(criterion.getClass()))
            throw new UnknownCriterionTypeException();

        MockCriterion
            mc = (MockCriterion)criterion;

        return (mc.stringVar != null && mc.stringVar.trim().length() > 0 &&
                "rutebega".equals (mc.multipleChoiceVar));
    }

    public Criterion createCriterion(CriteriaTemplate template, Map<String, String> bindings)
        throws CriterionCreationException
    {
        if (!(template instanceof MockCriteriaTemplate))
        {
            throw new CriterionCreationException("CriterionTemplate of type " + template.getClass().getName() + " is not supported by this CriteriaFactory");
        }

        String
            multVar = bindings.get("mult. choice var"),
            strVar = bindings.get("string var");

        MockCriterion
            criterion = new MockCriterion(multVar, strVar);

        return criterion;
    }

    public class MockCriteriaTemplate
        implements CriteriaTemplate
    {
        MockCriteriaFactory
            criteriaFactory = null;
        MockMultipleChoiceCriteriaTemplateVariable
            multiChoiceVar = new MockMultipleChoiceCriteriaTemplateVariable();
        MockStringCriteriaTemplateVariable
            stringVar = new MockStringCriteriaTemplateVariable();

        LinkedList<CriteriaTemplateVariable>
            variables = new LinkedList<CriteriaTemplateVariable>();

        public MockCriteriaTemplate()
        {
            variables.add(multiChoiceVar);
            variables.add(stringVar);
        }

        public String getExpression()
        {
            int
                numVars = getTemplateVariableCount();
            String
                args[] = new String[numVars];

            for (int i = 0; i < numVars; i++)
            {
                CriteriaTemplateVariable
                    var = getTemplateVariable(i);
                args[i] = var.getVariableKey();
            }

            return "This is a mock criteria. Select \"mult. choice var\" and \"string var\".\n (To cause criteria to succeed select \"rutebega\" and enter any value for \"string var\"";
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

        public CriteriaFactory getCriteriaFactory()
        {
            return criteriaFactory;
        }
    }

    public class MockMultipleChoiceCriteriaTemplateVariable
        implements CriteriaTemplateVariable
    {

        public String getVariableKey()
        {
            return "mult. choice var";
        }

        public boolean isMultipleChoice()
        {
            return true;
        }

        public String[] getValues()
        {
            return new String[]
                    {
                      "ocelot",
                      "rutebaga",
                      "entropy"
                    };
        }

        public boolean isValid(String value)
        {
            return (value instanceof String && (("ocelot".equals(value) || "rutebega".equals(value) || "entropy".equals(value))));
        }
    }

    public class MockStringCriteriaTemplateVariable
        implements CriteriaTemplateVariable
    {

        public String getVariableKey()
        {
            return "string var";
        }

        public boolean isMultipleChoice()
        {
            return false;
        }

        public String[] getValues()
        {
            return null;
        }

        public boolean isValid(String value)
        {
            return (value == null || (value instanceof String));
        }
    }

    public class MockCriterion
        implements Criterion
    {
        private String
            id = null,
            multipleChoiceVar = null,
            stringVar = null;

        public MockCriterion(String multipleChoiceVar, String stringVar)
        {
            id = "" + critId++;
            this.multipleChoiceVar = multipleChoiceVar;
            this.stringVar = stringVar;
        }

        public String getId()
        {
            return id;
        }

        public String getExpression()
        {
            StringBuffer
                exp = new StringBuffer();

            exp.append ("This criteria is set to: \"").append(multipleChoiceVar).append("\" and \"").append (stringVar);

            return exp.toString();
        }

        public Map<String, String> getVariableBindings()
        {
            HashMap<String, String>
                bindings = new HashMap<String, String>();

            bindings.put("mult. choice var", multipleChoiceVar);
            bindings.put("string var", stringVar);

            return bindings;
        }

        public String getFactoryClassname()
        {
            return MockCriteriaFactory.class.getName();
        }
    }
}
