package com.rsmart.certification.api;

import java.util.Set;

/**
 * User: duffy
 * Date: Jul 5, 2011
 * Time: 8:59:48 AM
 */
public interface VariableResolver
{
    public static final String CERT_NAME = "cert.name";
    public static final String UNASSIGNED = "unassigned";
    public static final String FULL_NAME = "recipient.fullname";
    public static final String FIRST_NAME = "recipient.firstname";
    public static final String LAST_NAME = "recipient.lastname";
    public static final String CERT_EXPIREDATE = "cert.expiredate";
    public static final String CERT_AWARDDATE = "cert.date";

    public Set<String> getVariableLabels();

    public String getVariableDescription(String varLabel);

    public String getValue(CertificateDefinition certDef, String varLabel, String userId) throws VariableResolutionException;
}
