package com.rsmart.certification.api;

/**
 * This encapsulates a binary file containing a template that can be rendered into a printable certificate. The
 * template will have labeled fields that can be populated at rendering time. These fields can be used for elements
 * such as the time/date of award, the name of the recipient, and the name of the Site.
 *
 * User: duffy
 * Date: Jun 7, 2011
 * Time: 5:09:06 PM
 */
public interface DocumentTemplate
{
    public static final String COLLECTION_ID = "/certification/templates/";

    public String getId();

    public String getName();

    public String getOutputMimeType();

    /**
     * @return the raw template without populated fields
     */
    public String getResourceId();
}
