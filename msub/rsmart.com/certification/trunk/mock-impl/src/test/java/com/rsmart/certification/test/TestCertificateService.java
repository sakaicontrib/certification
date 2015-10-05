package com.rsmart.certification.test;

import com.rsmart.certification.api.CertificateAward;
import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.CertificateDefinitionStatus;
import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.DocumentTemplate;
import com.rsmart.certification.api.DocumentTemplateService;
import com.rsmart.certification.api.IncompleteCertificateDefinitionException;
import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.CriteriaTemplateVariable;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.mock.MockCertificateDefinition;
import com.rsmart.certification.mock.criteria.MockCriteriaFactory;
import com.rsmart.certification.impl.hibernate.SpringUnitTest;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.exception.IdUnusedException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: duffy
 * Date: Jun 24, 2011
 * Time: 4:40:04 PM
 */
public class TestCertificateService extends SpringUnitTest
{
    private CertificateService getCertificateService()
    {
        return (CertificateService)getBeanFromContext("com.rsmart.certification.api.CertificateService");
    }

    private DocumentTemplateService getDocumentTemplateService()
    {
        return (DocumentTemplateService)getBeanFromContext("com.rsmart.certification.api.DocumentTemplateService");
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
        return cs.createCertificateDefinition("test name", "test description", "test site id");
    }

    private static CertificateDefinition createAndPopulateCertificateDefinition (CertificateService cs,
                                                                                 DocumentTemplateService dts,
                                                                                 boolean succeedsAward)
        throws Exception
    {
        CertificateDefinition
            cd = createCertificateDefinition(cs);
        String
            data = "This is the test data ${field1} ${field2}";
        ByteArrayInputStream
            bais = new ByteArrayInputStream(data.getBytes());

        DocumentTemplate
            docTemp = cs.setDocumentTemplate(cd.getId(), "text/plain", bais);

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

        HashSet<Criterion>
            criteria = new HashSet<Criterion>();

        for (CriteriaTemplate template : cs.getCriteriaTemplates())
        {
            List<CriteriaTemplateVariable>
                variables = template.getTemplateVariables();
            int
                size = variables.size();
            HashMap<String, String>
                bindings = new HashMap<String, String>();

            i = 0;

            for (CriteriaTemplateVariable variable : variables)
            {
                if (variable instanceof MockCriteriaFactory.MockMultipleChoiceCriteriaTemplateVariable)
                {
                    bindings.put(variable.getVariableKey(), succeedsAward ? "rutebega" : "entropy");
                }
                else
                {
                    bindings.put(variable.getVariableKey(), "foobar");
                }

            }

            Criterion
                criterion = template.getCriteriaFactory().createCriterion(template, bindings);

            criteria.add(criterion);
        }

        ((MockCertificateDefinition)cd).setAwardCriteria(criteria);

        return cd;
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

        cs.setDocumentTemplate(cd.getId(), "text/plain", bais);

        CertificateDefinition
            result = cs.getCertificateDefinition(cd.getId());
        DocumentTemplate
            dt = result.getDocumentTemplate();

        assertNotNull(dt);
        assertEquals("text/plain", dt.getOutputMimeType());
        assertNotNull(dt.getId());

        InputStream
            dtDataIs = dt.getTemplateFileInputStream();

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

    @Test
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

        cs.setDocumentTemplate(cd.getId(), "text/plain", bais);

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

    @Test
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

        for (CriteriaTemplate template : templates)
        {
            assertNotNull(template.getExpression());

            List<CriteriaTemplateVariable>
                variables = template.getTemplateVariables();

            int
                size = variables.size();

            assertEquals (size, template.getTemplateVariableCount());

            int i = 0;
            HashMap<String, String>
                bindings = new HashMap<String, String>();

            for (CriteriaTemplateVariable variable : variables)
            {
                assertEquals (variable, template.getTemplateVariable(i));

                assertNotNull(variable.getVariableKey());

                if (variable.isMultipleChoice())
                {
                    String[]
                        values = (String [])variable.getValues();

                    assertNotNull(values);
                    assertTrue (values.length > 0);

                    bindings.put(variable.getVariableKey(), values [i % values.length]);
                }
                else
                {
                    bindings.put(variable.getVariableKey(), "String Value");
                }

                i++;
            }

            Criterion
                criterion = template.getCriteriaFactory().createCriterion(template, bindings);

            criteria.add(criterion);
        }


        cs.setAwardCriteria(cd.getId(), criteria);
    }

    @Test
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

    @Test
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

        assertEquals (CertificateDefinitionStatus.INACTIVE, result.getStatus());
    }

    @Test
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

    @Test
    public void testAwardGrantedWhenCriteriaMet()
        throws Exception
    {
        final CertificateService
            cs = getCertificateService();
        final DocumentTemplateService
            dts = getDocumentTemplateService();
        CertificateDefinition
            cd = createAndPopulateCertificateDefinition(cs, dts, true);

        CertificateAward
            award = cs.awardCertificate(cd.getId());

        assertNotNull(award);
        assertEquals(cd.getId(), award.getCertificateDefinitionId());
        assertEquals("mockuser", award.getUserId());
        assertNotNull(award.getCertificationTimeStamp());
    }

}
