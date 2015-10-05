package com.rsmart.certification.mock;

import java.util.Calendar;
import java.util.Date;

import com.rsmart.certification.api.BaseCertificateDefinition;
import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.CertificateDefinitionStatus;

/**
 * User: duffy
 * Date: Jun 10, 2011
 * Time: 1:34:46 PM
 */
public class MockCertificateDefinition
    extends BaseCertificateDefinition
    implements CertificateDefinition
{
	public MockCertificateDefinition()
	{
		super();
		this.setId("1");
		this.setName("My Cert");
		this.setCreatorUserId("2233");
		this.setDescription("Blah Blah Bing");
		this.setSiteId("2ea6f130-e694-493c-87a0-025b9821e0f6");
	}
	
}
