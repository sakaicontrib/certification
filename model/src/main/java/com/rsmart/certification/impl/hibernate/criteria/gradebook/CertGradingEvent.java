package com.rsmart.certification.impl.hibernate.criteria.gradebook;

public class CertGradingEvent 
{
    private Long id;
    private CertGradebookObject gradableObject;
    private String studentId;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public CertGradebookObject getGradableObject()
    {
        return gradableObject;
    }

    public void setGradableObject(CertGradebookObject gradableObject)
    {
        this.gradableObject = gradableObject;
    }

    public String getStudentId()
    {
        return studentId;
    }

    public void setStudentId(String studentId)
    {
        this.studentId = studentId;
    }
}
