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

import org.sakaiproject.certification.api.criteria.CriterionProgress;
import org.sakaiproject.certification.api.criteria.UnknownCriterionTypeException;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DueDatePassedCriterion extends GradebookItemCriterion
{
    private final String MESSAGE_REPORT_TABLE_HEADER_DUEDATE = "report.table.header.duedate";
    private final String MESSAGE_CERT_AVAILABLE = "cert.available";
    private final String MESSAGE_CERT_UNAVAILABLE = "cert.unavailable";

    @Override
    public List<String> getReportHeaders()
    {
        List<String> reportHeaders = new ArrayList<>();

        String gradebookItem = getItemName();
        String header = getCertificateService().getFormattedMessage(MESSAGE_REPORT_TABLE_HEADER_DUEDATE, new Object[]{gradebookItem});

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

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, getCertificateService().getLocale());
        String progress = dateFormat.format(getDueDate());
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
