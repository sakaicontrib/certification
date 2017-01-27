package com.rsmart.certification.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

/**
 * This service manages creation and rendering of DocumentTemplates. It maintains a registry of
 * DocumentTemplateRenderingEngine objects, each of which is capable of rendering templates of one or more MIME types.
 * The ability to register more than one DocumentTemplateRenderingEngine or the resolution of which
 * to use when more than one supports the same MIME type are implementation-specific concerns.
 *
 * User: duffy
 * Date: Jun 7, 2011
 * Time: 4:41:50 PM
 */
public interface DocumentTemplateService
{
    public void register (String mimeType, DocumentTemplateRenderEngine engine);

    public boolean isPreviewable (DocumentTemplate template)
        throws TemplateReadException;

    public String getPreviewMimeType(DocumentTemplate template)
        throws TemplateReadException;

    public InputStream renderPreview (DocumentTemplate template, CertificateAward award, Map<String, String> bindings)
        throws TemplateReadException, VariableResolutionException;

    public Set<DocumentTemplateRenderEngine> getRenderEngines();

    public DocumentTemplateRenderEngine getRenderEngineForMimeType(String mimeType);

    public Set<String> getTemplateFields(DocumentTemplate template)
        throws TemplateReadException;

    public InputStream render (DocumentTemplate template, CertificateDefinition certDef, String userId)
        throws TemplateReadException, VariableResolutionException;

    public Set<VariableResolver> getVariableResolvers ();

    public Set<String> getRegisteredMimeTypes();
}