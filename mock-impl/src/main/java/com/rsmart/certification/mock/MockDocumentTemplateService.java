package com.rsmart.certification.mock;

import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.DocumentTemplate;
import com.rsmart.certification.api.DocumentTemplateRenderEngine;
import com.rsmart.certification.api.DocumentTemplateService;
import com.rsmart.certification.api.TemplateReadException;
import com.rsmart.certification.api.VariableResolutionException;
import com.rsmart.certification.api.VariableResolver;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    @Override
    public Set<String> getRegisteredMimeTypes()
    {
        return null;
    }

    @Override
    public Set<String> getTemplateFields( InputStream inputStream, String mimeType ) throws TemplateReadException
    {
        return null;
    }

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

    public InputStream render( DocumentTemplate template, CertificateDefinition certDef, String userId ) throws TemplateReadException, VariableResolutionException
    {
        DocumentTemplateRenderEngine
            engine = getRenderEngineForMimeType(template.getOutputMimeType());

        if (engine != null)
        {
            return engine.render(template, null);
        }

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Set<VariableResolver> getVariableResolvers()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
