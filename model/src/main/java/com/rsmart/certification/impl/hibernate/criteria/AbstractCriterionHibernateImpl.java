package com.rsmart.certification.impl.hibernate.criteria;

import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.api.criteria.UnknownCriterionTypeException;

import java.util.HashMap;
import java.util.Map;

/**
 * User: duffy
 * Date: Jun 28, 2011
 * Time: 10:25:44 AM
 */
public abstract class AbstractCriterionHibernateImpl
    implements Criterion
{
    private String
        id = null;
    private Map<String, String>
        bindings = new HashMap<String, String>();
    private CriteriaFactory
        cFact = null;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Map<String, String> getVariableBindings()
    {
        return bindings;
    }

    public void setVariableBindings(Map<String, String> bindings)
    {
        this.bindings = bindings;
    }

    public void setCriteriaFactory(CriteriaFactory cFact)
    {
        this.cFact = cFact;
    }

    public CriteriaFactory getCriteriaFactory()
    {
        return cFact;
    }
   
    public String getExpression()
    {
        String
            expression = null;

        try
        {
            expression = getCriteriaFactory().getCriteriaTemplate(this).getExpression(this);
        }
        catch (UnknownCriterionTypeException e)
        {
            //well now that would just be weird if my own CriteriaFactory was not able
            // to find the right CriteriaTemplate for my type, right?

            //I'm swallowing this exception.
        }

        return expression;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(AbstractCriterionHibernateImpl.class.isAssignableFrom(o.getClass()))) return false;

        AbstractCriterionHibernateImpl that = (AbstractCriterionHibernateImpl) o;

        return (id != null && id.equals(that.id));
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }
    
    public String getCurrentCriteriaTemplate() {
    	CriteriaTemplate criteriaTemplate=null;
		try {
			criteriaTemplate = getCriteriaFactory().getCriteriaTemplate(this);
		} catch (UnknownCriterionTypeException e) {
			// TODO Auto-generated catch block
		}
    	return criteriaTemplate.getClass().getName();
    	
    	
    }
}
