package com.rsmart.certification.impl.hibernate.criteria.gradebook;

/**
 * User: duffy
 * Date: Aug 3, 2011
 * Time: 1:33:35 PM
 */
public class CertGradebook
{
    private long id;
    private String uid;
    private int category_type;

    public int getCategory_type()
    {
        return category_type;
    }

    public void setCategory_type(int category_type)
    {
        this.category_type = category_type;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getUid()
    {
        return uid;
    }

    public void setUid(String uid)
    {
        this.uid = uid;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof CertGradebook))
        {
            return false;
        }

        CertGradebook that = (CertGradebook) o;
        if (id != that.id)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return (int) (id ^ (id >>> 32));
    }
}
