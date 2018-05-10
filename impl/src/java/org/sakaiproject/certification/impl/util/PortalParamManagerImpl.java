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

package org.sakaiproject.certification.impl.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.sakaiproject.certification.api.util.PortalParamManager;

public class PortalParamManagerImpl implements PortalParamManager {

    private List<String> parameters = null;

    public Map<String, String> getParams(ServletRequest request) {
        Map<String, String> map = new HashMap<>();
        for(String key : parameters) {
            String value = request.getParameter(key);
            if (value == null) {
                value = (String) request.getAttribute(key);
            }

            if (value != null) {
                map.put(key, value);
            }
        }

        return map;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }
}
