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

package org.sakaiproject.certification.tool.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ListScrollTag extends AbstractLocalizableTag {

    private String listUrl;
    private ListScroll listScroll;
    private String className;
    private boolean showDropdown  = true;
    private String dropdownOptions = "100;200;400;800;" + Integer.MAX_VALUE;

    public static String SCROLL_SIZE = "sroll_size";
    public static final String ALL_KEY = "listscroll_all";
    public static final String SHOW_KEY = "listscroll_show";
    public static final String VIEWING_KEY = "listscroll_viewing";


    /**
     * Default processing of the start tag returning EVAL_BODY_BUFFERED.
     *
     * @return EVAL_BODY_BUFFERED
     * @throws javax.servlet.jsp.JspException if an error occurred while processing this tag
     * @see javax.servlet.jsp.tagext.BodyTag#doStartTag
     */
    protected int doStartTagInternal() throws Exception {
        JspWriter writer = pageContext.getOut();

        try {
            if (listScroll == null) {
                listScroll = (ListScroll) pageContext.getRequest().getAttribute("listScroll");
            }

            // don't print page if no items in list
            if (listScroll.getTotal() == 0) {
                log.debug("nothing in list, nothing to render...");
                return EVAL_PAGE;
            }


            writer.write("<div ");

            if (className != null) {
                writer.write("class=\"" + className + "\"");
            }

            writer.write(">");
            writer.write("<form name=\"listScrollForm\" >");

            String htmlOptions = "&nbsp";
            if(showDropdown) {
                if(dropdownOptions!=null) {
                    String [] vals = dropdownOptions.split(";");
                    htmlOptions = "\n<select name=\""+SCROLL_SIZE+"\" onchange=\"window.document.location=\'"+listUrl+ "&" + ListScroll.STARTING_INDEX_TAG + "=" + (listScroll.getNextIndex()-listScroll.getPerPage());
                    htmlOptions += "&"+SCROLL_SIZE+"='+this.options[this.selectedIndex].value\">\n";
                    if(vals!=null && vals.length>0) {
                        if(listScroll.getPerPage()==-1) {
                            listScroll.setPerPage(getMinIntValue(vals));
                        }

                        for( String val : vals ) {
                            if(val!=null && ((val.trim().length())>0)) {
                                int value = Integer.parseInt(val.trim());
                                String displayValue = String.valueOf(value);
                                if (value == Integer.MAX_VALUE) {
                                    displayValue = resolveMessage(ALL_KEY);
                                }

                                htmlOptions += "<option value=\""+value +"\" "+((String.valueOf(listScroll.getPerPage()).equalsIgnoreCase(String.valueOf(value)))?("selected"):(""))+">" + resolveMessage(SHOW_KEY) + " " + displayValue+"</option>\n";
                            }
                        }

                        htmlOptions += "</select>";
                    }
                }
            }

            int lastIndex = listScroll.getFirstItem() + listScroll.getPerPage()-1;
            writer.write("&nbsp;");
            writer.write(resolveMessage(VIEWING_KEY, new String[]{String.valueOf(listScroll.getFirstItem()),
                                                                  String.valueOf((lastIndex<=listScroll.getTotal())?(lastIndex):(listScroll.getTotal())),
                                                                  String.valueOf(listScroll.getTotal()),
                                                                  }));

            writer.write("&nbsp;");
            writer.write("<br>");
            writer.write("<input type=\"button\" value=\"" + resolveMessage("listscroll_first") + "\" onclick=\"window.document.location=\'");
            writer.write(listUrl + "&" + ListScroll.STARTING_INDEX_TAG + "=0");
            writer.write("\'\"");
            if (listScroll.getPrevIndex() == -1) {
                writer.write(" disabled=\"disabled\" ");
            }

            writer.write(" >");
            writer.write("<input type=\"button\" value=\"" + resolveMessage("listscroll_previous") + "\" onclick=\"window.document.location=\'");
            writer.write(listUrl + "&" + ListScroll.STARTING_INDEX_TAG + "=" + listScroll.getPrevIndex());
            writer.write("\'\"");
            if (listScroll.getPrevIndex() == -1) {
                writer.write(" disabled=\"disabled\" ");
            }

            writer.write(" >");
            writer.write("&nbsp;");
            writer.write(htmlOptions);
            writer.write("&nbsp;");
            writer.write("<input type=\"button\" value=\"" + resolveMessage("listscroll_next") + "\" onclick=\"window.document.location=\'");
            writer.write(listUrl + "&" + ListScroll.STARTING_INDEX_TAG + "=" + listScroll.getNextIndex());
            writer.write("\'\"");
            if (listScroll.getNextIndex() == -1) {
                writer.write(" disabled=\"disabled\" ");
            }

            writer.write(" >");
            writer.write("<input type=\"button\" value=\"" + resolveMessage("listscroll_last") + "\" onclick=\"window.document.location=\'");
            writer.write(listUrl + "&" + ListScroll.STARTING_INDEX_TAG + "=" + Integer.MAX_VALUE);
            writer.write("\'\"");
            if (listScroll.getNextIndex() == -1) {
                writer.write(" disabled=\"disabled\" ");
            }

            writer.write(" >");
            writer.write("</form >");
            writer.write("</div>");
            writer.write("<br />");

            // make the begin, end, and total available to the jsp page
            pageContext.setAttribute("list_scroll_begin_index", listScroll.getFirstItem());
            pageContext.setAttribute("list_scroll_end_index"  , listScroll.getLastItem ());
            pageContext.setAttribute("list_scroll_total_index", listScroll.getTotal    ());
            pageContext.setAttribute("list_scroll_page_size"  , listScroll.getPerPage  ());
        } catch (IOException e) {
            log.error("ListScrollTag.doStartTagInternal IOException");
            throw new JspException(e);
        }

        return EVAL_PAGE;
    }

    private int getMinIntValue(String [] vals) {
        int result = -1; //hardcode this in the event all the attribute values are bad
        for(int d = 0; d < vals.length; d++) {
            try {
                if(d == 0) {
                    result  = Integer.parseInt(vals[d]);
                }
                else {
                    result =  Math.min(Integer.parseInt(vals[d]),result);
                }
            }
            catch(NumberFormatException nfe) {
                log.warn("getMinIntValue NumberFormatException: {}", vals[d]);
            }
        }

        return result;
    }

    /**
     * Default processing of the end tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     * @throws javax.servlet.jsp.JspException if an error occurred while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doEndTag
     */
    public int doEndTag() throws JspException {
        listScroll = null;
        listUrl = null;
        className = null;
        return EVAL_PAGE;
    }

    /**
     * Release state.
     *
     * @see javax.servlet.jsp.tagext.Tag#release
     */
    public void release() {
        listScroll = null;
        listUrl = null;
        className = null;
    }

    /**
     * attribute set by servlet container.
     * @param listScroll
     */
    public void setListScroll(ListScroll listScroll) {
        this.listScroll = listScroll;
    }

    /**
     * attribute set by servlet container.
     * @param listUrl
     */
    public void setListUrl(String listUrl) {
        this.listUrl = listUrl;
    }

    /**
     * attribute set by servlet container.
     * @param className
     */
    public void setClassName(String className) {
        this.className = className;
    }

    public String getDropdownOptions() {
        return dropdownOptions;
    }

    public void setDropdownOptions(String dropdownOptions) {
        this.dropdownOptions = dropdownOptions;
    }

    public boolean isShowDropdown() {
        return showDropdown;
    }

    public void setShowDropdown(boolean showDropdown) {
        this.showDropdown = showDropdown;
    }
}
