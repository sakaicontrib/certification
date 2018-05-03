package org.sakaiproject.certification.impl.hibernate.criteria.gradebook;

/**
 * User: duffy
 * Date: Aug 3, 2011
 * Time: 1:33:45 PM
 */
public class CertCategory
{
    private long id;
    private CertGradebook gradebook;
    private double weight;
    private boolean removed;

    public CertGradebook getGradebook()
    {
        return gradebook;
    }

    public void setGradebook(CertGradebook gradebook)
    {
        this.gradebook = gradebook;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public boolean isRemoved()
    {
        return removed;
    }

    public void setRemoved(boolean removed)
    {
        this.removed = removed;
    }

    public double getWeight()
    {
        return weight;
    }

    public void setWeight(double weight)
    {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof CertCategory))
        {
            return false;
        }

        CertCategory that = (CertCategory) o;
        return id == that.id;
    }

    @Override
    public int hashCode()
    {
        return (int) (id ^ (id >>> 32));
    }
}
