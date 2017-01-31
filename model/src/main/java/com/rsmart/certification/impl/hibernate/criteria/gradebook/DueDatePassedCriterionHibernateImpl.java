package com.rsmart.certification.impl.hibernate.criteria.gradebook;

import com.rsmart.certification.api.criteria.CriterionProgress;
import com.rsmart.certification.api.criteria.UnknownCriterionTypeException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: duffy
 * Date: Jul 5, 2011
 * Time: 9:59:47 AM
 */
public class DueDatePassedCriterionHibernateImpl extends GradebookItemCriterionHibernateImpl
{
    private final String MESSAGE_REPORT_TABLE_HEADER_DUEDATE = "report.table.header.duedate";
    private final String MESSAGE_CERT_AVAILABLE = "cert.available";
    private final String MESSAGE_CERT_UNAVAILABLE = "cert.unavailable";

    public static final DateFormat REPORT_DATE_FORMAT = new SimpleDateFormat("MMMM dd, yyyy");

    @Override
    public List<String> getReportHeaders()
    {
        List<String> reportHeaders = new ArrayList<String>();

        String gradebookItem = getItemName();
        String header = getCertificateService().getFormattedMessage(MESSAGE_REPORT_TABLE_HEADER_DUEDATE, new Object[]{gradebookItem});

        reportHeaders.add(header);
        return reportHeaders;
    }

    @Override
    public List<CriterionProgress> getReportData(String userId, String siteId, Date issueDate, boolean useCaching)
    {
        List<CriterionProgress> reportData = new ArrayList<CriterionProgress>();

        boolean met = false;
        try
        {
            met = getCriteriaFactory().isCriterionMet(this, userId, siteId, useCaching);
        }
        catch (UnknownCriterionTypeException e)
        {
            //impossible
        }

        String progress = REPORT_DATE_FORMAT.format(getDueDate());
        CriterionProgress datum = new CriterionProgress(progress, met);
        reportData.add(datum);
        return reportData;
    }

    @Override
    public Date getDateMet(String userId, String siteId, boolean useCaching)
    {
        try
        {
            if (!getCriteriaFactory().isCriterionMet(this, userId, siteId, useCaching))
            {
                return null;
            }
        }
        catch (UnknownCriterionTypeException e)
        {
            //impossible
            return null;
        }

        return getDueDate();
    }

    @Override
    public String getProgress(String userId, String siteId, boolean useCaching)
    {
        Date dueDate = getDueDate();
        Date today = new Date();
        if (today.before(dueDate))
        {
            return getCertificateService().getString(MESSAGE_CERT_UNAVAILABLE);
        }

        return getCertificateService().getString(MESSAGE_CERT_AVAILABLE);
    }
}
