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

package org.sakaiproject.certification.tool.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

import org.sakaiproject.certification.api.CertificateDefinition;
import org.sakaiproject.certification.api.VariableResolver;
import org.sakaiproject.certification.api.criteria.CriteriaTemplate;
import org.sakaiproject.certification.api.criteria.Criterion;
import org.sakaiproject.certification.api.criteria.gradebook.WillExpireCriterion;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;

@Slf4j
public class CertificateToolState {

    public static final String CERTIFICATE_TOOL_STATE = CertificateToolState.class.getName();
    private static final String CUSTOM_FIELD_VALUE = "__custom__";
    private CertificateDefinition certificateDefinition = null;
    private String newDocumentTemplateName;
    private String submitValue;
    private String selectedCert;
    private String mimeTypes;
    private byte[] templateByteArray;
    private String templateFilename;
    private String templateMimeType;
    private CommonsMultipartFile newTemplate;
    private Set<CriteriaTemplate> criteriaTemplates;
    private Set<Criterion> awardCriteria;
    private CriteriaTemplate selectedCriteriaTemplate;
    private Map<String, String> templateFields = null;
    private Map<String, String> customTemplateFields = null;
    private Map<String, String> predifinedFields = null;
    private boolean newDefinition;

    public CriteriaTemplate getSelectedCriteriaTemplate() {
        return selectedCriteriaTemplate;
    }

    public void setSelectedCriteriaTemplate(CriteriaTemplate selectedCriteriaTemplate) {
        this.selectedCriteriaTemplate = selectedCriteriaTemplate;
    }

    public CertificateDefinition getCertificateDefinition() {
        return certificateDefinition;
    }

    public void setCertificateDefinition(CertificateDefinition certificateDefinition) {
        this.certificateDefinition = certificateDefinition;
    }

    public String getNewDocumentTemplateName() {
        return newDocumentTemplateName;
    }

    public void setNewDocumentTemplateName(String newDocumentTemplateName) {
        this.newDocumentTemplateName = newDocumentTemplateName;
    }

    public boolean isNewDefinition() {
        return newDefinition;
    }

    public void setNewDefinition(boolean newDefinition) {
        this.newDefinition = newDefinition;
    }

    public String getTemplateFilename() {
        return templateFilename;
    }

    public void setTemplateFilename(String templateFilename) {
        this.templateFilename = templateFilename;
    }

    public byte[] getTemplateByteArray() {
        return templateByteArray;
    }

    public void setTemplateByteArray(byte[] templateByteArray) {
        this.templateByteArray = templateByteArray;
    }

    public InputStream getTemplateInputStream() {
        if (templateByteArray == null) {
            return null;
        }

        return new ByteArrayInputStream(templateByteArray);
    }

    public String getTemplateMimeType() {
        return templateMimeType;
    }

    public void setTemplateMimeType(String templateMimeType) {
        this.templateMimeType = templateMimeType;
    }

    public CommonsMultipartFile getNewTemplate() {
        return newTemplate;
    }

    public void setNewTemplate(CommonsMultipartFile newTemplate) {
        this.newTemplate = newTemplate;
    }

    public String getSubmitValue() {
        return submitValue;
    }

    public void setSubmitValue(String submitValue) {
        this.submitValue = submitValue;
    }

    public String getSelectedCert() {
        return selectedCert;
    }

    public void setSelectedCert(String selectedCert) {
        this.selectedCert = selectedCert;
    }

    public void setCriteriaTemplates(Set<CriteriaTemplate> criteriaTemplates) {
        this.criteriaTemplates = criteriaTemplates;
    }

    public Set<CriteriaTemplate> getCriteriaTemplates() {
        return criteriaTemplates;
    }

    public Map<String, String> getTemplateFields() {
        return templateFields;
    }

    public void addCriterion(Criterion criterion) {
        awardCriteria.add(criterion);
    }

    public Set<Criterion> getAwardCriteria() {
        return awardCriteria;
    }

    public void setTemplateFields(Map<String, String> templateFields) {
        this.templateFields = templateFields;
    }

    public Map<String, String> getCustomTemplateFields() {
        return customTemplateFields;
    }

    public void setCustomTemplateFields(Map<String, String> customTemplateFields) {
        this.customTemplateFields = customTemplateFields;
    }

    public String getCustomFieldValue() {
        return CUSTOM_FIELD_VALUE;
    }

    public int getMaxFieldValueLength() {
        return CertificateDefinition.FIELD_VALUE_MAX_LENGTH;
    }

    public Map<String, String> getFieldToDescription()
    {
        HashMap<String, String> fieldToDesc = new HashMap<>();
        for (String key : getTemplateFields().keySet())
        {
            String expression = getTemplateFields().get(key);
            String description = getPredifinedFields().get(expression);
            fieldToDesc.put(key, description == null ? expression : description);
        }

        return fieldToDesc;
    }

    /**
     *
     * @return a map of ${} format to description
     */
    public Map<String, String> getPredifinedFields() {
        return predifinedFields;
    }

    public List<String[]> getOrderedEscapedPredifinedFields() {
        List<String[]> retVal = new ArrayList<>();
        Map<String, String> predefFields = getPredifinedFields();
        if (predefFields==null || predefFields.isEmpty()) {
            //TODO: log it
            return retVal;
        }

        Iterator<String> itPredefFields = predefFields.keySet().iterator();
        while (itPredefFields.hasNext()) {
            String key = itPredefFields.next();

            //passing something of the form ${} makes jsp treat it like a variable
            //soln: remove the $ here and append it back in the jsp code
            if ("${unassigned}".equals(key)) {
                retVal.add(0, new String[] { key.substring(1), predefFields.get(key) });
            } else {
                retVal.add(new String[] { key.substring(1), predefFields.get(key) });
            }
        }

        return retVal;
    }

    public Map<String, String> getEscapedPredifinedFields() {
        Map<String, String> retVal = new HashMap<>();
        Map<String, String> predefFields = getPredifinedFields();
        if (predefFields==null || predefFields.isEmpty()) {
            //TODO: log it
            return predefFields;
        }

        Iterator<String> itPredefFields = predefFields.keySet().iterator();
        while (itPredefFields.hasNext()) {
            String key = itPredefFields.next();
            //passing something of the form ${} makes jsp treat it like a variable
            //soln: remove the $ here and append it back in the jsp code
            retVal.put(key.substring(1), predefFields.get(key));
        }

        return retVal;
    }

    public String getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(String mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public void setPredifinedFields(Map<String, String> predifinedFields) {
        boolean criteriaContainWechi = false;
        CertificateDefinition certDef = getCertificateDefinition();
        Set<Criterion> awardCriteria = certDef.getAwardCriteria();
        for (Criterion criterion : awardCriteria) {
            if (criterion instanceof WillExpireCriterion) {
                criteriaContainWechi = true;
            }
        }

        //hate to hard code this. I'd grab it from GradebookVariableResolver, but we can't access impl
        String expireDate = "${cert.expiredate}";
        boolean fieldValuesContainWechi = certDef.getFieldValues().values().contains(expireDate);

        Map<String, String> temp = null;
        if(predifinedFields != null) {
            temp = new HashMap<>();
            for(String key : predifinedFields.keySet()) {
                String newKey = key;
                if(!(key.startsWith("${") && key.endsWith("}"))) {
                    newKey = "${"+key+"}";
                }

                //if the award criteria don't contain a wechi and the key is the expiry date, skip it

                /* Anything other than the expiry date gets added.
                 * If the key is the expiry date, we only add it if the award criteria contain wechi
                 * or if the field values contain wechi (for whatever reason).
                 * So we add it if
                 * key != expiry date OR criteriacontainswechi OR fieldvaluescontainwechi*/
                if (!expireDate.equals(newKey) || criteriaContainWechi || fieldValuesContainWechi) {
                    temp.put(newKey, predifinedFields.get(key));
                }
            }
        }

        this.predifinedFields = temp;
    }

    /**
     * Prepares persisted field values for the template-fields form. Predefined
     * variables remain in the select while literal values use the custom option.
     */
    public void prepareTemplateFieldsForEditing() {
        Map<String, String> editableFields = new HashMap<>();
        Map<String, String> editableCustomFields = new HashMap<>();
        Map<String, String> fieldValues = getCertificateDefinition().getFieldValues();

        if (templateFields == null) {
            setTemplateFields(editableFields);
            setCustomTemplateFields(editableCustomFields);
            return;
        }

        for (String key : templateFields.keySet()) {
            String value = fieldValues == null ? null : fieldValues.get(key);
            if (value == null || value.trim().isEmpty()) {
                editableFields.put(key, getUnassignedValue());
                editableCustomFields.put(key, "");
            } else if (predifinedFields != null && predifinedFields.containsKey(value)) {
                editableFields.put(key, value.substring(1));
                editableCustomFields.put(key, "");
            } else {
                editableFields.put(key, CUSTOM_FIELD_VALUE);
                editableCustomFields.put(key, value);
            }
        }

        setTemplateFields(editableFields);
        setCustomTemplateFields(editableCustomFields);
    }

    public String getUnassignedValue() {
        return "{" + VariableResolver.UNASSIGNED + "}";
    }

    public CertificateToolState () {
        reset();
    }

    public void reset() {
        certificateDefinition = new CertificateDefinition();
        newDocumentTemplateName = null;
        submitValue = null;
        selectedCert = null;
        templateFields = null;
        customTemplateFields = null;
        predifinedFields = null;
        criteriaTemplates = null;
        awardCriteria = new HashSet<>();
        selectedCriteriaTemplate = null;
        newDefinition = true;
        templateFilename = null;
        templateByteArray = null;
        templateMimeType = null;
    }

    public void setTemplateFields(Set<String> templateFields) {
       Map<String, String> newTemplateField = null;
       if(templateFields != null) {
           newTemplateField = new HashMap<>();
           Map<String, String> newCustomTemplateField = new HashMap<>();
           for(String val : templateFields) {
               newTemplateField.put(val, getUnassignedValue());
               newCustomTemplateField.put(val, "");
           }
           setCustomTemplateFields(newCustomTemplateField);
       } else {
           setCustomTemplateFields(null);
       }

       setTemplateFields(newTemplateField);
    }

    private static ToolSession session() {
        final ToolSession session = SessionManager.getCurrentToolSession();
        if (session == null) {
            log.error("No tool session found; cannot manage CertificateToolState object");
        }

        return session;
    }

    public static CertificateToolState getState() {
        final ToolSession session = session();
        if (session == null) {
            return null;
        }

        CertificateToolState state = (CertificateToolState) session.getAttribute(CERTIFICATE_TOOL_STATE);
        if (state == null) {
            state = new CertificateToolState();
            session.setAttribute(CERTIFICATE_TOOL_STATE, state);
        }

        return state;
    }

    public static final void clear() {
        final ToolSession session = session();
        if (session != null) {
            session.removeAttribute(CERTIFICATE_TOOL_STATE);
        }
    }
}
