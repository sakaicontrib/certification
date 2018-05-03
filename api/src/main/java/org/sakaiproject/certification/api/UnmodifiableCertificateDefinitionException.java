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
 * Date: Jun 30, 2011
 * Time: 3:54:38 PM
 */
public class UnmodifiableCertificateDefinitionException extends CertificationException
{
    public UnmodifiableCertificateDefinitionException()
    {
        super();
    }

    public UnmodifiableCertificateDefinitionException(String s)
    {
        super(s);
    }

    public UnmodifiableCertificateDefinitionException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public UnmodifiableCertificateDefinitionException(Throwable throwable)
    {
        super(throwable);
    }
}
