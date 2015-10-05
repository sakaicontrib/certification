/*
 * Copyright 2008 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): jbush
 */
package com.rsmart.certification.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.metaobj.shared.control.RedirectView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Locale;


/**
 *
 */
public class SakaiInternalResourceViewResolver extends InternalResourceViewResolver {
   private static Log log = LogFactory.getLog(SakaiInternalResourceViewResolver.class);
   private static final String HELPER_REDIRECT_URL_PREFIX = "helper";

   protected AbstractUrlBasedView buildView(String viewName) throws Exception {
      log.info("inside buildView");
      return super.buildView(viewName);
   }

   protected View createView(String viewName, Locale locale) throws Exception {
      log.info("inside createView");

      // Check for special "redirect:" prefix.
      if (viewName.startsWith(REDIRECT_URL_PREFIX)) {
         String redirectUrl = viewName.substring(REDIRECT_URL_PREFIX.length());
         RedirectView redirectView = new RedirectView();
         redirectView.setUrl(redirectUrl);
         return redirectView;
      }

      // Check for special "helper:" prefix.
      /*
      if (viewName.startsWith(HELPER_REDIRECT_URL_PREFIX)) {
         String redirectUrl = viewName.substring(HELPER_REDIRECT_URL_PREFIX.length());
         HelperView helperView = new HelperView();
         helperView.setUrl(redirectUrl);
         return helperView;
      }
      */
      return super.createView(viewName, locale);
   }
/*
   protected View loadView(String viewName, Locale locale) throws Exception {
      log.info("inside loadView");

      AbstractUrlBasedView view = buildView(viewName);
      TemplateJstlView templateView = (TemplateJstlView) BeanUtils.instantiateClass(TemplateJstlView.class);
      templateView.setBody(prefix + viewName + suffix);
      templateView.setContentType(view.getContentType());
      templateView.setRequestContextAttribute(view.getRequestContextAttribute());
      templateView.setAttributesMap(getAttributesMap());
      templateView.setUrl(templateView.getDefaultTemplateDefName());
      templateView.setApplicationContext(getApplicationContext());
      templateView.afterPropertiesSet();
      return templateView;
   }
*/
}
