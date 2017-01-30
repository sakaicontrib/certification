package com.rsmart.certification.impl.hibernate.criteria.gradebook;

import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.criteria.CriteriaFactory;
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
    public List<String> getReportData(String userId, String siteId, Date issueDate)
    {
        List<String> reportData = new ArrayList<String>();

        Double score = getCriteriaFactory().getScore(getItemId(), userId, siteId);
        String datum = "";
        if (score == null)
        {
            datum = getCertificateService().getString(MESSAGE_REPORT_TABLE_INCOMPLETE);
        }
        else
        {
            NumberFormat numberFormat = NumberFormat.getInstance();
            datum = numberFormat.format(score);
        }

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
