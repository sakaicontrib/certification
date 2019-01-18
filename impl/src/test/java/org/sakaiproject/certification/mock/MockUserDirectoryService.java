package org.sakaiproject.certification.mock;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.PasswordPolicyProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserLockedException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MockUserDirectoryService implements UserDirectoryService
{
    public UserEdit addUser(String id, String eid) throws UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException
    {
        return null;
    }

    public User addUser(String id, String eid, String firstName, String lastName, String email, String pw, String type, ResourceProperties properties) throws UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException
    {
        return null;
    }

    public boolean allowAddUser()
    {
        return false;
    }

    public boolean allowRemoveUser(String id)
    {
        return false;
    }

    public boolean allowUpdateUser(String id)
    {
        return false;
    }

    public boolean allowUpdateUserName(String id)
    {
        return false;
    }

    public boolean allowUpdateUserEmail(String id)
    {
        return false;
    }

    public boolean allowUpdateUserPassword(String id)
    {
        return false;
    }

    public boolean allowUpdateUserType(String id)
    {
        return false;
    }

    public User authenticate(String loginId, String password)
    {
        return null;
    }

    public void cancelEdit(UserEdit user) {}

    public void commitEdit(UserEdit user) throws UserAlreadyDefinedException {}

    public int countSearchUsers(String criteria)
    {
        return 0;
    }

    public int countUsers()
    {
        return 0;
    }

    public void destroyAuthentication() {}

    public UserEdit editUser(String id) throws UserNotDefinedException, UserPermissionException, UserLockedException
    {
        return null;
    }

    public Collection<User> findUsersByEmail(String email)
    {
        return null;
    }

    public User getAnonymousUser()
    {
        return null;
    }

    public User getCurrentUser()
    {
        return new User()
        {
            public User getCreatedBy()
            {
                return null;
            }

            @Override
            public String getDisplayId( String context )
            {
                return null;
            }

            @Override
            public String getDisplayName( String context )
            {
                return null;
            }

            public User getModifiedBy()
            {
                return null;
            }

            public Time getCreatedTime()
            {
                return null;
            }

            public Date getCreatedDate()
            {
                return null;
            }

            public Time getModifiedTime()
            {
                return null;
            }

            public Date getModifiedDate()
            {
                return null;
            }

            public String getEmail()
            {
                return null;
            }

            public String getDisplayName()
            {
                return "Mock User";
            }

            public String getSortName()
            {
                return null;
            }

            public String getFirstName()
            {
                return null;
            }

            public String getLastName()
            {
                return null;
            }

            public boolean checkPassword(String pw)
            {
                return false;
            }

            public String getType()
            {
                return null;
            }

            public String getEid()
            {
                return null;
            }

            public String getDisplayId()
            {
                return null;
            }

            public int compareTo(Object o)
            {
                return 0;
            }

            public String getUrl()
            {
                return null;
            }

            public String getReference()
            {
                return null;
            }

            public String getUrl(String rootProperty)
            {
                return null;
            }

            public String getReference(String rootProperty)
            {
                return null;
            }

            public String getId()
            {
                return "mockuser";
            }

            public ResourceProperties getProperties()
            {
                return null;
            }

            public Element toXml(Document doc, Stack stack)
            {
                return null;
            }
        };
    }

    public User getUser(String id) throws UserNotDefinedException
    {
        return null;
    }

    public User getUserByEid(String eid) throws UserNotDefinedException
    {
        return null;
    }

    public String getUserEid(String id) throws UserNotDefinedException
    {
        return null;
    }

    public String getUserId(String eid) throws UserNotDefinedException
    {
        return null;
    }

    public List<User> getUsers()
    {
        return null;
    }

    public List<User> getUsers(Collection<String> ids)
    {
        return null;
    }

    public List<User> getUsers(int first, int last)
    {
        return null;
    }

    public List<User> getUsersByEids(Collection<String> eids)
    {
        return null;
    }

    public UserEdit mergeUser(Element el) throws UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException
    {
        return null;
    }

    public void removeUser(UserEdit user) throws UserPermissionException {}

    public List<User> searchUsers(String criteria, int first, int last)
    {
        return null;
    }

    public List<User> searchExternalUsers(String criteria, int first, int last)
    {
        return null;
    }

    @Override
    public boolean updateUserEid(String id, String newEid)
    {
        return false;
    }

    public String userReference(String id)
    {
        return null;
    }

    public String getLabel()
    {
        return null;
    }

    public boolean willArchiveMerge()
    {
        return false;
    }

    public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
    {
        return null;
    }

    public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport)
    {
        return null;
    }

    public boolean parseEntityReference(String reference, Reference ref)
    {
        return false;
    }

    public String getEntityDescription(Reference ref)
    {
        return null;
    }

    public ResourceProperties getEntityResourceProperties(Reference ref)
    {
        return null;
    }

    public Entity getEntity(Reference ref)
    {
        return null;
    }

    public String getEntityUrl(Reference ref)
    {
        return null;
    }

    public Collection getEntityAuthzGroups(Reference ref, String userId)
    {
        return null;
    }

    public HttpAccess getHttpAccess()
    {
        return null;
    }

    @Override
    public PasswordRating validatePassword(String password, User user)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PasswordPolicyProvider getPasswordPolicy()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean updateUserId(String eId, String newEmail)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean checkDuplicatedEmail(User user)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public User getUserByAid(String aid) throws UserNotDefinedException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
