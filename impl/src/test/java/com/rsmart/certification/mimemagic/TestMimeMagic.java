package com.rsmart.certification.mimemagic;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * User: duffy
 * Date: Jul 7, 2011
 * Time: 7:33:57 AM
 */
public class TestMimeMagic
{
    @Test
    public void testCheckMimeType() throws Exception
    {
        URL testFileURL = getClass().getResource("/afghanistan.pdf");
        File testFile = new File (new URI(testFileURL.toString()));
        MagicMatch mimeTypeMatch = Magic.getMagicMatch(testFile, true);

        assertEquals("application/pdf", mimeTypeMatch.getMimeType());
    }
}
