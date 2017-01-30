package com.rsmart.certification.tool.validator;

import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.tool.utils.CertificateToolState;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.commons.CommonsMultipartFile;


public class CertificateDefinitionValidator
{
    private Pattern variablePattern = Pattern.compile ("\\$\\{(.+)\\}");

    public void validateFirst(CertificateToolState certificateToolState, Errors errors)
    {
        if(certificateToolState.getData().getSize() > 0)
        {
            CommonsMultipartFile file = certificateToolState.getData();
            String mimeTypes[] = certificateToolState.getMimeTypes().split(", ");
            if(certificateToolState.getMimeTypes().indexOf(file.getContentType()) < 0)
            {
                errors.rejectValue("data", "mimeType", "invalid mimeType");
            }
        }
    }

    public void validateSecond(CertificateToolState certificateToolState, Errors errors)
    {
        CertificateDefinition certDef = certificateToolState.getCertificateDefinition();
        if(certDef.getAwardCriteria().isEmpty())
        {
            errors.rejectValue("certificateDefinition.awardCriteria", "required", "not provided");
        }
    }

    public void validateThird(CertificateToolState certificateToolState, Errors errors)
    {
        Map<String, String> currentFields = certificateToolState.getTemplateFields();
        //Add the $'s back in (they were removed in CertificateToolState.getEscapedPredifinedFields())
        Set<String> keys = new HashSet<String>();
        keys.addAll(currentFields.keySet());
        for (String key : keys)
        {
            String value = "$" + currentFields.get(key);
            currentFields.remove(key);
            currentFields.put(key, value);
        }
        certificateToolState.setTemplateFields(currentFields);
        Map<String, String> preDefFields = certificateToolState.getPredifinedFields();
        Set<String> keySet = preDefFields.keySet();
        for(String val : currentFields.values())
        {
            Matcher variableMatcher = variablePattern.matcher(val);
            if (variableMatcher.matches() && !keySet.contains(val))
            {
                errors.rejectValue("templateFields","not valid","not valid");
            }
        }
    }
}
