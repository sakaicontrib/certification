/*
 * Copyright 2011 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): duffy
 */

package com.rsmart.certification.tool.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rsmart.certification.api.BaseCertificateDefinition;
import com.rsmart.certification.api.CertificateDefinition;

import com.rsmart.certification.api.criteria.CriteriaTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.util.Set;

public class CertificateToolState
{
    private static final Log
        LOG = LogFactory.getLog(CertificateToolState.class);
    public static final String
        CERTIFICATE_TOOL_STATE = CertificateToolState.class.getName();
    private CertificateDefinition
        certificateDefinition = null;
    private String newDocumentTemplateName;
    private String submitValue;
    private String selectedCert;
    private String mimeTypes;
    private CommonsMultipartFile data;
    private Set<CriteriaTemplate> criteriaTemplates;
    private CriteriaTemplate selectedCriteriaTemplate;
    private Map<String, String> templateFields = null;
    private Map<String, String> predifinedFields = null;
    private boolean newDefinition;
    
    public CriteriaTemplate getSelectedCriteriaTemplate()
    {
        return selectedCriteriaTemplate;
    }

    public void setSelectedCriteriaTemplate(CriteriaTemplate selectedCriteriaTemplate)
    {
        this.selectedCriteriaTemplate = selectedCriteriaTemplate;
    }

    
	public CertificateDefinition getCertificateDefinition() {
		return certificateDefinition;
	}

	public void setCertificateDefinition(
			CertificateDefinition certificateDefinition) {
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

	public CommonsMultipartFile getData() {
		return data;
	}

	public void setData(CommonsMultipartFile data) {
		this.data = data;
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

    public void setCriteriaTemplates(Set<CriteriaTemplate> criteriaTemplates)
    {
        this.criteriaTemplates = criteriaTemplates;
    }

    public Set<CriteriaTemplate> getCriteriaTemplates()
    {
        return criteriaTemplates;
    }

   public Map<String, String> getTemplateFields() {
		return templateFields;
	}

	public void setTemplateFields(Map<String, String> templateFields) {
		this.templateFields = templateFields;
	}

	public Map<String, String> getPredifinedFields() {
		return predifinedFields;
	}

    public String getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(String mimeTypes) {
        this.mimeTypes = mimeTypes;
    }    

	public void setPredifinedFields(Map<String, String> predifinedFields) {
		Map<String, String> temp = null;
		if(predifinedFields != null)
		{
			temp = new HashMap<String, String>();
			for(String key : predifinedFields.keySet())
			{
				String newKey = key;
				if(!(key.startsWith("${") && key.endsWith("}")))
				{
					newKey = "${"+key+"}";
				}
				temp.put(newKey, predifinedFields.get(key));
			}
		}
		this.predifinedFields = temp;
	}

	public CertificateToolState ()
    {
        reset();
    }

    public void reset()
    {
    	certificateDefinition = new BaseCertificateDefinition();
    	newDocumentTemplateName = null;
    	submitValue = null;
    	selectedCert = null;
    	data = null;
    	templateFields = null;
    	predifinedFields = null;
        criteriaTemplates = null;
        selectedCriteriaTemplate = null;
        newDefinition = true;
    }

   public void setTemplateFields(Set<String> templateFields)
   {
	   Map<String, String> newTemplateField = null;
	   if(templateFields != null)
	   {
		   newTemplateField = new HashMap<String, String>();
		   for(String val : templateFields)
		   {
			   newTemplateField.put(val, val);
		   }
	   }
	   setTemplateFields(newTemplateField);
   }
   
    private static final ToolSession session()
    {
        final ToolSession
            session = SessionManager.getCurrentToolSession();

        if (session == null)
        {
            LOG.fatal("No tool session found; cannot manage CertificateToolState object");
        }

        return session;
    }

    public static final CertificateToolState getState()
    {
        final ToolSession
            session = session();

        if (session == null)
        {
            return null;
        }

        CertificateToolState
            state = (CertificateToolState) session.getAttribute(CERTIFICATE_TOOL_STATE);

        if (state == null)
        {
            state = new CertificateToolState();

            session.setAttribute(CERTIFICATE_TOOL_STATE, state);
        }

        return state;
    }

    public static final void clear()
    {
        final ToolSession
            session = session();

        if (session != null)
        {
            session.removeAttribute(CERTIFICATE_TOOL_STATE);
        }
    }

}
