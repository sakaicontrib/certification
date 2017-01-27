package com.rsmart.certification.tool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.tool.cover.SessionManager;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.beans.support.SortDefinition;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.rsmart.certification.api.CertificateAward;
import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.CertificateDefinitionStatus;
import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.DocumentTemplate;
import com.rsmart.certification.api.DocumentTemplateException;
import com.rsmart.certification.api.DocumentTemplateService;
import com.rsmart.certification.api.TemplateReadException;
import com.rsmart.certification.api.UnmetCriteriaException;
import com.rsmart.certification.api.VariableResolutionException;
import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.api.criteria.UnknownCriterionTypeException;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.DueDatePassedCriterionHibernateImpl;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.FinalGradeScoreCriterionHibernateImpl;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.GradebookItemCriterionHibernateImpl;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.GreaterThanScoreCriterionHibernateImpl;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.WillExpireCriterionHibernateImpl;
import com.rsmart.certification.tool.utils.ExtraUserPropertyUtility;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

/**
 * User: duffy
 * Date: Jun 7, 2011
 * Time: 4:15:18 PM
 */
@Controller
public class CertificateListController extends BaseCertificateController
{
	public static final String PAGINATION_NEXT = "next";
	public static final String PAGINATION_LAST = "last";
	public static final String PAGINATION_PREV = "previous";
	public static final String PAGINATION_FIRST = "first";
	public static final String PAGINATION_PAGE = "page";
	public static final String PAGE_SIZE = "pageSize";
	public static final String PAGE_NO = "pageNo";
	public static final List<Integer> PAGE_SIZE_LIST = Arrays.asList(10,25,50,100,200,Integer.MAX_VALUE);

    private final String STUDENT_NUMBER_SAKAI_PROPERTY= "certification.studentnumber.key";
    private final String studentNumberKey = ServerConfigurationService.getString(STUDENT_NUMBER_SAKAI_PROPERTY);

	private String getAbsoluteUrlForRedirect(String redirectTo)
	{
        String placementId = getToolManager().getCurrentPlacement().getId();
        String siteId = getToolManager().getCurrentPlacement().getContext();
        String portalurl = ServerConfigurationService.getPortalUrl();
        //SAKAI 10
        // String redirectPrefix = portalurl + "/tool/" + placementId;
        //This one is for SAKAI 11
        String redirectPrefix = portalurl + "/site/" + siteId + "/tool/" + placementId;
        String redirectString = "redirect:" + redirectPrefix + "/" + redirectTo;
        return redirectString;
	}
	
	@RequestMapping("/list.form")
	public ModelAndView certListHandler(@RequestParam(value=PAGINATION_PAGE, required=false) String page,
			@RequestParam(value=PAGE_SIZE, required=false) Integer pageSize,
			@RequestParam(value=PAGE_NO, required=false) Integer pageNo, HttpServletRequest request) throws Exception
    {
		if(isAdministrator())
		{
			return certAdminListHandler(page, pageSize, pageNo, request);
		}
		else
		{
			return certParticipantListHandler(page, pageSize, pageNo, request);
		}
	}

    public ModelAndView certAdminListHandler(String page, Integer pageSize, Integer pageNo, HttpServletRequest request) throws Exception
    {
    	ModelAndView
            mav = new ModelAndView("certviewAdmin");

    	Map<String, Object>
            model = new HashMap<String, Object>();
    	
    	List<CertificateDefinition>
            certDefList = new ArrayList<CertificateDefinition>();

        HttpSession
            session = request.getSession();

        PagedListHolder
            certList = null;

    	if(page==null)
		{
    		String
                siteId = siteId();

			certDefList.addAll(getCertificateService().getCertificateDefinitionsForSite(siteId));

            certList = new PagedListHolder(certDefList);
	    	if(pageSize != null)
	    	{
	    		certList.setPageSize(pageSize);
	    	}
	    	else
	    	{
	    		pageSize = PAGE_SIZE_LIST.get(3);
	    		certList.setPageSize(pageSize);
	    	}
	    	if(pageNo != null)
	    	{
	    		certList.setPage(pageNo);
	    	}
            certList.setSort(
                new SortDefinition()
                {
                    public String getProperty() {
                        return "name";
                    }

                    public boolean isIgnoreCase() {
                        return true;
                    }

                    public boolean isAscending() {
                        return true;
                    }
                }
            );

            certList.resort();                
		}
    	else
    	{
    		certList = (PagedListHolder) session.getAttribute("certList");

    		if(PAGINATION_NEXT.equals(page)  && !certList.isLastPage())
    		{
    			certList.nextPage();
    		}
    		else if(PAGINATION_LAST.equals(page))
    		{
    			certList.setPage(certList.getLastLinkedPage());
    		}
    		else if(PAGINATION_PREV.equals(page) && !certList.isFirstPage())
    		{
    			certList.previousPage();
    		}
    		else if(PAGINATION_FIRST.equals(page))
    		{
    			certList.setPage(certList.getFirstLinkedPage());
    		}
    	}

        session.setAttribute("certList", certList);
        model.put("certList", certList);
        model.put("pageSizeList", PAGE_SIZE_LIST);
        model.put("pageNo", certList.getPage());
        model.put("pageSize", pageSize);
        model.put("firstElement", (certList.getFirstElementOnPage()+1));
        model.put("lastElement", (certList.getLastElementOnPage()+1));
    	mav.addAllObjects(model);
    	return mav;
    }
    
    public ModelAndView certParticipantListHandler(String page, Integer pageSize, Integer pageNo, HttpServletRequest request) throws Exception
    {
        final CertificateService
            cs = getCertificateService();
    	ModelAndView
            mav = new ModelAndView("certviewParticipant");
		Map<String, Object>
            model = new HashMap<String, Object>();
    	
        Set<CertificateDefinition>
            certDefs = null;
    	List<CertificateDefinition>
            filteredList = new ArrayList<CertificateDefinition>();
        //TODO: Remove this when ready
    	Map<String, CertificateAward>
            certAwardList = new HashMap<String, CertificateAward>();

        Map<String, List<Map.Entry<String, String>>> certRequirementList = new HashMap<String, List<Map.Entry<String, String>>>();
        Map<String, Boolean> certificateIsAwarded = new HashMap<String, Boolean>();

        HttpSession
            session = request.getSession();
        PagedListHolder
            certList = null;

        Set<Criterion>
            unmet = (Set<Criterion>)SessionManager.getCurrentToolSession().getAttribute("unmetCriteria");

        if (unmet != null)
        {
            SessionManager.getCurrentToolSession().removeAttribute("unmetCriterion");
            request.setAttribute("unmetCriteria", unmet);
        }
                    
    	if(page==null)
		{
            certDefs = cs.getCertificateDefinitionsForSite
                        (siteId(),
                         new CertificateDefinitionStatus[]
                         {
                            CertificateDefinitionStatus.ACTIVE,
                            CertificateDefinitionStatus.INACTIVE
                         });

            List<String>
                certDefIds = new ArrayList<String>();

            for(CertificateDefinition cfl : certDefs)
            {
                certDefIds.add(cfl.getId());
                List<Map.Entry<String, String>> requirementList = new ArrayList<Map.Entry<String, String>>();
                try
                {
                    requirementList = cs.getCertificateRequirementsForUser(cfl.getId(), userId(), siteId());
                }
                catch (IdUnusedException e)
                {
                    logger.warn("While getting certificate requirements, found unused certificate id: " + cfl.getId());
                }
                certRequirementList.put (cfl.getId(), requirementList);
            }

            String
                cdIdArr[] = new String [certDefIds.size()];

            certDefIds.toArray(cdIdArr);

            certAwardList = cs.getCertificateAwardsForUser(cdIdArr);

            for (CertificateDefinition cd : certDefs)
            {
                if (CertificateDefinitionStatus.ACTIVE.equals(cd.getStatus()) ||
                    certAwardList.containsKey(cd.getId()))
                {
                    filteredList.add(cd);
                }

                boolean awarded=true;
                Set<Criterion> awardCriteria = cd.getAwardCriteria();
                Iterator<Criterion> itAwardCriteria = awardCriteria.iterator();
                while (itAwardCriteria.hasNext())
                {
                    Criterion crit = itAwardCriteria.next();
                    CriteriaFactory critFact = crit.getCriteriaFactory();
                    if (!critFact.isCriterionMet(crit))
                    {
                        awarded=false;
                    }
                }
                certificateIsAwarded.put(cd.getId(), awarded);
            }

	    	certList = new PagedListHolder();
	    	if(pageSize != null)
	    	{
	    		certList.setPageSize(pageSize);
	    	}
	    	else
	    	{
	    		pageSize = PAGE_SIZE_LIST.get(3);
	    		certList.setPageSize(pageSize);
	    	}
	    	if(pageNo != null)
	    	{
	    		certList.setPage(pageNo);
	    	}
            certList.setSource(filteredList);

            certList.setSort(
                new SortDefinition()
                {
                    public String getProperty() {
                        return "name";
                    }

                    public boolean isIgnoreCase() {
                        return true;
                    }

                    public boolean isAscending() {
                        return true;
                    }
                }
            );

            certList.resort();
		}
		else
		{
			certList = (PagedListHolder) session.getAttribute("certList");
			certAwardList = (Map) session.getAttribute("certAwardList");
			certRequirementList = (Map) session.getAttribute("certRequirementList");
			certificateIsAwarded = (Map) session.getAttribute("certIsAwarded");

    		if(PAGINATION_NEXT.equals(page)  && !certList.isLastPage())
    		{
    			certList.nextPage();
    		}
    		else if(PAGINATION_LAST.equals(page))
    		{
    			certList.setPage(certList.getLastLinkedPage());
    		}
    		else if(PAGINATION_PREV.equals(page) && !certList.isFirstPage())
    		{
    			certList.previousPage();
    		}
    		else if(PAGINATION_FIRST.equals(page))
    		{
    			certList.setPage(certList.getFirstLinkedPage());
    		}
		}

        session.setAttribute ("certList", certList);
        //TODO: Remove this when ready
        session.setAttribute ("certAwardList", certAwardList);
        session.setAttribute ("certRequirementList", certRequirementList);
        session.setAttribute ("certIsAwarded", certificateIsAwarded);
        model.put("certList", certList);
        //TODO: Remove this when ready
        model.put("certAwardList", certAwardList);
        model.put("certRequirementList", certRequirementList);
        model.put("certIsAwarded", certificateIsAwarded);
        model.put("pageSizeList", PAGE_SIZE_LIST);
        model.put("pageNo", certList.getPage());
        model.put("firstElement", (certList.getFirstElementOnPage()+1));
        model.put("lastElement", (certList.getLastElementOnPage()+1));

		mav.addAllObjects(model);
		return mav;
    }
    
    @RequestMapping("/checkstatus.form")
    public ModelAndView checkCertAwardStatus(@RequestParam("certId") String certId, HttpServletRequest request,
    		HttpServletResponse response)
        throws Exception
    {
    	/*
			should take a certificateDefinition ID as a parameter
			check if CertificateAward already exists
				(CertificateService.getCertificateAward)
			otherwise
				CertificateService.awardCertificate
				
			if user can't receive certificate an UnmetCriteriaException is thrown
				- this contains a Set<Criterion> to display what hasn't been completed
				
			otherwise - forward to printCertificate
    	 */
        CertificateAward
            certAward = null;
        HashMap<String, Object>
            model = new HashMap<String, Object>();

        try
        {
            certAward = getCertificateService().getCertificateAward(certId);
        }
        catch (IdUnusedException e)
        {
            //no problem - it simply may not have been awarded yet
        }

        try
    	{
    		if(certAward == null)
    		{
                getCertificateService().awardCertificate(certId, userId());
    		}

            return new ModelAndView(getAbsoluteUrlForRedirect("printPreview.form?certId=" + certId));
    	}
    	catch (UnmetCriteriaException umet)
    	{
    		Set<Criterion>
                criterion = umet.getUnmetConditions();

            SessionManager.getCurrentToolSession().setAttribute("unmetCriteria", criterion);
            
            return new ModelAndView(getAbsoluteUrlForRedirect("list.form"),model);
            //return certListHandler(null, null, null, request);
    	}
        catch (IdUnusedException e)
        {
            //error this is a bogus ID
            return new ModelAndView (getAbsoluteUrlForRedirect("list.form"), model);
        }
        catch (UnknownCriterionTypeException e)
        {
            //error processing the criteria
            return new ModelAndView (getAbsoluteUrlForRedirect("list.form"), model);
        }
    }
    
    @RequestMapping("/printPreview.form")
    public ModelAndView printPreviewCertificateHandler(@RequestParam("certId") String certId,
                                        HttpServletRequest request,
    		                            HttpServletResponse response)
        throws TemplateReadException
    {
        CertificateService
            certService = getCertificateService();
        CertificateDefinition
            definition = null;
        CertificateAward
            award = null;

        try
        {
            definition = certService.getCertificateDefinition(certId);
        }
        catch (IdUnusedException e)
        {
            //error
        }

        try
        {
            award = getCertificateService().getCertificateAward(certId);
        }
        catch (IdUnusedException e)
        {
            //error
        }

        if (!isAwardPrintable(award))
        {
            //error
        }

        if (award == null)
        {
            //error
        }
        
        
        Map<String, Object>
        model = new HashMap<String, Object>();

        model.put("cert", definition);
        model.put("award", award);
        
        //Below code is commented to remove the preview functionality issue #CLE-9696
        /*DocumentTemplate
            template = definition.getDocumentTemplate();
        DocumentTemplateService
            dts = getDocumentTemplateService();
        boolean
            previewable = dts.isPreviewable(template);
        
        model.put("previewable", previewable);

        if (previewable)
        {
            model.put ("previewableMimeType", dts.getPreviewMimeType(template));
        }*/

        return new ModelAndView ("printPreview", model);
    	/*
    		should take a certificateDefinition ID as a parameter
    		see if the user has a CertificateAward for the the CertDefn
    		get the DocumentTemplate from the CertificateDefinition
    		create a preview with DocumentTemplateService calls:
    			isPreviewable()
    			getPreviewMimeType()
    			renderPreview()
    		create a final rendering with:
    			render()
		*/
    }

    @RequestMapping("/printData.form")
    public void previewDataHandler(@RequestParam("certId") String certId,
                                        HttpServletRequest request,
    		                            HttpServletResponse response)
    {
        CertificateService
            certService = getCertificateService();
        CertificateDefinition
            definition = null;
        CertificateAward
            award = null;

        try
        {
            definition = certService.getCertificateDefinition(certId);
        }
        catch (IdUnusedException e)
        {
            //error
        }

        try
        {
            award = getCertificateService().getCertificateAward(certId);
        }
        catch (IdUnusedException e)
        {
            //error
        }

        if (!isAwardPrintable(award))
        {
            //error
        }

        if (award == null)
        {
            //error
        }

        DocumentTemplate
            template = definition.getDocumentTemplate();

        DocumentTemplateService
            dts = getDocumentTemplateService();

        try
        {
            if (!dts.isPreviewable(template))
            {

            }

            response.setContentType(dts.getPreviewMimeType(template));

            OutputStream
                out = response.getOutputStream();
            InputStream
                in = dts.renderPreview(template, award, definition.getFieldValues());

            byte
                buff[] = new byte[2048];
            int
                numread = 0;

            while ((numread = in.read(buff)) != -1)
            {
                out.write(buff, 0, numread);
            }
        }
        catch (TemplateReadException e)
        {
            //error
        }
        catch (VariableResolutionException e)
        {
        }
        catch (IOException e)
        {
        }

    }

    @RequestMapping("/delete.form")
    public ModelAndView deleteCertificateHandler(@RequestParam("certId") String certId,
                    HttpServletRequest request,
                    HttpServletResponse response)
    {

        HashMap<String, String>
            model = new HashMap<String, String>();

        if (!isAdministrator())
        {
            model.put(ERROR_MESSAGE, "error.not.admin");
        }

        if (certId == null || certId.trim().length() == 0)
        {
            model.put(ERROR_MESSAGE, "error.no.selection");
        }

        try
        {
            getCertificateService().deleteCertificateDefinition(certId);
        }
        catch (IdUnusedException e)
        {
            model.put(ERROR_MESSAGE, "error.bad.id");
        }
        catch (DocumentTemplateException dte)
        {
        	model.put(ERROR_MESSAGE, "form.error.templateProcessingError");
        }

        if (model.size () > 0)
        {
            return new ModelAndView ("redirect:list.form", model);
        }

        return new ModelAndView ("redirect:list.form");
    }

    @RequestMapping("/print.form")
    public void printCertificateHandler(@RequestParam("certId") String certId,
                                        HttpServletRequest request,
    		                            HttpServletResponse response)
    {
        CertificateService
            certService = getCertificateService();
        CertificateDefinition
            definition = null;

        try
        {
            definition = certService.getCertificateDefinition(certId);
        }
        catch (IdUnusedException e)
        {
            //error
        }

        DocumentTemplate
            template = definition.getDocumentTemplate();

        DocumentTemplateService
            dts = getDocumentTemplateService();

        try
        {
            StringBuffer
                fNameBuff = new StringBuffer();
            SimpleDateFormat
                sdf = new SimpleDateFormat("yyyy_MM_dd");
            String
                certName = definition.getName(),
                templName = template.getName(),
                extension = "";
            int
                dotIndex = -1;

            if (templName != null && (dotIndex = templName.lastIndexOf('.')) > -1)
            {
                extension = templName.substring(dotIndex);
            }

            certName = certName.replaceAll("[^a-zA-Z0-9]+","-");

            //TODO: replace with issue date
            //fNameBuff.append (sdf.format(award.getCertificationTimeStamp())).append('_');
            fNameBuff.append (certName).append(extension);

			response.setContentType(dts.getPreviewMimeType(template));
            response.addHeader("Content-Disposition", "attachement; filename = " + fNameBuff.toString());
            response.setHeader("Cache-Control", "");
            response.setHeader("Pragma", "");


            OutputStream
                out = response.getOutputStream();
            InputStream
                in = dts.render(template, definition, userId());

            byte
                buff[] = new byte[2048];
            int
                numread = 0;

            while ((numread = in.read(buff)) != -1)
            {
                out.write(buff, 0, numread);
            }
        }
        catch (TemplateReadException e)
        {
            //error
        }
        catch (VariableResolutionException e)
        {
        }
        catch (IOException e)
        {
        }

    }

    /**
     * This method handles the report. This includes landing on the report view, handling the paging navigators, 
     * and exporting the csv. However, returning to the certificates list is handled in jsp
     * @param certId the certificate on which is being reported
     * @param page the destination (next, previous, first, last)
     * @param pageSize the page size (for the paging navigator)
     * @param pageNo the destination (specified number)
     * @param export true if exporting a csv
     * @param request http request
     * @param response http response
     * @return the ModelAndView object for jsp
     * @throws Exception
     */
    @RequestMapping("/reportView.form")
    public ModelAndView certAdminReportHandler(@RequestParam("certId") String certId, @RequestParam(value=PAGINATION_PAGE, required=false) String page,
            @RequestParam(value=PAGE_SIZE, required=false) Integer pageSize,
            @RequestParam(value=PAGE_NO, required=false) Integer pageNo,
            @RequestParam(value="export", required=false) Boolean export,
            HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        if (!isAdministrator())
        {
            //only people who have permission to add/edit certificates can see this report
            return null;
        }

        //Will be used to 'cache' some data to speed up the paging navigator
        HttpSession session = request.getSession();

        /*The Report table's headers for columns that are related to the certificate definition's criteria 
         * (other headers are already handled in jsp)*/
        List<Object> criteriaHeaders = new ArrayList<Object>();

        //holds the contents of the table, the page number, the page size, etc.
        PagedListHolder reportList = null;

        //Everything we pass to the UI
        HashMap<String, Object> model = new HashMap<String, Object>();

        //Pass the certificate definition to the UI (so it can print its name and use its id as necessary)
        CertificateService certService = getCertificateService();
        CertificateDefinition definition = null;

        try
        {
            definition = certService.getCertificateDefinition(certId);
            if (logIfNull(definition, "cannot retrieve certificate definition for certId = " + certId))
            {
                return null;
            }
        }
        catch (IdUnusedException e)
        {
            //they sent an invalid certId in their http GET;
            /*possible causes: they clicked on View Report after another user deleted the certificate definition,
            or they attempted to do evil with a random http GET.
            We don't care, show them nothing*/
            return null;
        }

        model.put("cert", definition);

        //for internationalization - loads Messages.properties
        ResourceLoader messages = new ResourceLoader("com.rsmart.certification.tool.Messages");

        //we'll need this to get additional user properties
        ExtraUserPropertyUtility extraPropsUtil = ExtraUserPropertyUtility.getInstance();
        //determines if the current user has permission to view extra properties
        boolean canShowUserProps = extraPropsUtil.isExtraUserPropertiesEnabled() && extraPropsUtil.isExtraPropertyViewingAllowedForCurrentUser();
        List<String> propHeaders = new ArrayList<String>();
        List<String> requirements = new ArrayList<String>();

        if(page==null && export==null)
        {
            //It's their first time hitting the page or they changed the page size 
            // -we'll load/refresh all the data

            //get the requirements for the current user
            Iterator<Criterion> itCriterion = definition.getAwardCriteria().iterator();
            while (itCriterion.hasNext())
            {
                Criterion crit = itCriterion.next();
                if ( !(crit instanceof WillExpireCriterionHibernateImpl) )
                {
                    //we only care about criteria that affect whether the certificate is awarded
                    //WillExpireCriteironHibernateImpl has no effect on whether it is awarded
                    requirements.add(crit.getExpression());
                }
            }

            //Get the headers for the additional user properties
            //keeps track of the order of the keys so that we know that the headers and the cells line up
            List<String> propKeys = new ArrayList<String> ();
            //contains the headers that we'll push to jsp
            if (canShowUserProps)
            {
                Map<String, String> propKeysTitles = extraPropsUtil.getExtraUserPropertiesKeyAndTitleMap();
                propKeys = new ArrayList<String>(propKeysTitles.keySet());
                //perhaps valueSet() does the same thing, but I'm being cautious about the order
                Iterator<String> itPropKeys = propKeys.iterator();
                while (itPropKeys.hasNext())
                {
                    String key = itPropKeys.next();
                    propHeaders.add(propKeysTitles.get(key));
                }
            }

            //Use orderedCriteria to keep track of the order of the headers so that we can populate the table accordingly
            ArrayList<Criterion> orderedCriteria = new ArrayList<Criterion>();

            //truncates decimals if it gets a whole number; shows decimals otherwise
            NumberFormat numberFormat = NumberFormat.getNumberInstance();

            //iterate through the certificate definition's criteria, and grab headers for the criteria columns accordingly
            itCriterion = definition.getAwardCriteria().iterator();
            while (itCriterion.hasNext())
            {
                Criterion crit = itCriterion.next();
                if (logIfNull(crit, "definition contained null criterion. certId: " + certId))
                {
                    return null;
                }

                if (crit instanceof DueDatePassedCriterionHibernateImpl)
                {
                    DueDatePassedCriterionHibernateImpl ddpCrit = (DueDatePassedCriterionHibernateImpl) crit;
                    //says 'Due date for <itemName>'
                    criteriaHeaders.add(messages.getFormattedMessage("report.table.header.duedate", new Object[]{ddpCrit.getItemName()}));
                }
                else if (crit instanceof FinalGradeScoreCriterionHibernateImpl)
                {
                    FinalGradeScoreCriterionHibernateImpl fgsCrit = (FinalGradeScoreCriterionHibernateImpl) crit;
                    //says 'Final Course Grade'
                    criteriaHeaders.add(messages.getString("report.table.header.fcg"));
                }
                else if (crit instanceof GreaterThanScoreCriterionHibernateImpl)
                {
                    GreaterThanScoreCriterionHibernateImpl gtsCrit = (GreaterThanScoreCriterionHibernateImpl) crit;
                    //says '<itemName>'
                    criteriaHeaders.add(gtsCrit.getItemName());
                }
                else if (crit instanceof WillExpireCriterionHibernateImpl)
                {
                    //says 'Expires'
                    criteriaHeaders.add(0, messages.getString("report.table.header.expire"));
                }
                else if (crit instanceof GradebookItemCriterionHibernateImpl)
                {
                    //I believe this is only used as a parent class and this code will never be reached
                    logger.warn("certAdminReportHandler failed to find a child criterion for a GradebookItemCriterion");
                    GradebookItemCriterionHibernateImpl giCrit = (GradebookItemCriterionHibernateImpl) crit;
                    criteriaHeaders.add(giCrit.getItemName());
                }

                //Expiration date should immediately follow issue date
                if (crit instanceof WillExpireCriterionHibernateImpl)
                {
                    //0th position immediately follows the issue date
                    orderedCriteria.add(0, crit);
                }
                else
                {
                    //all other criteria go at the back
                    orderedCriteria.add(crit);
                }

            }

            //Prepare the Report table's contents
            List<ReportRow> reportRows = new ArrayList<ReportRow>();

            /* Iterate through the list of users who have the ability to be awarded certificates,
             * populate each row of the table accordingly*/
            List<String> userIds = getAwardableUserIds();
            Iterator<String> itUser = userIds.iterator();
            while (itUser.hasNext())
            {
                String userId = itUser.next();
                try
                {
                    //get their user object
                    User currentUser = getUserDirectoryService().getUser(userId);

                    //The user exists, so create their row
                    ReportRow currentRow = new ReportRow();

                    //set the name
                    String firstName = currentUser.getFirstName();
                    String lastName = currentUser.getLastName();
                    //do it in an appropriate format
                    setNameFieldForReportRow(currentRow, firstName, lastName);

                    currentRow.setUserId(currentUser.getEid());
                    ArrayList<String> extraProps = new ArrayList<String>();
                    if (canShowUserProps)
                    {
                        Map<String, String> extraPropsMap = extraPropsUtil.getExtraPropertiesMapForUser(currentUser);
                        Iterator<String> itKeys = propKeys.iterator();
                        while (itKeys.hasNext())
                        {
                            String key = itKeys.next();
                            extraProps.add(extraPropsMap.get(key));
                        }
                    }
                    currentRow.setExtraProps(extraProps);

                    //Get the issue date (need the CriteriaFactory to do this)
                    //We can get the criteria factory from any criterion

                    //don't be alarmed by the null checks, none of these should ever happen
                    if (orderedCriteria.isEmpty())
                    {
                        logger.error("orderedCriteria is empty. certId: " + certId);
                        return null;
                    }
                    Criterion tempCrit = orderedCriteria.get(0);
                    if (logIfNull(tempCrit, "null criterion in orderedCriteria for certId: " + certId))
                    {
                        return null;
                    }

                    CriteriaFactory criteriaFactory = tempCrit.getCriteriaFactory();
                    if (logIfNull(criteriaFactory, "null criteriaFactory for criterion: " + tempCrit.getId()))
                    {
                        return null;
                    }

                    Date issueDate = criteriaFactory.getDateIssued(userId, siteId(), definition);
                    if (issueDate == null)
                    {
                        //certificate was not awarded to this user
                        currentRow.setIssueDate(null);
                    }
                    else
                    {
                        //format the date
                        String formatted = dateFormat.format(issueDate);
                        currentRow.setIssueDate(formatted);
                    }

                    //Now populate the criterionCells by iterating through the criteria (in the order that they appear)
                    List<String> criterionCells = new ArrayList<String>();
                    Iterator<Criterion> itCriteria = orderedCriteria.iterator();
                    while (itCriteria.hasNext())
                    {
                        Criterion crit = itCriteria.next();
                        if (logIfNull(crit, "null criterion in orderedCriteria for certId: " + certId))
                        {
                            return null;
                        }

                        if (crit instanceof DueDatePassedCriterionHibernateImpl)
                        {
                            DueDatePassedCriterionHibernateImpl ddpCrit = (DueDatePassedCriterionHibernateImpl) crit;
                            Date dueDate = ddpCrit.getDueDate();

                            if (logIfNull(dueDate, "DueDatePassed Criterion without a due date" + crit.getId(), "warn"))
                            {
                                //place holder
                                criterionCells.add(null);
                            }
                            else
                            {
                                //add the formatted date to the criterion cells
                                String formatted = dateFormat.format(dueDate);
                                criterionCells.add(formatted);
                            }
                        }
                        else if (crit instanceof FinalGradeScoreCriterionHibernateImpl)
                        {
                            CriteriaFactory critFact = crit.getCriteriaFactory();
                            if (logIfNull (critFact, "criterion without a factory. crit: " + crit.getId()))
                            {
                                return null;
                            }

                            Double score = critFact.getFinalScore(userId, siteId());
                            if (score==null)
                            {
                                String incomplete = messages.getString("report.table.incomplete");
                                criterionCells.add(incomplete);
                            }
                            else
                            {
                                String formatted = numberFormat.format(score);
                                criterionCells.add(formatted);
                            }
                        }
                        else if (crit instanceof GreaterThanScoreCriterionHibernateImpl)
                        {
                            GreaterThanScoreCriterionHibernateImpl gtsCrit = (GreaterThanScoreCriterionHibernateImpl) crit;
                            CriteriaFactory critFact = gtsCrit.getCriteriaFactory();
                            if (logIfNull (critFact, "criterion without a factory. crit: " + gtsCrit.getId()))
                            {
                                return null;
                            }

                            Double score = critFact.getScore(gtsCrit.getItemId(), userId, siteId());
                            if (score == null)
                            {
                                String incomplete = messages.getString("report.table.incomplete");
                                criterionCells.add(incomplete);
                            }
                            else
                            {
                                String formatted = numberFormat.format(score);
                                criterionCells.add(formatted);
                            }
                        }
                        else if (crit instanceof WillExpireCriterionHibernateImpl)
                        {
                            if (issueDate == null)
                            {
                                //user didn't achieve the certificate, so expiration can't be calculated

                                //place holder
                                criterionCells.add(null);
                            }
                            else
                            {
                                WillExpireCriterionHibernateImpl weCrit = (WillExpireCriterionHibernateImpl) crit;
                                //get the expiry offset and add it to the issue date
                                String strExpiryOffset = weCrit.getExpiryOffset();
                                if (logIfNull(strExpiryOffset, "no expiry offset found for criterion: "+ weCrit.getId()))
                                {
                                    return null;
                                }
                                Integer expiryOffset = new Integer(strExpiryOffset);
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(issueDate);
                                cal.add(Calendar.MONTH, expiryOffset);
                                Date expiryDate = cal.getTime();
                                String formatted = dateFormat.format(expiryDate);
                                criterionCells.add(formatted);
                            }
                        }
                        else if (crit instanceof GradebookItemCriterionHibernateImpl)
                        {
                            //I believe this is only used as a parent class and this code will never be reached
                            logger.warn("certAdminReportHandler failed to find a child criterion for a GradebookItemCriterion");

                            //place holder
                            criterionCells.add(null);
                        }
                    }
                    currentRow.setCriterionCells(criterionCells);

                    //show whether the certificate was awarded
                    //certificate is awarded iff the issue date is null
                    if (issueDate == null)
                    {
                        String no = messages.getString("report.table.no");
                        currentRow.setAwarded(no);
                    }
                    else
                    {
                        String yes = messages.getString("report.table.yes");
                        currentRow.setAwarded(yes);
                    }

                    reportRows.add(currentRow);
                }
                catch (UserNotDefinedException e)
                {
                    //ignore
                }
            }

            //set up the paging navigator
            //the 'if' surrounding this scope: page == null && export == null
            //this happens when freshly arriving on this page or when changing the page size
            reportList = new PagedListHolder(reportRows);

            if(pageSize != null)
            {
                //they changed the page size
                reportList.setPageSize(pageSize);
            }
            else
            {
                //fresh arival, set the default page size
                //set default to 100
                pageSize = PAGE_SIZE_LIST.get(3);
                reportList.setPageSize(pageSize);
            }
            if(pageNo != null)
            {
                reportList.setPage(pageNo);
            }
            reportList.setSort(new SortDefinition()
            {
                public String getProperty() {
                    //sort by the getName() method
                    return "name";
                }

                public boolean isIgnoreCase() {
                    return true;
                }

                public boolean isAscending() {
                    return true;
                }
            });

            reportList.resort();
        }   // page==null && export==null
        else if (export == null)
        {
            // !(page == null && export == null) && export == null -> page != null
            // page != null -> they clicked a navigation button

            //pull the headers and the report list from the http session
            requirements = (List<String>) session.getAttribute("requirements");
            propHeaders = (List<String>) session.getAttribute("reportPropHeaders");
            criteriaHeaders = (List<Object>) session.getAttribute("reportCritHeaders");
            reportList = (PagedListHolder) session.getAttribute("reportList");

            //navigate appropriately
            if(PAGINATION_NEXT.equals(page) && !reportList.isLastPage())
            {
                reportList.nextPage();
            }
            else if(PAGINATION_LAST.equals(page))
            {
                reportList.setPage(reportList.getLastLinkedPage());
            }
            else if(PAGINATION_PREV.equals(page) && !reportList.isFirstPage())
            {
                reportList.previousPage();
            }
            else if(PAGINATION_FIRST.equals(page))
            {
                reportList.setPage(reportList.getFirstLinkedPage());
            }
        }   // export == null
        else if (export)
        {
            // they clicked Export as CSV
            //get the headers and the report list from the http session
            requirements = (List<String>) session.getAttribute("requirements");
            propHeaders = (List<String>) session.getAttribute("reportPropHeaders");
            criteriaHeaders = (List<Object>) session.getAttribute("reportCritHeaders");
            reportList = (PagedListHolder) session.getAttribute("reportList");

            try
            {
                definition = certService.getCertificateDefinition(certId);

                //prepare the http response header
                String mimeType = "text/csv";
                response.setContentType(mimeType);
                DateFormat filenameDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String today = filenameDateFormat.format(new Date());
                String report = messages.getString("report.export.fname");
                String defName = definition.getName();
                if (logIfNull(defName,"certificate name is null: "+ certId))
                {
                    return null;
                }
                defName = defName.replaceAll("\\s","_");
                response.addHeader("Content-Disposition", "attachment; filename = " + defName + "_" + report + "_" + today +".csv");
                response.setHeader("Cache-Control", "");
                response.setHeader("Pragma", "");

                //fill in the csv's header
                StringBuilder contents = new StringBuilder();
                appendItem(contents, messages.getString("report.table.header.name"), false);
                appendItem(contents, messages.getString("report.table.header.userid"), false);
                if (canShowUserProps)
                {
                    if (logIfNull(propHeaders, "propHeaders is null"))
                    {
                        return null;
                    }
                    Iterator<String> itPropHeaders = propHeaders.iterator();
                    while (itPropHeaders.hasNext())
                    {
                        appendItem(contents, itPropHeaders.next(), false);
                    }
                }
                appendItem(contents, messages.getString("report.table.header.issuedate"), false);

                Iterator<Object> itHeaders = criteriaHeaders.iterator();
                while (itHeaders.hasNext())
                {
                    appendItem(contents, (String) itHeaders.next(), false);
                }

                appendItem(contents, messages.getString("report.table.header.awarded"), true);

                // gets the original list of ReportRows
                List table = reportList.getSource();

                //fill the rest of the csv
                Iterator<Object> itTable = table.iterator();
                while (itTable.hasNext())
                {
                    Object objRow = itTable.next();
                    if (objRow instanceof ReportRow)
                    {
                        //represents a line in the table
                        ReportRow row = (ReportRow) objRow;
                        appendItem(contents, row.getName(), false);
                        appendItem(contents, row.getUserId(), false);
                        if (canShowUserProps)
                        {
                            List<String> extraProps = row.getExtraProps();
                            if (logIfNull(extraProps, "Extra props is null for certId: " + certId))
                            {
                                return null;
                            }
                            Iterator<String> itExtraProps = extraProps.iterator();
                            while (itExtraProps.hasNext())
                            {
                                appendItem(contents, itExtraProps.next(), false);
                            }
                        }
                        appendItem(contents, row.getIssueDate(), false);

                        Iterator<String> itCriterionCells = row.getCriterionCells().iterator();
                        while (itCriterionCells.hasNext())
                        {
                            appendItem(contents, itCriterionCells.next(), false);
                        }

                        appendItem(contents, row.getAwarded(), true);
                    }
                    else
                    {
                        //???
                        logger.warn("not a ReportRow:" + objRow);
                    }
                }

                //send contents
                String data = contents.toString();
                OutputStream out = response.getOutputStream();
                out.write(data.getBytes());
                out.flush();
                out.close();

                //we're not updating their view
                return null;
            }
            catch (IdUnusedException e)
            {
                //they sent an invalid certId in their http GET;
                /*possible causes: they clicked on View Report after another user deleted the certificate definition,
                or they attempted to do evil with a random http GET.
                We don't care*/
                return null;
            }
        }
        else
        {
            //should never happen
            logger.warn("hit reportView.form with export=false. Should never happen");
            return null;
        }

        //push the navigator and the headers to the http session
        session.setAttribute("requirements", requirements);
        session.setAttribute("reportPropHeaders", propHeaders);
        session.setAttribute("reportCritHeaders", criteriaHeaders);
        session.setAttribute("reportList", reportList);

        //populate the model as necessary
        model.put("requirements", requirements);
        model.put("userPropHeaders", propHeaders);
        model.put("critHeaders",criteriaHeaders);
        model.put("reportList", reportList);
        model.put("pageSizeList", PAGE_SIZE_LIST);
        model.put("pageNo", reportList.getPage());
        model.put("pageSize", reportList.getPageSize());
        model.put("firstElement", (reportList.getFirstElementOnPage()+1));
        model.put("lastElement", (reportList.getLastElementOnPage()+1));

        //send the model to the jsp
        ModelAndView mav = new ModelAndView("reportView", model);
        return mav;
    }

    /**
     * if the specified object is null, the specified message gets logged at the specified logging level
     * @param obj
     * @param message
     * @param level
     * @return
     */
    private boolean logIfNull(Object obj, String message, String level)
    {
        if (obj==null)
        {
            if (level == null)
            {
                logger.error(message);
            }
            else if ("warn".equals(level))
            {
                logger.warn(message);
            }
            return true;
        }
        return false;
    }

    /**
     * if the specified object is null, the specified message gets logged at the error logging level
     * @param obj
     * @param message
     * @return
     */
    private boolean logIfNull(Object obj, String message)
    {
        return logIfNull(obj, message, null);
    }

    /**
     * Sets the name field on the row in an appropriate format ('lastname, firstname' unless a name is missing)
     * @param row
     * @param firstName
     * @param lastName
     */
    private void setNameFieldForReportRow(ReportRow row, String firstName, String lastName)
    {
        if (lastName==null)
        {
            lastName = "";
        }

        if (firstName==null)
        {
            firstName = "";
        }

        //if one name is missing, use the opposite
        if ("".equals(lastName))
        {
            //use the opposite name or empty string if firstName is missing (both cases are covered here)
            row.setName(firstName);
        }
        else if ("".equals(firstName))
        {
            row.setName(lastName);
        }
        else
        {
            //both names present
            row.setName(lastName+", "+firstName);
        }
    }

    /**
     * Appends item to a StringBuilder for csv format by surrounding them in double quotes, and separating lines when appropriate
     * @param stringBuilder the StringBuilder we are appending to
     * @param item the item that we are appending to the csv
     * @param eol true if this is the last item in the current line
     */
    private void appendItem(StringBuilder stringBuilder, String item, boolean eol)
    {
        stringBuilder.append('\"');
        if (item!=null)
        {
            stringBuilder.append(item);
        }
        stringBuilder.append('\"');
        if (!eol)
        {
            stringBuilder.append(',');
        }
        else
        {
            stringBuilder.append('\n');
        }
    }

    public class ReportRow
    {
        String name = null;
        String userId = null;
        List<String> extraProps = null;
        String issueDate = null;
        List<String> criterionCells = null;
        String awarded = null;

        public ReportRow()
        {
            criterionCells = new ArrayList<String>();
        }

        public void setName(String name)
        {
            this.name=name;
        }

        public String getName()
        {
            return name;
        }

        public void setUserId(String userId)
        {
            this.userId=userId;
        }

        public String getUserId()
        {
            return userId;
        }

        public void setExtraProps(List<String> extraProps)
        {
            this.extraProps = extraProps;
        }

        public List<String> getExtraProps()
        {
            return extraProps;
        }

        public void setIssueDate(String issueDate)
        {
            this.issueDate = issueDate;
        }

        public String getIssueDate()
        {
            return issueDate;
        }

        public void setCriterionCells (List<String> criterionCells)
        {
            this.criterionCells = criterionCells;
        }

        public List<String> getCriterionCells()
        {
            return criterionCells;
        }

        public void setAwarded(String awarded)
        {
            this.awarded = awarded;
        }

        public String getAwarded()
        {
            return awarded;
        }
    }

   /* @RequestMapping("/admin/list/{pageno}")
	public ModelAndView certListHandler(@PathVariable("pageno") String pageno) 
    {
		ModelAndView mav = new ModelAndView("certviewAdmin");
		Map model = new HashMap();
    	setCertificateService(new MockCertificateService());
    	String siteid = toolManager.getCurrentPlacement().getContext();
    	Set<CertificateDefinition> certDefList = getCertificateService().getCertificateDefinitionsForSite(siteId);
    	
    	List<String> certDefIds = new ArrayList<String>();
    	for(CertificateDefinition cfl:certDefList)
    	{
    		certDefIds.add(cfl.getId());
    	}
    	
    	Map<String, CertificateAward> certAwardList = getCertificateService().getCertificateAwardsForUser((String[])certDefIds.toArray());
    	PagedListHolder certDefPageList = new PagedListHolder(certDefList);
    	certDefPageList.setPageSize(5);
		return mav;
	}*/
}
