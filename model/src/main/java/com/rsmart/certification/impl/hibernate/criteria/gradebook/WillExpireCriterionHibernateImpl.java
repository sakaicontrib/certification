package com.rsmart.certification.impl.hibernate.criteria.gradebook;

public class WillExpireCriterionHibernateImpl extends GradebookItemCriterionHibernateImpl
{
    public String getExpiryOffset()
    {
        return getVariableBindings().get("expiry.offset");
    }

    public void setExpiryOffset(String expiryOffset)
    {
        getVariableBindings().put("expiry.offset", expiryOffset);
    }
}
