package com.rsmart.certification.impl.hibernate;

import com.itextpdf.text.pdf.PdfReader;
import com.rsmart.certification.api.CertificateAward;
import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.CertificateDefinitionStatus;
import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.DocumentTemplate;
import com.rsmart.certification.api.DocumentTemplateService;
import com.rsmart.certification.api.IncompleteCertificateDefinitionException;
import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.criteria.impl.gradebook.GreaterThanScoreCriteriaTemplate;
import com.rsmart.certification.impl.DocumentTemplateServiceImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.exception.IdUnusedException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * User: duffy
 * Date: Jun 30, 2011
 * Time: 9:27:59 AM
 */
public class TestCertificateServiceHibernateImpl
    extends SpringUnitTest
{
    private CertificateServiceHibernateImpl getCertificateService()
        throws Exception
    {
        return (CertificateServiceHibernateImpl)getBeanFromContext("com.rsmart.certification.api.CertificateService");
    }
    private DocumentTemplateServiceImpl getDocumentTemplateService()
    {
        return (DocumentTemplateServiceImpl)getBeanFromContext("com.rsmart.certification.api.DocumentTemplateService");
    }

    interface ExceptionCheck
    {
        public void checkForException() throws Exception;
    }

    private final void assertException (ExceptionCheck ec, Class type, String msg)
    {
        Throwable
            caught = null;

        try
        {
            ec.checkForException();
        }
        catch(Throwable t)
        {
            if (type.isAssignableFrom(t.getClass()))
                return;

            caught = t;
        }

        StringBuilder
            sb = new StringBuilder();

        sb.append ("Exception expected of type '").append(type.getName()).append("'");

        if (caught != null)
            sb.append (", caught '").append(caught.getClass().getName()).append("'");

        if (msg != null && msg.length() > 0)
            sb.append(": ").append(msg);

        fail (sb.toString());
    }

    private final void assertNoException (ExceptionCheck ec, String msg)
    {
        try
        {
            ec.checkForException();;
        }
        catch(Throwable t)
        {
            StringBuilder
                sb = new StringBuilder();

            sb.append ("No exception expected, but got '").append(t.getClass().getName()).append("'");

            if (msg != null && msg.length() > 0)
                sb.append (": ").append(msg);

            Assert.fail (sb.toString());
        }
    }

    private static CertificateDefinition createCertificateDefinition(CertificateService cs)
        throws Exception
    {
        return createCertificateDefinition(cs, "test name", "test description", "test site id");
    }

    private static CertificateDefinition createCertificateDefinition(CertificateService cs, String name, String desc,
                                                                     String siteId)
        throws Exception
    {
        return cs.createCertificateDefinition(name, desc, siteId);
    }

    private static CertificateDefinition addDocumentTemplate(CertificateDefinition cd, CertificateService cs)
        throws Exception
    {
        return addDocumentTemplate (cd, cs, "text/plain", "This is the test data ${field1} ${field2}");
    }

    private static CertificateDefinition addDocumentTemplate(CertificateDefinition cd, CertificateService cs,
                                                             String mime, String data)
        throws Exception
    {
        ByteArrayInputStream
            bais = new ByteArrayInputStream(data.getBytes());

        cs.setDocumentTemplate(cd.getId(), mime, bais);

        return cs.getCertificateDefinition(cd.getId());
    }

    private static CertificateDefinition setFieldValues (CertificateService cs, DocumentTemplateService dts,
                                                         CertificateDefinition cd)
        throws Exception
    {
        DocumentTemplate
            docTemp = cd.getDocumentTemplate();
        Set<String>
            fields = dts.getTemplateFields(docTemp);
        Map<String, String>
            preDefFields = cs.getPredefinedTemplateVariables(),
            fieldBindings = new HashMap<String, String>();
        Set<String>
            keys = preDefFields.keySet();
        int
            i = 0,
            fieldSize = keys.size();
        String
            keyArr[] = new String[fieldSize];

        keys.toArray(keyArr);

        for (String field : fields)
        {
            fieldBindings.put(field, keyArr[i++ % fieldSize]);
        }

        cs.setFieldValues(cd.getId(), fieldBindings);

        return cs.getCertificateDefinition(cd.getId());
    }

    private static CertificateDefinition setAwardCriteria (CertificateService cs, CertificateDefinition cd, boolean succeed)
        throws Exception
    {
        HashSet<Criterion>
            criteria = new HashSet<Criterion>();

        for (CriteriaTemplate template : cs.getCriteriaTemplates())
        {
            if (template instanceof GreaterThanScoreCriteriaTemplate)
            {
                GreaterThanScoreCriteriaTemplate
                    gisct = (GreaterThanScoreCriteriaTemplate)template;

                HashMap<String, String>
                    bindings = new HashMap<String, String>();

                bindings.put ("gradebook.item","1");
                bindings.put ("score",succeed?"75":"80");

                criteria.add(template.getCriteriaFactory().createCriterion(template, bindings));
            }
        }

        cs.setAwardCriteria(cd.getId(), criteria);

        return cs.getCertificateDefinition(cd.getId());
    }

    private static CertificateDefinition createAndPopulateCertificateDefinition (CertificateService cs,
                                                                                 DocumentTemplateService dts,
                                                                                 boolean succeedsAward)
        throws Exception
    {
        CertificateDefinition
            cd = createCertificateDefinition(cs);

        cd = addDocumentTemplate(cd, cs);

        cd = setFieldValues(cs, dts, cd);

        return setAwardCriteria(cs, cd, succeedsAward);
    }

    @After
    public void cleanUpTemplatesDirectory()
        throws Exception
    {
        CertificateServiceHibernateImpl
            cs = getCertificateService();

        File
            templateDir = new File(cs.getTemplateDirectory());

        if (templateDir.exists() && templateDir.canWrite())
        {
            Stack<File>
                rmStack = new Stack<File>();

            rmStack.push(templateDir);
            while (!rmStack.isEmpty())
            {
                File
                    next = rmStack.peek();

                if(next.isDirectory())
                {
                    File
                        contents[] =next.listFiles
                            (
                                new FilenameFilter()
                                {
                                    public boolean accept(File file, String s)
                                    {
                                        return (!(".".equals(s) || "..".equals(s)));
                                    }
                                }
                            );

                    if (contents == null || contents.length == 0)
                    {
                        rmStack.pop();
                        next.delete();
                    }
                    else
                    {
                        for (File file : contents)
                        {
                            rmStack.push(file);
                        }
                    }
                }
                else
                {
                    rmStack.pop();
                    next.delete();
                }
            }
        }
    }
    
    @Test
    public void testCertficateServiceIsValid()
        throws Exception
    {
        CertificateService
            cs = getCertificateService();
    }

    //@Test
    public void testCreateCertificateDefinition()
        throws Exception
    {
        CertificateService
            cs = getCertificateService();
        CertificateDefinition
            cd = createCertificateDefinition(cs);

        assertNotNull(cd);
        assertEquals("test name", cd.getName());
        assertEquals("test description", cd.getDescription());
        assertEquals("test site id", cd.getSiteId());
        assertNotNull(cd.getId());
        assertEquals(CertificateDefinitionStatus.UNPUBLISHED, cd.getStatus());

        //test unique ID is generated
        CertificateDefinition
            cd2 = createCertificateDefinition(cs);

        assertNotNull(cd2);
        assertTrue(!cd.getId().equals(cd2.getId()));
    }

    //@Test
    public void testCreateCertificateDefinitionAndDocumentTemplateSimultaneously()
        throws Exception
    {
        CertificateService
            cs = getCertificateService();

        URL
            testFileURL = getClass().getResource("/afghanistan.pdf");
        File
            testFile = new File (new URI(testFileURL.toString()));
        FileInputStream
            fis = new FileInputStream(testFile);
        CertificateDefinition
            cd = cs.createCertificateDefinition("test name", "test description", "test site id", "afghanistan.pdf",
                                                "application/pdf", fis),
            result = cs.getCertificateDefinition(cd.getId());

        fis.close();

        assertNotNull(result);
        assertEquals("test name", result.getName());
        assertEquals("test description", result.getDescription());
        assertEquals("test site id", result.getSiteId());
        assertNotNull(result.getId());
        assertEquals(CertificateDefinitionStatus.UNPUBLISHED, result.getStatus());

        DocumentTemplate
            dt = result.getDocumentTemplate();

        assertNotNull(dt);
        assertEquals("afghanistan.pdf", dt.getName());
        assertEquals("application/pdf", dt.getOutputMimeType());
        assertEquals(result.getId(), dt.getId());

        PdfReader
            reader = new PdfReader (cs.getTemplateFileInputStream(dt.getResourceId()));

        assertNotNull(reader);
        assertNotNull(reader.getInfo());

        reader.close();

        fis = new FileInputStream(testFile);

        cd = cs.createCertificateDefinition("test name", "test description", "test site id", "afghanistan.pdf",
                                                null, fis);
        result = cs.getCertificateDefinition(cd.getId());

        fis.close();

        assertNotNull(result);
        assertEquals("test name", result.getName());
        assertEquals("test description", result.getDescription());
        assertEquals("test site id", result.getSiteId());
        assertNotNull(result.getId());
        assertEquals(CertificateDefinitionStatus.UNPUBLISHED, result.getStatus());

        dt = result.getDocumentTemplate();

        assertNotNull(dt);
        assertEquals("afghanistan.pdf", dt.getName());
        assertEquals("application/pdf", dt.getOutputMimeType());
        assertEquals(result.getId(), dt.getId());

        reader = new PdfReader (cs.getTemplateFileInputStream(dt.getResourceId()));

        assertNotNull(reader);
        assertNotNull(reader.getInfo());

        reader.close();
    }

    //@Test
    public void testDuplicateCertificateDefinition()
        throws Exception
    {
        CertificateService
            cs = getCertificateService();
        DocumentTemplateService
            dts = getDocumentTemplateService();
        CertificateDefinition
            cd = createAndPopulateCertificateDefinition(cs, dts, true),
            duplicate = null;

        cs.activateCertificateDefinition(cd.getId(), true);

        cd = cs.getCertificateDefinition(cd.getId());

        duplicate = cs.duplicateCertificateDefinition(cd.getId());

        assertNotNull(duplicate);
        assertFalse(cd.equals(duplicate));
        assertEquals("Copy of test name", duplicate.getName());
        assertEquals("test description", duplicate.getDescription());
        assertEquals("test site id", duplicate.getSiteId());
        assertEquals(CertificateDefinitionStatus.ACTIVE, cd.getStatus());
        assertEquals(CertificateDefinitionStatus.UNPUBLISHED, duplicate.getStatus());

        DocumentTemplate
            oldDT = cd.getDocumentTemplate(),
            newDT = duplicate.getDocumentTemplate();

        assertNotNull(newDT);
        assertTrue(!oldDT.equals(newDT));
        assertEquals(oldDT.getOutputMimeType(), newDT.getOutputMimeType());

        BufferedInputStream
            oldBIS = new BufferedInputStream(cs.getTemplateFileInputStream(oldDT.getResourceId())),
            newBIS = new BufferedInputStream(cs.getTemplateFileInputStream(newDT.getResourceId()));

        byte
            oldArr[] = new byte[2048],
            newArr[] = new byte[2048];
        int
            oldLen = oldBIS.read(oldArr),
            newLen = newBIS.read(newArr);
        String
            oldStr = new String(oldArr, 0, oldLen),
            newStr = new String(newArr, 0, newLen);

        assertEquals (oldStr, newStr);

        Set<Criterion>
            oldCrit = cd.getAwardCriteria(),
            newCrit = duplicate.getAwardCriteria();

        assertEquals(oldCrit.size(), newCrit.size());

        for (Criterion criterion : newCrit)
        {
            assertTrue(!oldCrit.contains(newCrit));
        }

        Map<String, String>
            oldBindings = cd.getFieldValues(),
            newBindings = duplicate.getFieldValues();

        assertEquals(oldBindings.size(), newBindings.size());

        for (String key : oldBindings.keySet())
        {
            assertTrue(newBindings.containsKey(key));
            assertEquals(oldBindings.get(key), newBindings.get(key));
        }
    }

    //@Test
    public void testGetNewlyCreatedCertificateDefinition()
        throws Exception
    {
        CertificateService
            cs = getCertificateService();
        CertificateDefinition
            cd = createCertificateDefinition(cs),
            cd2 = createCertificateDefinition(cs),
            result = cs.getCertificateDefinition(cd.getId());

        assertNotNull(result);
        assertEquals(cd.getName(), result.getName());
        assertEquals(cd.getDescription(), result.getDescription());
        assertEquals(cd.getSiteId(), result.getSiteId());
        assertEquals(cd.getId(), result.getId());
        assertEquals(cd.getStatus(), result.getStatus());

        result = cs.getCertificateDefinition(cd2.getId());
        assertNotNull(result);
        assertEquals(cd2.getName(), result.getName());
        assertEquals(cd2.getDescription(), result.getDescription());
        assertEquals(cd2.getSiteId(), result.getSiteId());
        assertEquals(cd2.getId(), result.getId());
        assertEquals(cd2.getStatus(), result.getStatus());
    }

    //@Test
    public void testIdUnusedExceptionThrownForInvalidCertificateDefinitionId()
        throws Exception
    {
        assertException(
            new ExceptionCheck()
            {
                public void checkForException()
                    throws Exception
                {
                    getCertificateService().getCertificateDefinition("bogus id");
                }
            },
            IdUnusedException.class,
            null);
    }

    //@Test
    public void testSetDocumentTemplate()
        throws Exception
    {
        CertificateService
            cs = getCertificateService();
        CertificateDefinition
            cd = createCertificateDefinition(cs);

        String
            data = "This is the test data";
        ByteArrayInputStream
            bais = new ByteArrayInputStream(data.getBytes());

        cs.setDocumentTemplate(cd.getId(), "foo.txt", "text/plain", bais);

        CertificateDefinition
            result = cs.getCertificateDefinition(cd.getId());
        DocumentTemplate
            dt = result.getDocumentTemplate();

        assertNotNull(dt);
        assertEquals("foo.txt", dt.getName());
        assertEquals("text/plain", dt.getOutputMimeType());
        assertNotNull(dt.getId());

        InputStream
            dtDataIs = cs.getTemplateFileInputStream(dt.getResourceId());

        int
            c,
            i = 0;
        StringBuffer
            readBuff = new StringBuffer();

        while ((c = dtDataIs.read()) != -1)
        {
            readBuff.append((char)c);
        }

        assertEquals(data, readBuff.toString());
    }

    //@Test
    public void testReadTemplateFields()
        throws Exception
    {
        CertificateService
            cs = getCertificateService();
        DocumentTemplateService
            dts = getDocumentTemplateService();

        CertificateDefinition
            cd = createCertificateDefinition(cs);

        String
            data = "This is the test data ${field1} ${field2}";
        ByteArrayInputStream
            bais = new ByteArrayInputStream(data.getBytes());

        cs.setDocumentTemplate(cd.getId(), "foo.txt", "text/plain", bais);

        CertificateDefinition
            result = cs.getCertificateDefinition(cd.getId());
        DocumentTemplate
            dt = result.getDocumentTemplate();

        Set<String>
            fields = dts.getTemplateFields(dt);

        assertNotNull(fields);
        assertEquals(2, fields.size());
        assertTrue(fields.contains("field1"));
        assertTrue(fields.contains("field2"));

        data = "no fields in this one";

        bais = new ByteArrayInputStream(data.getBytes());
        cs.setDocumentTemplate(cd.getId(), "text/plain", bais);
        result = cs.getCertificateDefinition(cd.getId());

        dt = result.getDocumentTemplate();
        fields = dts.getTemplateFields(dt);

        assertNotNull(fields);
        assertTrue(fields.isEmpty());
    }

    //@Test
    public void testGetCriteriaTemplatesAndSetBindings()
        throws Exception
    {
        CertificateService
            cs = getCertificateService();
        CertificateDefinition
            cd = createCertificateDefinition(cs);
        Set<CriteriaTemplate>
            templates = cs.getCriteriaTemplates();

        assertNotNull(templates);
        assertTrue(!templates.isEmpty());

        HashSet<Criterion>
            criteria = new HashSet<Criterion>();

        for (CriteriaTemplate template : cs.getCriteriaTemplates())
        {
            if (template instanceof GreaterThanScoreCriteriaTemplate)
            {
                GreaterThanScoreCriteriaTemplate
                    gisct = (GreaterThanScoreCriteriaTemplate)template;

                HashMap<String, String>
                    bindings = new HashMap<String, String>();

                bindings.put ("gradebook.item","1");
                bindings.put ("score","80");

                criteria.add(template.getCriteriaFactory().createCriterion(template, bindings));
            }
        }

        cs.setAwardCriteria(cd.getId(), criteria);

        CertificateDefinition
            result = cs.getCertificateDefinition(cd.getId());

        Set<Criterion>
            ac = result.getAwardCriteria();

        assertNotNull(ac);
        assertEquals (criteria.size(), ac.size());
        assertTrue (ac.containsAll(criteria));
    }

    //@Test
    public void testActivateCDFailsForIncompleteCD()
        throws Exception
    {
        final CertificateService
            cs = getCertificateService();
        final CertificateDefinition
            cd = createCertificateDefinition(cs);

        assertException
            (
                new ExceptionCheck()
                {
                    public void checkForException()
                        throws Exception
                    {
                        cs.activateCertificateDefinition(cd.getId(), true);
                    }
                },
                IncompleteCertificateDefinitionException.class,
                "incomplete CD was erroneously activated"
            );
    }

    //@Test
    public void testActivateCDUpdatesStatusAppropriately()
        throws Exception
    {
        final CertificateService
            cs = getCertificateService();
        final DocumentTemplateService
            dts = getDocumentTemplateService();
        final CertificateDefinition
            cd = createAndPopulateCertificateDefinition(cs, dts, true);

        assertNoException
            (
                new ExceptionCheck()
                {
                    public void checkForException()
                        throws Exception
                    {
                        cs.activateCertificateDefinition(cd.getId(), true);
                    }
                },
                "succeeded setting CD status"
            );

        CertificateDefinition
            result = cs.getCertificateDefinition(cd.getId());

        assertEquals (CertificateDefinitionStatus.ACTIVE, result.getStatus());

        assertNoException
            (
                new ExceptionCheck()
                {
                    public void checkForException()
                        throws Exception
                    {
                        cs.activateCertificateDefinition(cd.getId(), false);
                    }
                },
                "succeeded setting CD status"
            );

        result = cs.getCertificateDefinition(cd.getId());
        assertEquals (CertificateDefinitionStatus.INACTIVE, result.getStatus());
    }


    //@Test
    public void testUnmetAwardConditionsReported()
        throws Exception
    {
        final CertificateService
            cs = getCertificateService();
        final DocumentTemplateService
            dts = getDocumentTemplateService();
        CertificateDefinition
            cd = createAndPopulateCertificateDefinition(cs, dts, true);

        Set<Criterion>
            criteria = cs.getUnmetAwardConditions(cd.getId());

        assertNotNull (criteria);
        assertEquals (0, criteria.size());

        cd = createAndPopulateCertificateDefinition(cs, dts, false);
        criteria = cs.getUnmetAwardConditions(cd.getId());

        assertNotNull (criteria);
        assertEquals (1, criteria.size());
    }

    //@Test
    public void testAwardGrantedWhenCriteriaMet()
        throws Exception
    {
        final CertificateService
            cs = getCertificateService();
        final DocumentTemplateService
            dts = getDocumentTemplateService();
        final CertificateDefinition
            cd = createAndPopulateCertificateDefinition(cs, dts, true);

        CertificateAward
            award = null;

        assertException
            (
                new ExceptionCheck()
                {
                    public void checkForException()
                        throws Exception 
                    {
                        cs.getCertificateAward(cd.getId());
                    }
                },
                IdUnusedException.class,
                "should have thown an IdUnusedException"
            );

        award = cs.awardCertificate(cd.getId());

        assertNotNull(award);
        assertEquals(cd, award.getCertificateDefinition());
        assertEquals("mockuser", award.getUserId());
        assertNotNull(award.getCertificationTimeStamp());

        CertificateAward
            award2 = cs.awardCertificate(cd.getId()),
            award3 = cs.getCertificateAwardForUser(cd.getId(), "mockuser");

        assertEquals(award, award2);
        assertEquals(award, award3);

        Set<CertificateAward>
            cas = cs.getCertificateAwards(),
            cas2 = cs.getCertificateAwardsForUser("mockuser");
        Map<String, CertificateAward>
            cas3 = cs.getCertificateAwardsForUser(new String[] {cd.getId()});

        assertNotNull(cas3);
        assertTrue (cas3.containsKey(cd.getId()));

        CertificateAward
            award4 = cas3.get(cd.getId());

        assertNotNull(award4);
        assertEquals(award, award4);
        
        assertNotNull(cas);
        assertNotNull(cas2);
        assertEquals (1, cas.size());
        assertEquals (1, cas2.size());
        assertTrue(cas.contains(award));
        assertTrue(cas2.contains(award));
    }

    //@Test
    public void testGetCertificateAwardsForMulitpleCDs()
        throws Exception
    {
        final CertificateService
            cs = getCertificateService();
        final DocumentTemplateService
            dts = getDocumentTemplateService();
        CertificateDefinition
            cd = createAndPopulateCertificateDefinition(cs, dts, true),
            cd2 = createCertificateDefinition (cs, "name", "desc", "other site"),
            cd3 = createCertificateDefinition (cs, "name", "desc", "other site");

        addDocumentTemplate (cd2, cs);
        addDocumentTemplate (cd3, cs);

        cd2 = cs.getCertificateDefinition(cd2.getId());
        cd3 = cs.getCertificateDefinition(cd3.getId());
        
        cd2 = setFieldValues(cs, dts, cd2);
        cd3 = setFieldValues(cs, dts, cd3);

        cd2 = setAwardCriteria (cs, cd2, true);
        cd3 = setAwardCriteria (cs, cd3, true);

        cs.activateCertificateDefinition(cd.getId(), true);
        cs.awardCertificate(cd.getId());
        cs.activateCertificateDefinition(cd2.getId(), true);
        cs.awardCertificate(cd2.getId());
        cs.activateCertificateDefinition(cd3.getId(), true);
        cs.awardCertificate(cd3.getId());

        Set<CertificateDefinition>
            cds = new HashSet<CertificateDefinition>(),
            cdsForSite = cs.getCertificateDefinitionsForSite("other site");

        assertEquals(2, cdsForSite.size());
        assertTrue(cdsForSite.contains(cd2));
        assertTrue(cdsForSite.contains(cd3));

        Set<CertificateAward>
            allForUser = cs.getCertificateAwardsForUser("mockuser");

        assertNotNull(allForUser);
        assertEquals(1, allForUser.size());

        Map<String, CertificateAward>
            allForIDs = cs.getCertificateAwardsForUser (new String[] {cd.getId(), cd3.getId()});

        assertNotNull(allForIDs.get(cd.getId()));
        assertNull(allForIDs.get(cd2.getId()));
        assertNotNull(allForIDs.get(cd3.getId()));

        CertificateDefinition
            cd4 = createCertificateDefinition (cs, "name", "desc", "other site");

        cs.activateCertificateDefinition(cd2.getId(), false);

        cdsForSite = cs.getCertificateDefinitionsForSite("other site", new CertificateDefinitionStatus[]
                                                                        {
                                                                            CertificateDefinitionStatus.UNPUBLISHED,
                                                                            CertificateDefinitionStatus.INACTIVE
                                                                        });

        assertNotNull(cdsForSite);
        assertEquals(2, cdsForSite.size());
        assertTrue (cdsForSite.contains(cd2));
        assertTrue (!cdsForSite.contains(cd3));
        assertTrue (cdsForSite.contains(cd4));
    }

    @Test
    public void testBogusDataConditions()
        throws Exception
    {
        CertificateService
            cs = getCertificateService();

        assertNotNull(cs.getCertificateAwardsForUser(new String[0]));
    }

    //@Test
    public void testDeleteCertificateDefinition()
        throws Exception
    {
        final CertificateService
            cs = getCertificateService();
        final DocumentTemplateService
            dts = getDocumentTemplateService();

        final CertificateDefinition
            cd = createAndPopulateCertificateDefinition (cs, dts, true);

        cs.deleteCertificateDefinition(cd.getId());

        assertException(
            new ExceptionCheck()
            {
                public void checkForException() throws Exception {
                    CertificateDefinition
                        result = cs.getCertificateDefinition(cd.getId());

                }
            },
            IdUnusedException.class,
            "getting certificate definition after delete should have failed"
        );
    }

    //@Test
    public void testAddAwardCriterion()
        throws Exception
    {
        CertificateService
            cs = getCertificateService();

        URL
            testFileURL = getClass().getResource("/afghanistan.pdf");
        File
            testFile = new File (new URI(testFileURL.toString()));
        FileInputStream
            fis = new FileInputStream(testFile);
        CertificateDefinition
            cd = cs.createCertificateDefinition("test name", "test description", "test site id", "afghanistan.pdf",
                                                "application/pdf", fis);
        GreaterThanScoreCriteriaTemplate
            gisct = null;

        for (CriteriaTemplate template : cs.getCriteriaTemplates())
        {
            if (template instanceof GreaterThanScoreCriteriaTemplate)
            {
                gisct = (GreaterThanScoreCriteriaTemplate)template;
                break;
            }
        }

        Criterion
            criterion1 = null,
            criterion2 = null;

        HashMap<String, String>
            bindings = new HashMap<String, String>();

        bindings.put ("gradebook.item","1");
        bindings.put ("score", "75");

        criterion1 = gisct.getCriteriaFactory().createCriterion(gisct, bindings);

        cs.addAwardCriterion(cd.getId(), criterion1);

        CertificateDefinition
            result = cs.getCertificateDefinition(cd.getId());

        Set<Criterion>
            criteria = result.getAwardCriteria();

        assertEquals (1, criteria.size());
        assertTrue (criteria.contains (criterion1));
        
        bindings.put ("gradebook.item","2");
        bindings.put ("score", "80");

        criterion2 = gisct.getCriteriaFactory().createCriterion(gisct, bindings);

        cs.addAwardCriterion(cd.getId(), criterion2);

        result = cs.getCertificateDefinition(cd.getId());
        criteria = result.getAwardCriteria();

        assertEquals (2, criteria.size());
        assertTrue (criteria.contains (criterion1));
        assertTrue (criteria.contains (criterion2));
    }

    //@Test
    public void testSetAwardCriterion()
        throws Exception
    {
        CertificateService
            cs = getCertificateService();

        URL
            testFileURL = getClass().getResource("/afghanistan.pdf");
        File
            testFile = new File (new URI(testFileURL.toString()));
        FileInputStream
            fis = new FileInputStream(testFile);
        CertificateDefinition
            cd = cs.createCertificateDefinition("test name", "test description", "test site id", "afghanistan.pdf",
                                                "application/pdf", fis);
        GreaterThanScoreCriteriaTemplate
            gisct = null;

        for (CriteriaTemplate template : cs.getCriteriaTemplates())
        {
            if (template instanceof GreaterThanScoreCriteriaTemplate)
            {
                gisct = (GreaterThanScoreCriteriaTemplate)template;
                break;
            }
        }

        Criterion
            criterion1 = null,
            criterion2 = null,
            criterion3 = null;

        HashMap<String, String>
            bindings = new HashMap<String, String>();

        bindings.put ("gradebook.item","1");
        bindings.put ("score", "75");

        criterion1 = gisct.getCriteriaFactory().createCriterion(gisct, bindings);

        bindings.put ("gradebook.item","2");
        bindings.put ("score", "80");

        criterion2 = gisct.getCriteriaFactory().createCriterion(gisct, bindings);

        assertTrue (!criterion1.equals(criterion2));

        HashSet<Criterion>
            criteria = new HashSet<Criterion>();

        criteria.add(criterion1);
        criteria.add(criterion2);

        cs.setAwardCriteria(cd.getId(), criteria);

        CertificateDefinition
            result = cs.getCertificateDefinition(cd.getId());
        Set<Criterion>
            resultCriteria = result.getAwardCriteria();

        assertEquals (2, criteria.size());

        bindings.put ("gradebook.item","3");
        bindings.put ("score", "85");

        criterion3 = gisct.getCriteriaFactory().createCriterion(gisct, bindings);

        resultCriteria.add(criterion3);

        cs.setAwardCriteria(cd.getId(), resultCriteria);

        result = cs.getCertificateDefinition(cd.getId());
        resultCriteria = result.getAwardCriteria();

        assertEquals (3, resultCriteria.size());
    }
}
