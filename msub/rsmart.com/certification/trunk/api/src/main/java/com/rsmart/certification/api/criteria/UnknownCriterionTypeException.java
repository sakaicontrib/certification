package com.rsmart.certification.api.criteria;

/**
 * User: duffy
 * Date: Jun 28, 2011
 * Time: 4:22:16 PM
 */
public class UnknownCriterionTypeException extends Exception
{
    public UnknownCriterionTypeException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public UnknownCriterionTypeException(String s) {
        super(s);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public UnknownCriterionTypeException(String s, Throwable throwable) {
        super(s, throwable);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public UnknownCriterionTypeException(Throwable throwable) {
        super(throwable);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
