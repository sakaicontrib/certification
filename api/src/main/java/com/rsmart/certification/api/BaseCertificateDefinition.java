package com.rsmart.certification.api;

import com.rsmart.certification.api.criteria.Criterion;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User: duffy
 * Date: Jun 29, 2011
 * Time: 2:54:55 PM
 */
public class BaseCertificateDefinition
    implements CertificateDefinition
{
    protected String
        id,
        creatorUserId,
        name,
        description,
        siteId,
        shortUrl,
        expiryOffset;
    protected Date
        createDate;
    protected CertificateDefinitionStatus
        status = CertificateDefinitionStatus.UNPUBLISHED;
    protected DocumentTemplate
        documentTemplate;
    protected Map<String, String>
        fieldValues = new HashMap<String, String>(0);
    protected Set<Criterion>
        awardCriteria = null;

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(String creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description!=null)
        {
            this.description = description.trim();
        }
        else
        {
            description = null;
        }
    }

    public DocumentTemplate getDocumentTemplate() {
        return documentTemplate;
    }

    public void setDocumentTemplate(DocumentTemplate documentTemplate) {
        this.documentTemplate = documentTemplate;
    }

    public Map<String, String> getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(Map<String, String> fieldValues) {
        this.fieldValues = fieldValues;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getShortUrl() {
		return shortUrl;
	}

	public void setShortUrl(String shortUrl) {
		this.shortUrl = shortUrl;
	}

	public CertificateDefinitionStatus getStatus() {
        return status;
    }

    public void setStatus(CertificateDefinitionStatus status) {
        this.status = status;
    }

    public Set<Criterion> getAwardCriteria() {
        return awardCriteria;
    }

    public void setAwardCriteria(Set<Criterion> awardCriteria) {
        this.awardCriteria = awardCriteria;
    }

    public String getExpiryOffset()
    {
        return expiryOffset;
    }
    
    public void setExpiryOffset( String expiryOffset )
    {
        this.expiryOffset = expiryOffset;
    }
}
