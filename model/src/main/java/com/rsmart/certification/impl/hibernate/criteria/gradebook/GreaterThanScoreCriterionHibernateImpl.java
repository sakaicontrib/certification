package com.rsmart.certification.impl.hibernate.criteria.gradebook;

import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.criteria.CriteriaFactory;
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
public class GreaterThanScoreCriterionHibernateImpl extends GradebookItemCriterionHibernateImpl
{
    private static final String MESSAGE_REPORT_TABLE_INCOMPLETE = "report.table.incomplete";
    private static final String MESSAGE_ITEM_COMPLETE = "item.complete";
    private static final String MESSAGE_ITEM_INCOMPLETE = "item.incomplete";
    private static final String MESSAGE_POINT = "point";
    private static final String MESSAGE_POINTS = "points";

    public String getScore()
    {
        return getVariableBindings().get(CriteriaFactory.KEY_SCORE);
    }

    public void setScore(String score)
    {
        getVariableBindings().put(CriteriaFactory.KEY_SCORE, score);
    }

    @Override
    public List<String> getReportHeaders()
    {
        List<String> reportHeaders = new ArrayList<String>();
        String header = getItemName();
        reportHeaders.add(header);
        return reportHeaders;
    }

    @Override
    public List<CriterionProgress> getReportData(String userId, String siteId, Date issueDate)
    {
        List<CriterionProgress> reportData = new ArrayList<CriterionProgress>();

        boolean met = false;
        try
        {
            met = getCriteriaFactory().isCriterionMet(this, userId, siteId);
        }
        catch (UnknownCriterionTypeException e)
        {
            //impossible
        }

        Double score = getCriteriaFactory().getScore(getItemId(), userId, siteId);
        String progress = "";
        if (score == null)
        {
            progress = getCertificateService().getString(MESSAGE_REPORT_TABLE_INCOMPLETE);
        }
        else
        {
            NumberFormat numberFormat = NumberFormat.getInstance();
            progress = numberFormat.format(score);
        }

        CriterionProgress datum = new CriterionProgress(progress, met);
        reportData.add(datum);
        return reportData;
    }

    @Override
    public Date getDateMet(String userId, String siteId)
    {
        try
        {
            if (!getCriteriaFactory().isCriterionMet(this, userId, siteId))
            {
                return null;
            }
        }
        catch (UnknownCriterionTypeException e)
        {
            return null;
        }

        return getCriteriaFactory().getDateRecorded(getItemId(), userId, siteId);
    }

    @Override
    public String getProgress(String userId, String siteId)
    {
        CertificateService certServ = getCertificateService();
        NumberFormat numberFormat = NumberFormat.getInstance();

        Double dblScore = getCriteriaFactory().getScore(getItemId(), userId, siteId);
        if (dblScore  == null)
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
}
