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
import java.util.Set;

/**
 * This service manages creation and rendering of DocumentTemplates. It maintains a registry of
 * DocumentTemplateRenderingEngine objects, each of which is capable of rendering templates of one or more MIME types.
 * The ability to register more than one DocumentTemplateRenderingEngine or the resolution of which
 * to use when more than one supports the same MIME type are implementation-specific concerns.
 */
public interface DocumentTemplateService {

    /**
     * Allows the given mimeType to be rendered by the specified DocumentTemplateRenderEngine
     * @param mimeType
     * @param engine
     */
    public void register(String mimeType, DocumentTemplateRenderEngine engine);

    /**
     * Determines whether this document template is supported for preview
     * @param template
     * @return
     * @throws TemplateReadException
     */
    public boolean isPreviewable(DocumentTemplate template) throws TemplateReadException;

    /**
     * Gets the mime type for previewing this document template
     * @param template
     * @return
     * @throws TemplateReadException
     */
    public String getPreviewMimeType(DocumentTemplate template) throws TemplateReadException;

    /**
     * Gets all registered DocumentTemplateRenderEngines
     * @return
     */
    public Set<DocumentTemplateRenderEngine> getRenderEngines();

    /**
     * Gets the render engine that is registered to the specified mimeType
     * @param mimeType
     * @return
     */
    public DocumentTemplateRenderEngine getRenderEngineForMimeType(String mimeType);

    /**
     * Gets the names of the fields in the DocumentTemplate where values can be substituted
     * @param template
     * @return
     * @throws TemplateReadException
     */
    public Set<String> getTemplateFields(DocumentTemplate template) throws TemplateReadException;

    public Set<String> getTemplateFields(InputStream inputStream, String mimeType) throws TemplateReadException;

     /**
     * Renders a user's certificate into an input stream
     * @param template the DocumentTemplate for the certificate
     * @param certDef the Certificate Definition used to populate the template fields
     * @param userId the user for whom we are populating the template fields
     * @return
     * @throws TemplateReadException
     * @throws VariableResolutionException
     */
    public InputStream render(DocumentTemplate template, CertificateDefinition certDef, String userId) throws TemplateReadException, VariableResolutionException;

    public Set<VariableResolver> getVariableResolvers();

    /**
     * Gets all mimeTypes that have a registered DocumentTemplateRenderEngine
     * @return
     */
    public Set<String> getRegisteredMimeTypes();
}
