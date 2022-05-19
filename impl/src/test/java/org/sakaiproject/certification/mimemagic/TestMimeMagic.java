package org.sakaiproject.certification.mimemagic;

import org.apache.tika.Tika;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;

import static org.junit.Assert.*;

public class TestMimeMagic
{
    @Test
    public void testCheckMimeType() throws Exception
    {
        URL testFileURL = getClass().getResource("/afghanistan.pdf");
        File testFile = new File (new URI(testFileURL.toString()));
        Tika tika = new Tika();
        String mimeTypeMatch = tika.detect(testFile);

        assertEquals("application/pdf", mimeTypeMatch);
    }
}
