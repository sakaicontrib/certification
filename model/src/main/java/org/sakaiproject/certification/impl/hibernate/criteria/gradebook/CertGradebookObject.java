package org.sakaiproject.certification.impl.hibernate.criteria.gradebook;

/**
 * User: duffy
 * Date: Aug 3, 2011
 * Time: 1:18:06 PM
 */
public class CertGradebookObject
{
    private long id;
    private String name;

    private CertGradebook gradebook;
    private CertCategory category;
    private boolean removed;
    private boolean ungraded;
    private boolean released;

    public CertGradebook getGradebook()
    {
        return gradebook;
    }

    public void setGradebook(CertGradebook gradebook)
    {
        this.gradebook = gradebook;
    }

    public CertCategory getCategory()
    {
        return category;
    }

    public void setCategory(CertCategory category)
    {
        this.category = category;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isRemoved()
    {
        return removed;
    }

    public void setRemoved(boolean removed)
    {
        this.removed = removed;
    }

    public boolean isUngraded()
    {
        return ungraded;
    }

    public void setUngraded(boolean ungraded)
    {
        this.ungraded = ungraded;
    }

    public boolean isReleased()
    {
        return released;
    }

    public void setReleased(boolean released)
    {
        this.released = released;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(CertGradebookObject.class.isAssignableFrom(o.getClass())))
        {
            return false;
        }

        CertGradebookObject that = (CertGradebookObject) o;
        return id == that.id;
    }

    @Override
    public int hashCode()
    {
        return (int) (id ^ (id >>> 32));
    }
}
