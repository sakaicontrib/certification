package com.rsmart.certification.impl.hibernate;

import com.rsmart.certification.api.CertificateDefinitionStatus;

import org.sakaiproject.springframework.orm.hibernate.EnumUserType;

/**
 * User: duffy
 * Date: Jun 28, 2011
 * Time: 11:55:46 AM
 */
public class CertificateDefinitionStatusEnumUserType extends EnumUserType<CertificateDefinitionStatus>
{
    public CertificateDefinitionStatusEnumUserType()
    {
        super(CertificateDefinitionStatus.class);
    }
}
