package com.rsmart.certification.api;

/**
 * User: duffy
 * Date: Jul 11, 2011
 * Time: 12:15:35 PM
 */
public class UnsupportedTemplateTypeException
    extends DocumentTemplateException
{
    public UnsupportedTemplateTypeException()
    {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public UnsupportedTemplateTypeException(String s)
    {
        super(s);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public UnsupportedTemplateTypeException(String s, Throwable throwable)
    {
        super(s, throwable);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public UnsupportedTemplateTypeException(Throwable throwable)
    {
        super(throwable);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
