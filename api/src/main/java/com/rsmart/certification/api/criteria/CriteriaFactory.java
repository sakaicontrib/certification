package com.rsmart.certification.api.criteria;

import java.util.Map;
import java.util.Set;

/**
 * User: duffy
 * Date: Jun 23, 2011
 * Time: 11:43:32 AM
 */
public interface CriteriaFactory
{
    public Set<CriteriaTemplate> getCriteriaTemplates();

    public CriteriaTemplate getCriteriaTemplate(String id)
        throws UnknownCriterionTypeException;
    
    public CriteriaTemplate getCriteriaTemplate(Criterion criterion)
        throws UnknownCriterionTypeException;

    public Set<Class <? extends Criterion>> getCriterionTypes();

    public boolean isCriterionMet (Criterion criterion)
        throws UnknownCriterionTypeException;

    public boolean isCriterionMet (Criterion criterion, String userId, String contextId)
        throws UnknownCriterionTypeException;

    public Criterion createCriterion (CriteriaTemplate template, Map<String, String> bindings)
            throws InvalidBindingException, CriterionCreationException, UnknownCriterionTypeException;
}
