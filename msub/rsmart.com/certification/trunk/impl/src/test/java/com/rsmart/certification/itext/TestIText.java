package com.rsmart.certification.itext;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import org.junit.Test;

import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * User: duffy
 * Date: Jul 6, 2011
 * Time: 12:37:24 PM
 */
public class TestIText
{
    @Test
    public void testReadPdfFields()
        throws Exception
    {
        PdfReader
            reader = new PdfReader("afghanistan.pdf");

        AcroFields
            acroFields = reader.getAcroFields();

        Map<String, AcroFields.Item>
            fields = acroFields.getFields();

        Set<String>
            fieldKeys = fields.keySet(),
            textFieldKeys = new HashSet<String>();

        for (String key : fieldKeys)
        {
            System.out.println ("field: " + key);
            if (acroFields.getFieldType(key) == (AcroFields.FIELD_TYPE_TEXT))
            {
                textFieldKeys.add(key);
            }
        }

        assertEquals(2, textFieldKeys.size());

        FileOutputStream
            fos = new FileOutputStream("completed.pdf");
        PdfStamper
            stamper = new PdfStamper (reader, fos);
        AcroFields
            toFill = stamper.getAcroFields();
        stamper.setFormFlattening(true);
        stamper.setFreeTextFlattening(true);
        int
            i = 1;

        for (String key : textFieldKeys)
        {
            toFill.setField(key, "value " + i++);
        }

        stamper.close();
    }
}
