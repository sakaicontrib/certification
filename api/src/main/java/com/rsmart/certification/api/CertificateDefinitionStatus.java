package com.rsmart.certification.api;

/**
 * CertificateDefinition objects may be in one of three states:
 *
 *      INCOMPLETE  - The CertificateDefinition has not yet been fully defined
 *      ACTIVE      - The CertificateDefinition is in use and can be used for awards
 *      UNPUBLISHED    - The CertificateDefinition is not presently available for awards
 *
 * User: duffy
 * Date: Jun 9, 2011
 * Time: 10:43:15 AM
 */
public enum CertificateDefinitionStatus
{
    UNPUBLISHED,
    ACTIVE,
    INACTIVE,
}
