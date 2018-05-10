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

package org.sakaiproject.certification.tool.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.servlet.tags.RequestContextAwareTag;

abstract public class AbstractLocalizableTag extends RequestContextAwareTag {

    /**
     * Resolve the specified message into a concrete message String.
     * The returned message String should be unescaped.
     * @param message
     * @return
     * @throws javax.servlet.jsp.JspException
     */
    protected String resolveMessage(String message) throws JspException, NoSuchMessageException {
        MessageSource messageSource = getMessageSource();
        if (messageSource == null) {
            throw new JspTagException("No corresponding MessageSource found");
        }

        return messageSource.getMessage(message, null, "??" + message + "??", getRequestContext().getLocale());
    }

    /**
     * Resolve the specified message into a concrete message String.
     * The returned message String should be unescaped.
     * @param message
     * @param args
     * @return
     * @throws javax.servlet.jsp.JspException
     */
    protected String resolveMessage(String message, Object[] args) throws JspException, NoSuchMessageException {
        MessageSource messageSource = getMessageSource();
        if (messageSource == null) {
            throw new JspTagException("No corresponding MessageSource found");
        }

        return messageSource.getMessage(message, args, "??" + message + "??", getRequestContext().getLocale());
    }

    /**
     * Use the application context itself for default message resolution.
     * @return
     */
    protected MessageSource getMessageSource() {
        return getRequestContext().getWebApplicationContext();
    }
}
