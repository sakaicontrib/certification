package com.rsmart.certification.api;

/**
 * @author bbailla2
 *
 * Thrown when creating a certificate definition, but some constraint isn't met
 */
public class InvalidCertificateDefinitionException extends CertificationException
{
    public static final int REASON_TOO_LONG = 1;

    private int invalidField = 0;
    private int reason = 0;

    public InvalidCertificateDefinitionException()
    {
        super();
    }

    public InvalidCertificateDefinitionException(String message)
    {
        super(message);
    }

    public InvalidCertificateDefinitionException(String message, Throwable t)
    {
        super(message, t);
    }

    public InvalidCertificateDefinitionException(Throwable t)
    {
        super(t);
    }

    public void setInvalidField(int field)
    {
        this.invalidField = field;
    }

    public int getInvalidField()
    {
        return invalidField;
    }

    public void setReason(int reason)
    {
        this.reason = reason;
    }

    public int getReason()
    {
        return reason;
    }
}
