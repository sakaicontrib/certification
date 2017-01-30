package com.rsmart.certification.impl;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.DocumentTemplate;
import com.rsmart.certification.api.DocumentTemplateRenderEngine;
import com.rsmart.certification.api.DocumentTemplateService;
import com.rsmart.certification.api.TemplateReadException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: duffy
 * Date: Jul 6, 2011
 * Time: 12:14:52 PM
 */
public class ITextDocumentTemplateRenderEngine implements DocumentTemplateRenderEngine
{
    private static final String MIME_TYPE = "application/pdf";
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

    public void init()
    {
        getDocumentTemplateService().register(MIME_TYPE, this);
    }

    public String getOutputMimeType(DocumentTemplate template)
    {
        return MIME_TYPE;
    }

    private final void assertCorrectType(final DocumentTemplate template) throws TemplateReadException
    {
        final String mimeType = template.getOutputMimeType();

        if (!MIME_TYPE.equalsIgnoreCase(mimeType))
        {
            throw new TemplateReadException("incorrect mime type: " + mimeType);
        }
    }

    public Set<String> getTemplateFields(DocumentTemplate template) throws TemplateReadException
    {
        assertCorrectType(template);

        try
        {
            PdfReader reader = new PdfReader(certificateService.getTemplateFileInputStream(template.getResourceId()));
            AcroFields acroFields = reader.getAcroFields();
            Map<String, AcroFields.Item> fields = acroFields.getFields();

            Set<String> fieldKeys = fields.keySet();
            Set<String> textFieldKeys = new HashSet<String>();

            for (String key : fieldKeys)
            {
                if (acroFields.getFieldType(key) == (AcroFields.FIELD_TYPE_TEXT))
                {
                    textFieldKeys.add(key);
                }
            }

            return textFieldKeys;
        }
        catch (IOException e)
        {
            throw new TemplateReadException (e);
        }
    }

    public InputStream render(DocumentTemplate template, Map<String, String> bindings) throws TemplateReadException
    {
        assertCorrectType(template);

        try
        {
            PdfReader reader = new PdfReader (certificateService.getTemplateFileInputStream(template.getResourceId()));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfStamper stamper = new PdfStamper (reader, baos);

            stamper.setFormFlattening(true);
            stamper.setFreeTextFlattening(true);

            AcroFields form = stamper.getAcroFields();

            for (String key : form.getFields().keySet())
            {
                String binding = bindings.get(key);
                form.setField(key, binding);
            }

            stamper.close();

            return new ByteArrayInputStream(baos.toByteArray());
        }
        catch (IOException e)
        {
            throw new TemplateReadException(e);
        }
        catch (DocumentException e)
        {
            throw new TemplateReadException(e);
        }
    }

    public boolean supportsPreview(DocumentTemplate template) throws TemplateReadException
    {
        assertCorrectType(template);
        return true;
    }

    public String getPreviewMimeType(DocumentTemplate template) throws TemplateReadException
    {
        assertCorrectType(template);
        return MIME_TYPE;
    }

    public InputStream renderPreview(DocumentTemplate template, Map<String, String> bindings) throws TemplateReadException
    {
        assertCorrectType(template);
        try
        {
            PdfReader reader = new PdfReader (certificateService.getTemplateFileInputStream(template.getResourceId()));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfStamper stamper = new PdfStamper (reader, baos);

            stamper.setFormFlattening(true);
            stamper.setFreeTextFlattening(true);
            stamper.close();

            return new ByteArrayInputStream(baos.toByteArray());
        }
        catch (IOException e)
        {
            throw new TemplateReadException(e);
        }
        catch (DocumentException e)
        {
            throw new TemplateReadException(e);
        }
    }
}
