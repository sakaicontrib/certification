package org.sakaiproject.certification.itext;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestIText
{
    @Test
    public void testReadPdfFields() throws Exception
    {
        PdfReader reader = new PdfReader("afghanistan.pdf");
        AcroFields acroFields = reader.getAcroFields();
        Map<String, AcroFields.Item> fields = acroFields.getFields();
        Set<String> fieldKeys = fields.keySet();
        Set<String> textFieldKeys = new HashSet<>();

        for (String key : fieldKeys)
        {
            if (acroFields.getFieldType(key) == (AcroFields.FIELD_TYPE_TEXT))
            {
                textFieldKeys.add(key);
            }
        }

        assertEquals(2, textFieldKeys.size());

        FileOutputStream fos = new FileOutputStream("completed.pdf");
        PdfStamper stamper = new PdfStamper (reader, fos);
        AcroFields toFill = stamper.getAcroFields();
        stamper.setFormFlattening(true);
        stamper.setFreeTextFlattening(true);
        int i = 1;

        for (String key : textFieldKeys)
        {
            toFill.setField(key, "value " + i++);
        }

        stamper.close();
    }
}
