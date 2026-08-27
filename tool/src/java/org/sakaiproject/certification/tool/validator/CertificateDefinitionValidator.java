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

package org.sakaiproject.certification.tool.validator;

import java.util.Map;
import java.util.Set;

import org.springframework.validation.Errors;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import org.sakaiproject.certification.api.CertificateService;
import org.sakaiproject.certification.api.DocumentTemplateException;
import org.sakaiproject.certification.tool.util.CertificateToolState;

public class CertificateDefinitionValidator {

    public void validateFirst(CertificateToolState certificateToolState, Errors errors, CertificateService service) {
        CommonsMultipartFile newTemplate = certificateToolState.getNewTemplate();
        if (newTemplate != null && newTemplate.getSize() > 0) {
            if(!certificateToolState.getMimeTypes().contains( newTemplate.getContentType() )) {
                // could be browser misreporting (ie. Firefox), so get a second opinion
                try {
                    String mimeType = service.getMimeType(newTemplate.getBytes());
                    if (!certificateToolState.getMimeTypes().contains(mimeType)) {
                        errors.rejectValue("newTemplate", "mimeType", "invalid mimeType");
                    }
                } catch (DocumentTemplateException e) {
                    errors.rejectValue("newTemplate", "mimeType", "invalid mimeType");
                }
            }
        }
    }

    public void validateSecond(CertificateToolState certificateToolState, Errors errors) {
        // The only invalid case is when the expiry date is your only criterion.
        // This case is handled in CertificateEditController
    }

    public void validateThird(CertificateToolState certificateToolState, Errors errors) {
        Map<String, String> currentFields = certificateToolState.getTemplateFields();
        Set<String> predefinedFields = certificateToolState.getEscapedPredifinedFields().keySet();

        for (Map.Entry<String, String> entry : currentFields.entrySet()) {
            String value = entry.getValue();
            if (value == null || value.trim().isEmpty()) {
                entry.setValue("$" + certificateToolState.getUnassignedValue());
            } else if (predefinedFields.contains(value)) {
                // The JSP removes the leading $ from predefined variables so it
                // does not interpret them as expressions. Restore it for storage.
                entry.setValue("$" + value);
            }
        }

        certificateToolState.setTemplateFields(currentFields);
    }
}
