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

package org.sakaiproject.certification.tool.control;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.certification.api.util.PortalParamManager;
import org.sakaiproject.component.cover.ComponentManager;

public class RedirectView extends org.springframework.web.servlet.view.RedirectView {

    /**
     * Determine whether the given model element should be exposed as a
     * query property. The default implementation considers Strings and
     * primitives as eligible, and also arrays and Collections/Iterables
     * with corresponding elements..
     *
     * This is changed behavior from Spring 2.0, so we always return true
     * for backward compatibility.
     * @param key
     * @param value
     * @return
     */
    protected boolean isEligibleProperty(String key, Object value) {
        return true;
    }

    /**
     * Determine whether the given model element should be exposed as a
     * query property. The default implementation considers Strings and
     * primitives as eligible, and also arrays and Collections/Iterables
     * with corresponding elements..
     *
     * This is changed behavior from Spring 2.0, so we always return true
     * for backward compatibility.
     * @param value
     * @return
     */
    protected boolean isEligibleValue(Object value) {
        return true;
    }

    /**
     * Prepares the view given the specified model, merging it with static
     * attributes and a RequestContext attribute, if necessary.
     * Delegates to renderMergedOutputModel for the actual rendering.
     *
     * @param model
     * @param request
     * @param response
     * @throws java.lang.Exception
     * @see #renderMergedOutputModel
     */
    public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (model == null) {
            model = new HashMap();
        }
        model.putAll(getPortalParamManager().getParams(request));
        super.render(model, request, response);
    }

    protected PortalParamManager getPortalParamManager() {
        return (PortalParamManager) ComponentManager.getInstance().get(PortalParamManager.class.getName());
    }
}
