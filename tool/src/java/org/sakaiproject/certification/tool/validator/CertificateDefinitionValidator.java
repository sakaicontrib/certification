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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.validation.Errors;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import org.sakaiproject.certification.api.CertificateService;
import org.sakaiproject.certification.api.DocumentTemplateException;
import org.sakaiproject.certification.tool.util.CertificateToolState;

public class CertificateDefinitionValidator {

    private final Pattern variablePattern = Pattern.compile ("\\$\\{(.+)\\}");

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

        // Validate mappings; throw away any invalid mappings
        Set<String> predefinedFields = certificateToolState.getEscapedPredifinedFields().keySet();
        for( Iterator<Map.Entry<String, String>> itr = currentFields.entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry<String, String> entry = itr.next();
            if( !predefinedFields.contains( entry.getValue() ) ) {
                itr.remove();
            }
        }

        //Add the $'s back in (they were removed in CertificateToolState.getEscapedPredifinedFields())
        Set<String> keys = new HashSet<>();
        keys.addAll(currentFields.keySet());
        for (String key : keys) {
            String value = "$" + currentFields.get(key);
            currentFields.remove(key);
            currentFields.put(key, value);
        }

        certificateToolState.setTemplateFields(currentFields);
        Map<String, String> preDefFields = certificateToolState.getPredifinedFields();
        Set<String> keySet = preDefFields.keySet();
        for(String val : currentFields.values()) {
            Matcher variableMatcher = variablePattern.matcher(val);
            if (variableMatcher.matches() && !keySet.contains(val)) {
                errors.rejectValue("templateFields","not valid","not valid");
            }
        }
    }
}
