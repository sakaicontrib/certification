package com.rsmart.certification.mock;

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

/**
 * User: duffy
 * Date: Jun 27, 2011
 * Time: 1:30:56 PM
 */
public class MockUserDirectoryService
    implements UserDirectoryService
{
    public UserEdit addUser(String id, String eid) throws UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public User addUser(String id, String eid, String firstName, String lastName, String email, String pw, String type, ResourceProperties properties) throws UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean allowAddUser() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean allowRemoveUser(String id) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean allowUpdateUser(String id) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean allowUpdateUserName(String id) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean allowUpdateUserEmail(String id) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean allowUpdateUserPassword(String id) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean allowUpdateUserType(String id) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public User authenticate(String loginId, String password) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void cancelEdit(UserEdit user) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean checkDuplicatedEmail( User user )
    {
        return false;
    }

    public void commitEdit(UserEdit user) throws UserAlreadyDefinedException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int countSearchUsers(String criteria) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int countUsers() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void destroyAuthentication() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public UserEdit editUser(String id) throws UserNotDefinedException, UserPermissionException, UserLockedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection<User> findUsersByEmail(String email) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public User getAnonymousUser() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public User getCurrentUser()
    {
        return
            new User()
            {
                public User getCreatedBy() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public User getModifiedBy() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public Time getCreatedTime() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public Date getCreatedDate() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public Time getModifiedTime() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public Date getModifiedDate() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public String getEmail() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public String getDisplayName() {
                    return "Mock User";  //To change body of implemented methods use File | Settings | File Templates.
                }

                public String getSortName() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public String getFirstName() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public String getLastName() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public boolean checkPassword(String pw) {
                    return false;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public String getType() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public String getEid() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public String getDisplayId() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public int compareTo(Object o) {
                    return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public String getUrl() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public String getReference() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public String getUrl(String rootProperty) {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public String getReference(String rootProperty) {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public String getId() {
                    return "mockuser";  //To change body of implemented methods use File | Settings | File Templates.
                }

                public ResourceProperties getProperties() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public Element toXml(Document doc, Stack stack) {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }
            };
    }

    @Override
    public PasswordPolicyProvider getPasswordPolicy()
    {
        return null;
    }

    public User getUser(String id) throws UserNotDefinedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public User getUserByAid( String aid ) throws UserNotDefinedException
    {
        return null;
    }

    public User getUserByEid(String eid) throws UserNotDefinedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUserEid(String id) throws UserNotDefinedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUserId(String eid) throws UserNotDefinedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<User> getUsers() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<User> getUsers(Collection<String> ids) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<User> getUsers(int first, int last) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<User> getUsersByEids(Collection<String> eids) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public UserEdit mergeUser(Element el) throws UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeUser(UserEdit user) throws UserPermissionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<User> searchUsers(String criteria, int first, int last) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<User> searchExternalUsers(String criteria, int first, int last) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean updateUserId( String eId, String newEmail )
    {
        return false;
    }

    public String userReference(String id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getLabel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PasswordRating validatePassword( String password, User user )
    {
        return null;
    }

    public boolean willArchiveMerge() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean parseEntityReference(String reference, Reference ref) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getEntityDescription(Reference ref) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResourceProperties getEntityResourceProperties(Reference ref) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Entity getEntity(Reference ref) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getEntityUrl(Reference ref) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection getEntityAuthzGroups(Reference ref, String userId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public HttpAccess getHttpAccess() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
