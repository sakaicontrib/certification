/**
 * Copyright (c) 2003-2018 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.certification.api;

/**
 * User: duffy
 * Date: Jun 9, 2011
 * Time: 11:15:58 AM
 */
public class CertificationException extends Exception
{
    public CertificationException()
    {
        super();
    }

    public CertificationException(String message)
    {
        super(message);
    }

    public CertificationException(Throwable t)
    {
        super(t);
    }

    public CertificationException(String message, Throwable t)
    {
        super(message, t);
    }
}
