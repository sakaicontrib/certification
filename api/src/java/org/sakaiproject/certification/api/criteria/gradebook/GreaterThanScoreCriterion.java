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
import org.sakaiproject.certification.api.criteria.CriteriaFactory;
import org.sakaiproject.certification.api.criteria.CriterionProgress;
import org.sakaiproject.certification.api.criteria.UnknownCriterionTypeException;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GreaterThanScoreCriterion extends GradebookItemCriterion
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
        List<String> reportHeaders = new ArrayList<>();
        String header = getItemName();
        reportHeaders.add(header);
        return reportHeaders;
    }

    @Override
    public List<CriterionProgress> getReportData(String userId, String siteId, Date issueDate, boolean useCaching)
    {
        List<CriterionProgress> reportData = new ArrayList<>();

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
            Double score = getCriteriaFactory().getScore(getItemId(), userId, siteId, useCaching);
            if (score == null)
            {
                progress = getCertificateService().getString(MESSAGE_REPORT_TABLE_INCOMPLETE);
            }
            else
            {
                NumberFormat numberFormat = NumberFormat.getInstance();
                progress = numberFormat.format(score);
            }
        }
        catch( NumberFormatException ex )
        {
            progress = getCertificateService().getString( MESSAGE_GRADE_NOT_NUMERIC );
        }

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
            return null;
        }

        return getCriteriaFactory().getDateRecorded(getItemId(), userId, siteId, useCaching);
    }

    @Override
    public String getProgress(String userId, String siteId, boolean useCaching)
    {
        CertificateService certServ = getCertificateService();
        NumberFormat numberFormat = NumberFormat.getInstance();

        try
        {
            Double dblScore = getCriteriaFactory().getScore(getItemId(), userId, siteId, useCaching);
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
        catch( NumberFormatException ex )
        {
            return certServ.getString( MESSAGE_GRADE_NOT_NUMERIC );
        }
    }
}
