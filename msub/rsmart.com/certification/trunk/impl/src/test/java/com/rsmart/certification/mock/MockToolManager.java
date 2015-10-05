package com.rsmart.certification.mock;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.w3c.dom.Document;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

/**
 * User: duffy
 * Date: Jun 30, 2011
 * Time: 10:07:22 AM
 */
public class MockToolManager
    implements ToolManager
{
    public void register(Tool tool) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void register(Document toolXml) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void register(File toolXmlFile) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void register(InputStream toolXmlStream) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Tool getTool(String id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Set<Tool> findTools(Set<String> categories, Set<String> keywords) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Tool getCurrentTool() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Placement getCurrentPlacement()
    {
        return new Placement()
        {

            public Properties getConfig() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getContext() {
                return "test site id";  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getId() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Properties getPlacementConfig() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getTitle() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Tool getTool() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getToolId() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public void setTitle(String title) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void setTool(String toolId, Tool tool) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void save() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

    public void setResourceBundle(String toolId, String filename) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isVisible(Site site, ToolConfiguration config) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
