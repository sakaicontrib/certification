package com.rsmart.certification.api;

/**
 * User: duffy
 * Date: Jun 9, 2011
 * Time: 1:05:51 PM
 */
public class TemplateReadException extends DocumentTemplateException
{
    public TemplateReadException()
    {
        super();
    }

    public TemplateReadException(String s)
    {
        super(s);
    }

    public TemplateReadException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public TemplateReadException(Throwable throwable)
    {
        super(throwable);
    }
}
