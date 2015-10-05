package com.rsmart.certification.criteria.impl.gradebook;

import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.criteria.CriteriaTemplateVariable;
import com.rsmart.certification.api.criteria.CriterionCreationException;
import com.rsmart.certification.api.criteria.InvalidBindingException;
import com.rsmart.certification.api.criteria.UnknownCriterionTypeException;
import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.DueDatePassedCriterionHibernateImpl;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.FinalGradeScoreCriterionHibernateImpl;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.GreaterThanScoreCriterionHibernateImpl;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: duffy
 * Date: Jun 23, 2011
 * Time: 11:44:38 AM
 */
public class GradebookCriteriaFactory
    implements CriteriaFactory
{
    private CertificateService
        certService = null;
    private GradebookService
        gbService = null;
    private ToolManager
        toolManager = null;
    private UserDirectoryService
        userDirectoryService = null;
    private HashMap<String, CriteriaTemplate>
        criteriaTemplates = new HashMap<String, CriteriaTemplate>();
    private HashSet<Class<? extends Criterion>>
        criterionClasses = new HashSet<Class<? extends Criterion>>();
    private GreaterThanScoreCriteriaTemplate
        gbItemScoreTemplate = null;
    private DueDatePassedCriteriaTemplate
        gbDueDatePassedTemplate = null;
    private FinalGradeScoreCriteriaTemplate
    	gbFinalGradeScoreTemplate = null;
    private ResourceLoader
        resourceLoader = null;
    private SecurityService
        securityService = null;
    private SessionManager
        sessionManager = null;
    private String
        adminUser = null;

    public void init()
    {
        gbItemScoreTemplate = new GreaterThanScoreCriteriaTemplate(this);
        gbItemScoreTemplate.setResourceLoader(resourceLoader);

        gbFinalGradeScoreTemplate = new FinalGradeScoreCriteriaTemplate(this);
        gbFinalGradeScoreTemplate.setResourceLoader(resourceLoader);
        
        gbDueDatePassedTemplate = new DueDatePassedCriteriaTemplate(this);
        gbDueDatePassedTemplate.setResourceLoader(resourceLoader);

        gbFinalGradeScoreTemplate = new FinalGradeScoreCriteriaTemplate(this);
        gbFinalGradeScoreTemplate.setResourceLoader(resourceLoader);

        criteriaTemplates.put(gbItemScoreTemplate.getId(), gbItemScoreTemplate);
        criteriaTemplates.put(gbDueDatePassedTemplate.getId(), gbDueDatePassedTemplate);
        criteriaTemplates.put(gbFinalGradeScoreTemplate.getId(), gbFinalGradeScoreTemplate);

        criterionClasses.add(GreaterThanScoreCriterionHibernateImpl.class);
        criterionClasses.add(DueDatePassedCriterionHibernateImpl.class);
        criterionClasses.add(FinalGradeScoreCriterionHibernateImpl.class);

        if (certService != null)
        {
            certService.registerCriteriaFactory(this);
        }
    }

    public CertificateService getCertificateService()
    {
        return certService;
    }

    public void setCertificateService(CertificateService certService)
    {
        this.certService = certService;
    }

    public void setGradebookService(GradebookService gbs)
    {
        gbService = gbs;
    }

    public GradebookService getGradebookService()
    {
        return gbService;
    }

    public void setToolManager (ToolManager tm)
    {
        toolManager = tm;
    }

    public ToolManager getToolManager()
    {
        return toolManager;
    }

    public void setUserDirectoryService (UserDirectoryService uds)
    {
        userDirectoryService = uds;
    }

    public UserDirectoryService getUserDirectoryService()
    {
        return userDirectoryService;
    }

    public ResourceLoader getResourceLoader()
    {
        return resourceLoader;
    }

    public void setResourceLoader(ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    protected final String contextId()
    {
        return getToolManager().getCurrentPlacement().getContext();
    }

    protected final String userId()
    {
        return getUserDirectoryService().getCurrentUser().getId();
    }

    public Set<CriteriaTemplate> getCriteriaTemplates()
    {
        HashSet<CriteriaTemplate>
            values = new HashSet<CriteriaTemplate>();

        values.addAll(criteriaTemplates.values());

        return values;
    }

    public CriteriaTemplate getCriteriaTemplate(Criterion criterion)
        throws UnknownCriterionTypeException
    {
        if (GreaterThanScoreCriterionHibernateImpl.class.isAssignableFrom (criterion.getClass()))
            return gbItemScoreTemplate;
        else if (FinalGradeScoreCriterionHibernateImpl.class.isAssignableFrom(criterion.getClass()))
        	return gbFinalGradeScoreTemplate;
        else if (DueDatePassedCriterionHibernateImpl.class.isAssignableFrom (criterion.getClass()))
            return gbDueDatePassedTemplate;

        throw new UnknownCriterionTypeException(criterion.getClass().getName());

    }

    public Set<Class<? extends Criterion>> getCriterionTypes()
    {
        return criterionClasses;
    }

    public boolean isCriterionMet(Criterion criterion)
        throws UnknownCriterionTypeException
    {
        if (!criterionClasses.contains(criterion.getClass()))
            throw new UnknownCriterionTypeException (criterion.getClass().getName());

        return isCriterionMet (criterion, userId(), contextId());
    }

    protected Object doSecureGradebookAction(SecureGradebookActionCallback callback)
        throws Exception
    {
        final SessionManager
            sessionManager = getSessionManager();
        final SecurityService
            securityService = getSecurityService();

        final Session
            sakaiSession = sessionManager.getCurrentSession();
        final String
            contextId = contextId(),
            currentUserId = userId(),
            currentUserEid = getUserDirectoryService().getCurrentUser().getEid(),
            adminUser = getAdminUser();

        try
        {
            sakaiSession.setUserId(adminUser);
            sakaiSession.setUserEid(adminUser);

            securityService.pushAdvisor
                (
                    new SecurityAdvisor ()
                    {
                        public SecurityAdvice isAllowed(String userId, String function, String reference)
                        {
                            String
                                compTo = null;

                            if (contextId.startsWith("/site/"))
                            {
                                compTo = contextId;
                            }
                            else
                            {
                                compTo = "/site/" + contextId;
                            }

                            if (reference.equals(compTo) && ("gradebook.viewOwnGrades".equals(function) ||
                                                             "gradebook.editAssignments".equals(function)))
                            {
                                return SecurityAdvice.ALLOWED;
                            }
                            else
                            {
                                return SecurityAdvice.PASS;
                            }
                        }
                    }
                );

            return callback.doSecureAction();
        }
        finally
        {
            securityService.popAdvisor();

            sakaiSession.setUserId(currentUserId);
            sakaiSession.setUserEid(currentUserEid);
        }
    }

    public boolean isCriterionMet(final Criterion criterion, final String userId, final String contextId)
        throws UnknownCriterionTypeException
    {
        if (!criterionClasses.contains(criterion.getClass()))
            throw new UnknownCriterionTypeException (criterion.getClass().getName());

        if (GreaterThanScoreCriterionHibernateImpl.class.isAssignableFrom(criterion.getClass()))
        {
            GreaterThanScoreCriterionHibernateImpl
                gischi = (GreaterThanScoreCriterionHibernateImpl)criterion;
            final GradebookService
                gbs = getGradebookService();
            final Long
                itemId = gischi.getItemId();

            if (itemId == null)
            {
                //log it
                return false;
            }

            String
                score = null;

            try
            {
                score = (String) doSecureGradebookAction (new SecureGradebookActionCallback()
                {
                    public Object doSecureAction()
                    {
                        // pull the assignment from the gradebook to check the score
            Assignment
                            assn = null;


                        // actually get the assignment
                assn = gbs.getAssignment(contextId, itemId);

                        if (assn == null)
            {
                //log it
                return false;
            }

                        return gbs.getAssignmentScoreString (contextId, itemId, userId);
                    }
                });
            }
            catch (Exception e)
            {
                //log
                return false;
            }

            return (score != null && Math.round(Double.parseDouble(score)*100.0)/100.0 >= Double.parseDouble(gischi.getScore()));
        }
        else if (FinalGradeScoreCriterionHibernateImpl.class.isAssignableFrom(criterion.getClass()))
        {
        	FinalGradeScoreCriterionHibernateImpl
        		fgschi = (FinalGradeScoreCriterionHibernateImpl)criterion;
        	final CertificateService certService = getCertificateService();
        	double score = 0;
        	try
        	{
	        	score =  (Double)doSecureGradebookAction(new SecureGradebookActionCallback()
	            {
	        		public Object doSecureAction()
	        		{
	        			//TODO
	        			//get gradebook for the site
	        			//check category type
	        			// if category type is CATEGORY_TYPE_WEIGHTED_CATEGORY than it is weighted category
	        			//loop through category definitions
	        			//get assignments for each category and multiply weight of category to weight of assignment to possible points
	        			
	        			//if category type is CATEGORY_TYPE_NO_CATEGORY it does not have category 
	        			//get all assignments and add possible points
	        			
	        			//if category type is CATEGORY_TYPE_ONLY_CATEGORY than loop through category definitions
	        			//get assignments for each category and add assignments possible points
	        			
	        			Map<Long,Double> catWeights = certService.getCategoryWeights(contextId);
	        			Map<Long,Double> assgnWeights = certService.getAssignmentWeights(contextId);
	        			Map<Long,Double> assgnScores = certService.getAssignmentScores(contextId, userId);
	        			Map<Long,Double> assgnPoints = certService.getAssignmentPoints(contextId);
	        			
	        			double studentTotalScore = 0;
	        			
	        			int categoryType = certService.getCategoryType(contextId);
	        			
	        			switch(categoryType)
	        			{
	        				case GradebookService.CATEGORY_TYPE_NO_CATEGORY:
	        				{
			        			for(Map.Entry<Long, Double> assgnScore : assgnScores.entrySet())
			        			{
			        				Double score = assgnScore.getValue();
	        						studentTotalScore += score == null ? 0:score;
			        			}
			        			break;
	        				}
	        				case GradebookService.CATEGORY_TYPE_ONLY_CATEGORY:
	        				{
	        					for(Map.Entry<Long, Double> assgnScore : assgnScores.entrySet())
	        					{
	        						if(catWeights.containsKey(assgnScore.getKey()))
	        						{
	        							Double score = assgnScore.getValue();
		        						studentTotalScore += score == null ? 0:score;
	        						}
	        					}
	        					break;
	        				}
	        				case GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY:
	        				{
	        					//List<CategoryDefinition> categories = gbs.getCategoryDefinitions(contextId);
	        					//Map<String, Double> categoryWeightMap = new HashMap();
	        					
//	        					for(CategoryDefinition category : categories)
//	        					{
//        							categoryWeightMap.put(category.getName(), category.getWeight());
//	        					}
	        					
	        					for(Map.Entry<Long, Double> assgnScore : assgnScores.entrySet())
	        					{
	        						//String catName = assign.getCategoryName();
	        						if(catWeights.containsKey(assgnScore.getKey()))
	        						{
	        							//String strScore = gbs.getAssignmentScoreString(contextId, assign.getId(), userId);
		        						Double score = assgnScore.getValue(),
		        							   points = assgnPoints.get(assgnScore.getKey()),
		        							   catWeight = catWeights.get(assgnScore.getKey()),
		        							   assgnWeight = assgnWeights.get(assgnScore.getKey());
		        						
		        						studentTotalScore += 100* (((score == null) ? 0:score) /
		        											 ((points == null) ? 1:points))*
		        											 ((catWeight == null ? 1:catWeight)) *
		        											 ((assgnWeight == null ? 1:assgnWeight));	   
		        					}
	        					}
	        					break;
	        				}
	        			}
	        			
	        			return studentTotalScore;

                    }
	            });
	        	
        	}
        	catch (Exception e)
            {
                //log
                return false;
            }
        	
        	return (score >= Double.parseDouble(fgschi.getScore()));
        }
        else if (DueDatePassedCriterionHibernateImpl.class.isAssignableFrom(criterion.getClass()))
        {
            DueDatePassedCriterionHibernateImpl
                ddpchi = (DueDatePassedCriterionHibernateImpl)criterion;
            final GradebookService
                gbs = getGradebookService();
            final Long
                itemId = ddpchi.getItemId();

            if (itemId == null)
            {
                //log it
                return false;
            }

            Assignment
                assn = null;

            try
            {
                assn = (Assignment) doSecureGradebookAction(new SecureGradebookActionCallback()
                {
                    public Object doSecureAction()
                    {
                        return gbs.getAssignment(contextId, itemId);
                    }
                });
            }
            catch (Exception e)
            {
                //log it
                return false;
            }

            return (assn != null && (new Date()).compareTo(assn.getDueDate()) > 0);

        }
        else
        {
            throw new UnknownCriterionTypeException(criterion.getClass().getName());
        }
    }

    public Criterion createCriterion(CriteriaTemplate template, Map<String, String> bindings)
            throws InvalidBindingException, CriterionCreationException, UnknownCriterionTypeException
    {

        List<CriteriaTemplateVariable>
            variables = template.getTemplateVariables();
        final ResourceLoader
            rl = getResourceLoader();

        for (CriteriaTemplateVariable variable : variables)
        {
            String
                value = bindings.get(variable.getVariableKey());

            if (value == null || !variable.isValid(value))
            {
                InvalidBindingException
                     ibe = new InvalidBindingException ();

                ibe.setBindingKey(variable.getVariableKey());
                ibe.setBindingValue(value);

                ibe.setLocalizedMessage(rl.getFormattedMessage("value.invalid",
                                                                 new String[]
                                                                     {value}
                                                              )
                                        );

                throw ibe;
            }
        }

        if (GreaterThanScoreCriteriaTemplate.class.isAssignableFrom(template.getClass()))
        {
            GreaterThanScoreCriterionHibernateImpl
                criterion = new GreaterThanScoreCriterionHibernateImpl();

            Long
                itemId = new Long(bindings.get("gradebook.item"));
            GradebookService
                gbs = getGradebookService();
            String
                contextId = getToolManager().getCurrentPlacement().getContext();
            Assignment
                assn = gbs.getAssignment(contextId, itemId);
            String
                scoreStr = bindings.get("score");

            criterion.setAssignment(assn);
//            criterion.setItemId(assn.getId());
//            criterion.setItemName(assn.getName());

            double
                score = -1;

            try
            {
                score = Double.parseDouble(scoreStr);
            }
            catch (NumberFormatException nfe)
            {
                InvalidBindingException
                    ibe = new InvalidBindingException();

                ibe.setBindingKey("score");
                ibe.setBindingValue(scoreStr);

                ibe.setLocalizedMessage (rl.getFormattedMessage("value.notanumber",
                                                                new String[] {scoreStr}));

                throw ibe;
            }

            if (score < 0)
            {
                InvalidBindingException
                    ibe = new InvalidBindingException();

                ibe.setBindingKey("score");
                ibe.setBindingValue(scoreStr);

                ibe.setLocalizedMessage (rl.getFormattedMessage("value.negativenumber",
                                                                new String[] {scoreStr}));

                throw ibe;
            }

            if (score > assn.getPoints())
            {
                InvalidBindingException
                    ibe = new InvalidBindingException("" + assn.getPoints());

                ibe.setBindingKey("score");
                ibe.setBindingValue(scoreStr);

                ibe.setLocalizedMessage (rl.getFormattedMessage("value.toohigh",
                                                                new Object[] {scoreStr}));

                throw ibe;
            }

            criterion.setScore(scoreStr);

            return criterion;
        }
        else if (FinalGradeScoreCriteriaTemplate.class.isAssignableFrom(template.getClass()))
        {
        	FinalGradeScoreCriterionHibernateImpl criterion = new FinalGradeScoreCriterionHibernateImpl();
        	
         //GradebookService
         //    gbs = getGradebookService();
         String
             contextId = getToolManager().getCurrentPlacement().getContext();
         String
             scoreStr = bindings.get("score");

            //TODO
         	//List<Assignment> assignments = gbs.getAssignments(contextId);
         	Map<Long,Double> catWeights = certService.getCategoryWeights(contextId);
			//Map<Long,Double> assgnWeights = certService.getAssignmentWeights(contextId);
			Map<Long,Double> assgnPoints = certService.getAssignmentPoints(contextId);

			double totalAvailable = 0;
         	
         	int categoryType = certService.getCategoryType(contextId);
         	
         	switch(categoryType)
         	{
	         	case GradebookService.CATEGORY_TYPE_NO_CATEGORY:
	         	{
	         		for(Map.Entry<Long, Double> assgnPoint : assgnPoints.entrySet())
	         		{
	         			Double point = assgnPoint.getValue();
    					totalAvailable += point == null ? 0:point; 
	         		}
	         		break;
	         	}
	         	case GradebookService.CATEGORY_TYPE_ONLY_CATEGORY:
	         	{
	         		for(Map.Entry<Long, Double> assgnPoint : assgnPoints.entrySet())
	         		{
	         			if(catWeights.containsKey(assgnPoint.getKey()))
	         			{
	         				Double point = assgnPoint.getValue();
	    					totalAvailable += point == null ? 0:point;
	         			}
	         		}
	         		break;
	         	}
	         	case GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY:
	         	{
	         		totalAvailable = 100;
	         		/*for(Map.Entry<Long, Double> assgnPoint : assgnPoints.entrySet())
	         		{
	         			if(catWeights.containsKey(assgnPoint.getKey()))
	         			{
	         				Double catWeight = catWeights.get(assgnPoint.getKey()),
	         					   assignWeight = assgnWeights.get(assgnPoint.getKey()),
	         					   points = assgnPoint.getValue();
	         				
	         				totalAvailable += ((catWeight == null) ? 1:catWeight) *
	         								  ((assignWeight == null) ? 1:assignWeight) *
	         								  ((points == null) ? 0:points);
	         			}
	         		}*/
	         		break;
	         	}
         	}

         double
             score = -1;

         try
         {
             score = Double.parseDouble(scoreStr);
         }
         catch (NumberFormatException nfe)
         {
             InvalidBindingException
                 ibe = new InvalidBindingException();

             ibe.setBindingKey("score");
             ibe.setBindingValue(scoreStr);

             ibe.setLocalizedMessage (rl.getFormattedMessage("value.notanumber",
                                                             new String[] {scoreStr}));

             throw ibe;
         }

         if (score < 0)
         {
             InvalidBindingException
                 ibe = new InvalidBindingException();

             ibe.setBindingKey("score");
             ibe.setBindingValue(scoreStr);

             ibe.setLocalizedMessage (rl.getFormattedMessage("value.negativenumber",
                                                             new String[] {scoreStr}));

             throw ibe;
         }

         if (score > totalAvailable)
         {
             InvalidBindingException
                 ibe = new InvalidBindingException("" + totalAvailable);

             ibe.setBindingKey("score");
             ibe.setBindingValue(scoreStr);

             ibe.setLocalizedMessage (rl.getFormattedMessage("value.toohigh",
                                                             new Object[] {scoreStr}));

             throw ibe;
         }

         criterion.setScore(scoreStr);

         return criterion;
        }
        else if (DueDatePassedCriteriaTemplate.class.isAssignableFrom(template.getClass()))
        {
            DueDatePassedCriterionHibernateImpl
                criterion = new DueDatePassedCriterionHibernateImpl();

            Long
                itemId = new Long(bindings.get("gradebook.item"));
            GradebookService
                gbs = getGradebookService();
            String
                contextId = getToolManager().getCurrentPlacement().getContext();
            Assignment
                assn = gbs.getAssignment(contextId, itemId);

            criterion.setAssignment(assn);
//            criterion.setItemId(assn.getId());
//            criterion.setItemName(assn.getName());

            return criterion;
        }
        throw new UnknownCriterionTypeException (template.getClass().getName());
    }

    public CriteriaTemplate getCriteriaTemplate (String id)
        throws UnknownCriterionTypeException
    {
        CriteriaTemplate
            template = criteriaTemplates.get(id);

        if (template == null)
            throw new UnknownCriterionTypeException (id);

        return template;
    }
}