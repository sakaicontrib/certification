package com.rsmart.certification.mock;

import com.rsmart.certification.api.CertificateAward;
import com.rsmart.certification.api.DocumentTemplate;
import com.rsmart.certification.api.DocumentTemplateRenderEngine;
import com.rsmart.certification.api.DocumentTemplateService;
import com.rsmart.certification.api.TemplateReadException;
import com.rsmart.certification.api.VariableResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: duffy
 * Date: Jun 20, 2011
 * Time: 3:00:00 PM
 */
public class MockDocumentTemplateService
    implements DocumentTemplateService
{
    private static final Log
        LOG = LogFactory.getLog(MockDocumentTemplateService.class);

    private Map <String, MockDocumentTemplateRenderEngine>
        renderers = new HashMap<String, MockDocumentTemplateRenderEngine>();

    public void register(String mimeType, DocumentTemplateRenderEngine engine)
    {
        renderers.put (mimeType.trim().toLowerCase(), (MockDocumentTemplateRenderEngine)engine);
    }

    public boolean isPreviewable(DocumentTemplate template)
    {
        DocumentTemplateRenderEngine
            dtre = renderers.get(template.getOutputMimeType());

        if (dtre != null)
        {
            try
            {
                return dtre.supportsPreview(template);
            }
            catch (TemplateReadException e)
            {
                LOG.error(e);
            }
        }

        return false;
    }

    public String getPreviewMimeType(DocumentTemplate template)
    {
        DocumentTemplateRenderEngine
            dtre = renderers.get(template.getOutputMimeType());

        if (dtre != null)
        {
            try
            {
                return dtre.getPreviewMimeType(template);
            }
            catch (TemplateReadException e) {
                LOG.error(e);
            }
        }

        return null;
    }

    public InputStream renderPreview(DocumentTemplate template, CertificateAward award, Map<String, String> bindings)
            throws TemplateReadException
    {
        DocumentTemplateRenderEngine
            dtre = renderers.get(template.getOutputMimeType());

        if (dtre != null)
        {
            return dtre.renderPreview(template, bindings);
        }

        return null;
    }

    public void setRendererMap (Map<String, MockDocumentTemplateRenderEngine> map)
    {
        renderers = map;
    }
    
    public Set<DocumentTemplateRenderEngine> getRenderEngines()
    {
        return new HashSet(renderers.values());
    }

    public DocumentTemplateRenderEngine getRenderEngineForMimeType(String mimeType)
    {
        return renderers.get(mimeType);
    }

    public Set<String> getTemplateFields(DocumentTemplate template)
        throws TemplateReadException
    {
        DocumentTemplateRenderEngine
            engine = getRenderEngineForMimeType(template.getOutputMimeType());

        if (engine != null)
        {
            return engine.getTemplateFields(template);
        }

        return null;
    }

    public InputStream render(DocumentTemplate template, CertificateAward award, Map<String, String> bindings)
        throws TemplateReadException
    {
        DocumentTemplateRenderEngine
            engine = getRenderEngineForMimeType(template.getOutputMimeType());

        if (engine != null)
        {
            return engine.render(template, bindings);
        }

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Set<VariableResolver> getVariableResolvers()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
