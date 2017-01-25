package com.rsmart.certification.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rsmart.certification.api.criteria.InvalidBindingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.DocumentTemplate;
import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.CriteriaTemplateVariable;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.tool.utils.CertificateToolState;

/**
 * User: duffy
 * Date: Jun 7, 2011
 * Time: 4:15:32 PM
 */
@Controller
@SessionAttributes(types = CertificateToolState.class)
public class CertificateEditController
    extends BaseCertificateController
{
    private static final Log
        LOG = LogFactory.getLog(CertificateEditController.class);

    public final static String
        MIME_TYPES = "mimeTypes";

    private Pattern
        varValuePattern = Pattern.compile ("variableValues\\[(.*)\\]"),
        variablePattern = Pattern.compile ("\\$\\{(.+)\\}");

    private ObjectMapper
        mapper = new ObjectMapper();

	@ModelAttribute(MOD_ATTR)
	public  CertificateToolState initializeModel(@RequestParam(value="certId", required=false) String certId) throws Exception
	{
		CertificateToolState certificateToolState = CertificateToolState.getState();
		if(certId != null)
		{
			CertificateDefinition certificateDefinition = getCertificateService().getCertificateDefinition(certId);
			certificateToolState.setCertificateDefinition(certificateDefinition);
			certificateToolState.setTemplateFields(certificateDefinition.getFieldValues());
			certificateToolState.setNewDefinition(false);
		}
		return certificateToolState;
	}


    @RequestMapping(value="/first.form")
    protected ModelAndView createCertHandlerFirst(@ModelAttribute(MOD_ATTR) CertificateToolState certificateToolState,
    		 BindingResult result, HttpServletRequest request, SessionStatus status) throws Exception
    {
    	//CertificateDefinition certDef = certificateToolState.getCertificateDefinition();
    	Map<String, Object> model = new HashMap<String, Object>();

		if (!isAdministrator())
		{
			CertificateToolState.clear();
			status.setComplete();
		    return new ModelAndView("redirect:list.form", ERROR_MESSAGE, "error.not.admin");
		}
		 
    	if("cancel".equals(certificateToolState.getSubmitValue()))
    	{
    		if(certificateToolState.isNewDefinition())
    		{
    			/*
    			 * delete certificate definition from the db;
    			 */
    			CertificateToolState.clear();
    			status.setComplete();
    			return new ModelAndView("redirect:list.form");
    		}
    		else
    		{
    			CertificateToolState.clear();
    			status.setComplete();
    			return new ModelAndView("redirect:list.form");
    		}

		}


    	if(result.hasErrors())
    	{
    		return new ModelAndView("createCertificateOne", STATUS_MESSAGE_KEY, FORM_ERR);
    	}
        else if("next".equals(certificateToolState.getSubmitValue()) && !result.hasErrors())
        {
            try
            {
            	certificateDefinitionValidator.validateFirst(certificateToolState, result);
            	if(!result.hasErrors())
            	{
                    certificateToolState = persistFirstFormData(certificateToolState);
            		certificateToolState.setNewDefinition(false);
            	}
            	else
            	{
            		model.put(STATUS_MESSAGE_KEY, FORM_ERR);
            		model.put(ERROR_MESSAGE, INVALID_TEMPLATE);
                    model.put(MOD_ATTR, certificateToolState);
                    return new ModelAndView("createCertificateOne", model);
            	}

            }
            catch (IdUsedException iue)
            {
                logger.warn("CertificateEditController.createCertHandlerFirst.save", iue);

                model.put(STATUS_MESSAGE_KEY, FORM_ERR);
                model.put(ERROR_MESSAGE, DUPLICATE_NAME_ERR);
                model.put(MOD_ATTR, certificateToolState);

                return new ModelAndView("createCertificateOne", model);
            }
            catch (Exception e)
            {
                logger.warn("CertificateEditController.createCertHandlerFirst.next", e);
                CertificateDefinition certificateDefinition = certificateToolState.getCertificateDefinition();
                if(certificateToolState.isNewDefinition() && certificateDefinition.getId() != null)
                {
	                try
	                {
	                  getCertificateService().deleteCertificateDefinition(certificateDefinition.getId());
	                }
	                catch(Exception e2)
	                {
                        logger.warn("", e);
	                }
                }
                model.put(STATUS_MESSAGE_KEY, FORM_ERR);
				model.put(ERROR_MESSAGE, TEMPLATE_PROCESSING_ERR);
                model.put(MOD_ATTR, certificateToolState);
                return new ModelAndView("createCertificateOne", model);
            }

            /*
                    process file upload
                    call certSvc.setDocumentTemplate (mimeType, InputStream)
             */

            certificateToolState.setSubmitValue(null);
            return createCertHandlerSecond(certificateToolState, result, request, status);
        }
        else if("save".equals(certificateToolState.getSubmitValue()) && !result.hasErrors())
    	{

    		try
    		{
    			certificateDefinitionValidator.validateFirst(certificateToolState, result);
    			if(!result.hasErrors())
    			{
				    certificateToolState = persistFirstFormData(certificateToolState);
    				certificateToolState.setNewDefinition(false);
				    model.put(STATUS_MESSAGE_KEY, SUCCESS);
			    }
    			else
    			{
            		model.put(STATUS_MESSAGE_KEY, FORM_ERR);
            		model.put(ERROR_MESSAGE, INVALID_TEMPLATE);
    			}
			}
            catch (IdUsedException iue)
            {
                logger.warn("CertificateEditController.createCertHandlerFirst.save", iue);

                model.put(STATUS_MESSAGE_KEY, FORM_ERR);
                model.put(ERROR_MESSAGE, DUPLICATE_NAME_ERR);
            }
    		catch (Exception e)
    		{
    			logger.warn("CertificateEditController.createCertHandlerFirst.save", e);
    			CertificateDefinition certificateDefinition = certificateToolState.getCertificateDefinition();
                if(certificateToolState.isNewDefinition() && certificateDefinition.getId() != null)
                {
	                try
	                {
	                    getCertificateService().deleteCertificateDefinition(certificateDefinition.getId());
	                }
	                catch(Exception e2)
	                {
    			        logger.warn("", e);
	                }
                }
    			model.put(STATUS_MESSAGE_KEY, FORM_ERR);
				model.put(ERROR_MESSAGE, TEMPLATE_PROCESSING_ERR);
			}

    		model.put(MOD_ATTR, certificateToolState);
    		return new ModelAndView("createCertificateOne", model);
    	}
    	else
    	{

    		/*
    		 	empty cert definition put in model
    		 */
    		String delim = "";
	        StringBuffer mimeBuff = new StringBuffer();

	        for (String mimeType : getDocumentTemplateService().getRegisteredMimeTypes())
	        {
	            mimeBuff.append(delim).append(mimeType);
	            delim = ", ";
	        }

	        certificateToolState.setMimeTypes(mimeBuff.toString());
    		return new ModelAndView("createCertificateOne", MOD_ATTR, certificateToolState);
    	}
    }

    private CertificateToolState persistFirstFormData(CertificateToolState certificateToolState) throws Exception
    {
    	CertificateDefinition certDef = certificateToolState.getCertificateDefinition();
        CommonsMultipartFile data = certificateToolState.getData();

    	if(certDef.getId() == null)
    	{

            CertificateDefinition
                existing = null;

            try
            {
                existing = getCertificateService().getCertificateDefinitionByName(siteId(), certDef.getName());
            }
            catch (IdUnusedException iue)
            {
                // this is good! not a duplicate
            }

            if (existing != null)
            {
                throw new IdUsedException (certDef.getName());
            }
            
            certDef = getCertificateService().createCertificateDefinition(certDef.getName(), certDef.getDescription(), 
                    siteId(), data.getOriginalFilename(), data.getContentType(), data.getInputStream(), certDef.getExpiryOffset());

    		certDef = getCertificateService().getCertificateDefinition(certDef.getId());
			DocumentTemplate dt = certDef.getDocumentTemplate();
			certificateToolState.setTemplateFields(getDocumentTemplateService().getTemplateFields(dt));
    	}
    	else
    	{
			//added the following line - wouldn't allow us to change the certDef name
			//only tested with data.getSize() > 0
			getCertificateService().updateCertificateDefinition(certDef);
			if(data.getSize() > 0)
			{
    			DocumentTemplate dt = getCertificateService().setDocumentTemplate(certDef.getId(), data.getOriginalFilename(), data.getContentType(), data.getInputStream());
    			certificateToolState.setTemplateFields(getDocumentTemplateService().getTemplateFields(dt));
    		}
    		else
    		{
    			if(certDef.getFieldValues().isEmpty())
    			{
    				DocumentTemplate dt = certDef.getDocumentTemplate();
    				certificateToolState.setTemplateFields(getDocumentTemplateService().getTemplateFields(dt));
    			}
    			else
    			{
    				certificateToolState.setTemplateFields(certDef.getFieldValues());
    			}
    		}
    		//commented the following line - wouldn't allow us to change the template file
    		//only tested with data.getSize() > 0
    		//certificateService.updateCertificateDefinition(certDef);
			certDef = getCertificateService().getCertificateDefinition(certDef.getId());
		}

		certificateToolState.setCertificateDefinition(certDef);
		return certificateToolState;
    }


	@RequestMapping(value="/second.form")
    protected ModelAndView createCertHandlerSecond(@ModelAttribute(MOD_ATTR) CertificateToolState certificateToolState,
   		                                           BindingResult result, HttpServletRequest request,
                                                   SessionStatus status)
    throws Exception
    {
		Map<String, Object> model = new HashMap<String, Object>();

		if (!isAdministrator())
		{
			CertificateToolState.clear();
			status.setComplete();
		    return new ModelAndView("redirect:list.form", ERROR_MESSAGE, "error.not.admin");
		}
		
    	if("cancel".equals(certificateToolState.getSubmitValue()))
    	{
    		if(certificateToolState.isNewDefinition())
    		{
    			/*
    			 * delete certificate definition from the db;
    			 */
    			CertificateToolState.clear();
    			status.setComplete();
    			return new ModelAndView("redirect:list.form");
    		}
    		else
    		{
    			CertificateToolState.clear();
    			status.setComplete();
    			return new ModelAndView("redirect:list.form");
    		}

		}
    	else if("back".equals(certificateToolState.getSubmitValue()))
    	{
    		return createCertHandlerFirst(certificateToolState, result, request, status);
    	}

    	if(result.hasErrors())
		{
			return new ModelAndView("createCertificateTwo",STATUS_MESSAGE_KEY,FORM_ERR);
		}
    	else if("save".equals(certificateToolState.getSubmitValue()))
    	{
    		try
    		{
	    		certificateDefinitionValidator.validateSecond(certificateToolState, result);
	    		if(!result.hasErrors())
	    		{
	    		CertificateDefinition certDef = certificateToolState.getCertificateDefinition();
		    		getCertificateService().setFieldValues(certDef.getId(), certificateToolState.getTemplateFields());
		    		model.put(STATUS_MESSAGE_KEY, SUCCESS);
	    		}
	    		else
	    		{
	    			model.put(STATUS_MESSAGE_KEY, FORM_ERR);
	    			model.put(ERROR_MESSAGE, PREDEFINED_VAR_EXCEPTION);
	    			model.put(MOD_ATTR, certificateToolState);
	        		return new ModelAndView("createCertificateTwo", model);
	    		}
    		}
    		catch(Exception e)
    		{
    			logger.warn("CertificateEditController.createCertHandlerSecond.save", e);
    			model.put(STATUS_MESSAGE_KEY, FORM_ERR);
    		}
    		/*
		 	get DocumentTemplate from certificateDefinition
		 	put into model:
		 		1) template fields for certificateDefinition:
		 			DocTemplateSvc.getTemplateFields(DocTemplate)
		 		2) predefined variables:
		 			CertSvc.getPredefinedTemplateVariables(...)
		 */
    		/*
    		   Add any fields that have been set in the CertDef.
    		   		CertDefn.getFieldValues()
    		 */
    		model.put(MOD_ATTR, certificateToolState);
    		return new ModelAndView("createCertificateTwo", model);
    	}
    	else if("next".equals(certificateToolState.getSubmitValue()))
    	{
    		try
    		{
	    		certificateDefinitionValidator.validateSecond(certificateToolState, result);
	    		if(!result.hasErrors())
	    		{
	    		CertificateDefinition certDef = certificateToolState.getCertificateDefinition();
		    		getCertificateService().setFieldValues(certDef.getId(), certificateToolState.getTemplateFields());
		    		model.put(STATUS_MESSAGE_KEY, SUCCESS);
	    		}
	    		else
	    		{
	    			model.put(STATUS_MESSAGE_KEY, FORM_ERR);
	    			model.put(MOD_ATTR, certificateToolState);
	    			model.put(ERROR_MESSAGE, PREDEFINED_VAR_EXCEPTION);
	        		return new ModelAndView("createCertificateTwo", model);
	    		}
    		}
    		catch(Exception e)
    		{
    			logger.warn("CertificateEditController.createCertHandlerSecond.next", e);
    			model.put(STATUS_MESSAGE_KEY, FORM_ERR);
    			model.put(MOD_ATTR, certificateToolState);
    			return new ModelAndView("createCertificateTwo", model);
    		}
    		/*
		 	get DocumentTemplate from certificateDefinition
		 	put into model:
		 		1) template fields for certificateDefinition:
		 			DocTemplateSvc.getTemplateFields(DocTemplate)
		 		2) predefined variables:
		 			CertSvc.getPredefinedTemplateVariables(...)
		 */
    		/*
 		   Add any fields that have been set in the CertDef.
 		   		CertDefn.getFieldValues()
 		 */

            certificateToolState.setSubmitValue(null);
            return createCertHandlerThird(certificateToolState, result, request, status);
    	}
    	else
    	{
    		/*
    		 	get DocumentTemplate from certificateDefinition
    		 	put into model:
    		 		1) template fields for certificateDefinition:
    		 			DocTemplateSvc.getTemplateFields(DocTemplate)
    		 		2) predefined variables:
    		 			CertSvc.getPredefinedTemplateVariables(...)
    		 */
            certificateToolState.setPredifinedFields(getCertificateService().getPredefinedTemplateVariables());
    		return new ModelAndView("createCertificateTwo",MOD_ATTR,certificateToolState);
    	}
    }

   /* private boolean validateSecondForm(CertificateToolState certificateToolState) {
    	Map<String, String> currentFields = certificateToolState.getTemplateFields();
    	Map<String, String> preDefFields = certificateToolState.getPredifinedFields();
    	Set<String> keySet = preDefFields.keySet();
    	for(String val : currentFields.values())
    	{
            Matcher
                variableMatcher = variablePattern.matcher(val);

            if (variableMatcher.matches() && !keySet.contains(val))
            {
                return false;
            }
    	}
		return true;
	}*/

	@RequestMapping(value="/third.form")
    protected ModelAndView createCertHandlerThird(@ModelAttribute(MOD_ATTR) CertificateToolState certificateToolState,
                                                  BindingResult result, HttpServletRequest request, SessionStatus status)
        throws Exception
    {
        final String
            subVal = certificateToolState.getSubmitValue();
        CertificateDefinition
            certDef = certificateToolState.getCertificateDefinition();
        CertificateService
            certSvc = getCertificateService();
        Map<String, Object> model = new HashMap<String, Object>();
        String
            viewName = null;

        if (!isAdministrator())
		{
			CertificateToolState.clear();
			status.setComplete();
		    return new ModelAndView("redirect:list.form", ERROR_MESSAGE, "error.not.admin");
		}
        
        if("cancel".equals(certificateToolState.getSubmitValue()))
		{
    		if(certificateToolState.isNewDefinition())
    		{
    			/*
    			 * delete certificate definition from the db;
    			 */
    			CertificateToolState.clear();
    			status.setComplete();
    			return new ModelAndView("redirect:list.form");
    		}
    		else
    		{
    			CertificateToolState.clear();
    			status.setComplete();
    			return new ModelAndView("redirect:list.form");
    		}

		}
        else if("back".equals(subVal))
    	{
            certificateToolState.setSubmitValue(null);
            return createCertHandlerSecond(certificateToolState, result, request, status);
    	}

        if(result.hasErrors())
        {
        	viewName="createCertificateThree";
        }
    	else if("save".equals(subVal))
    	{
    		/*
    		 	save criteria using:
    		 		CertificateSvc.setCriteria(Set<Criterion>)
    		 */
    		certificateDefinitionValidator.validateThird(certificateToolState, result);
    		viewName="createCertificateThree";
			if(!result.hasErrors())
			{
				model.put(STATUS_MESSAGE_KEY, SUCCESS);
			}
			else
			{
				model.put(STATUS_MESSAGE_KEY, FORM_ERR);
				model.put(ERROR_MESSAGE, CRITERION_EXCEPTION);
			}
    	}
    	else if("next".equals(subVal))
    	{
    		/*
			 	save criteria using:
			 		CertificateSvc.setCriteria(Set<Criterion>)
			 */
    		certificateDefinitionValidator.validateThird(certificateToolState, result);
			if(!result.hasErrors())
			{
    		    certificateToolState.setSubmitValue(null);
    		    return createCertHandlerFourth(certificateToolState, result, request, status);
        	}
    	    else
    	    {
				viewName="createCertificateThree";
				model.put(STATUS_MESSAGE_KEY, FORM_ERR);
				model.put(ERROR_MESSAGE, CRITERION_EXCEPTION);
			}
    	}
    	else
    	{

    		/*
    		   add criteria templates to model
    		   in JSP loop through templates
    		   		add a template to the dropdown
    		   			CriteriaTemplate.getExpression()
    		   		call JS function to populate remainder of form
    		   			loop through CriteriaTemplateVariables
    		   				if multiChoice - add a dropdown to the form
    		   					(eg. pick a gradebook item)
    		   				else add a textbox to the form
    		   					(eg. enter minimum score)
    		 */
    		Set<CriteriaTemplate>
                criteriaTemplates = certSvc.getCriteriaTemplates();

            certificateToolState.setCriteriaTemplates(criteriaTemplates);
            certificateToolState.setCertificateDefinition(certDef);

            viewName="createCertificateThree";
    	}

        model.put(MOD_ATTR, certificateToolState);
    	return new ModelAndView (viewName, model);
    }

    @RequestMapping(value="/fourth.form", method=RequestMethod.POST)
    protected ModelAndView createCertHandlerFourth(@ModelAttribute(MOD_ATTR) CertificateToolState certificateToolState,
      		 BindingResult result, HttpServletRequest request, SessionStatus status)
    throws Exception
    {
    	Map<String, Object> model = new HashMap<String, Object>();

    	if (!isAdministrator())
		{
			CertificateToolState.clear();
			status.setComplete();
		    return new ModelAndView("redirect:list.form", ERROR_MESSAGE, "error.not.admin");
		}
    	
    	if("cancel".equals(certificateToolState.getSubmitValue()))
    	{
    		if(certificateToolState.isNewDefinition())
    		{
    			/*
    			 * delete certificate definition from the db;
    			 */
    			CertificateToolState.clear();
    			status.setComplete();
    			return new ModelAndView("redirect:list.form");
    		}
    		else
    		{
    			CertificateToolState.clear();
    			status.setComplete();
    			return new ModelAndView("redirect:list.form");
    		}

		}
    	if("back".equals(certificateToolState.getSubmitValue()))
    	{
    		certificateToolState.setSubmitValue(null);
    		return createCertHandlerThird(certificateToolState, result, request, status);
    	}
    	else if("save".equals(certificateToolState.getSubmitValue()))
    	{
    		/*
    		 	call to certServ.activateCertificateDef
    		 	redirect to cert list page

    		 */
    		try
    		{
	    		CertificateDefinition certDef = certificateToolState.getCertificateDefinition();
	    		getCertificateService().activateCertificateDefinition(certDef.getId(), true);
    		}
    		catch (Exception e)
    		{
    			model.put(STATUS_MESSAGE_KEY, FORM_ERR);
    			model.put(MOD_ATTR, certificateToolState);
    			return new ModelAndView("createCertificateFour",model);
    		}

    		CertificateToolState.clear();
    		status.setComplete();
    		return new ModelAndView("redirect:list.form");
    	}
    	else
    	{
    		/*
    		 send the entire CertDefinition to the JSP page.
    		 */
    		return new ModelAndView("createCertificateFour",MOD_ATTR,certificateToolState);
    	}
    }
    
    private class TemplateTransferObject
    {
        private String
            id,
            expression;
        private List<VariableTransferObject>
            variables = new ArrayList<VariableTransferObject>();

        TemplateTransferObject (CriteriaTemplate template)
        {
            id = template.getId();
            expression = template.getExpression();

            for (CriteriaTemplateVariable variable : template.getTemplateVariables())
            {
                variables.add(new VariableTransferObject(variable));
            }
        }

        public String getId()
        {
            return id;
        }

        public String getExpression()
        {
            return expression;
        }

        public List<VariableTransferObject> getTemplateVariables()
        {
            return variables;
        }
    }

    private class VariableTransferObject
    {
        private String
            key,
            label;
        private boolean
            multipleChoice;
        private HashMap<String, String>
            values = new HashMap<String, String>();

        public VariableTransferObject (CriteriaTemplateVariable variable)
        {
            key = variable.getVariableKey();
            label = variable.getVariableLabel();
            multipleChoice = variable.isMultipleChoice();

            if (multipleChoice)
            {
                values.putAll(variable.getValues());
            }
        }

        public String getVariableKey()
        {
            return key;
        }
        
        public String getVariableLabel()
        {
            return label;
        }

        public boolean isMultipleChoice()
        {
            return multipleChoice;
        }

        public Map<String, String> getValues()
        {
            return values;
        }
    }

    private class CriterionTransferObject
    {
        private String
            id,
            expression;

        public CriterionTransferObject (CriteriaTemplate template, Criterion criterion)
        {
            id = criterion.getId();
            expression = template.getExpression(criterion);
        }

        public String getId()
        {
            return id;
        }

        public String getExpression()
        {
            return expression;
        }
    }

    @RequestMapping(value="/getTemplate.form")
    protected void getCriteriaTemplate(HttpServletRequest request, HttpServletResponse response)
        throws Exception
    {
        String
            templateId = request.getParameter("templateId");

        if (templateId == null)
        {
            response.sendError(400);
            return;
        }

        CriteriaFactory
            critFact = getCertificateService().getCriteriaFactory(templateId);

        if (critFact == null)
        {
            response.sendError(400);
            return;
        }

        CriteriaTemplate
            template = critFact.getCriteriaTemplate(templateId);

        if (template == null)
        {
            response.sendError(400);
            return;
        }

        TemplateTransferObject
            tto = new TemplateTransferObject(template);

        mapper.writeValue(response.getOutputStream(), tto);
    }

    @RequestMapping(value="/addCriterion.form")
    protected void addCertCriteria(HttpServletRequest request, HttpServletResponse response)
    throws Exception
    {
    	
    	if (!isAdministrator())
		{
    		response.sendError(400);
            return;
		}

        CertificateToolState
            state = CertificateToolState.getState();
        CertificateService
            cs = getCertificateService();

        // place to store parameters from HTTP request
        Map <String, String[]>
            params = request.getParameterMap();

        // variable bindings for the new Criterion
        HashMap<String,String>
            varMap = new HashMap<String, String>(0);

        // grab the parameters that we know
        String
            certId[] = params.get("certId"),
            templateId[] = params.get("templateId");

        // loop through to find request parameters for setting variable values (from dynamic Criterion creation form)
        for (String key : params.keySet())
        {
            // should look like ${variable}
            Matcher
                matcher = varValuePattern.matcher(key);

            if (matcher.matches())
            {
                String
                    mapKey = matcher.group(1),
                    value[] = params.get(key);
                
                varMap.put(mapKey, value[0]);
            }
        }

        // report protocol level errors for bad requests
        if (certId == null || certId.length == 0)
        {
            //error
            response.sendError(400, "certificate ID not provided");
            return;
        }
        if (templateId == null || templateId.length == 0)
        {
            //error
            response.sendError(400, "template ID not provided");
            return;
        }

        // preconditions are met for a valid request - do the actual work
        // get the certificate def.
        CertificateDefinition
            cert = cs.getCertificateDefinition(certId[0]);

        // get the CriteriaTemplate - first need to get the CriteriaFactory which holds the CriteraTemplate
        CriteriaFactory
            critFact = cs.getCriteriaFactory(templateId[0]);
        CriteriaTemplate
            template = critFact.getCriteriaTemplate(templateId[0]);

        Criterion
            newCriterion = null;

        // create the criterion based on the form contents
        try
        {
            newCriterion = critFact.createCriterion(template, varMap);
        }
        catch (InvalidBindingException ibe)
        {
            response.sendError(400, ibe.getLocalizedMessage());
            return;
        }
       
        cs.addAwardCriterion(certId[0], newCriterion);

        //refresh the certificate definition in memory so it has the new criterion
        state.setCertificateDefinition(cs.getCertificateDefinition(certId[0]));
        mapper.writeValue(response.getOutputStream(), new CriterionTransferObject(template,newCriterion));
    }

    @RequestMapping(value="/removeCriterion.form")
    protected void removeCertCriteria(HttpServletRequest request, HttpServletResponse response)
    throws Exception
    {
    	if (!isAdministrator())
		{
    		response.sendError(400);
            return;
		}
    	
    	Map <String, String[]> params = request.getParameterMap();
    	
    	String certId[] = params.get("certId");
    	String criterionId[] = params.get("criterionId");
    	CertificateService
        	cs = getCertificateService();
    	
    	cs.removeAwardCriterion(certId[0], criterionId[0]);
	    
	    CertificateToolState.getState().setCertificateDefinition(cs.getCertificateDefinition(certId[0]));
	    mapper.writeValue(response.getOutputStream(), criterionId[0]);
	  
    }

}
