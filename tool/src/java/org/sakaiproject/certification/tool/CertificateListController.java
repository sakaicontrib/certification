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

package org.sakaiproject.certification.tool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.support.PagedListHolder;
import org.springframework.beans.support.SortDefinition;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import org.sakaiproject.certification.api.CertificateDefinition;
import org.sakaiproject.certification.api.CertificateDefinitionStatus;
import org.sakaiproject.certification.api.DocumentTemplate;
import org.sakaiproject.certification.api.DocumentTemplateException;
import org.sakaiproject.certification.api.ReportRow;
import org.sakaiproject.certification.api.TemplateReadException;
import org.sakaiproject.certification.api.VariableResolutionException;
import org.sakaiproject.certification.api.criteria.Criterion;
import org.sakaiproject.certification.api.criteria.CriterionProgress;
import org.sakaiproject.certification.api.criteria.gradebook.WillExpireCriterion;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.util.api.FormattedText;

@Slf4j
@Controller
public class CertificateListController extends BaseCertificateController {

    public static final String THIS_PAGE = "list.form";

    //Pagination request params
    public static final String PAGINATION_NEXT = "next";
    public static final String PAGINATION_LAST = "last";
    public static final String PAGINATION_PREV = "previous";
    public static final String PAGINATION_FIRST = "first";
    public static final String PAGINATION_PAGE = "page";
    public static final String PAGE_SIZE = "pageSize";
    public static final String PAGE_NO = "pageNo";
    public static final List<Integer> PAGE_SIZE_LIST = Arrays.asList(100,200,400,800,1600,Integer.MAX_VALUE);

    //Other request params
    public static final String PARAM_CERT_ID = "certId";
    public static final String PARAM_EXPORT = "export";
    public static final String PARAM_SORT = "sort";
    public static final String PARAM_PROP = "prop";
    public static final String PARAM_CRITERION = "criterion";

    //Request params to filter the report view
    public static final String PARAM_DISPLAY_FILTER_TYPE = "filterType";
    public static final String PARAM_DISPLAY_FILTER_DATE_TYPE = "filterDateType";
    public static final String PARAM_DISPLAY_FILTER_START_DATE = "filterStartDate";
    public static final String PARAM_DISPLAY_FILTER_END_DATE = "filterEndDate";
    public static final String PARAM_DISPLAY_FILTER_HISTORICAL = "filterHistorical";

    //sakai.properties
    private final String MAIL_SUPPORT_SAKAI_PROPERTY =  "mail.support";
    private final String MAIL_SUPPORT = ServerConfigurationService.getString(MAIL_SUPPORT_SAKAI_PROPERTY);

    private FormattedText formattedText;
    private String csvSeparator = ",";
    private String decimalSeparator = ".";

    private static final int DEFAULT_FILTER_DAYS;
    static {
        String strDefaultFilterDays = ServerConfigurationService.getString("certification.reportFilter.defaultFilterDays");
        int intDefaultFilterDays;
        try {
            intDefaultFilterDays = Integer.parseInt(strDefaultFilterDays);
        } catch (Exception e) {
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
    private final String SESSION_REPORT_CRIT_HEADERS_ATTRIBUTE = "reportCritHeaders";
    private final String SESSION_REPORT_LIST_ATTRIBUTE = "reportList";
    private final String SESSION_REQUIREMENT_LIST_ATTRIBUTE = "certRequirementList";
    private final String SESSION_IS_AWARDED_ATTRIBUTE = "certIsAwarded";
    private final String SESSION_ORDERED_CRITERIA = "orderedCriteria";
    private final String SESSION_SORT_REPORT_ASC = "sortReportAsc";
    private final String SESSION_SORT_REPORT_KEY = "sortReportKey";
    private final String SESSION_CAN_USER_VIEW_STUDENT_NUMS = "canUserViewStudentNums";

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
    private final String MODEL_KEY_CRIT_HEADERS_ATTRIBUTE = "critHeaders";
    private final String MODEL_KEY_REPORT_LIST_ATTRIBUTE = "reportList";
    private final String MODEL_KEY_HIGH_MEMBERS = "highMembers";
    private final String MODEL_KEY_CAN_USER_VIEW_STUDENT_NUMS = "canUserViewStudentNums";

    //UI Message keys
    private final String MESSAGE_ERROR_NOT_ADMIN = "error.not.admin";
    private final String MESSAGE_ERROR_NO_SELECTION = "error.no.selection";
    private final String MESSAGE_ERROR_BAD_ID = "error.bad.id";
    private final String MESSAGE_GRADE_NOT_NUMERIC = "form.error.grade.notNumeric";
    private final String MESSAGE_TEMPLATE_PROCESSING_ERROR = "form.error.templateProcessingError";
    private final String MESSAGE_FORM_PRINT_ERROR = "form.print.error";
    private final String MESSAGE_REPORT_EXPORT_FNAME = "report.export.fname";
    private final String MESSAGE_REPORT_EXPORT_ERROR = "report.export.error";
    private final String MESSAGE_REPORT_TABLE_HEADER_NAME = "report.table.header.name";
    private final String MESSAGE_REPORT_TABLE_HEADER_USER_ID = "report.table.header.userid";
    private final String MESSAGE_REPORT_TABLE_HEADER_STUDENT_NUM = "report.table.header.studentNum";
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
    private final String FILTER_DATE_FORMAT = "yyyy-MM-dd";

    //Mime types
    private static final String PDF_MIME_TYPE = "application/pdf";
    private static final String CSV_MIME_TYPE = "text/csv";

    //Logging levels supported by logifNull()
    private final String LEVEL_WARN = "warn";

    @RequestMapping("/" + THIS_PAGE)
    public ModelAndView certListHandler(@RequestParam(value=PAGINATION_PAGE, required=false) String page,
                                        @RequestParam(value=PAGE_SIZE, required=false) Integer pageSize,
                                        @RequestParam(value=PAGE_NO, required=false) Integer pageNo, HttpServletRequest request) throws Exception {
        if(isAdministrator()) {
            return certAdminListHandler(page, pageSize, pageNo, request);

        } else if (isAwardable()) {
            return certParticipantListHandler(page, pageSize, pageNo, request);

        } else {
            return certUnauthorizedListHandler(page, pageSize, pageNo, request);
        }
    }

    public ModelAndView certAdminListHandler(String page, Integer pageSize, Integer pageNo, HttpServletRequest request) throws Exception {
        ModelAndView mav = new ModelAndView(ADMIN_VIEW);
        Map<String, Object> model = new HashMap<>();
        List<CertificateDefinition> certDefList = new ArrayList<>();
        HttpSession session = request.getSession();
        PagedListHolder certList;

        if(page == null) {
            String siteId = siteId();
            certDefList.addAll(certificateService.getCertificateDefinitionsForSite(siteId));

            certList = new PagedListHolder(certDefList);
            if(pageSize != null) {
                certList.setPageSize(pageSize);
            } else {
                pageSize = PAGE_SIZE_LIST.get(3);
                certList.setPageSize(pageSize);
            }
            if(pageNo != null) {
                certList.setPage(pageNo);
            }
            certList.setSort(new SortDefinition() {
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

        } else {
            certList = (PagedListHolder) session.getAttribute(SESSION_LIST_ATTRIBUTE);

            if (PAGINATION_NEXT.equals(page)  && !certList.isLastPage()) {
                certList.nextPage();

            } else if (PAGINATION_LAST.equals(page)) {
                certList.setPage(certList.getLastLinkedPage());

            } else if (PAGINATION_PREV.equals(page) && !certList.isFirstPage()) {
                certList.previousPage();

            } else if (PAGINATION_FIRST.equals(page)) {
                certList.setPage(certList.getFirstLinkedPage());
            }
        }

        int numMembers = 0;
        final int HIGH_NUMBER_OF_MEMBERS = 500;
        Site currentSite = getCurrentSite();
        if (currentSite != null) {
            Set<String> users = currentSite.getUsers();
            if (users != null) {
                numMembers = users.size();
            }
        }

        session.setAttribute(SESSION_LIST_ATTRIBUTE, certList);
        if (numMembers > HIGH_NUMBER_OF_MEMBERS) {
            model.put(MODEL_KEY_HIGH_MEMBERS, Boolean.TRUE);
        } else {
            model.put(MODEL_KEY_HIGH_MEMBERS, Boolean.FALSE);
        }

        model.put(MODEL_KEY_CERTIFICATE_LIST, certList);
        model.put(MODEL_KEY_PAGE_SIZE_LIST, PAGE_SIZE_LIST);
        model.put(MODEL_KEY_PAGE_NO, certList.getPage());
        model.put(MODEL_KEY_PAGE_SIZE, pageSize);
        model.put(MODEL_KEY_FIRST_ELEMENT, (certList.getFirstElementOnPage() + 1));
        model.put(MODEL_KEY_LAST_ELEMENT, (certList.getLastElementOnPage() + 1));
        model.put("view", "list");
        mav.addAllObjects(model);
        return mav;
    }

    public ModelAndView certParticipantListHandler(String page, Integer pageSize, Integer pageNo, HttpServletRequest request) throws Exception {
        ModelAndView mav = new ModelAndView(PARTICIPANT_VIEW);
        Map<String, Object> model = new HashMap<>();
        model.put("view", "list");

        Set<CertificateDefinition> certDefs;

        Map<String, List<Map.Entry<String, String>>> certRequirementList = new HashMap<>();
        Map<String, Boolean> certificateIsAwarded = new HashMap<>();

        HttpSession session = request.getSession();
        PagedListHolder certList;

        // If this is the first time we're going to the page, or changing the paging size
        if(page == null) {
            certDefs = certificateService.getCertificateDefinitionsForSite(siteId(), new CertificateDefinitionStatus[] {
               CertificateDefinitionStatus.ACTIVE
            });

            for(CertificateDefinition cfl : certDefs) {
                List<Map.Entry<String, String>> requirementList = new ArrayList<>();
                try {
                    requirementList = certificateService.getCertificateRequirementsForUser(cfl.getId(), userId(), siteId(), false);
                } catch (IdUnusedException e) {
                    log.warn("While getting certificate requirements, found unused certificate id: {}", cfl.getId());
                }
                certRequirementList.put (cfl.getId(), requirementList);
            }

            for (CertificateDefinition cd : certDefs) {
                boolean awarded = false;
                if (isAwardable() && cd.isAwarded(userId(), false)) {
                    awarded = true;
                }

                certificateIsAwarded.put(cd.getId(), awarded);
            }

            certList = new PagedListHolder();
            if(pageSize != null) {
                certList.setPageSize(pageSize);
            } else {
                pageSize = PAGE_SIZE_LIST.get(3);
                certList.setPageSize(pageSize);
            }

            if(pageNo != null) {
                certList.setPage(pageNo);
            }

            certList.setSource(Arrays.asList(certDefs.toArray()));
            certList.setSort(new SortDefinition() {
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
        } else {
            //They're changing pages
            certList = (PagedListHolder) session.getAttribute(SESSION_LIST_ATTRIBUTE);
            certRequirementList = (Map) session.getAttribute(SESSION_REQUIREMENT_LIST_ATTRIBUTE);
            certificateIsAwarded = (Map) session.getAttribute(SESSION_IS_AWARDED_ATTRIBUTE);

            if(PAGINATION_NEXT.equals(page)  && !certList.isLastPage()) {
                certList.nextPage();

            } else if(PAGINATION_LAST.equals(page)) {
                certList.setPage(certList.getLastLinkedPage());

            } else if(PAGINATION_PREV.equals(page) && !certList.isFirstPage()) {
                certList.previousPage();

            } else if(PAGINATION_FIRST.equals(page)) {
                certList.setPage(certList.getFirstLinkedPage());
            }
        }

        session.setAttribute(SESSION_LIST_ATTRIBUTE, certList);
        session.setAttribute(SESSION_REQUIREMENT_LIST_ATTRIBUTE, certRequirementList);
        session.setAttribute(SESSION_IS_AWARDED_ATTRIBUTE, certificateIsAwarded);
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

    public ModelAndView certUnauthorizedListHandler(String page, Integer pageSize, Integer pageNo, HttpServletRequest request) throws Exception {
        ModelAndView mav = new ModelAndView(UNAUTHORIZED_VIEW);
        Map<String, Object> model = new HashMap<>();
        model.put("view", "list");
        mav.addAllObjects(model);
        return mav;
    }

    @RequestMapping("/delete.form")
    public ModelAndView deleteCertificateHandler(@RequestParam(PARAM_CERT_ID) String certId, HttpServletRequest request, HttpServletResponse response) {
        HashMap<String, String> model = new HashMap<>();
        if (!isAdministrator()) {
            model.put(ERROR_MESSAGE, MESSAGE_ERROR_NOT_ADMIN);
        }

        if (StringUtils.isBlank( certId )) {
            model.put(ERROR_MESSAGE, MESSAGE_ERROR_NO_SELECTION);
        }

        try {
            certificateService.deleteCertificateDefinition(certId);
        } catch (IdUnusedException e) {
            model.put(ERROR_MESSAGE, MESSAGE_ERROR_BAD_ID);
        } catch (DocumentTemplateException dte) {
            model.put(ERROR_MESSAGE, MESSAGE_TEMPLATE_PROCESSING_ERROR);
        }

        if (model.size () > 0) {
            return new ModelAndView (REDIRECT + THIS_PAGE, model);
        }

        return new ModelAndView (REDIRECT + THIS_PAGE);
    }

    @RequestMapping("/print.form")
    public ModelAndView printCertificateHandler(@RequestParam(PARAM_CERT_ID) String certId, HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = null;
        OutputStream out;

        //true if there's trouble creating the certificate
        boolean creationError = false;
        CertificateDefinition definition;

        try {
            definition = certificateService.getCertificateDefinition(certId);

        } catch (IdUnusedException iue) {
            try {
                mav = certParticipantListHandler(null, null, null, request);

                //this gets mav's actual model (not a clone)
                Map model = mav.getModel();

                //add the error to mav's model
                model.put(ERROR_MESSAGE, MESSAGE_ERROR_BAD_ID);
                return mav;

            } catch (Exception e) {
                //Guess there's nothing we can do
                log.error("{} has attempted to download certificate for non existant certificate: {}, failed to provide feedback", userId(), certId);
                return null;
            }
        }

        Date issueDate = definition.getIssueDate(userId(), false);
        boolean awarded = false;
        try {
            awarded = definition.isAwarded(userId(), false);
        } catch (Exception e) {}

        if (awarded && isAwardable()) {
            DocumentTemplate template = definition.getDocumentTemplate();

            try {
                //get an input stream for the PDF
                InputStream in = documentTemplateService.render(template, definition, userId());

                //Creating the pdf was a success
                //proceed to create the http response

                //Make the filename
                StringBuilder fNameBuff = new StringBuilder();
                SimpleDateFormat sdf = new SimpleDateFormat(PDF_FILE_NAME_DATE_FORMAT);
                String  certName = definition.getName();
                String templName = template.getName();
                String extension = "";
                int dotIndex = -1;

                if (templName != null && (dotIndex = templName.lastIndexOf('.')) > -1) {
                    extension = templName.substring(dotIndex);
                }

                certName = certName.replaceAll("[^a-zA-Z0-9]+","-");

                String strIssueDate = "";
                if (issueDate != null) {
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
                int numread;
                out = response.getOutputStream();

                while ((numread = in.read(buff)) != -1) {
                    out.write(buff, 0, numread);
                }

                out.flush();
                out.close();
            } catch (TemplateReadException | VariableResolutionException | IOException e) {
                creationError = true;
            }
        }

        if (creationError) {
            try {
                mav = certParticipantListHandler(null, null, null, request);

                //this gets mav's actual model (not a clone)
                Map model = mav.getModel();

                //add these entries to mav's model
                model.put(ERROR_MESSAGE, MESSAGE_FORM_PRINT_ERROR);
                model.put(MODEL_KEY_ERROR_ARGUMENTS_ATTRIBUTE, MAIL_SUPPORT);
            } catch (Exception e) {
                //An exception while handling previous errors
                //Guess there's nothing we can do
                log.error("Couldn't create the pdf for {}, certId is {}, failed to provide feedback", userId(), certId);
                return null;
            }
        }

        return mav;
    }

    /**
     * This method handles the report. This includes landing on the report view, handling the paging navigators,
     * and exporting the CSV. However, returning to the certificates list is handled in JSP
     * @param certId the certificate on which is being reported
     * @param page the destination (next, previous, first, last)
     * @param pageSize the page size (for the paging navigator)
     * @param pageNo the destination (specified number)
     * @param export true if exporting a CSV
     * @param request HTTP request
     * @param response HTTP response
     * @return the ModelAndView object for JSP
     * @throws Exception
     */
    @RequestMapping("/reportView.form")
    public ModelAndView certAdminReportHandler(@RequestParam(PARAM_CERT_ID) String certId, @RequestParam(value=PAGINATION_PAGE, required=false) String page,
                                               @RequestParam(value=PAGE_SIZE, required=false) Integer pageSize,
                                               @RequestParam(value=PAGE_NO, required=false) Integer pageNo,
                                               @RequestParam(value=PARAM_EXPORT, required=false) Boolean export,
                                               HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!isAdministrator()) {
            //only people who have permission to add/edit certificates can see this report
            return null;
        }

        //The model that will be sent to the UI
        Map<String, Object> model = new HashMap<>();

        //Any errors that need to be sent to the UI
        List<String> errors = new ArrayList<>();

        //Will be used to 'cache' some data to speed up the paging navigator
        HttpSession session = request.getSession();

        /*The Report table's headers for columns that are related to the certificate definition's criteria
         * (other headers are already handled in jsp)*/
        List<Object> criteriaHeaders = new ArrayList<>();

        //holds the contents of the table, the page number, the page size, etc.
        PagedListHolder reportList;

        //Pass the certificate definition to the UI (so it can print its name and use its id as necessary)
        CertificateDefinition definition;

        try {
            definition = certificateService.getCertificateDefinition(certId);
            if (logIfNull(definition, "cannot retrieve certificate definition for certId = " + certId)) {
                return null;
            }

            if ( !siteId().equals(definition.getSiteId()) ) {
                log.warn("{} is trying to access a certificate outside of their site", userId());
                return null;
            }
        } catch (IdUnusedException e) {
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

        // Determine if the current user can view student numbers
        boolean canShowStudentNums = certificateService.canUserViewStudentNumbers();
        model.put(this.MODEL_KEY_CAN_USER_VIEW_STUDENT_NUMS, canShowStudentNums);

        List<String> requirements = new ArrayList<>();
        Integer expiryOffset = null;

        if(page == null && export == null && pageSize == null) {
            //It's their first time hitting the page or they changed the page size
            // -we'll load/refresh all the data
            model.put(MODEL_KEY_USE_DEFAULT_DISPLAY_OPTIONS, true);

            //get the requirements for the current user
            Iterator<Criterion> itCriterion = definition.getAwardCriteria().iterator();
            while (itCriterion.hasNext()) {
                Criterion crit = itCriterion.next();
                if ( !(crit instanceof WillExpireCriterion) ) {
                    //we only care about criteria that affect whether the certificate is awarded
                    //WillExpireCriteiron has no effect on whether it is awarded
                    requirements.add(crit.getExpression());
                }
            }

            //Use orderedCriteria to keep track of the order of the headers so that we can populate the table accordingly
            ArrayList<Criterion> orderedCriteria = new ArrayList<>();

            //iterate through the certificate definition's criteria, and grab headers for the criteria columns accordingly
            itCriterion = definition.getAwardCriteria().iterator();
            while (itCriterion.hasNext()) {
                Criterion crit = itCriterion.next();
                if (logIfNull(crit, "definition contained null criterion. certId: " + certId)) {
                    return null;
                }

                if (crit instanceof WillExpireCriterion) {
                    /* special case because expiration offset is used on the UI
                     * and this is always the first column after the Issue Date
                     * */
                    WillExpireCriterion wechi = (WillExpireCriterion) crit;

                    //expiration comes first (immediately after issue date)
                    criteriaHeaders.addAll(0, crit.getReportHeaders());
                    String strExpiryOffset = wechi.getExpiryOffset();
                    if (logIfNull(strExpiryOffset, "no expiry offset found for criterion: "+ wechi.getId())) {
                        return null;
                    }

                    expiryOffset = new Integer(strExpiryOffset);

                } else {
                    criteriaHeaders.addAll(crit.getReportHeaders());
                }

                //Expiration date should immediately follow issue date
                if (crit instanceof WillExpireCriterion) {
                    //0th position immediately follows the issue date
                    orderedCriteria.add(0, crit);
                } else {
                    //all other criteria go at the back
                    orderedCriteria.add(crit);
                }
            }

            session.setAttribute(SESSION_ORDERED_CRITERIA, orderedCriteria);

            //Prepare the Report table's contents
            List<ReportRow> reportRows = new ArrayList<>();

            /* Iterate through the list of users who have the ability to be awarded certificates,
             * populate each row of the table accordingly*/
            List<String> userIds = getAwardableUserIds();
            try {
                reportRows = getReportRows( definition, "all", null, null, null, userIds, session );
            } catch( NumberFormatException ex ) {
                model.put( ERROR_MESSAGE, MESSAGE_GRADE_NOT_NUMERIC );
            }

            //set up the paging navigator
            //the 'if' surrounding this scope: page == null && export == null
            //this happens when freshly arriving on this page or when changing the page size
            reportList = new PagedListHolder(reportRows);

            if(pageSize != null) {
                //they changed the page size
                reportList.setPageSize(pageSize);
            } else {
                //fresh arrival, set the default page size
                //set default to 100
                pageSize = PAGE_SIZE_LIST.get(3);
                reportList.setPageSize(pageSize);
            }

            if(pageNo != null) {
                reportList.setPage(pageNo);
            }

            reportList.setSort(new SortDefinition() {
                public String getProperty() {
                    //sort by the getName() method
                    return CERTIFICATE_NAME_PROPERTY;
                }

                public boolean isIgnoreCase() {
                    return true;
                }

                public boolean isAscending() {
                    return true;
                }
            });

            reportList.resort();

        } else if (export == null) {
            // !(page == null && export == null) && export == null -> page != null
            // page != null -> they clicked a navigation button

            //pull the headers and the report list from the http session
            requirements = (List<String>) session.getAttribute(SESSION_REQUIREMENTS_ATTRIBUTE);
            expiryOffset = (Integer) session.getAttribute(SESSION_EXPIRY_OFFSET_ATTRIBUTE);
            criteriaHeaders = (List<Object>) session.getAttribute(SESSION_REPORT_CRIT_HEADERS_ATTRIBUTE);
            reportList = (PagedListHolder) session.getAttribute(SESSION_REPORT_LIST_ATTRIBUTE);

            //navigate appropriately
            if(PAGINATION_NEXT.equals(page) && !reportList.isLastPage()) {
                reportList.nextPage();

            } else if(PAGINATION_LAST.equals(page)) {
                reportList.setPage(reportList.getLastLinkedPage());

            } else if(PAGINATION_PREV.equals(page) && !reportList.isFirstPage()) {
                reportList.previousPage();

            } else if(PAGINATION_FIRST.equals(page)) {
                reportList.setPage(reportList.getFirstLinkedPage());

            } else if (pageSize != null) {
                reportList.setPageSize(pageSize);
            }

        } else if (export) {
            // they clicked Export as CSV
            //get the headers and the report list from the http session
            requirements = (List<String>) session.getAttribute(SESSION_REQUIREMENTS_ATTRIBUTE);
            criteriaHeaders = (List<Object>) session.getAttribute(SESSION_REPORT_CRIT_HEADERS_ATTRIBUTE);
            reportList = (PagedListHolder) session.getAttribute(SESSION_REPORT_LIST_ATTRIBUTE);

            try {
                definition = certificateService.getCertificateDefinition(certId);

                //prepare the file name for the http response header
                DateFormat filenameDateFormat = new SimpleDateFormat(CSV_FILE_NAME_FORMAT);
                String today = filenameDateFormat.format(new Date());
                String report = messages.getString(MESSAGE_REPORT_EXPORT_FNAME);
                String defName = definition.getName();
                if (logIfNull(defName,"certificate name is null: "+ certId)) {
                    errors.add(getReportExportErrorMessage());
                    return reportViewError(model, errors, requirements, canShowStudentNums, criteriaHeaders, reportList);
                }

                defName = defName.replaceAll("[^a-zA-Z0-9]+","-");

                //fill in the csv's header
                StringBuilder contents = new StringBuilder();
                decimalSeparator = formattedText.getDecimalSeparator();
                csvSeparator = ",".equals(decimalSeparator) ? ";" : ",";
                appendItem(contents, messages.getString(MESSAGE_REPORT_TABLE_HEADER_NAME), false);
                appendItem(contents, messages.getString(MESSAGE_REPORT_TABLE_HEADER_USER_ID), false);
                if (canShowStudentNums){
                    appendItem(contents, messages.getString(MESSAGE_REPORT_TABLE_HEADER_STUDENT_NUM), false);
                }
                appendItem(contents, messages.getString(MESSAGE_REPORT_TABLE_HEADER_ROLE), false);
                appendItem(contents, messages.getString(MESSAGE_REPORT_TABLE_HEADER_ISSUEDATE), false);
                Iterator<Object> itHeaders = criteriaHeaders.iterator();
                while (itHeaders.hasNext()) {
                    appendItem(contents, (String) itHeaders.next(), false);
                }

                appendItem(contents, messages.getString(MESSAGE_REPORT_TABLE_HEADER_AWARDED), true);

                // gets the original list of ReportRows
                List<ReportRow> table;
                try {
                    table = (List<ReportRow>) reportList.getSource();
                } catch( Exception ex ) {
                    log.error("Couldn't cast reportList for the reportView. certId: {}", certId);
                    errors.add(getReportExportErrorMessage());
                    return reportViewError(model, errors, requirements, canShowStudentNums, criteriaHeaders, reportList);
                }

                //fill the rest of the csv
                Iterator<ReportRow> itTable = table.iterator();
                while (itTable.hasNext()) {
                    //represents a line in the table
                    ReportRow row = itTable.next();
                    appendItem(contents, row.getName(), false);
                    appendItem(contents, row.getUserId(), false);
                    if (canShowStudentNums) {
                        appendItem(contents, row.getStudentNumber(), false);
                    }

                    appendItem(contents, row.getRole(), false);
                    appendItem(contents, row.getIssueDate(), false);
                    Iterator<CriterionProgress> itCriterionCells = row.getCriterionCells().iterator();
                    while (itCriterionCells.hasNext()) {
                        String progressItem = itCriterionCells.next().getProgress();
                        try{
                            Double.parseDouble(progressItem);
                            //It's a double, if it uses a different decimal separator replace it.
                            if(",".equals(decimalSeparator)){
                                progressItem = progressItem.replace(".",",");
                            }
                        //Swallow exception
                        } catch(Exception ex){}
                        appendItem(contents, progressItem, false);
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
                try( OutputStream out = response.getOutputStream() ) {
                    out.write(data.getBytes());
                    out.flush();
                }

                //we're not updating their view
                return null;

            } catch (IdUnusedException e) {
                //they sent an invalid certId in their http GET;
                /*possible causes: they clicked on View Report after another user deleted the certificate definition,
                or they attempted to do evil with a random http GET.
                We don't care*/
                log.error("unused certificate id passed to report's csv export: {}", certId);
                errors.add(getReportExportErrorMessage());
                return reportViewError(model, errors, requirements, canShowStudentNums, criteriaHeaders, reportList);
            }

        } else {
            //should never happen
            log.warn("hit reportView.form with export=false. Should never happen");
            return null;
        }

        //handle plurals when appropriate
        String strExpiryOffset = null;
        if (expiryOffset != null && expiryOffset == 1) {
            strExpiryOffset = "1 " + messages.getString(MESSAGE_EXPIRY_OFFSET_MONTH);
        } else if (expiryOffset != null) {
            strExpiryOffset = expiryOffset + " " + messages.getString(MESSAGE_EXPIRY_OFFSET_MONTHS);
        }

        //push the navigator and the headers to the http session
        session.setAttribute(SESSION_REQUIREMENTS_ATTRIBUTE, requirements);
        session.setAttribute(SESSION_EXPIRY_OFFSET_ATTRIBUTE, expiryOffset);
        session.setAttribute(SESSION_CAN_USER_VIEW_STUDENT_NUMS, canShowStudentNums);
        session.setAttribute(SESSION_REPORT_CRIT_HEADERS_ATTRIBUTE, criteriaHeaders);
        session.setAttribute(SESSION_REPORT_LIST_ATTRIBUTE, reportList);

        //populate the model as necessary
        model.put(MODEL_KEY_ERRORS_ATTRIBUTE, errors);
        model.put(MODEL_KEY_REQUIREMENTS_ATTRIBUTE, requirements);
        model.put(MODEL_KEY_EXPIRY_OFFSET_ATTRIBUTE, strExpiryOffset);
        model.put(MODEL_KEY_CAN_USER_VIEW_STUDENT_NUMS, canShowStudentNums);
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

    private String getReportExportErrorMessage() {
        return getMessages().getFormattedMessage(MESSAGE_REPORT_EXPORT_ERROR, new Object[]{MAIL_SUPPORT});
    }

    /**
     * If an error occurs that prevents us from generating the report view,
     * this will give us a return value such that the user will see the relevant error
     *
     * @param model
     * @param errors
     * @param requirements
     * @param canUserViewStudentNums
     * @param criteriaHeaders
     * @param reportList
     * @return
     */
    private ModelAndView reportViewError(Map<String, Object> model, List<String> errors, List<String> requirements, Boolean canUserViewStudentNums, List<Object> criteriaHeaders, PagedListHolder reportList) {
        //Include what we can, but ultimately ensure that we can display the errors to the user
        if (model.get(MODEL_KEY_ERRORS_ATTRIBUTE) == null) {
            model.put(MODEL_KEY_ERRORS_ATTRIBUTE, errors);
        }
        if (model.get(MODEL_KEY_REQUIREMENTS_ATTRIBUTE) == null) {
            model.put(MODEL_KEY_REQUIREMENTS_ATTRIBUTE, requirements);
        }
        if (model.get(MODEL_KEY_CAN_USER_VIEW_STUDENT_NUMS) == null) {
            model.put(MODEL_KEY_CAN_USER_VIEW_STUDENT_NUMS, canUserViewStudentNums);
        }
        if (model.get(MODEL_KEY_CRIT_HEADERS_ATTRIBUTE) == null) {
            model.put(MODEL_KEY_CRIT_HEADERS_ATTRIBUTE, criteriaHeaders);
        }

        PagedListHolder plh = (PagedListHolder) model.get(MODEL_KEY_REPORT_LIST_ATTRIBUTE);
        if (plh == null) {
            if (reportList == null) {
                reportList = new PagedListHolder(new ArrayList<>());
            }

            plh = reportList;
            model.put(MODEL_KEY_REPORT_LIST_ATTRIBUTE, reportList);
        }

        if (model.get(MODEL_KEY_PAGE_SIZE_LIST) == null) {
            model.put(MODEL_KEY_PAGE_SIZE_LIST, PAGE_SIZE_LIST);
        }
        if (model.get(MODEL_KEY_PAGE_NO) == null) {
            model.put(MODEL_KEY_PAGE_NO, plh.getPage());
        }
        if (model.get(MODEL_KEY_PAGE_SIZE) == null) {
            model.put(MODEL_KEY_PAGE_SIZE, plh.getPageSize());
        }
        if (model.get(MODEL_KEY_FIRST_ELEMENT) == null) {
            model.put(MODEL_KEY_FIRST_ELEMENT, plh.getFirstElementOnPage() + 1);
        }
        if (model.get(MODEL_KEY_LAST_ELEMENT) == null) {
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
                                                     HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!isAdministrator()) {
            //only people who have permission to add/edit certificates can see this report
            return null;
        }

        CertificateDefinition definition;

        try {
            definition = certificateService.getCertificateDefinition(certId);
        } catch (IdUnusedException iue) {
            //TODO
            return null;
        }

        if ( !siteId().equals(definition.getSiteId()) ) {
            //TODO
            return null;
        }

        Map<String, Object> model = new HashMap<>();
        HttpSession session = request.getSession();

        //use a set to avoid duplicates
        Set<String> setUserIds = new HashSet<>();
        setUserIds.addAll(getAwardableUserIds());
        if (includeHistorical) {
            setUserIds.addAll(getHistoricalGradedUserIds());
        }

        List<String> userIds = new ArrayList<>();
        userIds.addAll(setUserIds);

        SimpleDateFormat sdf = new SimpleDateFormat(FILTER_DATE_FORMAT);
        Date startDate = null;
        Date endDate = null;

        // We only care about date ranges if the filter type is awarded.
        if ("awarded".equals(filterType)) {
            try {
                startDate = sdf.parse(filterStartDate);
            } catch (ParseException e) {
                //leave the value as null - getReportRows will show everything up to the end date
            }

            try {
                endDate = sdf.parse(filterEndDate);
            } catch (ParseException e) {
                //leave the value as null - getReportRows will show everything after the start date
                //if they're both null it will display everything
            }

            if (endDate != null) {
                if (startDate != null) {
                    //order them correctly
                    if (endDate.before(startDate)) {
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
        }

        List<ReportRow> reportRows = null;
        try {
            reportRows = getReportRows( definition, filterType, filterDateType, startDate, endDate, userIds, session );
        } catch( NumberFormatException ex ) {
            model.put( ERROR_MESSAGE, MESSAGE_GRADE_NOT_NUMERIC );
        }

        //set up the paging navigator
        //the 'if' surrounding this scope: page == null && export == null
        //this happens when freshly arriving on this page or when changing the page size
        PagedListHolder reportList = new PagedListHolder(reportRows);

        //fresh arival, set the default page size
        //set default to 100
        int pageSize = PAGE_SIZE_LIST.get(3);
        reportList.setPageSize(pageSize);
        reportList.setSort(new SortDefinition() {
            public String getProperty() {
                //sort by the getName() method
                return CERTIFICATE_NAME_PROPERTY;
            }

            public boolean isIgnoreCase() {
                return true;
            }

            public boolean isAscending() {
                return true;
            }
        });

        reportList.resort();
        session.setAttribute(SESSION_REPORT_LIST_ATTRIBUTE, reportList);
        model.put(MODEL_KEY_REPORT_LIST_ATTRIBUTE, reportList);

        List<String> requirements = (List<String>) session.getAttribute(SESSION_REQUIREMENTS_ATTRIBUTE);
        Integer expiryOffset = (Integer) session.getAttribute(SESSION_EXPIRY_OFFSET_ATTRIBUTE);
        Boolean canUserViewStudentNums = (Boolean) session.getAttribute(SESSION_CAN_USER_VIEW_STUDENT_NUMS);
        List<Object> criteriaHeaders = (List<Object>) session.getAttribute(SESSION_REPORT_CRIT_HEADERS_ATTRIBUTE);

        //handle plurals when appropriate
        String strExpiryOffset = null;
        if (expiryOffset != null && expiryOffset == 1) {
            strExpiryOffset = "1 " + messages.getString(MESSAGE_EXPIRY_OFFSET_MONTH);
        } else if (expiryOffset != null) {
            strExpiryOffset = expiryOffset + " " + messages.getString(MESSAGE_EXPIRY_OFFSET_MONTHS);
        }

        model.put(MODEL_KEY_CERTIFICATE, definition);
        model.put(MODEL_KEY_TOOL_URL, getToolUrl());
        model.put(MODEL_KEY_REQUIREMENTS_ATTRIBUTE, requirements);
        model.put(MODEL_KEY_EXPIRY_OFFSET_ATTRIBUTE, strExpiryOffset);
        model.put(MODEL_KEY_CAN_USER_VIEW_STUDENT_NUMS, canUserViewStudentNums);
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
            List<String> userIds, HttpSession session) throws NumberFormatException {
        if (!isAdministrator()) {
            //only people who have permission to add/edit certificates can see this report
            return null;
        }

        //Verify they're not hacking to view results from another site
        if ( !siteId().equals(definition.getSiteId()) ) {
            log.warn("{} is trying to access a certificate outside of their site", userId());
            //TODO
            return null;
        }

        List<Criterion> orderedCriteria = (List<Criterion>) session.getAttribute(SESSION_ORDERED_CRITERIA);
        List<ReportRow> reportRows = certificateService.getReportRows(userIds, definition, filterType, filterDateType, startDate, endDate, orderedCriteria);
        return reportRows;
    }

    /**
     * Called when clicking the reportView's table's headers to sort the columns
     * @param certId the reported certificate definition's id
     * @param sortKey the key to be sorted on
     * @param request
     * @param response
     * @return the model and view of the reportView page with the sorted table
     */
    @RequestMapping("/reportViewSort.form")
    public ModelAndView certAdminReportFilterHandler(@RequestParam(PARAM_CERT_ID) String certId, @RequestParam(PARAM_SORT) String sortKey,
                                                     HttpServletRequest request, HttpServletResponse response) {
        if (!isAdministrator()) {
            return null;
        }

        if (certId == null || sortKey == null) {
            //TODO
            return null;
        }

        CertificateDefinition definition;
        try {
            definition = certificateService.getCertificateDefinition(certId);
        } catch (IdUnusedException iue) {
            //TODO
            return null;
        }

        if ( !siteId().equals(definition.getSiteId()) ) {
            //TODO
            return null;
        }

        Map<String, Object> model = new HashMap<>();
        final HttpSession session = request.getSession();

        //determines if we are sorting the report column in ascending order
        Boolean sortReportAsc = (Boolean) session.getAttribute(SESSION_SORT_REPORT_ASC);
        if (sortReportAsc == null) {
            sortReportAsc = Boolean.TRUE;
            session.setAttribute(SESSION_SORT_REPORT_ASC, sortReportAsc);
        } else {
            //if it's the same column that we've previously clicked, flip sortReportAsc, otherwise set sortReportAsc to true
            String oldKey = (String) session.getAttribute(SESSION_SORT_REPORT_KEY);
            if (sortKey.equals(oldKey)) {
                sortReportAsc = !sortReportAsc;
            } else {
                sortReportAsc = Boolean.TRUE;
            }
        }

        session.setAttribute(SESSION_SORT_REPORT_KEY, sortKey);
        session.setAttribute(SESSION_SORT_REPORT_ASC, sortReportAsc);

        PagedListHolder reportList = (PagedListHolder) session.getAttribute(SESSION_REPORT_LIST_ATTRIBUTE);
        SortDefinition sortDefinition = new SortDefinition() {
            @Override
            public String getProperty() {
                return (String) session.getAttribute(SESSION_SORT_REPORT_KEY);
            }

            public boolean isAscending() {
                return ((Boolean) session.getAttribute(SESSION_SORT_REPORT_ASC));
            }

            public boolean isIgnoreCase() {
                return true;
            }
        };

        reportList.setSort(sortDefinition);
        reportList.resort();
        session.setAttribute(SESSION_REPORT_LIST_ATTRIBUTE, reportList);

        model.put(MODEL_KEY_REPORT_LIST_ATTRIBUTE, reportList);
        List<String> requirements = (List<String>) session.getAttribute(SESSION_REQUIREMENTS_ATTRIBUTE);
        Integer expiryOffset = (Integer) session.getAttribute(SESSION_EXPIRY_OFFSET_ATTRIBUTE);
        Boolean canUserViewStudentNums = (Boolean) session.getAttribute(SESSION_CAN_USER_VIEW_STUDENT_NUMS);
        List<Object> criteriaHeaders = (List<Object>) session.getAttribute(SESSION_REPORT_CRIT_HEADERS_ATTRIBUTE);

        //handle plurals when appropriate
        String strExpiryOffset = null;
        if (expiryOffset != null && expiryOffset == 1) {
            strExpiryOffset = "1 " + messages.getString(MESSAGE_EXPIRY_OFFSET_MONTH);
        } else if (expiryOffset != null) {
            strExpiryOffset = expiryOffset + " " + messages.getString(MESSAGE_EXPIRY_OFFSET_MONTHS);
        }

        model.put(MODEL_KEY_CERTIFICATE, definition);
        model.put(MODEL_KEY_TOOL_URL, getToolUrl());
        model.put(MODEL_KEY_REQUIREMENTS_ATTRIBUTE, requirements);
        model.put(MODEL_KEY_EXPIRY_OFFSET_ATTRIBUTE, strExpiryOffset);
        model.put(MODEL_KEY_CAN_USER_VIEW_STUDENT_NUMS, canUserViewStudentNums);
        model.put(MODEL_KEY_CRIT_HEADERS_ATTRIBUTE, criteriaHeaders);

        model.put(MODEL_KEY_PAGE_SIZE_LIST, PAGE_SIZE_LIST);
        model.put(MODEL_KEY_PAGE_NO, reportList.getPage());
        model.put(MODEL_KEY_PAGE_SIZE, reportList.getPageSize());
        model.put(MODEL_KEY_FIRST_ELEMENT, (reportList.getFirstElementOnPage() + 1));
        model.put(MODEL_KEY_LAST_ELEMENT, (reportList.getLastElementOnPage() + 1));

        ModelAndView mav = new ModelAndView(REPORT_VIEW, model);
        return mav;
    }

    /**
     * if the specified object is null, the specified message gets logged at the specified logging level
     * @param obj
     * @param message
     * @param level
     * @return
     */
    private boolean logIfNull(Object obj, String message, String level) {
        if (obj == null) {
            if (level == null) {
                log.error(message);
            } else if (LEVEL_WARN.equals(level)) {
                log.warn(message);
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
    private boolean logIfNull(Object obj, String message) {
        return logIfNull(obj, message, null);
    }

    /**
     * Appends item to a StringBuilder for CSV format by surrounding them in double quotes, and separating lines when appropriate
     * @param stringBuilder the StringBuilder we are appending to
     * @param item the item that we are appending to the CSV
     * @param eol true if this is the last item in the current line
     */
    private void appendItem(StringBuilder stringBuilder, String item, boolean eol) {
        stringBuilder.append('\"');
        if (item!=null) {
            stringBuilder.append(item);
        }
        stringBuilder.append('\"');
        if (!eol) {
            stringBuilder.append(csvSeparator);
        } else {
            stringBuilder.append('\n');
        }
    }

    @Resource(name="org.sakaiproject.util.api.FormattedText")
    public void setFormattedText(FormattedText formattedText) {
        this.formattedText = formattedText;
    }
}
