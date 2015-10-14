/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msub/rsmart.com/metaobj/trunk/metaobj-util/tool-lib/src/java/org/sakaiproject/metaobj/shared/control/SakaiStyleSheetInterceptor.java $
 * $Id: SakaiStyleSheetInterceptor.java 314667 2014-10-20 22:24:11Z bbiltimier@anisakai.com $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package com.rsmart.certification.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;




public class SakaiStyleSheetInterceptor extends HandlerInterceptorAdapter {
    protected SiteService siteService;
    protected ToolManager toolManager;

    private static Log M_log = LogFactory.getLog(SakaiStyleSheetInterceptor.class);

    public SakaiStyleSheetInterceptor() {
           }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // code borrowed from sakai's VmServlet.setVmStdRef() method
        // form the skin based on the current site, and the defaults as configured
        //String skinRoot = ServerConfigurationService.getString("skin.root", "/sakai-shared/css/");
        String skinRoot = ServerConfigurationService.getString("skin.repo", "/library/skin");
        String skin = ServerConfigurationService.getString("skin.default", "default");

        String siteId = toolManager.getCurrentPlacement().getContext();

        if (siteId != null) {
            String siteSkin = siteService.getSiteSkin(siteId);
            if (siteSkin != null) {
                skin = siteSkin;
            }
            request.setAttribute("sakai_skin_base", skinRoot + "/tool_base.css");
            request.setAttribute("sakai_skin", skinRoot + "/" + skin + "/tool.css");

            //TODO figure out if this is still needed
            // form the portal root for the skin - removing the .css and adding "portalskins" before
            int pos = skin.indexOf(".css");
            if (pos != -1) {
                skin = skin.substring(0, pos);
            }

            request.setAttribute("sakai_portalskin", skinRoot + "portalskins" + "/" + skin + "/");
            request.setAttribute("sakai_skin_id", skin);
        }
    }
    public ToolManager getToolManager() {
        return toolManager;
    }

    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
