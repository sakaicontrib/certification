package com.rsmart.certification.mock;

import com.rsmart.certification.api.CertificateAward;
import com.rsmart.certification.api.CertificateDefinition;

import java.util.Date;

/**
 * User: duffy
 * Date: Jun 20, 2011
 * Time: 9:08:22 AM
 */
public class MockCertificateAward
    implements CertificateAward
{
    private String
        id,
        userId;
    private MockCertificateDefinition
        certificateDefinition;
    private Date
        certificationTimeStamp = new Date();

    public Date getCertificationTimeStamp() {
        return certificationTimeStamp;
    }

    public void setCertificationTimeStamp(Date certificationTimeStamp) {
        this.certificationTimeStamp = certificationTimeStamp;
    }

    public CertificateDefinition getCertificateDefinition() {
        return certificateDefinition;
    }

    public void setCertificateDefinition (CertificateDefinition certificateDefinition) {
        this.certificateDefinition = (MockCertificateDefinition)certificateDefinition;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
