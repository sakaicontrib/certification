package com.rsmart.certification.impl;

/**
 * com.rsmart.certification.criteria.impl.gradebook's interface wasn't public. 
 * I assume there's a security reason for this, so I decided to create the exact same thing in this package
 * @author bbailla2
 *
 */
interface SecureGradebookActionCallback
{
    public Object doSecureAction();
}
