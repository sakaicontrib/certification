package org.sakaiproject.certification.mock;

import org.sakaiproject.certification.api.CertificateService;
import org.sakaiproject.certification.api.DocumentTemplate;
import org.sakaiproject.certification.api.DocumentTemplateRenderEngine;
import org.sakaiproject.certification.api.DocumentTemplateService;
import org.sakaiproject.certification.api.TemplateReadException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MockDocumentTemplateRenderEngine implements DocumentTemplateRenderEngine
{
    private DocumentTemplateService documentTemplateService = null;
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

    @Override
    public Set<String> getTemplateFields( InputStream inputStream ) throws TemplateReadException
    {
        return null;
    }

    public void init()
    {
        getDocumentTemplateService().register("text/plain", this);
    }

    public String getOutputMimeType(DocumentTemplate template)
    {
        return "text/plain";
    }

    public Set<String> getTemplateFields(DocumentTemplate template) throws TemplateReadException
    {
        InputStream is = certificateService.getTemplateFileInputStream(template.getResourceId());
        HashSet<String> variables = new HashSet<>();

        BufferedInputStream bis;
        int b;

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
                    int lBracket = bis.read();
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

    public InputStream render(DocumentTemplate template, Map<String, String> bindings) throws TemplateReadException
    {
        InputStream is = certificateService.getTemplateFileInputStream(template.getResourceId());
        byte data[] = null;

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

    public boolean supportsPreview(DocumentTemplate template)
    {
        return true;
    }

    public String getPreviewMimeType(DocumentTemplate template)
    {
        return "text/html";
    }

    protected byte[] populateFields (InputStream is, Map<String, String> bindings) throws IOException, TemplateReadException
    {
        StringBuilder output = new StringBuilder();
        BufferedInputStream bis;
        int b;

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
                int lBracket = bis.read();
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
                        String value = bindings.get(variable.toString());

                        if (value != null){
                            output.append(value);
                        }
                    }
                }
                else
                {
                    output.append('$');
                    output.append((char)lBracket);
                }
            }
            else
            {
                output.append((char)b);
            }
        }

        return output.toString().getBytes();
    }

    public InputStream renderPreview(DocumentTemplate template, Map<String, String> bindings) throws TemplateReadException
    {
        InputStream is = certificateService.getTemplateFileInputStream(template.getResourceId());
        byte data[] = null;
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
