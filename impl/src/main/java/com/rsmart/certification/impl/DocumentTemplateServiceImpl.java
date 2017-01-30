package com.rsmart.certification.impl;

import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.DocumentTemplate;
import com.rsmart.certification.api.DocumentTemplateRenderEngine;
import com.rsmart.certification.api.DocumentTemplateService;
import com.rsmart.certification.api.TemplateReadException;
import com.rsmart.certification.api.VariableResolutionException;
import com.rsmart.certification.api.VariableResolver;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: duffy
 * Date: Jun 30, 2011
 * Time: 1:51:57 PM
 */
public class DocumentTemplateServiceImpl implements DocumentTemplateService
{
    private final Pattern varPattern = Pattern.compile("\\$\\{(.+)\\}");

    private Map<String, DocumentTemplateRenderEngine> renderers = new HashMap<String, DocumentTemplateRenderEngine>();
    private HashMap<String, VariableResolver> variableResolvers = new HashMap<String, VariableResolver>();

    public void register(String mimeType, DocumentTemplateRenderEngine engine)
    {
        renderers.put (mimeType.trim().toLowerCase(), (DocumentTemplateRenderEngine)engine);
    }

    public boolean isPreviewable(DocumentTemplate template)
        throws TemplateReadException
    {
        DocumentTemplateRenderEngine dtre = renderers.get(template.getOutputMimeType());
        if (dtre != null)
        {
            return dtre.supportsPreview(template);
        }

        return false;
    }

    public String getPreviewMimeType(DocumentTemplate template)
        throws TemplateReadException
    {
        DocumentTemplateRenderEngine dtre = renderers.get(template.getOutputMimeType());
        if (dtre != null)
        {
            return dtre.getPreviewMimeType(template);
        }

        return null;
    }

    public void setRendererMap (Map<String, DocumentTemplateRenderEngine> map)
    {
        renderers = map;
    }

    public Set<DocumentTemplateRenderEngine> getRenderEngines()
    {
        return new HashSet(renderers.values());
    }

    public DocumentTemplateRenderEngine getRenderEngineForMimeType(String mimeType)
    {
        return renderers.get(mimeType);
    }

    public Set<String> getTemplateFields(DocumentTemplate template)
        throws TemplateReadException
    {
        DocumentTemplateRenderEngine engine = getRenderEngineForMimeType(template.getOutputMimeType());
        if (engine != null)
        {
            return engine.getTemplateFields(template);
        }

        throw new TemplateReadException ("No rendering engine supports the supplied template type");
    }

    public Set<String> getTemplateFields(InputStream inputStream, String mimeType) throws TemplateReadException
    {
        DocumentTemplateRenderEngine engine = getRenderEngineForMimeType(mimeType);
        if (engine != null)
        {
            return engine.getTemplateFields(inputStream);
        }

        throw new TemplateReadException ("No rendering engine supports the supplied template type");
    }

    public InputStream render(DocumentTemplate template, CertificateDefinition certDef, String userId)
            throws TemplateReadException, VariableResolutionException
    {
        // Maps key values to display messages (ie. expiry.offset -> "Expiration Date")
        Map<String, String> bindings = certDef.getFieldValues();

        // Maps key values to substitution values (ie. expiry.offset -> "November 21, 2022")
        HashMap<String, String> resolvedBindings = new HashMap<String, String> ();
        DocumentTemplateRenderEngine engine = getRenderEngineForMimeType(template.getOutputMimeType());

        for (String key : bindings.keySet())
        {
            String value = bindings.get(key);
            Matcher varMatch = varPattern.matcher(value);

            if (varMatch.matches())
            {
                String varName = varMatch.group(1);
                VariableResolver resolver = variableResolvers.get(varName);

                if (resolver != null)
                {
                    resolvedBindings.put (key, resolver.getValue(certDef, varName, userId));
                    continue;
                }
            }

            resolvedBindings.put (key, value);
        }

        if (engine != null)
        {
            return engine.render(template, resolvedBindings);
        }

        return null;
    }

    public void setVariableResolvers(Set<VariableResolver> resolvers)
    {
        variableResolvers.clear();

        for (VariableResolver var : resolvers)
        {
            for (String label : var.getVariableLabels())
            {
                variableResolvers.put (label, var);
            }
        }
    }

    public Set<VariableResolver> getVariableResolvers()
    {
        HashSet<VariableResolver> resolvers = new HashSet<VariableResolver>();
        resolvers.addAll(variableResolvers.values());
        return resolvers;
    }

    public Set<String> getRegisteredMimeTypes()
    {
        return renderers.keySet();
    }
}