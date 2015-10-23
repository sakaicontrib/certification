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
import org.springframework.web.servlet.view.RedirectView;

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
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.api.criteria.UnknownCriterionTypeException;

/**
 * User: duffy
 * Date: Jun 7, 2011
 * Time: 4:15:18 PM
 */
@Controller
public class CertificateListController
    extends BaseCertificateController
{
	
	public static final String PAGINATION_NEXT = "next";
	public static final String PAGINATION_LAST = "last";
	public static final String PAGINATION_PREV = "previous";
	public static final String PAGINATION_FIRST = "first";
	public static final String PAGINATION_PAGE = "page";
	public static final String PAGE_SIZE = "pageSize";
	public static final String PAGE_NO = "pageNo";
	public static final List<Integer> PAGE_SIZE_LIST = Arrays.asList(10,25,50,100,Integer.MAX_VALUE);
	
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
	    		pageSize = PAGE_SIZE_LIST.get(0);
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
    	Map<String, CertificateAward>
            certAwardList = new HashMap<String, CertificateAward>();
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
            }
			
	    	certList = new PagedListHolder();
	    	if(pageSize != null)
	    	{
	    		certList.setPageSize(pageSize);
	    	}
	    	else
	    	{
	    		pageSize = PAGE_SIZE_LIST.get(0);
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
        session.setAttribute ("certAwardList", certAwardList);
        model.put("certList", certList);
        model.put("certAwardList", certAwardList);
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
            certAward = certificateService.getCertificateAward(certId);
        }
        catch (IdUnusedException e)
        {
            //no problem - it simply may not have been awarded yet
        }

        try
    	{
    		if(certAward == null)
    		{
                certificateService.awardCertificate(certId, userId());
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

            certName = certName.replaceAll("[^a-zA-Z0-9]","_");

            fNameBuff.append (sdf.format(award.getCertificationTimeStamp())).append('_');
            fNameBuff.append (certName).append(extension);

			response.setContentType(dts.getPreviewMimeType(template));
            response.addHeader("Content-Disposition", "attachement; filename = " + fNameBuff.toString());
            response.setHeader("Cache-Control", "");
            response.setHeader("Pragma", "");


            OutputStream
                out = response.getOutputStream();
            InputStream
                in = dts.render(template, award, definition.getFieldValues());

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
