package com.rsmart.certification.impl.hibernate;

import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.DocumentTemplate;

/**
 * User: duffy
 * Date: Jun 30, 2011
 * Time: 7:37:18 AM
 */
public class DocumentTemplateHibernateImpl implements DocumentTemplate
{
    private String id;
    private String name;
    private String outputMimeType;
    private String resourceId;

    private CertificateDefinition certificateDefinition = null;

    public CertificateDefinition getCertificateDefinition()
    {
        return certificateDefinition;
    }

    public void setCertificateDefinition(CertificateDefinition certificateDefinition)
    {
        this.certificateDefinition = certificateDefinition;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
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

    public String getOutputMimeType()
    {
        return outputMimeType;
    }

    public void setOutputMimeType(String outputMimeType)
    {
        this.outputMimeType = outputMimeType;
    }

    public String getResourceId()
    {
        return resourceId;
    }

    public void setResourceId(String resourceId)
    {
        this.resourceId = resourceId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof DocumentTemplateHibernateImpl))
        {
            return false;
        }

        DocumentTemplateHibernateImpl that = (DocumentTemplateHibernateImpl) o;
        return !(id != null ? !id.equals(that.id) : that.id != null);
    }

    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }
}
