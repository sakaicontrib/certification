package com.rsmart.certification.impl.hibernate;

import com.rsmart.certification.api.CertificateAward;
import com.rsmart.certification.api.CertificateDefinition;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

/**
 * User: duffy
 * Date: Jun 28, 2011
 * Time: 2:01:56 PM
 */
public class CertificateAwardHibernateImpl implements CertificateAward
{
    private String
        id,
        userId;
    private CertificateDefinition
        certificateDefinition;
    private Date
        certificationTimeStamp = new Date();

    public CertificateDefinition getCertificateDefinition()
    {
        return certificateDefinition;
    }

    public void setCertificateDefinition(CertificateDefinition certificateDefinition) {
        this.certificateDefinition = certificateDefinition;
    }

    public Date getCertificationTimeStamp() {
        return certificationTimeStamp;
    }

    public void setCertificationTimeStamp(Date certificationTimeStamp) {
        this.certificationTimeStamp = certificationTimeStamp;
    }

    public String getFormattedCertificationTimeStamp()
    {
        DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy hh:mm aa");
        return dateFormat.format(getCertificationTimeStamp());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CertificateAwardHibernateImpl)) return false;

        CertificateAwardHibernateImpl that = (CertificateAwardHibernateImpl) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
