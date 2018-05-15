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

package org.sakaiproject.certification.impl;

import java.util.HashMap;
import java.util.Set;

import org.sakaiproject.certification.api.VariableResolver;
import org.sakaiproject.util.ResourceLoader;

public abstract class AbstractVariableResolver implements VariableResolver {

    private final ResourceLoader messages = new ResourceLoader("org.sakaiproject.certification.Messages");
    private final HashMap<String, String> descriptions = new HashMap<>();

    public void addVariable (String variable, String description) {
        descriptions.put(variable, description);
    }

    public Set<String> getVariableLabels() {
        return descriptions.keySet();
    }

    public String getVariableDescription(String key) {
        return descriptions.get(key);
    }

    public ResourceLoader getMessages() {
        return messages;
    }
}
