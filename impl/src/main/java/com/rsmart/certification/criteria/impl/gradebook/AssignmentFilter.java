package com.rsmart.certification.criteria.impl.gradebook;

import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * User: duffy
 * Date: Jul 19, 2011
 * Time: 12:02:42 AM
 */
public interface AssignmentFilter
{
    public boolean include (Assignment assignment);
}
