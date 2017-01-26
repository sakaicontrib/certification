package com.rsmart.certification.impl;

import com.rsmart.certification.api.CertificateAward;
import com.rsmart.certification.api.VariableResolutionException;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;


/**
 * User: duffy
 * Date: Jul 7, 2011
 * Time: 7:52:26 AM
 */
public class RecipientVariableResolver
    extends AbstractVariableResolver
{
    private UserDirectoryService
        uds = null;

    private static final String
        FULL_NAME               =       "recipient.fullname",
        FIRST_NAME              =       "recipient.firstname",
        LAST_NAME               =       "recipient.lastname";

    public RecipientVariableResolver()
    {
        //TODO: Internationalize
        addVariable (FULL_NAME, "full name of the recipient");
        addVariable (FIRST_NAME, "first name of the recipient");
        addVariable (LAST_NAME, "last name of the recipient");
    }

    public void setUserDirectoryService(UserDirectoryService uds)
    {
        this.uds = uds;
    }

    public UserDirectoryService getUserDirectoryService()
    {
        return uds;
    }
    
    public String getValue(CertificateAward award, String key)
        throws VariableResolutionException
    {
        User
            user = null;
        try
        {
            user = getUserDirectoryService().getUser(award.getUserId());
        }
        catch (UserNotDefinedException e)
        {
            throw new VariableResolutionException("could not resolve variable \"" + key + "\"", e);
        }

        if (FULL_NAME.equals(key))
        {
            return user.getFirstName() + " " + user.getLastName();
        }
        else if (FIRST_NAME.equals(key))
        {
            return user.getFirstName();
        }
        else if (LAST_NAME.equals(key))
        {
            return user.getLastName();
        }

        throw new VariableResolutionException ("key \"" + key + "\" has not been resolved");
    }
}
