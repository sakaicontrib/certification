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

package org.sakaiproject.certification.impl.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class FormatHelper {

    private static ResourceLoader rl = new ResourceLoader();

    /**
     * Parse from user's locale to Double format in a String
     * @param input - grade in user's locale
     *
     * @return
     */
    public static String inputStringToFormatString(final String input) {
        String formatString = input;
        NumberFormat format = NumberFormat.getInstance(rl.getLocale());
        Number number = 0;
        try {
            number = format.parse(input);
            Double d = number.doubleValue();
            formatString = d.toString();

        } catch (ParseException ex) {
            log.warn("Could not parse value:{} to locale:{}", input, rl.getLocale());
        }
        return formatString;
    }

    /**
     * Format a grade from the root locale for display using the user's locale
     *
     * @param grade - string representation of a grade
     * @return
     */
    public static String formatGradeForDisplay(final String grade) {
        if (StringUtils.isBlank(grade)) {
            return "";
        }

        String s;
        try {
            final DecimalFormat dfParse = (DecimalFormat) NumberFormat.getInstance(Locale.ROOT);
            dfParse.setParseBigDecimal(true);
            final BigDecimal d = (BigDecimal) dfParse.parse(grade);

            final DecimalFormat dfFormat = (DecimalFormat) NumberFormat.getInstance(rl.getLocale());
            dfFormat.setMinimumFractionDigits(0);
            dfFormat.setMaximumFractionDigits(2);
            dfFormat.setGroupingUsed(true);
            s = dfFormat.format(d);
        } catch (final NumberFormatException | ParseException e) {
            log.debug("Bad format, returning original string: {}", grade);
            s = grade;
        }

        return StringUtils.removeEnd(s, ".0");
    }
}
