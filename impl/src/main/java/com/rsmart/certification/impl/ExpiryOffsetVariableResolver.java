package com.rsmart.certification.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.rsmart.certification.api.CertificateAward;
import com.rsmart.certification.api.VariableResolutionException;

public class ExpiryOffsetVariableResolver extends AbstractVariableResolver
{
    private static final String CERT_EXPIRY_OFFSET = "cert.expiryOffset";

    public ExpiryOffsetVariableResolver()
    {
        addVariable( CERT_EXPIRY_OFFSET, "expiry date" );
    }

    public String getValue( CertificateAward award, String varLabel ) throws VariableResolutionException
    {
        if( CERT_EXPIRY_OFFSET.equals( varLabel ) )
        {
            // Get the certifications time stamp
            Calendar cal = Calendar.getInstance();
            Date certTimeStamp = award.getCertificationTimeStamp();

            // If an expiry offset was provided, add the expiry offset to the timestamp
            if( award.getCertificateDefinition().getExpiryOffset() != null && award.getCertificateDefinition().getExpiryOffset().length() > 0 )
            {
                cal.setTime( certTimeStamp );
                cal.add( Calendar.MONTH, Integer.parseInt( award.getCertificateDefinition().getExpiryOffset() ) );

                // Format the expiry date and return it
                DateFormat formatter = new SimpleDateFormat( "MMM dd, yyyy" );
                return formatter.format( cal.getTime() );
            }

            // Otherwise no expiry date/offset was provided, just return n/a
            else
            {
                return "n/a";
            }


        }

        throw new VariableResolutionException( "could not resolve variable: \"" + varLabel + "\"" );
    }
}
