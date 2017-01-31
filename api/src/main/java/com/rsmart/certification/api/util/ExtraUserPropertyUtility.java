package com.rsmart.certification.api.util;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import org.sakaiproject.user.api.User;

/**
 *
 */
public interface ExtraUserPropertyUtility
{
    public static final String NULL_DISPLAY_VALUE= "";

    /**
     * Determines if the extra user properties feature has been enabled globally.
     * This is controlled via sakai.properties
     * @return
     */
    public boolean isExtraUserPropertiesEnabled();

    /**
     * Returns a map containing the extra properties for the given user.
     * If a property value is not found for the user, or the user's account type prohibits display,
     * the NULL_DISPLAY_VALUE will be substituted for that property value
     *
     * @param user the Sakai user to retrieve properties for
     * @return a map containing the extra properties, or an empty map if something goes wrong. Will not return null
     */
    public Map<String, String> getExtraPropertiesMapForUser(User user);

    /**
     * Returns  a map containing the extra properties for the given user.
     * If a property value is not found for the user, or the user's account type prohibits display,
     * the NULL_DISPLAY_VALUE will be substituted for that property value
     *
     * @param eid the EID of the Sakai user fto retrieve properties for
     * @return a map containing the extra properties, or an empty map if something goes wrong. Will not return null
     */
    public Map<String, String> getExtraPropertiesMapForUserByEid(String eid);

    /**
     * Returns a map containing the extra properties for the given user.
     * If a property value is not found for the user, or the user's account type prohibits display,
     * the NULL_DISPLAY_VALUE will be substituted for that property value
     *
     * @param uid the internal UID of the Sakai user to retrieve properties for (this is NOT a username)
     * @return a map containing the extra properties, or an empty map if something goes wrong. Will not return null.
     */
    public Map<String, String> getExtraPropertiesMapForUserByUid(String uid);

    /**
     * Checks the permission for the current user to determine if they are allowed to view extra user properties
     * @return true if the user isallowed to view extra user properties
     */
    public boolean isExtraPropertyViewingAllowedForCurrentUser();

    /**
     * Returns a read-only map of property keys to column titles
     *
     * @return an immutable map, possibly empty. Will not return null
     */
    public Map<String, String> getExtraUserPropertiesKeyAndTitleMap();

    /**
     * Conveneience method to return the key and title map as a set of map entries,
     * which makes it easier to iterate over user JSF tags. Back by an immutable map.
     * @return a set of map entires, possibly empty
     */
    public Set<Map.Entry<String, String>> getExtraUserPropertyKeyAndTitleMapAsSet();

    /**
     * Given a column title, returns the preopty key associated with that column
     *
     * @param title the title of the column of interest
     * @return the property key for the column, or an empty string i fno found or key was null. Will not return null
     */
    public String getKeyForTitle(String title);

    /**
     * Every user has a uid. This method returns a comparator for uid strings based not on the string itself, but on other properties of the user.
     * The specific porperty to compare on is passed in on  the constructor.
     *
     * @param sortKey user property key to compare on
     * @return a comparator for user uid strings. Comparison based not on string but on user properties. Will not reutnr null
     */
    public Comparator<String> getUidComparator(String sortKey);
}
