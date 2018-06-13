package org.sakaiproject.certification.impl.hibernate;

import com.itextpdf.text.pdf.PdfReader;

import org.sakaiproject.certification.api.CertificateDefinition;
import org.sakaiproject.certification.api.CertificateDefinitionStatus;
import org.sakaiproject.certification.api.CertificateService;
import org.sakaiproject.certification.api.DocumentTemplate;
import org.sakaiproject.certification.api.DocumentTemplateService;
import org.sakaiproject.certification.api.IncompleteCertificateDefinitionException;
import org.sakaiproject.certification.api.criteria.CriteriaTemplate;
import org.sakaiproject.certification.api.criteria.Criterion;
import org.sakaiproject.certification.criteria.impl.gradebook.GreaterThanScoreCriteriaTemplate;
import org.sakaiproject.certification.impl.DocumentTemplateServiceImpl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.junit.After;
import org.junit.Assert;

import org.sakaiproject.exception.IdUnusedException;

public class TestCertificateServiceHibernateImpl extends SpringUnitTest
{
    private CertificateService getCertificateService() throws Exception
    {
        return (CertificateService) getBeanFromContext("org.sakaiproject.certification.api.CertificateService");
    }

    private DocumentTemplateServiceImpl getDocumentTemplateService()
    {
        return (DocumentTemplateServiceImpl) getBeanFromContext("org.sakaiproject.certification.api.DocumentTemplateService");
    }

    interface ExceptionCheck
    {
        public void checkForException() throws Exception;
    }

    private void assertException(ExceptionCheck ec, Class type, String msg)
    {
        Throwable caught = null;
        try
        {
            ec.checkForException();
        }
        catch(Throwable t)
        {
            if (type.isAssignableFrom(t.getClass()))
            {
                return;
            }

            caught = t;
        }

        StringBuilder sb = new StringBuilder();
        sb.append ("Exception expected of type '").append(type.getName()).append("'");

        if (caught != null)
        {
            sb.append(", caught '").append(caught.getClass().getName()).append("'");
        }

        if (msg != null && msg.length() > 0)
        {
            sb.append(": ").append(msg);
        }

        Assert.fail (sb.toString());
    }

    private void assertNoException(ExceptionCheck ec, String msg)
    {
        try
        {
            ec.checkForException();
        }
        catch(Throwable t)
        {
            StringBuilder sb = new StringBuilder();
            sb.append ("No exception expected, but got '").append(t.getClass().getName()).append("'");

            if (msg != null && msg.length() > 0)
            {
                sb.append(": ").append(msg);
            }

            Assert.fail(sb.toString());
        }
    }

    private static CertificateDefinition createCertificateDefinition(CertificateService cs) throws Exception
    {
        return createCertificateDefinition(cs, "test name", "test description", "test site id", false, "testFileName.pdf", "application/pdf", null);
    }

    private static CertificateDefinition createCertificateDefinition(CertificateService cs, String name, String desc,
                                                                     String siteId, Boolean progressHidden, String fileName,
                                                                     String mimeType, InputStream template)
        throws Exception
    {
        return cs.createCertificateDefinition(name, desc, siteId, progressHidden, fileName, mimeType, template);
    }

    private static CertificateDefinition addDocumentTemplate(CertificateDefinition cd, CertificateService cs) throws Exception
    {
        return addDocumentTemplate (cd, cs, "text/plain", "This is the test data ${field1} ${field2}");
    }

    private static CertificateDefinition addDocumentTemplate(CertificateDefinition cd, CertificateService cs,
                                                             String mime, String data)
        throws Exception
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes());
        cs.setDocumentTemplate(cd.getId(), mime, bais);
        return cs.getCertificateDefinition(cd.getId());
    }

    private static CertificateDefinition setFieldValues (CertificateService cs, DocumentTemplateService dts,
                                                         CertificateDefinition cd)
        throws Exception
    {
        DocumentTemplate docTemp = cd.getDocumentTemplate();
        Set<String> fields = dts.getTemplateFields(docTemp);
        Map<String, String> preDefFields = cs.getPredefinedTemplateVariables();
        Map<String, String> fieldBindings = new HashMap<>();
        Set<String> keys = preDefFields.keySet();
        int i = 0;
        int fieldSize = keys.size();
        String keyArr[] = new String[fieldSize];

        keys.toArray(keyArr);
        for (String field : fields)
        {
            fieldBindings.put(field, keyArr[i++ % fieldSize]);
        }

        cs.setFieldValues(cd.getId(), fieldBindings);
        return cs.getCertificateDefinition(cd.getId());
    }

    private static CertificateDefinition setAwardCriteria (CertificateService cs, CertificateDefinition cd, boolean succeed) throws Exception
    {
        HashSet<Criterion> criteria = new HashSet<>();
        for (CriteriaTemplate template : cs.getCriteriaTemplates())
        {
            if (template instanceof GreaterThanScoreCriteriaTemplate)
            {
                HashMap<String, String> bindings = new HashMap<>();
                bindings.put ("gradebook.item","1");
                bindings.put ("score",succeed?"75":"80");
                criteria.add(template.getCriteriaFactory().createCriterion(template, bindings));
            }
        }

        cs.setAwardCriteria(cd.getId(), criteria);
        return cs.getCertificateDefinition(cd.getId());
    }

    private static CertificateDefinition createAndPopulateCertificateDefinition (CertificateService cs, DocumentTemplateService dts, boolean succeedsAward)
        throws Exception
    {
        CertificateDefinition cd = createCertificateDefinition(cs);
        cd = addDocumentTemplate(cd, cs);
        cd = setFieldValues(cs, dts, cd);
        return setAwardCriteria(cs, cd, succeedsAward);
    }

    @After
    public void cleanUpTemplatesDirectory() throws Exception
    {
        CertificateService cs = getCertificateService();
        File templateDir = new File(cs.getTemplateDirectory());

        if (templateDir.exists() && templateDir.canWrite())
        {
            Stack<File> rmStack = new Stack<>();

            rmStack.push(templateDir);
            while (!rmStack.isEmpty())
            {
                File next = rmStack.peek();

                if(next.isDirectory())
                {
                    File contents[] =next.listFiles((File file, String s) -> (!(".".equals(s) || "..".equals(s))));

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

    //@Test
    public void testCertficateServiceIsValid() throws Exception
    {
        CertificateService cs = getCertificateService();
    }

    //@Test
    public void testCreateCertificateDefinition() throws Exception
    {
        CertificateService cs = getCertificateService();
        CertificateDefinition cd = createCertificateDefinition(cs);

        Assert.assertNotNull(cd);
        Assert.assertEquals("test name", cd.getName());
        Assert.assertEquals("test description", cd.getDescription());
        Assert.assertEquals("test site id", cd.getSiteId());
        Assert.assertNotNull(cd.getId());
        Assert.assertEquals(CertificateDefinitionStatus.UNPUBLISHED, cd.getStatus());

        //test unique ID is generated
        CertificateDefinition cd2 = createCertificateDefinition(cs);

        Assert.assertNotNull(cd2);
        Assert.assertTrue(!cd.getId().equals(cd2.getId()));
    }

    //@Test
    public void testCreateCertificateDefinitionAndDocumentTemplateSimultaneously() throws Exception
    {
        CertificateService cs = getCertificateService();

        URL testFileURL = getClass().getResource("/afghanistan.pdf");
        File testFile = new File (new URI(testFileURL.toString()));
        FileInputStream fis = new FileInputStream(testFile);
        CertificateDefinition cd = cs.createCertificateDefinition("test name", "test description", "test site id", false, "afghanistan.pdf",
                                                "application/pdf", fis);
        CertificateDefinition result = cs.getCertificateDefinition(cd.getId());

        fis.close();

        Assert.assertNotNull(result);
        Assert.assertEquals("test name", result.getName());
        Assert.assertEquals("test description", result.getDescription());
        Assert.assertEquals("test site id", result.getSiteId());
        Assert.assertNotNull(result.getId());
        Assert.assertEquals(CertificateDefinitionStatus.UNPUBLISHED, result.getStatus());

        DocumentTemplate dt = result.getDocumentTemplate();

        Assert.assertNotNull(dt);
        Assert.assertEquals("afghanistan.pdf", dt.getName());
        Assert.assertEquals("application/pdf", dt.getOutputMimeType());
        Assert.assertEquals(result.getId(), dt.getId());

        PdfReader reader = new PdfReader (cs.getTemplateFileInputStream(dt.getResourceId()));

        Assert.assertNotNull(reader);
        Assert.assertNotNull(reader.getInfo());

        reader.close();

        fis = new FileInputStream(testFile);

        cd = cs.createCertificateDefinition("test name", "test description", "test site id", false, "afghanistan.pdf", null, fis);
        result = cs.getCertificateDefinition(cd.getId());

        fis.close();

        Assert.assertNotNull(result);
        Assert.assertEquals("test name", result.getName());
        Assert.assertEquals("test description", result.getDescription());
        Assert.assertEquals("test site id", result.getSiteId());
        Assert.assertNotNull(result.getId());
        Assert.assertEquals(CertificateDefinitionStatus.UNPUBLISHED, result.getStatus());

        dt = result.getDocumentTemplate();

        Assert.assertNotNull(dt);
        Assert.assertEquals("afghanistan.pdf", dt.getName());
        Assert.assertEquals("application/pdf", dt.getOutputMimeType());
        Assert.assertEquals(result.getId(), dt.getId());

        reader = new PdfReader (cs.getTemplateFileInputStream(dt.getResourceId()));

        Assert.assertNotNull(reader);
        Assert.assertNotNull(reader.getInfo());

        reader.close();
    }

    //@Test
    public void testDuplicateCertificateDefinition() throws Exception
    {
        CertificateService cs = getCertificateService();
        DocumentTemplateService dts = getDocumentTemplateService();
        CertificateDefinition cd = createAndPopulateCertificateDefinition(cs, dts, true);
        CertificateDefinition duplicate;

        cs.activateCertificateDefinition(cd.getId(), true);

        cd = cs.getCertificateDefinition(cd.getId());

        duplicate = createAndPopulateCertificateDefinition(cs, dts, true);

        Assert.assertNotNull(duplicate);
        Assert.assertFalse(cd.equals(duplicate));
        Assert.assertEquals("Copy of test name", duplicate.getName());
        Assert.assertEquals("test description", duplicate.getDescription());
        Assert.assertEquals("test site id", duplicate.getSiteId());
        Assert.assertEquals(CertificateDefinitionStatus.ACTIVE, cd.getStatus());
        Assert.assertEquals(CertificateDefinitionStatus.UNPUBLISHED, duplicate.getStatus());

        DocumentTemplate oldDT = cd.getDocumentTemplate();
        DocumentTemplate newDT = duplicate.getDocumentTemplate();

        Assert.assertNotNull(newDT);
        Assert.assertTrue(!oldDT.equals(newDT));
        Assert.assertEquals(oldDT.getOutputMimeType(), newDT.getOutputMimeType());

        BufferedInputStream oldBIS = new BufferedInputStream(cs.getTemplateFileInputStream(oldDT.getResourceId()));
        BufferedInputStream newBIS = new BufferedInputStream(cs.getTemplateFileInputStream(newDT.getResourceId()));

        byte oldArr[] = new byte[2048];
        byte newArr[] = new byte[2048];
        int oldLen = oldBIS.read(oldArr);
        int newLen = newBIS.read(newArr);
        String oldStr = new String(oldArr, 0, oldLen);
        String newStr = new String(newArr, 0, newLen);

        Assert.assertEquals (oldStr, newStr);

        Set<Criterion> oldCrit = cd.getAwardCriteria();
        Set<Criterion> newCrit = duplicate.getAwardCriteria();

        Assert.assertEquals(oldCrit.size(), newCrit.size());

        for (Criterion criterion : newCrit)
        {
            Assert.assertTrue(!oldCrit.contains(newCrit));
        }

        Map<String, String> oldBindings = cd.getFieldValues();
        Map<String, String> newBindings = duplicate.getFieldValues();

        Assert.assertEquals(oldBindings.size(), newBindings.size());

        for (String key : oldBindings.keySet())
        {
            Assert.assertTrue(newBindings.containsKey(key));
            Assert.assertEquals(oldBindings.get(key), newBindings.get(key));
        }
    }

    //@Test
    public void testGetNewlyCreatedCertificateDefinition() throws Exception
    {
        CertificateService cs = getCertificateService();
        CertificateDefinition cd = createCertificateDefinition(cs);
        CertificateDefinition cd2 = createCertificateDefinition(cs);
        CertificateDefinition result = cs.getCertificateDefinition(cd.getId());

        Assert.assertNotNull(result);
        Assert.assertEquals(cd.getName(), result.getName());
        Assert.assertEquals(cd.getDescription(), result.getDescription());
        Assert.assertEquals(cd.getSiteId(), result.getSiteId());
        Assert.assertEquals(cd.getId(), result.getId());
        Assert.assertEquals(cd.getStatus(), result.getStatus());

        result = cs.getCertificateDefinition(cd2.getId());
        Assert.assertNotNull(result);
        Assert.assertEquals(cd2.getName(), result.getName());
        Assert.assertEquals(cd2.getDescription(), result.getDescription());
        Assert.assertEquals(cd2.getSiteId(), result.getSiteId());
        Assert.assertEquals(cd2.getId(), result.getId());
        Assert.assertEquals(cd2.getStatus(), result.getStatus());
    }

    //@Test
    public void testIdUnusedExceptionThrownForInvalidCertificateDefinitionId() throws Exception
    {
        ExceptionCheck check = () -> { getCertificateService().getCertificateDefinition("bogus id"); };
        assertException( check, IdUnusedException.class, null );
    }

    //@Test
    public void testSetDocumentTemplate() throws Exception
    {
        CertificateService cs = getCertificateService();
        CertificateDefinition cd = createCertificateDefinition(cs);

        String data = "This is the test data";
        ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes());

        cs.setDocumentTemplate(cd.getId(), "foo.txt", "text/plain", bais);

        CertificateDefinition result = cs.getCertificateDefinition(cd.getId());
        DocumentTemplate dt = result.getDocumentTemplate();

        Assert.assertNotNull(dt);
        Assert.assertEquals("foo.txt", dt.getName());
        Assert.assertEquals("text/plain", dt.getOutputMimeType());
        Assert.assertNotNull(dt.getId());

        InputStream dtDataIs = cs.getTemplateFileInputStream(dt.getResourceId());

        int c;
        int i = 0;
        StringBuilder readBuff = new StringBuilder();

        while ((c = dtDataIs.read()) != -1)
        {
            readBuff.append((char)c);
        }

        Assert.assertEquals(data, readBuff.toString());
    }

    //@Test
    public void testReadTemplateFields() throws Exception
    {
        CertificateService cs = getCertificateService();
        DocumentTemplateService dts = getDocumentTemplateService();

        CertificateDefinition cd = createCertificateDefinition(cs);

        String data = "This is the test data ${field1} ${field2}";
        ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes());

        cs.setDocumentTemplate(cd.getId(), "foo.txt", "text/plain", bais);

        CertificateDefinition result = cs.getCertificateDefinition(cd.getId());
        DocumentTemplate dt = result.getDocumentTemplate();

        Set<String> fields = dts.getTemplateFields(dt);

        Assert.assertNotNull(fields);
        Assert.assertEquals(2, fields.size());
        Assert.assertTrue(fields.contains("field1"));
        Assert.assertTrue(fields.contains("field2"));

        data = "no fields in this one";

        bais = new ByteArrayInputStream(data.getBytes());
        cs.setDocumentTemplate(cd.getId(), "text/plain", bais);
        result = cs.getCertificateDefinition(cd.getId());

        dt = result.getDocumentTemplate();
        fields = dts.getTemplateFields(dt);

        Assert.assertNotNull(fields);
        Assert.assertTrue(fields.isEmpty());
    }

    //@Test
    public void testGetCriteriaTemplatesAndSetBindings() throws Exception
    {
        CertificateService cs = getCertificateService();
        CertificateDefinition cd = createCertificateDefinition(cs);
        Set<CriteriaTemplate> templates = cs.getCriteriaTemplates();

        Assert.assertNotNull(templates);
        Assert.assertTrue(!templates.isEmpty());

        HashSet<Criterion> criteria = new HashSet<>();

        for (CriteriaTemplate template : cs.getCriteriaTemplates())
        {
            if (template instanceof GreaterThanScoreCriteriaTemplate)
            {
                HashMap<String, String> bindings = new HashMap<>();
                bindings.put ("gradebook.item","1");
                bindings.put ("score","80");
                criteria.add(template.getCriteriaFactory().createCriterion(template, bindings));
            }
        }

        cs.setAwardCriteria(cd.getId(), criteria);

        CertificateDefinition result = cs.getCertificateDefinition(cd.getId());
        Set<Criterion> ac = result.getAwardCriteria();

        Assert.assertNotNull(ac);
        Assert.assertEquals (criteria.size(), ac.size());
        Assert.assertTrue (ac.containsAll(criteria));
    }

    //@Test
    public void testActivateCDFailsForIncompleteCD() throws Exception
    {
        final CertificateService cs = getCertificateService();
        final CertificateDefinition cd = createCertificateDefinition(cs);

        ExceptionCheck check = () -> { cs.activateCertificateDefinition(cd.getId(), true); };
        assertException( check, IncompleteCertificateDefinitionException.class, "incomplete CD was erroneously activated" );
    }

    //@Test
    public void testActivateCDUpdatesStatusAppropriately() throws Exception
    {
        final CertificateService cs = getCertificateService();
        final DocumentTemplateService dts = getDocumentTemplateService();
        final CertificateDefinition cd = createAndPopulateCertificateDefinition(cs, dts, true);

        ExceptionCheck check = () -> { cs.activateCertificateDefinition(cd.getId(), true); };
        assertNoException( check, "succeeded setting CD status" );

        CertificateDefinition result = cs.getCertificateDefinition(cd.getId());

        Assert.assertEquals (CertificateDefinitionStatus.ACTIVE, result.getStatus());

        check = () -> { cs.activateCertificateDefinition(cd.getId(), false); };
        assertNoException( check, "succeeded setting CD status" );

        result = cs.getCertificateDefinition(cd.getId());
        Assert.assertEquals (CertificateDefinitionStatus.INACTIVE, result.getStatus());
    }


    //@Test
    public void testUnmetAwardConditionsReported() throws Exception
    {
        final CertificateService cs = getCertificateService();
        final DocumentTemplateService dts = getDocumentTemplateService();
        CertificateDefinition cd = createAndPopulateCertificateDefinition(cs, dts, true);

        Set<Criterion> criteria = cs.getUnmetAwardConditions(cd.getId(), false);

        Assert.assertNotNull (criteria);
        Assert.assertEquals (0, criteria.size());

        cd = createAndPopulateCertificateDefinition(cs, dts, false);
        criteria = cs.getUnmetAwardConditions(cd.getId(), false);

        Assert.assertNotNull (criteria);
        Assert.assertEquals (1, criteria.size());
    }

    //@Test
    public void testDeleteCertificateDefinition() throws Exception
    {
        final CertificateService cs = getCertificateService();
        final DocumentTemplateService dts = getDocumentTemplateService();

        final CertificateDefinition cd = createAndPopulateCertificateDefinition (cs, dts, true);

        cs.deleteCertificateDefinition(cd.getId());

        ExceptionCheck check = () -> { cs.getCertificateDefinition(cd.getId()); };
        assertException( check, IdUnusedException.class, "getting certificate definition after delete should have failed" );
    }

    //@Test
    public void testAddAwardCriterion() throws Exception
    {
        CertificateService cs = getCertificateService();

        URL testFileURL = getClass().getResource("/afghanistan.pdf");
        File testFile = new File (new URI(testFileURL.toString()));
        FileInputStream fis = new FileInputStream(testFile);
        CertificateDefinition cd = cs.createCertificateDefinition("test name", "test description", "test site id", false, "afghanistan.pdf",
                                                "application/pdf", fis);
        GreaterThanScoreCriteriaTemplate gisct = null;

        for (CriteriaTemplate template : cs.getCriteriaTemplates())
        {
            if (template instanceof GreaterThanScoreCriteriaTemplate)
            {
                gisct = (GreaterThanScoreCriteriaTemplate)template;
                break;
            }
        }

        Criterion criterion1;
        Criterion criterion2;

        HashMap<String, String> bindings = new HashMap<>();

        bindings.put ("gradebook.item","1");
        bindings.put ("score", "75");

        criterion1 = gisct.getCriteriaFactory().createCriterion(gisct, bindings);

        cs.addAwardCriterion(cd.getId(), criterion1);

        CertificateDefinition result = cs.getCertificateDefinition(cd.getId());

        Set<Criterion> criteria = result.getAwardCriteria();

        Assert.assertEquals (1, criteria.size());
        Assert.assertTrue (criteria.contains (criterion1));

        bindings.put ("gradebook.item","2");
        bindings.put ("score", "80");

        criterion2 = gisct.getCriteriaFactory().createCriterion(gisct, bindings);

        cs.addAwardCriterion(cd.getId(), criterion2);

        result = cs.getCertificateDefinition(cd.getId());
        criteria = result.getAwardCriteria();

        Assert.assertEquals (2, criteria.size());
        Assert.assertTrue (criteria.contains (criterion1));
        Assert.assertTrue (criteria.contains (criterion2));
    }

    //@Test
    public void testSetAwardCriterion() throws Exception
    {
        CertificateService cs = getCertificateService();

        URL testFileURL = getClass().getResource("/afghanistan.pdf");
        File testFile = new File (new URI(testFileURL.toString()));
        FileInputStream fis = new FileInputStream(testFile);
        CertificateDefinition cd = cs.createCertificateDefinition("test name", "test description", "test site id", false, "afghanistan.pdf",
                                                "application/pdf", fis);
        GreaterThanScoreCriteriaTemplate gisct = null;

        for (CriteriaTemplate template : cs.getCriteriaTemplates())
        {
            if (template instanceof GreaterThanScoreCriteriaTemplate)
            {
                gisct = (GreaterThanScoreCriteriaTemplate)template;
                break;
            }
        }

        Criterion criterion1;
        Criterion criterion2;
        Criterion criterion3;

        HashMap<String, String> bindings = new HashMap<>();

        bindings.put ("gradebook.item","1");
        bindings.put ("score", "75");

        criterion1 = gisct.getCriteriaFactory().createCriterion(gisct, bindings);

        bindings.put ("gradebook.item","2");
        bindings.put ("score", "80");

        criterion2 = gisct.getCriteriaFactory().createCriterion(gisct, bindings);

        Assert.assertTrue (!criterion1.equals(criterion2));

        HashSet<Criterion> criteria = new HashSet<>();

        criteria.add(criterion1);
        criteria.add(criterion2);

        cs.setAwardCriteria(cd.getId(), criteria);

        CertificateDefinition result = cs.getCertificateDefinition(cd.getId());
        Set<Criterion> resultCriteria = result.getAwardCriteria();

        Assert.assertEquals (2, criteria.size());

        bindings.put ("gradebook.item","3");
        bindings.put ("score", "85");

        criterion3 = gisct.getCriteriaFactory().createCriterion(gisct, bindings);

        resultCriteria.add(criterion3);

        cs.setAwardCriteria(cd.getId(), resultCriteria);

        result = cs.getCertificateDefinition(cd.getId());
        resultCriteria = result.getAwardCriteria();

        Assert.assertEquals (3, resultCriteria.size());
    }
}
