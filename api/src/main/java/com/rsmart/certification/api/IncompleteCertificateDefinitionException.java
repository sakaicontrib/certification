package com.rsmart.certification.api;

/**
 * User: duffy
 * Date: Jun 9, 2011
 * Time: 12:41:42 PM
 */
public class IncompleteCertificateDefinitionException extends CertificationException
{
    public IncompleteCertificateDefinitionException()
    {
        super();
    }

    public IncompleteCertificateDefinitionException(String message)
    {
        super(message);
    }

    public IncompleteCertificateDefinitionException(String message, Throwable t)
    {
        super(message, t);
    }

    public IncompleteCertificateDefinitionException(Throwable t)
    {
        super(t);
    }
}
