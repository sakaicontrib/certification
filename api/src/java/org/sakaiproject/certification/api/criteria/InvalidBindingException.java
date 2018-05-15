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

package org.sakaiproject.certification.api.criteria;

public class InvalidBindingException extends CriterionCreationException {

    private String bindingKey;
    private String bindingValue;
    private String localizedMessage;

    public InvalidBindingException() {
        super();
    }

    public InvalidBindingException(String s) {
        super(s);
    }

    public InvalidBindingException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public InvalidBindingException(Throwable throwable) {
        super(throwable);
    }

    public String getBindingKey() {
        return bindingKey;
    }

    public void setBindingKey(String bindingKey) {
        this.bindingKey = bindingKey;
    }

    public String getBindingValue() {
        return bindingValue;
    }

    public void setBindingValue(String bindingValue) {
        this.bindingValue = bindingValue;
    }

    public String getLocalizedMessage() {
        if (localizedMessage != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("ERROR_MESSAGE").append(localizedMessage).append("/ERROR_MESSAGE");
            return sb.toString();
        }

        return null;
    }

    public void setLocalizedMessage(String localizedMessage) {
        this.localizedMessage = localizedMessage;
    }
}
