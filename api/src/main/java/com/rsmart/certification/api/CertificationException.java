package com.rsmart.certification.api;

/**
 * User: duffy
 * Date: Jun 9, 2011
 * Time: 11:15:58 AM
 */
public class CertificationException extends Exception
{
    public CertificationException()
    {
        super();
    }

    public CertificationException(String message)
    {
        super(message);
    }

    public CertificationException(Throwable t)
    {
        super(t);
    }

    public CertificationException(String message, Throwable t)
    {
        super(message, t);
    }
}
