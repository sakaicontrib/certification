package com.rsmart.certification.api.criteria;

/**
 * User: duffy
 * Date: Jun 28, 2011
 * Time: 4:22:16 PM
 */
public class UnknownCriterionTypeException extends Exception
{
    public UnknownCriterionTypeException()
    {
        super();
    }

    public UnknownCriterionTypeException(String s)
    {
        super(s);
    }

    public UnknownCriterionTypeException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public UnknownCriterionTypeException(Throwable throwable)
    {
        super(throwable);
    }
}
