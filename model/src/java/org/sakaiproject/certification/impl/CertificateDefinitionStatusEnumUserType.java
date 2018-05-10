package org.sakaiproject.certification.impl;

import org.sakaiproject.certification.api.CertificateDefinitionStatus;

import org.sakaiproject.springframework.orm.hibernate.EnumUserType;

public class CertificateDefinitionStatusEnumUserType extends EnumUserType<CertificateDefinitionStatus>
{
    public CertificateDefinitionStatusEnumUserType()
    {
        super(CertificateDefinitionStatus.class);
    }
}
