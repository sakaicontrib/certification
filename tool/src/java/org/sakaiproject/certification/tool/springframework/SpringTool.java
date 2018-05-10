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

package org.sakaiproject.certification.tool.springframework;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.Web;

/**
 * this class is a replacement for org.sakaiproject.spring.util.SpringTool.
 * it replaces the hard coded values with servlet init parameters.
 */
@Slf4j
public class SpringTool extends HttpServlet {

    // The following fields below are all configurable by setting their values within the <servlet> tag in the web.xml file.
    // The fields all have default values, so you only have to specify values for the ones you want to change.
    private static final String INIT_PARAM_NAME_DEFAULT_PAGE          = "default_page";
    private static final String INIT_PARAM_NAME_DEFAULT_TO_LAST_VIEW  = "default_to_last_view";
    private static final String INIT_PARAM_NAME_HELPER_EXTENSION      = "helper_extension";
    private static final String INIT_PARAM_NAME_HELPER_SESSION_PREFIX = "helper_session_prefix";
    private static final String INIT_PARAM_NAME_JSP_PATH              = "jsp_path";
    private static final String INIT_PARAM_NAME_LAST_VIEW_VISITED     = "last_view_visited";
    private static final String INIT_PARAM_NAME_REQUEST_EXTENSION     = "request_extension";
    private static final String INIT_PARAM_NAME_URL_EXTENSION         = "url_extension";
    private static final String INIT_PARAM_NAME_URL_PATH              = "url_path";

    // data members
    private String helperSessionPrefix;
    private String helperExt;
    //  TODO: Note, these two values must match those in jsf-app's SakaiViewHandler
    /**
     * The default resource (jsp page) to return.  This is the first page the user will see when s/he clicks on the tool.
     */
    protected String defaultResource;
    /**
     * if true, we preserve the last visit per placement / user, and use it if we get a request with no path.
     */
    protected boolean defaultToLastView;
    /**
     * The folder where the jsp files are located.
     */
    protected String jspPath;
    /**
     * Session attribute to hold the last view visited.
     */
    public String lastViewVisited;
    /**
     * The file extension for requests.  This should be the same extension specified in the <url-pattern> tag under the <servlet-mapping> in the web.xml file.
     */
    protected String requestExt;
    /**
     * Request attribute we set to help the return URL know what extension we add (does not need to be in the URL).
     */
    public String urlExt;
    /**
     * Request attribute we set to help the return URL know what path we add (does not need to be in the URL).
     */
    public String urlPath;

    /**
     * initialize the servlet with the <init-param> values specified in the web.xml file.
     * @param servletConfig
     * @throws javax.servlet.ServletException
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        String value;

        defaultResource     = ((value = servletConfig.getInitParameter(INIT_PARAM_NAME_DEFAULT_PAGE         )) == null || value.trim().length() == 0 ? "index"                            : value);
        defaultToLastView   = ((value = servletConfig.getInitParameter(INIT_PARAM_NAME_DEFAULT_TO_LAST_VIEW )) == null || value.trim().length() == 0 ? false                               : Boolean.valueOf(value));
        helperExt           = ((value = servletConfig.getInitParameter(INIT_PARAM_NAME_HELPER_EXTENSION     )) == null || value.trim().length() == 0 ? ".helper"                          : value);
        helperSessionPrefix = ((value = servletConfig.getInitParameter(INIT_PARAM_NAME_HELPER_SESSION_PREFIX)) == null || value.trim().length() == 0 ? "session."                         : value);
        jspPath             = ((value = servletConfig.getInitParameter(INIT_PARAM_NAME_JSP_PATH             )) == null || value.trim().length() == 0 ? ""                                 : value);
        lastViewVisited     = ((value = servletConfig.getInitParameter(INIT_PARAM_NAME_LAST_VIEW_VISITED    )) == null || value.trim().length() == 0 ? "sakai.jsf.tool.last.view.visited" : value);
        requestExt          = ((value = servletConfig.getInitParameter(INIT_PARAM_NAME_REQUEST_EXTENSION    )) == null || value.trim().length() == 0 ? ".osp"                             : value);
        urlExt              = ((value = servletConfig.getInitParameter(INIT_PARAM_NAME_URL_EXTENSION        )) == null || value.trim().length() == 0 ? "sakai.jsf.tool.URL.ext"           : value);
        urlPath             = ((value = servletConfig.getInitParameter(INIT_PARAM_NAME_URL_PATH             )) == null || value.trim().length() == 0 ? "sakai.jsf.tool.URL.path"          : value);

        // remove the trailing slash
        if (jspPath.endsWith("/")){
            jspPath = jspPath.substring(0, jspPath.length() - 1);
        }

        log.info("init: default: {} path: {}", defaultResource, jspPath);
    }

    /**
     * Compute a target (i.e. the servlet path info, not including folder root or jsf extension) for the case of the actual path being empty.
     *
     * @param lastVisited
     * @return The servlet info path target computed for the case of empty actual path.
     */
    protected String computeDefaultTarget(boolean lastVisited) {
        // setup for the default view as configured
        String target = "/" + defaultResource;

        // if we are doing lastVisit and there's a last-visited view, for this tool placement / user, use that
        if (lastVisited) {
            ToolSession session = SessionManager.getCurrentToolSession();
            String last = (String) session.getAttribute(lastViewVisited);
            if (last != null) {
               target = last;
            }
        }

        return target;
    }

    protected String computeDefaultTarget() {
        return computeDefaultTarget(defaultToLastView);
    }

    /**
     * Shutdown the servlet.
     */
    public void destroy() {
        log.info("destroy");

        super.destroy();
    }

    /**
     * Respond to requests.
     *
     * @param req The servlet request.
     * @param res The servlet response.
     * @throws ServletException
     * @throws IOException
     */
    protected void dispatch(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // NOTE: this is a simple path dispatching, taking the path as the view id = jsp file name for the view,
        //       with default used if no path and a path prefix as configured.
        // TODO: need to allow other sorts of dispatching, such as pulling out drill-down ids and making them
        //       available to the JSF

        // build up the target that will be dispatched to
        String target = req.getPathInfo();

        // see if we have a helper request
        if (sendToHelper(req, res, target)) {
            return;
        }

        // see if we have a resource request - i.e. a path with an extension, and one that is not the requestExt
        if (isResourceRequest(target)) {
            // get a dispatcher to the path
            RequestDispatcher resourceDispatcher = getServletContext().getRequestDispatcher(target);
            if (resourceDispatcher != null) {
                resourceDispatcher.forward(req, res);
                return;
            }
        }

        if ("Title".equals(req.getParameter("panel"))) {
            // This allows only one Title JSF for each tool
            target = "/title.osp";
        } else {
            ToolSession session = SessionManager.getCurrentToolSession();

            if (target == null || "/".equals(target)) {
                target = computeDefaultTarget();

                // make sure it's a valid path
                if (!target.startsWith("/")) {
                    target = "/" + target;
                }

                // now that we've messed with the URL, send a redirect to make it official
                res.sendRedirect(Web.returnUrl(req, target));
                return;
            }

            // see if we want to change the specifically requested view
            String newTarget = redirectRequestedTarget(target);

            // make sure it's a valid path
            if (!newTarget.startsWith("/")) {
                newTarget = "/" + newTarget;
            }

            if (!newTarget.equals(target)) {
                // now that we've messed with the URL, send a redirect to make it official
                res.sendRedirect(Web.returnUrl(req, newTarget));
                return;
            }
            target = newTarget;

            // store this
            session.setAttribute(lastViewVisited, target);
        }

        // add the configured folder root and extension (if missing)
        target = jspPath + target;

        // add the default JSF extension (if we have no extension)
        int lastSlash = target.lastIndexOf("/");
        int lastDot = target.lastIndexOf(".");
        if (lastDot < 0 || lastDot < lastSlash) {
            target += requestExt;
        }

        // set the information that can be removed from return URLs
        req.setAttribute(urlPath, jspPath);
        req.setAttribute(urlExt, ".jsp");

        // set the sakai request object wrappers to provide the native, not Sakai set up, URL information
        // - this assures that the FacesServlet can dispatch to the proper view based on the path info
        req.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

        // TODO: Should setting the HTTP headers be moved up to the portal level as well?
        res.setContentType("text/html; charset=UTF-8");
        res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
        res.addDateHeader("Last-Modified", System.currentTimeMillis());
        res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
        res.addHeader("Pragma", "no-cache");

        // dispatch to the target
        log.debug("dispatching path: {} to: {} context: {}", req.getPathInfo(), target, getServletContext().getServletContextName());
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(target);
        dispatcher.forward(req, res);

        // restore the request object
        req.removeAttribute(Tool.NATIVE_URL);
        req.removeAttribute(urlPath);
        req.removeAttribute(urlExt);
    }

    protected boolean sendToHelper(HttpServletRequest req, HttpServletResponse res, String target) throws ToolException {
        String path = req.getPathInfo();
        if (path == null) {
            path = "/";
        }

        // 0 parts means the path was just "/", otherwise parts[0] = "", parts[1] = item id, parts[2] if present is "edit"...
        String[] parts = path.split("/");

        if (parts.length < 2) {
            return false;
        }

        if (!parts[1].endsWith(helperExt)) {
            return false;
        }

        ToolSession toolSession = SessionManager.getCurrentToolSession();

        Enumeration params = req.getParameterNames();
        while (params.hasMoreElements()) {
            String paramName = (String) params.nextElement();
            if (paramName.startsWith(helperSessionPrefix)) {
                String attributeName = paramName.substring(helperSessionPrefix.length());
                toolSession.setAttribute(attributeName, req.getParameter(paramName));
            }
        }

        // calc helper id
        int posEnd = parts[1].lastIndexOf(".");

        String helperId = target.substring(1, posEnd + 1);
        ActiveTool helperTool = ActiveToolManager.getActiveTool(helperId);

        if (toolSession.getAttribute(helperTool.getId() + Tool.HELPER_DONE_URL) == null) {
            toolSession.setAttribute(helperTool.getId() + Tool.HELPER_DONE_URL,
                req.getContextPath() + req.getServletPath() + computeDefaultTarget(true));
        }

        String context = req.getContextPath() + req.getServletPath() + Web.makePath(parts, 1, 2);
        String toolPath = Web.makePath(parts, 2, parts.length);
        helperTool.help(req, res, context, toolPath);

        return true; // was handled as helper call
    }

    /**
     * Respond to requests.
     *
     * @param req The servlet request.
     * @param res The servlet response.
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        dispatch(req, res);
    }

    /**
     * Respond to requests.
     *
     * @param req The servlet request.
     * @param res The servlet response.
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        dispatch(req, res);
    }

    /**
     * Access the Servlet's information display.
     *
     * @return servlet information.
     */
    public String getServletInfo() {
        return "Sakai JSF Tool Servlet";
    }

    /**
     * Recognize a path that is a resource request. It must have an "extension", i.e. a dot followed by characters that do not include a slash.
     *
     * @param path The path to check
     * @return true if the path is a resource request, false if not.
     */
    protected boolean isResourceRequest(String path) {
        // we need some path
        if ((path == null) || (path.length() == 0)) {
            return false;
        }

        // we need a last dot
        int pos = path.lastIndexOf(".");
        if (pos == -1) {
            return false;
        }

        // we need that last dot to be the end of the path, not burried in the path somewhere (i.e. no more slashes after the last dot)
        String ext = path.substring(pos);
        if (ext.contains( "/" )) {
            return false;
        }
        // we need the ext to not be the requestExt
        // ok, it's a resource request

        return !ext.equals(requestExt);
    }

    /**
     * Compute a new target (i.e. the servlet path info, not including folder root or jsf extension) if needed based on the requested target.
     *
     * @param target The servlet path info target requested.
     * @return The target we will actually respond with.
     */
    protected String redirectRequestedTarget(String target) {
        return target;
    }
}
