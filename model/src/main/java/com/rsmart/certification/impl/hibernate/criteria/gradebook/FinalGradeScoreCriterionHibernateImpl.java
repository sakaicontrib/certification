package com.rsmart.certification.impl.hibernate.criteria.gradebook;

import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.criteria.CriterionProgress;
import com.rsmart.certification.api.criteria.UnknownCriterionTypeException;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: duffy
 * Date: Jul 5, 2011
 * Time: 9:59:47 AM
 */
public class FinalGradeScoreCriterionHibernateImpl extends GradebookItemCriterionHibernateImpl
{
    private static final String MESSAGE_REPORT_TABLE_HEADER_FCG = "report.table.header.fcg";
    private static final String MESSAGE_REPORT_TABLE_INCOMPLETE = "report.table.incomplete";
    private static final String MESSAGE_ITEM_COMPLETE = "item.complete";
    private static final String MESSAGE_ITEM_INCOMPLETE = "item.incomplete";
    private static final String MESSAGE_POINT = "point";
    private static final String MESSAGE_POINTS = "points";

    public String getScore()
    {
        return getVariableBindings().get("score");
    }

    public void setScore(String score)
    {
        getVariableBindings().put("score", score);
    }

    @Override
    public List<String> getReportHeaders()
    {
        List<String> reportHeaders = new ArrayList<>();

        String fcg = getCertificateService().getString(MESSAGE_REPORT_TABLE_HEADER_FCG);

        reportHeaders.add(fcg);
        return reportHeaders;
    }

    @Override
    public List<CriterionProgress> getReportData(String userId, String siteId, Date issueDate, boolean useCaching)
    {
        List<CriterionProgress> reportHeaders = new ArrayList<>();

        boolean met = false;
        try
        {
            met = getCriteriaFactory().isCriterionMet(this, userId, siteId, useCaching);
        }
        catch (UnknownCriterionTypeException e)
        {
            //impossible
        }

        String progress;
        try
        {
            Double grade = getCriteriaFactory().getFinalScore(userId, siteId);
            if (grade == null)
            {
                progress = getCertificateService().getString(MESSAGE_REPORT_TABLE_INCOMPLETE);
            }
            else
            {
                NumberFormat numberFormat = NumberFormat.getInstance();
                progress = numberFormat.format(grade);
            }
        }
        catch( NumberFormatException ex )
        {
            progress = getCertificateService().getString( MESSAGE_GRADE_NOT_NUMERIC );
        }

        CriterionProgress datum = new CriterionProgress(progress, met);
        reportHeaders.add(datum);
        return reportHeaders;
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

        return getCriteriaFactory().getFinalGradeDateRecorded(userId, siteId);
    }

    @Override
    public String getProgress(String userId, String siteId, boolean useCaching)
    {
        CertificateService certServ = getCertificateService();
        NumberFormat numberFormat = NumberFormat.getInstance();

        try
        {
            Double dblScore = getCriteriaFactory().getFinalScore(userId, siteId);
            if (dblScore == null)
            {
                return certServ.getString(MESSAGE_ITEM_INCOMPLETE);
            }
            else
            {
                StringBuilder score = new StringBuilder(numberFormat.format(dblScore));
                if (dblScore == 1)
                {
                    score.append(" ").append(certServ.getString(MESSAGE_POINT));
                }
                else
                {
                    score.append(" ").append(certServ.getString(MESSAGE_POINTS));
                }

                return certServ.getFormattedMessage(MESSAGE_ITEM_COMPLETE, new String[]{ score.toString() });
            }
        }
        catch( NumberFormatException ex )
        {
            return certServ.getString( MESSAGE_GRADE_NOT_NUMERIC );
        }
    }
}
