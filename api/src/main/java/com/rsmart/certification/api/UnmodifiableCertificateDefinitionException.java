package com.rsmart.certification.api;

/**
 * User: duffy
 * Date: Jun 30, 2011
 * Time: 3:54:38 PM
 */
public class UnmodifiableCertificateDefinitionException extends CertificationException
{
    public UnmodifiableCertificateDefinitionException()
    {
        super();
    }

    public UnmodifiableCertificateDefinitionException(String s)
    {
        super(s);
    }

    public UnmodifiableCertificateDefinitionException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public UnmodifiableCertificateDefinitionException(Throwable throwable)
    {
        super(throwable);
    }
}
