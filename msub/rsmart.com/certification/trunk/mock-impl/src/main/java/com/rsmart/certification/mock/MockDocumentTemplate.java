package com.rsmart.certification.mock;

import com.rsmart.certification.api.DocumentTemplate;
import com.rsmart.certification.api.TemplateReadException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: duffy
 * Date: Jun 10, 2011
 * Time: 1:43:58 PM
 */
public class MockDocumentTemplate
    implements DocumentTemplate
{
    private byte
        data[] = null;
    private String
        id = null,
        mimeType = null;

    public void setData (byte data[])
    {
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    public void setId(String id)
    {
        this.id = id;
    }
    
    public String getId()
    {
        return id;
    }

    public void setOutputMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    public String getOutputMimeType()
    {
        return mimeType;
    }

    public InputStream getTemplateFileInputStream()
        throws TemplateReadException
    {
        return new ByteArrayInputStream(data);
    }

}