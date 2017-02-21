package com.rsmart.certification.api;

/**
 * User: duffy
 * Date: Jul 11, 2011
 * Time: 12:15:35 PM
 */
public class UnsupportedTemplateTypeException extends DocumentTemplateException
{
    public UnsupportedTemplateTypeException()
    {
        super();
    }

    public UnsupportedTemplateTypeException(String s)
    {
        super(s);
    }

    public UnsupportedTemplateTypeException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public UnsupportedTemplateTypeException(Throwable throwable)
    {
        super(throwable);
    }
}
