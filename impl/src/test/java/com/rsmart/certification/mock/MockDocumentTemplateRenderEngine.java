package com.rsmart.certification.mock;

import com.rsmart.certification.api.DocumentTemplate;
import com.rsmart.certification.api.DocumentTemplateRenderEngine;
import com.rsmart.certification.api.DocumentTemplateService;
import com.rsmart.certification.api.TemplateReadException;
import com.rsmart.certification.api.CertificateService;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: duffy
 * Date: Jun 20, 2011
 * Time: 3:02:22 PM
 */
public class MockDocumentTemplateRenderEngine
    implements DocumentTemplateRenderEngine
{
    private DocumentTemplateService
        documentTemplateService = null;

    private CertificateService certificateService = null;

    public void setDocumentTemplateService(DocumentTemplateService dts)
    {
        this.documentTemplateService = (DocumentTemplateService)dts;
    }

    public DocumentTemplateService getDocumentTemplateService()
    {
        return documentTemplateService;
    }

    public CertificateService getCertificateService()
    {
        return certificateService;
    }

    public void setCertificateService(CertificateService certificateService)
    {
        this.certificateService = certificateService;
    }

    public void init()
    {
        getDocumentTemplateService().register("text/plain", this);
    }

    public String getOutputMimeType(DocumentTemplate template) {
        return "text/plain";
    }

    public Set<String> getTemplateFields(DocumentTemplate template)
        throws TemplateReadException
    {
        InputStream is = certificateService.getTemplateFileInputStream(template.getResourceId());
        HashSet<String>
            variables = new HashSet<String>();

        BufferedInputStream
            bis = null;
        int
            b = -1;

        if (!BufferedInputStream.class.isAssignableFrom(is.getClass()))
        {
            bis = new BufferedInputStream(is);
        }
        else
        {
            bis = (BufferedInputStream)is;
        }

        try
        {
            while ((b = bis.read()) > -1)
            {
                if ('$' == b)
                {
                    int
                        lBracket = bis.read();
                    StringBuilder variable = new StringBuilder();

                    if ('{' == lBracket)
                    {
                        while ((b = bis.read()) != '}')
                        {
                            if (b < 0)
                            {
                                throw new TemplateReadException ("Unterminated template field");
                            }
                            variable.append((char)b);
                        }

                        variables.add(variable.toString());
                    }
                }
            }
        }
        catch (IOException ioe)
        {
            throw new TemplateReadException (ioe);
        }

        return variables;
    }

    public InputStream render(DocumentTemplate template, Map<String, String> bindings)
        throws TemplateReadException
    {
        InputStream is = certificateService.getTemplateFileInputStream(template.getResourceId());

        byte
            data[] = null;

        try
        {
            data = populateFields (is, bindings);
        }
        catch (IOException e)
        {
            throw new TemplateReadException (e);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                //ignore
            }
        }

        return new ByteArrayInputStream(data);
    }

    public boolean supportsPreview(DocumentTemplate template) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getPreviewMimeType(DocumentTemplate template) {
        return "text/html";  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected byte[] populateFields (InputStream is, Map<String, String> bindings)
            throws IOException, TemplateReadException {
        StringBuilder output = new StringBuilder();
        BufferedInputStream
            bis = null;
        int
            b = -1;

        if (!BufferedInputStream.class.isAssignableFrom(is.getClass()))
        {
            bis = new BufferedInputStream(is);
        }
        else
        {
            bis = (BufferedInputStream)is;
        }

        while ((b = bis.read()) > -1)
        {
            if ('$' == b)
            {
                int
                    lBracket = bis.read();
                StringBuilder variable = new StringBuilder();

                if ('{' == lBracket)
                {
                    while ((b = bis.read()) != '}')
                    {
                        if (b < 0)
                        {
                            throw new TemplateReadException ("Unterminated template field");
                        }
                        variable.append((char)b);
                    }

                    if (bindings != null)
                    {
                        String
                            value = bindings.get(variable.toString());

                        if (value != null)
                            output.append(value);
                    }
                }
                else
                {
                    output.append('$');
                    output.append((char)lBracket);
                }
            }
            else
                output.append((char)b);
        }

        return output.toString().getBytes();
    }

    public InputStream renderPreview(DocumentTemplate template, Map<String, String> bindings)
        throws TemplateReadException
    {
        InputStream is = certificateService.getTemplateFileInputStream(template.getResourceId());

        byte
            data[] = null;

        StringBuilder previewOutput = new StringBuilder();

        previewOutput.append("<html><body><p><b>This is what will be printed:</b></p><code>");
        
        try
        {
            data = populateFields (is, bindings);
        }
        catch (IOException e)
        {
            throw new TemplateReadException (e);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                //ignore
            }
        }

        previewOutput.append(data);

        previewOutput.append("</code></body></html>");

        return new ByteArrayInputStream(previewOutput.toString().getBytes());
    }
}
