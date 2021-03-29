package org.sakaiproject.certification.mock;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;

import org.w3c.dom.Document;

public class MockToolManager implements ToolManager
{
    public void register(Tool tool) {}

    public void register(Document toolXml) {}

    public void register(File toolXmlFile) {}

    public void register(InputStream toolXmlStream) {}

    public Tool getTool(String id)
    {
        return null;
    }

    public Set<Tool> findTools(Set<String> categories, Set<String> keywords)
    {
        return null;
    }

    public Tool getCurrentTool()
    {
        return null;
    }

    public Placement getCurrentPlacement()
    {
        return new Placement()
        {
            public Properties getConfig()
            {
                return null;
            }

            public String getContext()
            {
                return "test site id";
            }

            public String getId()
            {
                return null;
            }

            public Properties getPlacementConfig()
            {
                return null;
            }

            public String getTitle()
            {
                return null;
            }

            public Tool getTool()
            {
                return null;
            }

            public String getToolId()
            {
                return null;
            }

            public void setTitle(String title) {}

            public void setTool(String toolId, Tool tool) {}

            public void save() {}
        };
    }

    public void setResourceBundle(String toolId, String filename) {}

    @Override
    public List<Set<String>> getRequiredPermissions(ToolConfiguration config) {
        return null;
    }

    @Override
    public boolean isFirstToolVisibleToAnyNonMaintainerRole(SitePage page) {
        return false;
    }

    public boolean isVisible(Site site, ToolConfiguration config)
    {
        return false;
    }

    @Override
    public String getLocalizedToolProperty(String toolId, String key)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isHidden(Placement placement)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean allowTool(Site site, Placement placement)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
