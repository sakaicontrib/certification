package com.rsmart.certification.api.criteria;

/**
 * User: duffy
 * Date: Jul 15, 2011
 * Time: 4:46:54 PM
 */
public class InvalidBindingException
    extends CriterionCreationException
{
    private String
        bindingKey,
        bindingValue,
        localizedMessage;

    public InvalidBindingException()
    {
        super();
    }

    public InvalidBindingException(String s)
    {
        super(s);
    }

    public InvalidBindingException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public InvalidBindingException(Throwable throwable)
    {
        super(throwable);
    }

    public String getBindingKey() {
        return bindingKey;
    }

    public void setBindingKey(String bindingKey) {
        this.bindingKey = bindingKey;
    }

    public String getBindingValue() {
        return bindingValue;
    }

    public void setBindingValue(String bindingValue) {
        this.bindingValue = bindingValue;
    }

    public String getLocalizedMessage() {
        if (localizedMessage != null)
        {
            StringBuffer
                sb = new StringBuffer();

            sb.append("ERROR_MESSAGE").append(localizedMessage).append("/ERROR_MESSAGE");

            return sb.toString();
        }
        return null;
    }

    public void setLocalizedMessage(String localizedMessage) {
        this.localizedMessage = localizedMessage;
    }
}