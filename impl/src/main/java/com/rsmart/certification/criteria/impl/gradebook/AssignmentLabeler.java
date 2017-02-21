package com.rsmart.certification.criteria.impl.gradebook;

import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * User: duffy
 * Date: Jul 18, 2011
 * Time: 11:15:05 PM
 */
public interface AssignmentLabeler
{
    public String getLabel(Assignment assignment);
}
