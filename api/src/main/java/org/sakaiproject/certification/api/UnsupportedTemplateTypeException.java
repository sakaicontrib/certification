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
 * Date: Jul 11, 2011
 * Time: 12:15:35 PM
 */
public class UnsupportedTemplateTypeException extends DocumentTemplateException
{
    public UnsupportedTemplateTypeException()
    {
        super();
    }

    public UnsupportedTemplateTypeException(String s)
    {
        super(s);
    }

    public UnsupportedTemplateTypeException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public UnsupportedTemplateTypeException(Throwable throwable)
    {
        super(throwable);
    }
}
