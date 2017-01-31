package com.rsmart.certification.tool;

import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.CertificateDefinitionStatus;
import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.DocumentTemplate;
import com.rsmart.certification.api.DocumentTemplateException;
import com.rsmart.certification.api.DocumentTemplateService;
import com.rsmart.certification.api.TemplateReadException;
import com.rsmart.certification.api.VariableResolutionException;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.WillExpireCriterionHibernateImpl;
import com.rsmart.certification.tool.utils.ExtraUserPropertyUtility;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.beans.support.SortDefinition;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * User: duffy
 * Date: Jun 7, 2011
 * Time: 4:15:18 PM
 */
@Controller
public class CertificateListController extends BaseCertificateController
{
	public static final String THIS_PAGE = "list.form";

	//Pagination request params
	public static final String PAGINATION_NEXT = "next";
	public static final String PAGINATION_LAST = "last";
	public static final String PAGINATION_PREV = "previous";
	public static final String PAGINATION_FIRST = "first";
	public static final String PAGINATION_PAGE = "page";
	public static final String PAGE_SIZE = "pageSize";
	public static final String PAGE_NO = "pageNo";
	public static final List<Integer> PAGE_SIZE_LIST = Arrays.asList(10,25,50,100,200,Integer.MAX_VALUE);

	//Other request params
	public static final String PARAM_CERT_ID = "certId";
	public static final String PARAM_EXPORT = "export";

	//Request params to filter the report view
	public static final String PARAM_DISPLAY_FILTER_TYPE = "filterType";
	public static final String PARAM_DISPLAY_FILTER_DATE_TYPE = "filterDateType";
	public static final String PARAM_DISPLAY_FILTER_START_DATE = "filterStartDate";
	public static final String PARAM_DISPLAY_FILTER_END_DATE = "filterEndDate";
	public static final String PARAM_DISPLAY_FILTER_HISTORICAL = "filterHistorical";

	//sakai.properties
	private final String MAIL_SUPPORT_SAKAI_PROPERTY =  "mail.support";
	private final String MAIL_SUPPORT = ServerConfigurationService.getString(MAIL_SUPPORT_SAKAI_PROPERTY);

	private static final int DEFAULT_FILTER_DAYS;
	static
	{
		String strDefaultFilterDays = ServerConfigurationService.getString("certification.reportFilter.defaultFilterDays");
		int intDefaultFilterDays;
		try
		{
			intDefaultFilterDays = Integer.parseInt(strDefaultFilterDays);
		}
		catch (Exception e)
		{
			//default is 1 year
			intDefaultFilterDays = 365;
		}
		DEFAULT_FILTER_DAYS = intDefaultFilterDays;
	}

	// JSP views
	private final String ADMIN_VIEW = "certviewAdmin";
	private final String PARTICIPANT_VIEW = "certviewParticipant";
	private final String UNAUTHORIZED_VIEW = "certviewUnauthorized";
	private final String REPORT_VIEW = "reportView";

	private final String CERTIFICATE_NAME_PROPERTY = "name";

	//Keys for http session attributes
	private final String SESSION_LIST_ATTRIBUTE = "certList";
	private final String SESSION_REQUIREMENTS_ATTRIBUTE = "requirements";
	private final String SESSION_EXPIRY_OFFSET_ATTRIBUTE = "expiryOffset";
	private final String SESSION_REPORT_PROP_HEADERS_ATTRIBUTE = "reportPropHeaders";
	private final String SESSION_REPORT_CRIT_HEADERS_ATTRIBUTE = "reportCritHeaders";
	private final String SESSION_REPORT_LIST_ATTRIBUTE = "reportList";
	private final String SESSION_REQUIREMENT_LIST_ATTRIBUTE = "certRequirementList";
	private final String SESSION_IS_AWARDED_ATTRIBUTE = "certIsAwarded";
	private final String SESSION_ORDERED_CRITERIA = "orderedCriteria";

	//Keys for mav models
	private final String MODEL_KEY_CERTIFICATE_LIST = "certList";
	private final String MODEL_KEY_PAGE_SIZE_LIST = "pageSizeList";
	private final String MODEL_KEY_PAGE_NO = "pageNo";
	private final String MODEL_KEY_PAGE_SIZE = "pageSize";
	private final String MODEL_KEY_FIRST_ELEMENT = "firstElement";
	private final String MODEL_KEY_LAST_ELEMENT = "lastElement";
	private final String MODEL_KEY_CERTIFICATE = "cert";
	private final String MODEL_KEY_REQUIREMENT_LIST_ATTRIBUTE = "certRequirementList";
	private final String MODEL_KEY_IS_AWARDED_ATTRIBUTE = "certIsAwarded";
	private final String MODEL_KEY_ERROR_ARGUMENTS_ATTRIBUTE = "errorArgs";
	private final String MODEL_KEY_ERRORS_ATTRIBUTE = "errors";
	private final String MODEL_KEY_USE_DEFAULT_DISPLAY_OPTIONS = "useDefaultDisplayOptions";
	private final String MODEL_KEY_FILTER_START_DATE = "filterStartDate";
	private final String MODEL_KEY_FILTER_END_DATE = "filterEndDate";
	private final String MODEL_KEY_REQUIREMENTS_ATTRIBUTE = "requirements";
	private final String MODEL_KEY_EXPIRY_OFFSET_ATTRIBUTE = "expiryOffset";
	private final String MODEL_KEY_USER_PROP_HEADERS_ATTRIBUTE = "userPropHeaders";
	private final String MODEL_KEY_CRIT_HEADERS_ATTRIBUTE = "critHeaders";
	private final String MODEL_KEY_REPORT_LIST_ATTRIBUTE = "reportList";

    //UI Message keys
    private final String MESSAGE_ERROR_NOT_ADMIN = "error.not.admin";
    private final String MESSAGE_ERROR_NO_SELECTION = "error.no.selection";
    private final String MESSAGE_ERROR_BAD_ID = "error.bad.id";
    private final String MESSAGE_TEMPLATE_PROCESSING_ERROR = "form.error.templateProcessingError";
    private final String MESSAGE_FORM_PRINT_ERROR = "form.print.error";
    private final String MESSAGE_REPORT_TABLE_HEADER_DUEDATE = "report.table.header.duedate";
    private final String MESSAGE_REPORT_TABLE_HEADER_FCG = "report.table.header.fcg";
    private final String MESSAGE_REPORT_TABLE_HEADER_EXPIRE = "report.table.header.expire";
    private final String MESSAGE_NO = "report.table.no";
    private final String MESSAGE_YES = "report.table.yes";
    private final String MESSAGE_REPORT_EXPORT_FNAME = "report.export.fname";
    private final String MESSAGE_REPORT_EXPORT_ERROR = "report.export.error";
    private final String MESSAGE_REPORT_TABLE_HEADER_NAME = "report.table.header.name";
    private final String MESSAGE_REPORT_TABLE_HEADER_USER_ID = "report.table.header.userid";
    private final String MESSAGE_REPORT_TABLE_HEADER_ROLE = "report.table.header.role";
    private final String MESSAGE_REPORT_TABLE_HEADER_ISSUEDATE = "report.table.header.issuedate";
    private final String MESSAGE_REPORT_TABLE_HEADER_AWARDED = "report.table.header.awarded";
    private final String MESSAGE_EXPIRY_OFFSET_MONTH = "report.expiry.offset.month";
    private final String MESSAGE_EXPIRY_OFFSET_MONTHS = "report.expiry.offset.months";

    //HTTP Headers
    private final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    private final String HEADER_CACHE_CONTROL = "Cache-Control";
    private final String HEADER_PRAGMA = "Pragma";

    //Date Formats
    private final String PDF_FILE_NAME_DATE_FORMAT = "yyyy_MM_dd";
    private final String CSV_FILE_NAME_FORMAT = "yyyy-MM-dd";
    private final String FILTER_DATE_FORMAT = "MM-dd-yyyy";

    //Mime types
    private static final String PDF_MIME_TYPE = "application/pdf";
    private static final String CSV_MIME_TYPE = "text/csv";

    //Logging levels supported by logifNull()
    private final String LEVEL_WARN = "warn";

	private String getAbsoluteUrlForRedirect(String redirectTo)
	{
        String placementId = getToolManager().getCurrentPlacement().getId();
        String siteId = getToolManager().getCurrentPlacement().getContext();
        String portalurl = ServerConfigurationService.getPortalUrl();
        //SAKAI 10
        // String redirectPrefix = portalurl + "/tool/" + placementId;
        //This one is for SAKAI 11
        String redirectPrefix = portalurl + "/site/" + siteId + "/tool/" + placementId;
        String redirectString = REDIRECT + redirectPrefix + "/" + redirectTo;
        return redirectString;
	}

	@RequestMapping("/" + THIS_PAGE)
	public ModelAndView certListHandler(@RequestParam(value=PAGINATION_PAGE, required=false) String page,
			@RequestParam(value=PAGE_SIZE, required=false) Integer pageSize,
			@RequestParam(value=PAGE_NO, required=false) Integer pageNo, HttpServletRequest request) throws Exception
	{
		if(isAdministrator())
		{
			return certAdminListHandler(page, pageSize, pageNo, request);
		}
		else if (isAwardable())
		{
			return certParticipantListHandler(page, pageSize, pageNo, request);
		}
		else
		{
			return certUnauthorizedListHandler(page, pageSize, pageNo, request);
		}
	}

    public ModelAndView certAdminListHandler(String page, Integer pageSize, Integer pageNo, HttpServletRequest request) throws Exception
    {
        ModelAndView mav = new ModelAndView(ADMIN_VIEW);
        Map<String, Object> model = new HashMap<String, Object>();
        List<CertificateDefinition> certDefList = new ArrayList<CertificateDefinition>();
        HttpSession session = request.getSession();
        PagedListHolder certList = null;

        if(page == null)
        {
            String siteId = siteId();
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
            certList.setSort(new SortDefinition()
            {
                public String getProperty()
                {
                    return CERTIFICATE_NAME_PROPERTY;
                }

                public boolean isIgnoreCase()
                {
                    return true;
                }

                public boolean isAscending()
                {
                    return true;
                }
            });

            certList.resort();
        }
        else
        {
            certList = (PagedListHolder) session.getAttribute(SESSION_LIST_ATTRIBUTE);

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

        session.setAttribute(SESSION_LIST_ATTRIBUTE, certList);
        model.put(MODEL_KEY_CERTIFICATE_LIST, certList);
        model.put(MODEL_KEY_PAGE_SIZE_LIST, PAGE_SIZE_LIST);
        model.put(MODEL_KEY_PAGE_NO, certList.getPage());
        model.put(MODEL_KEY_PAGE_SIZE, pageSize);
        model.put(MODEL_KEY_FIRST_ELEMENT, (certList.getFirstElementOnPage() + 1));
        model.put(MODEL_KEY_LAST_ELEMENT, (certList.getLastElementOnPage() + 1));
        mav.addAllObjects(model);
        return mav;
    }

    public ModelAndView certParticipantListHandler(String page, Integer pageSize, Integer pageNo, HttpServletRequest request) throws Exception
    {
        final CertificateService cs = getCertificateService();
        ModelAndView mav = new ModelAndView(PARTICIPANT_VIEW);
        Map<String, Object> model = new HashMap<String, Object>();

        Set<CertificateDefinition> certDefs = null;

        Map<String, List<Map.Entry<String, String>>> certRequirementList = new HashMap<String, List<Map.Entry<String, String>>>();
        Map<String, Boolean> certificateIsAwarded = new HashMap<String, Boolean>();

        HttpSession session = request.getSession();
        PagedListHolder certList = null;

        // If this is the first time we're going to the page, or changing the paging size
        if(page==null)
        {
            certDefs = cs.getCertificateDefinitionsForSite(siteId(), new CertificateDefinitionStatus[]
            {
               CertificateDefinitionStatus.ACTIVE
            });

            List<String> certDefIds = new ArrayList<String>();

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

            for (CertificateDefinition cd : certDefs)
            {
                boolean awarded = false;
                if (isAwardable() && cd.isAwarded(userId()))
                {
                    awarded = true;
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
            certList.setSource(Arrays.asList(certDefs.toArray()));

            certList.setSort(new SortDefinition()
            {
                public String getProperty() {
                    return CERTIFICATE_NAME_PROPERTY;
                }

                public boolean isIgnoreCase() {
                    return true;
                }

                public boolean isAscending() {
                    return true;
                }
            });

            certList.resort();
        }
        else
        {
            //They're changing pages
            certList = (PagedListHolder) session.getAttribute(SESSION_LIST_ATTRIBUTE);
            certRequirementList = (Map) session.getAttribute(SESSION_REQUIREMENT_LIST_ATTRIBUTE);
            certificateIsAwarded = (Map) session.getAttribute(SESSION_IS_AWARDED_ATTRIBUTE);

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

        session.setAttribute (SESSION_LIST_ATTRIBUTE, certList);
        session.setAttribute (SESSION_REQUIREMENT_LIST_ATTRIBUTE, certRequirementList);
        session.setAttribute (SESSION_IS_AWARDED_ATTRIBUTE, certificateIsAwarded);
        model.put(MODEL_KEY_CERTIFICATE_LIST, certList);
        model.put(MODEL_KEY_REQUIREMENT_LIST_ATTRIBUTE, certRequirementList);
        model.put(MODEL_KEY_TOOL_URL, getToolUrl());
        model.put(MODEL_KEY_IS_AWARDED_ATTRIBUTE, certificateIsAwarded);
        model.put(MODEL_KEY_PAGE_SIZE_LIST, PAGE_SIZE_LIST);
        model.put(MODEL_KEY_PAGE_NO, certList.getPage());
        model.put(MODEL_KEY_FIRST_ELEMENT, (certList.getFirstElementOnPage() + 1));
        model.put(MODEL_KEY_LAST_ELEMENT, (certList.getLastElementOnPage() + 1));

        mav.addAllObjects(model);
        return mav;
    }

    public ModelAndView certUnauthorizedListHandler(String page, Integer pageSize, Integer pageNo, HttpServletRequest request) throws Exception
    {
        ModelAndView mav = new ModelAndView(UNAUTHORIZED_VIEW);
        return mav;
    }

    @RequestMapping("/delete.form")
    public ModelAndView deleteCertificateHandler(@RequestParam(PARAM_CERT_ID) String certId, HttpServletRequest request, HttpServletResponse response)
    {

        HashMap<String, String> model = new HashMap<String, String>();
        if (!isAdministrator())
        {
            model.put(ERROR_MESSAGE, MESSAGE_ERROR_NOT_ADMIN);
        }

        if (certId == null || certId.trim().length() == 0)
        {
            model.put(ERROR_MESSAGE, MESSAGE_ERROR_NO_SELECTION);
        }

        try
        {
            getCertificateService().deleteCertificateDefinition(certId);
        }
        catch (IdUnusedException e)
        {
            model.put(ERROR_MESSAGE, MESSAGE_ERROR_BAD_ID);
        }
        catch (DocumentTemplateException dte)
        {
            model.put(ERROR_MESSAGE, MESSAGE_TEMPLATE_PROCESSING_ERROR);
        }

        if (model.size () > 0)
        {
            return new ModelAndView (REDIRECT + THIS_PAGE, model);
        }

        return new ModelAndView (REDIRECT + THIS_PAGE);
    }

    @RequestMapping("/print.form")
    public ModelAndView printCertificateHandler(@RequestParam(PARAM_CERT_ID) String certId, HttpServletRequest request, HttpServletResponse response)
    {
        ModelAndView mav = null;
        OutputStream out = null;

        //true if there's trouble creating the certificate
        boolean creationError = false;
        CertificateService certService = getCertificateService();
        CertificateDefinition definition = null;

        try
        {
            definition = certService.getCertificateDefinition(certId);
        }
        catch (IdUnusedException iue)
        {
            try
            {
                mav = certParticipantListHandler(null, null, null, request);
                //this gets mav's actual model (not a clone)
                Map model = mav.getModel();
                //add the error to mav's model
                model.put(ERROR_MESSAGE, MESSAGE_ERROR_BAD_ID);
                return mav;
            }
            catch (Exception e)
            {
                //Guess there's nothing we can do
                logger.error(userId() + " has attempted to download certificate for non existant certificate: " + certId+ ", failed to provide feedback");
                return null;
            }
        }

        Date issueDate = definition.getIssueDate(userId());

        boolean awarded = false;
        try
        {
            awarded = definition.isAwarded(userId());
        }
        catch (Exception e)
        {
        }

        if (awarded && isAwardable())
        {
            DocumentTemplate template = definition.getDocumentTemplate();
            DocumentTemplateService dts = getDocumentTemplateService();

            try
            {
                //get an input stream for the PDF
                InputStream in = dts.render(template, definition, userId());

                //Creating the pdf was a success
                //proceed to create the http response

                //Make the filename
                StringBuilder fNameBuff = new StringBuilder();
                SimpleDateFormat sdf = new SimpleDateFormat(PDF_FILE_NAME_DATE_FORMAT);
                String  certName = definition.getName();
                String templName = template.getName();
                String extension = "";
                int dotIndex = -1;

                if (templName != null && (dotIndex = templName.lastIndexOf('.')) > -1)
                {
                    extension = templName.substring(dotIndex);
                }

                certName = certName.replaceAll("[^a-zA-Z0-9]+","-");

                String strIssueDate = "";
                if (issueDate != null)
                {
                    strIssueDate = sdf.format(issueDate);
                }

                fNameBuff.append (certName);
                fNameBuff.append('_').append(strIssueDate);
                fNameBuff.append(extension);

                //Configure the http headers
                response.setContentType(PDF_MIME_TYPE);
                response.addHeader(HEADER_CONTENT_DISPOSITION, "inline; filename = " + fNameBuff.toString());
                response.setHeader(HEADER_CACHE_CONTROL, "");
                response.setHeader(HEADER_PRAGMA, "");

                //put the pdf into the payload (2kb at a time)
                byte buff[] = new byte[2048];
                int numread = 0;
                out = response.getOutputStream();

                while ((numread = in.read(buff)) != -1)
                {
                    out.write(buff, 0, numread);
                }

                out.flush();
                out.close();
            }
            catch (TemplateReadException | VariableResolutionException | IOException e)
            {
                creationError = true;
            }
        }

        if (creationError)
        {
            try
            {
                mav = certParticipantListHandler(null, null, null, request);
                //this gets mav's actual model (not a clone)
                Map model = mav.getModel();
                //add these entries to mav's model
                model.put(ERROR_MESSAGE, MESSAGE_FORM_PRINT_ERROR);
                model.put(MODEL_KEY_ERROR_ARGUMENTS_ATTRIBUTE, MAIL_SUPPORT);
            }
            catch (Exception e)
            {
                //An exception while handling previous errors
                //Guess there's nothing we can do
                logger.error("Couldn't create the pdf for " + userId() + ", certId is " + certId + ", failed to provide feedback");
                return null;
            }
        }

        return mav;
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
    public ModelAndView certAdminReportHandler(@RequestParam(PARAM_CERT_ID) String certId, @RequestParam(value=PAGINATION_PAGE, required=false) String page,
            @RequestParam(value=PAGE_SIZE, required=false) Integer pageSize,
            @RequestParam(value=PAGE_NO, required=false) Integer pageNo,
            @RequestParam(value=PARAM_EXPORT, required=false) Boolean export,
            HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        if (!isAdministrator())
        {
            //only people who have permission to add/edit certificates can see this report
            return null;
        }

        //The model that will be sent to the UI
        Map<String, Object> model = new HashMap<String, Object>();

        //Any errors that need to be sent to the UI
        List<String> errors = new ArrayList<String>();

        //Will be used to 'cache' some data to speed up the paging navigator
        HttpSession session = request.getSession();

        /*The Report table's headers for columns that are related to the certificate definition's criteria
         * (other headers are already handled in jsp)*/
        List<Object> criteriaHeaders = new ArrayList<Object>();

        //holds the contents of the table, the page number, the page size, etc.
        PagedListHolder reportList = null;

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

            if ( !siteId().equals(definition.getSiteId()) )
            {
                logger.warn(userId() + " is trying to access a certificate outside of their site");
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

        model.put(MODEL_KEY_CERTIFICATE, definition);
        model.put(MODEL_KEY_TOOL_URL, getToolUrl());

        //Prepare the default filter start and end dates
        //start date is specified by sakai.properties
        Calendar filterStartDate = Calendar.getInstance();
        filterStartDate.add(Calendar.DATE, -1 * DEFAULT_FILTER_DAYS);
        SimpleDateFormat sdf = new SimpleDateFormat(FILTER_DATE_FORMAT);
        String strFilterStartDate = sdf.format(filterStartDate.getTime());
        model.put(MODEL_KEY_FILTER_START_DATE, strFilterStartDate);
        //end date is always the current date
        Calendar filterEndDate = Calendar.getInstance();
        String strFilterEndDate = sdf.format(filterEndDate.getTime());
        model.put(MODEL_KEY_FILTER_END_DATE, strFilterEndDate);

        //for internationalization - loads Messages.properties
        ResourceLoader messages = getMessages();

        //we'll need this to get additional user properties
        ExtraUserPropertyUtility extraPropsUtil = ExtraUserPropertyUtility.getInstance();
        //determines if the current user has permission to view extra properties
        boolean canShowUserProps = extraPropsUtil.isExtraUserPropertiesEnabled() && extraPropsUtil.isExtraPropertyViewingAllowedForCurrentUser();
        List<String> propHeaders = new ArrayList<String>();
        List<String> requirements = new ArrayList<String>();
        Integer expiryOffset = null;

        if(page == null && export == null && pageSize == null)
        {
            //It's their first time hitting the page or they changed the page size
            // -we'll load/refresh all the data
            model.put(MODEL_KEY_USE_DEFAULT_DISPLAY_OPTIONS, new Boolean(true));

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

                if (crit instanceof WillExpireCriterionHibernateImpl)
                {
                    /* special case because expiration offset is used on the UI
                     * and this is always the first column after the Issue Date
                     * */
                    WillExpireCriterionHibernateImpl wechi = (WillExpireCriterionHibernateImpl) crit;
                    //expiration comes first (immediately after issue date)
                    criteriaHeaders.addAll(0, crit.getReportHeaders());
                    String strExpiryOffset = wechi.getExpiryOffset();
                    if (logIfNull(strExpiryOffset, "no expiry offset found for criterion: "+ wechi.getId()))
                    {
                        return null;
                    }
                    expiryOffset = new Integer(strExpiryOffset);
                }
                else
                {
                    criteriaHeaders.addAll(crit.getReportHeaders());
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

            session.setAttribute(SESSION_ORDERED_CRITERIA, orderedCriteria);

            //Prepare the Report table's contents
            List<ReportRow> reportRows = new ArrayList<ReportRow>();

            /* Iterate through the list of users who have the ability to be awarded certificates,
             * populate each row of the table accordingly*/
            List<String> userIds = getAwardableUserIds();
            reportRows = getReportRows(definition, "all", null, null, null, userIds, session);

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
                public String getProperty()
                {
                    //sort by the getName() method
                    return CERTIFICATE_NAME_PROPERTY;
                }

                public boolean isIgnoreCase()
                {
                    return true;
                }

                public boolean isAscending()
                {
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
            requirements = (List<String>) session.getAttribute(SESSION_REQUIREMENTS_ATTRIBUTE);
            expiryOffset = (Integer) session.getAttribute(SESSION_EXPIRY_OFFSET_ATTRIBUTE);
            propHeaders = (List<String>) session.getAttribute(SESSION_REPORT_PROP_HEADERS_ATTRIBUTE);
            criteriaHeaders = (List<Object>) session.getAttribute(SESSION_REPORT_CRIT_HEADERS_ATTRIBUTE);
            reportList = (PagedListHolder) session.getAttribute(SESSION_REPORT_LIST_ATTRIBUTE);

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
            else if (pageSize != null)
            {
                reportList.setPageSize(pageSize);
            }
        }   // export == null
        else if (export)
        {
            // they clicked Export as CSV
            //get the headers and the report list from the http session
            requirements = (List<String>) session.getAttribute(SESSION_REQUIREMENTS_ATTRIBUTE);
            expiryOffset = (Integer) session.getAttribute(SESSION_EXPIRY_OFFSET_ATTRIBUTE);
            propHeaders = (List<String>) session.getAttribute(SESSION_REPORT_PROP_HEADERS_ATTRIBUTE);
            criteriaHeaders = (List<Object>) session.getAttribute(SESSION_REPORT_CRIT_HEADERS_ATTRIBUTE);
            reportList = (PagedListHolder) session.getAttribute(SESSION_REPORT_LIST_ATTRIBUTE);

            try
            {
                definition = certService.getCertificateDefinition(certId);

                //prepare the file name for the http response header
                DateFormat filenameDateFormat = new SimpleDateFormat(CSV_FILE_NAME_FORMAT);
                String today = filenameDateFormat.format(new Date());
                String report = messages.getString(MESSAGE_REPORT_EXPORT_FNAME);
                String defName = definition.getName();
                if (logIfNull(defName,"certificate name is null: "+ certId))
                {
                    errors.add(getReportExportErrorMessage());
                    return reportViewError(model, errors, requirements, propHeaders, criteriaHeaders, reportList);
                }
                defName = defName.replaceAll("[^a-zA-Z0-9]+","-");

                //fill in the csv's header
                StringBuilder contents = new StringBuilder();
                appendItem(contents, messages.getString(MESSAGE_REPORT_TABLE_HEADER_NAME), false);
                appendItem(contents, messages.getString(MESSAGE_REPORT_TABLE_HEADER_USER_ID), false);
                appendItem(contents, messages.getString(MESSAGE_REPORT_TABLE_HEADER_ROLE), false);
                if (canShowUserProps)
                {
                    if (logIfNull(propHeaders, "propHeaders is null"))
                    {
                        errors.add(getReportExportErrorMessage());
                        return reportViewError(model, errors, requirements, propHeaders, criteriaHeaders, reportList);
                    }
                    Iterator<String> itPropHeaders = propHeaders.iterator();
                    while (itPropHeaders.hasNext())
                    {
                        appendItem(contents, itPropHeaders.next(), false);
                    }
                }
                appendItem(contents, messages.getString(MESSAGE_REPORT_TABLE_HEADER_ISSUEDATE), false);

                Iterator<Object> itHeaders = criteriaHeaders.iterator();
                while (itHeaders.hasNext())
                {
                    appendItem(contents, (String) itHeaders.next(), false);
                }

                appendItem(contents, messages.getString(MESSAGE_REPORT_TABLE_HEADER_AWARDED), true);

                // gets the original list of ReportRows
                List<ReportRow> table;
                try
                {
                    table = (List<ReportRow>) reportList.getSource();
                }
                catch( Exception ex )
                {
                    logger.error( "Couldn't cast reportList for the reportView. certId: " + certId);
                    errors.add(getReportExportErrorMessage());
                    return reportViewError(model, errors, requirements, propHeaders, criteriaHeaders, reportList);
                }

                //fill the rest of the csv
                Iterator<ReportRow> itTable = table.iterator();
                while (itTable.hasNext())
                {
                    //represents a line in the table
                    ReportRow row = itTable.next();
                    appendItem(contents, row.getName(), false);
                    appendItem(contents, row.getUserId(), false);
                    appendItem(contents, row.getRole(), false);
                    if (canShowUserProps)
                    {
                        List<String> extraProps = row.getExtraProps();
                        if (logIfNull(extraProps, "Extra props is null for certId: " + certId))
                        {
                            errors.add(getReportExportErrorMessage());
                            return reportViewError(model, errors, requirements, propHeaders, criteriaHeaders, reportList);
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

                //Everything went well, set up the http headers to send the file
                response.setContentType(CSV_MIME_TYPE);
                response.addHeader(HEADER_CONTENT_DISPOSITION, "attachment; filename = " + defName + "_" + report + "_" + today +".csv");
                response.setHeader(HEADER_CACHE_CONTROL, "");
                response.setHeader(HEADER_PRAGMA, "");

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
                logger.error("unused certificate id passed to report's csv export: "+ certId);
                errors.add(getReportExportErrorMessage());
                return reportViewError(model, errors, requirements, propHeaders, criteriaHeaders, reportList);
            }
        }
        else
        {
            //should never happen
            logger.warn("hit reportView.form with export=false. Should never happen");
            return null;
        }

        //handle plurals when appropriate
        String strExpiryOffset = null;
        if (expiryOffset != null && expiryOffset == 1)
        {
            strExpiryOffset = "1 " + messages.getString(MESSAGE_EXPIRY_OFFSET_MONTH);
        }
        else if (expiryOffset != null)
        {
            strExpiryOffset = expiryOffset + " " + messages.getString(MESSAGE_EXPIRY_OFFSET_MONTHS);
        }

        //push the navigator and the headers to the http session
        session.setAttribute(SESSION_REQUIREMENTS_ATTRIBUTE, requirements);
        session.setAttribute(SESSION_EXPIRY_OFFSET_ATTRIBUTE, expiryOffset);
        session.setAttribute(SESSION_REPORT_PROP_HEADERS_ATTRIBUTE, propHeaders);
        session.setAttribute(SESSION_REPORT_CRIT_HEADERS_ATTRIBUTE, criteriaHeaders);
        session.setAttribute(SESSION_REPORT_LIST_ATTRIBUTE, reportList);

        //populate the model as necessary
        model.put(MODEL_KEY_ERRORS_ATTRIBUTE, errors);
        model.put(MODEL_KEY_REQUIREMENTS_ATTRIBUTE, requirements);
        model.put(MODEL_KEY_EXPIRY_OFFSET_ATTRIBUTE, strExpiryOffset);
        model.put(MODEL_KEY_USER_PROP_HEADERS_ATTRIBUTE, propHeaders);
        model.put(MODEL_KEY_CRIT_HEADERS_ATTRIBUTE,criteriaHeaders);
        model.put(MODEL_KEY_REPORT_LIST_ATTRIBUTE, reportList);
        model.put(MODEL_KEY_PAGE_SIZE_LIST, PAGE_SIZE_LIST);
        model.put(MODEL_KEY_PAGE_NO, reportList.getPage());
        model.put(MODEL_KEY_PAGE_SIZE, reportList.getPageSize());
        model.put(MODEL_KEY_FIRST_ELEMENT, (reportList.getFirstElementOnPage() + 1));
        model.put(MODEL_KEY_LAST_ELEMENT, (reportList.getLastElementOnPage() + 1));

        //send the model to the jsp
        ModelAndView mav = new ModelAndView(REPORT_VIEW, model);
        return mav;
    }

    private String getReportExportErrorMessage()
    {
        return getMessages().getFormattedMessage(MESSAGE_REPORT_EXPORT_ERROR, new Object[]{MAIL_SUPPORT});
    }

    /**
     * If an error occurs that prevents us from generating the report view,
     * this will give us a return value such that the user will see the relevant error
     *
     * @param model
     * @param errors
     * @param requirements
     * @param propHeaders
     * @param criteriaHeaders
     * @param reportList
     * @return
     */
    private ModelAndView reportViewError(Map<String, Object> model, List<String> errors, List<String> requirements, List<String> propHeaders, List<Object> criteriaHeaders, PagedListHolder reportList)
    {
        //Include what we can, but ultimately ensure that we can display the errors to the user
        if (model.get(MODEL_KEY_ERRORS_ATTRIBUTE) == null)
        {
            model.put(MODEL_KEY_ERRORS_ATTRIBUTE, errors);
        }
        if (model.get(MODEL_KEY_REQUIREMENTS_ATTRIBUTE) == null)
        {
            model.put(MODEL_KEY_REQUIREMENTS_ATTRIBUTE, requirements);
        }
        if (model.get(MODEL_KEY_USER_PROP_HEADERS_ATTRIBUTE) == null)
        {
            model.put(MODEL_KEY_USER_PROP_HEADERS_ATTRIBUTE, propHeaders);
        }
        if (model.get(MODEL_KEY_CRIT_HEADERS_ATTRIBUTE) == null)
        {
            model.put(MODEL_KEY_CRIT_HEADERS_ATTRIBUTE, criteriaHeaders);
        }

        PagedListHolder plh = (PagedListHolder) model.get(MODEL_KEY_REPORT_LIST_ATTRIBUTE);
        if (plh == null)
        {
            if (reportList == null)
            {
                reportList = new PagedListHolder(new ArrayList<String>());
            }
            plh = reportList;
            model.put(MODEL_KEY_REPORT_LIST_ATTRIBUTE, reportList);
        }

        if (model.get(MODEL_KEY_PAGE_SIZE_LIST) == null)
        {
            model.put(MODEL_KEY_PAGE_SIZE_LIST, PAGE_SIZE_LIST);
        }
        if (model.get(MODEL_KEY_PAGE_NO) == null)
        {
            model.put(MODEL_KEY_PAGE_NO, plh.getPage());
        }
        if (model.get(MODEL_KEY_PAGE_SIZE) == null)
        {
            model.put(MODEL_KEY_PAGE_SIZE, plh.getPageSize());
        }
        if (model.get(MODEL_KEY_FIRST_ELEMENT) == null)
        {
            model.put(MODEL_KEY_FIRST_ELEMENT, plh.getFirstElementOnPage() + 1);
        }
        if (model.get(MODEL_KEY_LAST_ELEMENT) == null)
        {
            model.put(MODEL_KEY_LAST_ELEMENT, plh.getLastElementOnPage() + 1);
        }
        return new ModelAndView(REPORT_VIEW, model);
    }

    @RequestMapping("/reportViewFilter.form")
    public ModelAndView certAdminReportFilterHandler(@RequestParam(PARAM_CERT_ID) String certId,
        @RequestParam(PARAM_DISPLAY_FILTER_TYPE) String filterType,
        @RequestParam(PARAM_DISPLAY_FILTER_DATE_TYPE) String filterDateType,
        @RequestParam(PARAM_DISPLAY_FILTER_START_DATE) String filterStartDate,
        @RequestParam(PARAM_DISPLAY_FILTER_END_DATE) String filterEndDate,
        @RequestParam(PARAM_DISPLAY_FILTER_HISTORICAL) Boolean includeHistorical,
        HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        CertificateService certServ = getCertificateService();
        CertificateDefinition definition = null;

        try
        {
            definition = certServ.getCertificateDefinition(certId);
        }
        catch (IdUnusedException iue)
        {
            //TODO
            return null;
        }

        Map<String, Object> model = new HashMap<String, Object>();
        HttpSession session = request.getSession();

        //use a set to avoid duplicates
        Set<String> setUserIds = new HashSet<String>();
        setUserIds.addAll(getAwardableUserIds());
        if (includeHistorical)
        {
            setUserIds.addAll(getHistoricalGradedUserIds());
        }

        List<String> userIds = new ArrayList<String>();
        userIds.addAll(setUserIds);

        SimpleDateFormat sdf = new SimpleDateFormat(FILTER_DATE_FORMAT);
        Date startDate = null;
        Date endDate = null;
        try
        {
            startDate = sdf.parse(filterStartDate);
        }
        catch (ParseException e)
        {
            //leave the value as null - getReportRows will show everything up to the end date
        }
        try
        {
            endDate = sdf.parse(filterEndDate);
        }
        catch (ParseException e)
        {
            //leave the value as null - getReportRows will show everything after the start date
            //if they're both null it will display everything
        }

        if (endDate != null)
        {
            if (startDate != null)
            {
                //order them correctly
                if (endDate.before(startDate))
                {
                    //swap
                    Date temp = endDate;
                    endDate = startDate;
                    startDate = temp;
                }
            }

            //Add a day to the end date to make it inclusive
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(endDate);
            calEnd.add(Calendar.DATE, 1);
            endDate = calEnd.getTime();
        }

        List<ReportRow> reportRows = getReportRows(definition, filterType, filterDateType, startDate, endDate, userIds, session);

        //set up the paging navigator
        //the 'if' surrounding this scope: page == null && export == null
        //this happens when freshly arriving on this page or when changing the page size
        PagedListHolder reportList = new PagedListHolder(reportRows);

        //fresh arival, set the default page size
        //set default to 100
        int pageSize = PAGE_SIZE_LIST.get(3);
        reportList.setPageSize(pageSize);
        reportList.setSort(new SortDefinition()
        {
            public String getProperty()
            {
                //sort by the getName() method
                return CERTIFICATE_NAME_PROPERTY;
            }

            public boolean isIgnoreCase()
            {
                return true;
            }

            public boolean isAscending()
            {
                return true;
            }
        });

        reportList.resort();
        session.setAttribute(SESSION_REPORT_LIST_ATTRIBUTE, reportList);
        model.put(MODEL_KEY_REPORT_LIST_ATTRIBUTE, reportList);

        List<String> requirements = (List<String>) session.getAttribute(SESSION_REQUIREMENTS_ATTRIBUTE);
        Integer expiryOffset = (Integer) session.getAttribute(SESSION_EXPIRY_OFFSET_ATTRIBUTE);
        List<String> propHeaders = (List<String>) session.getAttribute(SESSION_REPORT_PROP_HEADERS_ATTRIBUTE);
        List<Object> criteriaHeaders = (List<Object>) session.getAttribute(SESSION_REPORT_CRIT_HEADERS_ATTRIBUTE);

        //handle plurals when appropriate
        String strExpiryOffset = null;
        if (expiryOffset != null && expiryOffset == 1)
        {
            strExpiryOffset = "1 " + messages.getString(MESSAGE_EXPIRY_OFFSET_MONTH);
        }
        else if (expiryOffset != null)
        {
            strExpiryOffset = expiryOffset + " " + messages.getString(MESSAGE_EXPIRY_OFFSET_MONTHS);
        }

        model.put(MODEL_KEY_CERTIFICATE, definition);
        model.put(MODEL_KEY_TOOL_URL, getToolUrl());
        model.put(MODEL_KEY_REQUIREMENTS_ATTRIBUTE, requirements);
        model.put(MODEL_KEY_EXPIRY_OFFSET_ATTRIBUTE, strExpiryOffset);
        model.put(MODEL_KEY_USER_PROP_HEADERS_ATTRIBUTE, propHeaders);
        model.put(MODEL_KEY_CRIT_HEADERS_ATTRIBUTE, criteriaHeaders);

        model.put(MODEL_KEY_PAGE_SIZE_LIST, PAGE_SIZE_LIST);
        model.put(MODEL_KEY_PAGE_NO, reportList.getPage());
        model.put(MODEL_KEY_PAGE_SIZE, reportList.getPageSize());
        model.put(MODEL_KEY_FIRST_ELEMENT, (reportList.getFirstElementOnPage()+1));
        model.put(MODEL_KEY_LAST_ELEMENT, (reportList.getLastElementOnPage()+1));

        ModelAndView mav = new ModelAndView(REPORT_VIEW, model);
        return mav;
    }

    /**
     * Generates rows for the reportView
     * @param definition the certificate we are reporting on
     * @param filterType possible values - all, unawarded, awarded.
     * all - includes awarded and unawarded users
     * unawarded - removes all awarded users from the report
     * awarded - removes all unawarded users from the report, and (optionally?) filters to a specified date range on the issue date or the expiry date
     * @param filterDateType when filterType is 'awarded', this determines which date type to filter on. Possible values are "issue date" or "expiry date"
     * @param startDate when filterType is 'awarded', all dates of the filterDateType before this date will be excluded
     * @param endDate when filterType is 'awarded', all dates of the filterDateType after this date will be excluded
     * @param userIds the users to display in the report
     * @param session the session which stores data we'll need (ie. orderedCriteria - the order of the criterion columns)
     * @return a list of ReportRows representing the rows in the report
     */
    public List<ReportRow> getReportRows(CertificateDefinition definition, String filterType, String filterDateType, Date startDate, Date endDate,
            List<String> userIds, HttpSession session)
    {
        //Verify they're not hacking to view results from another site
        if ( !siteId().equals(definition.getSiteId()) )
        {
            logger.warn(userId() + " is trying to access a certificate outside of their site");
            //TODO
            return null;
        }

        boolean showUnawarded = false;
        if ("all".equals(filterType))
        {
            showUnawarded = true;
        }
        else if ("unawarded".equals(filterType))
        {
            showUnawarded = true;
        }

        List<ReportRow> reportRows = new ArrayList<ReportRow>();
        ResourceLoader messages = getMessages();

        //we'll need this to get additional user properties
        ExtraUserPropertyUtility extraPropsUtil = ExtraUserPropertyUtility.getInstance();
        //determines if the current user has permission to view extra properties
        boolean canShowUserProps = extraPropsUtil.isExtraUserPropertiesEnabled() && extraPropsUtil.isExtraPropertyViewingAllowedForCurrentUser();

        //Get the headers for the additional user properties
        //keeps track of the order of the keys so that we know that the headers and the cells line up
        Map<String, String> propKeysTitles = extraPropsUtil.getExtraUserPropertiesKeyAndTitleMap();
        List<String> propKeys = new ArrayList<String>(propKeysTitles.keySet());

        //Get the criteria in the order of the displayed columns
        List<Criterion> orderedCriteria = (List<Criterion>) session.getAttribute(SESSION_ORDERED_CRITERIA);
        WillExpireCriterionHibernateImpl wechi = null;
        Iterator<Criterion> itOrderedCriteria = orderedCriteria.iterator();
        while (itOrderedCriteria.hasNext())
        {
            Criterion crit = itOrderedCriteria.next();
            if (crit instanceof WillExpireCriterionHibernateImpl)
            {
                wechi = (WillExpireCriterionHibernateImpl) crit;
                break;
            }
        }

        Iterator<String> itUser = userIds.iterator();
        while (itUser.hasNext())
        {
            String userId = itUser.next();
            Date issueDate = definition.getIssueDate(userId);

            //Determine whether this row should be included in the report
            boolean includeRow = true;
            if (issueDate == null)
            {
                //they are unawarded
                if (!showUnawarded)
                {
                    includeRow = false;
                }
            }
            else
            {
                //they are awarded
                if ("unawarded".equals(filterType))
                {
                    includeRow = false;
                }
                else if ("awarded".equals(filterType))
                {
                    if ("issueDate".equals(filterDateType))
                    {
                        if (startDate != null && issueDate.before(startDate))
                        {
                            includeRow = false;
                        }
                        if (endDate != null && issueDate.after(endDate))
                        {
                            includeRow = false;
                        }
                    }
                    else if ("expiryDate".equals(filterDateType) && wechi != null)
                    {
                        Date expiryDate = wechi.getExpiryDate(issueDate);
                        if (startDate != null && expiryDate.before(startDate))
                        {
                            includeRow = false;
                        }
                        if (endDate != null && expiryDate.after(endDate))
                        {
                            includeRow = false;
                        }
                    }
                }
            }

            if ( includeRow )
            {
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
                    currentRow.setRole(getRole(userId));

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
                        if (logIfNull(crit, "null criterion in orderedCriteria for certId: " + definition.getId()))
                        {
                            return null;
                        }

                        criterionCells.addAll(crit.getReportData(userId, siteId(), issueDate));
                    }

                    currentRow.setCriterionCells(criterionCells);

                    //show whether the certificate was awarded
                    boolean awarded = false;
                    try
                    {
                        awarded = definition.isAwarded(userId);
                    }
                    catch (Exception e)
                    {
                    }

                    if (awarded)
                    {
                        String yes = messages.getString(MESSAGE_YES);
                        currentRow.setAwarded(yes);
                    }
                    else
                    {
                        String no = messages.getString(MESSAGE_NO);
                        currentRow.setAwarded(no);
                    }

                    reportRows.add(currentRow);
                }
                catch (UserNotDefinedException e)
                {
                    //user's not in the system anymore. Ignore
                }
            }
        }

        return reportRows;
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
        if (obj == null)
        {
            if (level == null)
            {
                logger.error(message);
            }
            else if (LEVEL_WARN.equals(level))
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

    /**
     * A row in the report table. Represents a user who is awardable
      */
    public class ReportRow
    {
        private String name = "";
        private String userId = "";
        private String role = "";
        private List<String> extraProps = new ArrayList<String>();
        private String issueDate = "";
        private List<String> criterionCells = new ArrayList<String>();
        private String awarded = "";

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

        public void setRole(String role)
        {
            this.role = role;
        }

        public String getRole()
        {
            return role;
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
}
