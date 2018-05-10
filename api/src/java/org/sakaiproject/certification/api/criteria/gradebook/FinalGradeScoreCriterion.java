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

package org.sakaiproject.certification.api.criteria.gradebook;

import org.sakaiproject.certification.api.CertificateService;
import org.sakaiproject.certification.api.criteria.CriterionProgress;
import org.sakaiproject.certification.api.criteria.UnknownCriterionTypeException;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FinalGradeScoreCriterion extends GradebookItemCriterion
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
