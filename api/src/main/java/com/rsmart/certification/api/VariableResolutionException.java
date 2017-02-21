package com.rsmart.certification.api;

/**
 * User: duffy
 * Date: Jul 7, 2011
 * Time: 8:23:49 AM
 */
public class VariableResolutionException extends Exception
{
    public VariableResolutionException()
    {
        super();
    }

    public VariableResolutionException(String s)
    {
        super(s);
    }

    public VariableResolutionException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public VariableResolutionException(Throwable throwable)
    {
        super(throwable);
    }
}
