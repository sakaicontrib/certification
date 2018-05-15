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

package org.sakaiproject.certification.api;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * This represents a service capable of rendering templates of a specific set of MIME types.
 */
public interface DocumentTemplateRenderEngine {

    /**
     * @param template
     * @return the array of MIME types this service can handle
     */
    public String getOutputMimeType(DocumentTemplate template);

    /**
     * @param template
     * @return extracts named fields from the template.
     * @throws org.sakaiproject.certification.api.TemplateReadException
     */
    public Set<String> getTemplateFields(DocumentTemplate template) throws TemplateReadException;

    /**
     * @param inputStream
     * @return extracts named fields from the inputStream
     * @throws org.sakaiproject.certification.api.TemplateReadException
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
     * @throws org.sakaiproject.certification.api.TemplateReadException
     */
    public InputStream render(DocumentTemplate template, Map<String, String> bindings) throws TemplateReadException;

    public boolean supportsPreview(DocumentTemplate template) throws TemplateReadException;

    public String getPreviewMimeType(DocumentTemplate template) throws TemplateReadException;

    public InputStream renderPreview(DocumentTemplate template, Map<String, String> bindings) throws TemplateReadException;
}
