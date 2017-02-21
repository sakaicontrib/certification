package com.rsmart.certification.api;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;
/**
 * This represents a service capable of rendering templates of a specific set of MIME types.
 *
 * User: duffy
 * Date: Jun 7, 2011
 * Time: 5:06:44 PM
 */
public interface DocumentTemplateRenderEngine
{
    /**
     * @param template
     * @return the array of MIME types this service can handle
     */
    public String getOutputMimeType(DocumentTemplate template);

    /**
     * @param template
     * @return extracts named fields from the template.
     * @throws com.rsmart.certification.api.TemplateReadException
     */
    public Set<String> getTemplateFields(DocumentTemplate template) throws TemplateReadException;

    /**
     * @param inputStream
     * @return extracts named fields from the inputStream
     * @throws com.rsmart.certification.api.TemplateReadException
     */
    public Set<String> getTemplateFields(InputStream inputStream) throws TemplateReadException;

    /**
     * Renders the given template to an OutputStream, populating template fields with the supplied bindings.
     * No validation of field bindings is preformed. Extra bindings are ignored. Missing field bindings are
     * passed on as empty fields.
     *
     * @param template
     * @param bindings
     * @return
     * @throws com.rsmart.certification.api.TemplateReadException
     */
    public InputStream render (DocumentTemplate template, Map<String, String> bindings) throws TemplateReadException;

    public boolean supportsPreview(DocumentTemplate template) throws TemplateReadException;

    public String getPreviewMimeType(DocumentTemplate template) throws TemplateReadException;

    public InputStream renderPreview (DocumentTemplate template, Map<String, String> bindings) throws TemplateReadException;
}
