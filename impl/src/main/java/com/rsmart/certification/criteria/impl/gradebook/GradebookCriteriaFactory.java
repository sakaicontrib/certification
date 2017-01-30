package com.rsmart.certification.criteria.impl.gradebook;

import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.CertificateService;
import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.CriteriaTemplateVariable;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.api.criteria.CriterionCreationException;
import com.rsmart.certification.api.criteria.InvalidBindingException;
import com.rsmart.certification.api.criteria.UnknownCriterionTypeException;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.DueDatePassedCriterionHibernateImpl;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.FinalGradeScoreCriterionHibernateImpl;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.GreaterThanScoreCriterionHibernateImpl;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.WillExpireCriterionHibernateImpl;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

/**
 * User: duffy
 * Date: Jun 23, 2011
 * Time: 11:44:38 AM
 */
public class GradebookCriteriaFactory implements CriteriaFactory
{
    protected final Log logger = LogFactory.getLog(getClass());
    private CertificateService certService = null;
    private GradebookService gbService = null;
    private ToolManager toolManager = null;
    private UserDirectoryService userDirectoryService = null;
    private SecurityService securityService = null;
    private SessionManager sessionManager = null;

    private HashMap<String, CriteriaTemplate> criteriaTemplates = new HashMap<String, CriteriaTemplate>();
    private HashSet<Class<? extends Criterion>> criterionClasses = new HashSet<Class<? extends Criterion>>();

    private GreaterThanScoreCriteriaTemplate gbItemScoreTemplate = null;
    private DueDatePassedCriteriaTemplate gbDueDatePassedTemplate = null;
    private FinalGradeScoreCriteriaTemplate gbFinalGradeScoreTemplate = null;
    private WillExpireCriteriaTemplate gbWillExpireTemplate = null;
    private ResourceLoader resourceLoader = null;

    private static final String PERM_VIEW_OWN_GRADES = "gradebook.viewOwnGrades";
    private static final String PERM_EDIT_ASSIGNMENTS = "gradebook.editAssignments";

    private static final String ERROR_NO_GRADEBOOK = "value.noGradebook";
    private static final String ERROR_EMPTY_GRADEBOOK = "value.emptyGradebook";
    private static final String ERROR_NO_DUE_DATES = "value.noDueDates";
    private static final String ERROR_MIN_REQUIRED = "value.minRequired";
    private static final String ERROR_EXPIRY_OFFSET_REQUIRED = "value.expiryOffsetRequired";
    private static final String ERROR_NAN = "value.notanumber";
    private static final String ERROR_NEGATIVE_NUMBER = "value.negativenumber";
    private static final String ERROR_TOO_HIGH = "value.toohigh";

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

        gbWillExpireTemplate = new WillExpireCriteriaTemplate(this);
        gbWillExpireTemplate.setResourceLoader(resourceLoader);

        criteriaTemplates.put(gbItemScoreTemplate.getId(), gbItemScoreTemplate);
        criteriaTemplates.put(gbDueDatePassedTemplate.getId(), gbDueDatePassedTemplate);
        criteriaTemplates.put(gbFinalGradeScoreTemplate.getId(), gbFinalGradeScoreTemplate);
        criteriaTemplates.put(gbWillExpireTemplate.getId(), gbWillExpireTemplate);

        criterionClasses.add(GreaterThanScoreCriterionHibernateImpl.class);
        criterionClasses.add(DueDatePassedCriterionHibernateImpl.class);
        criterionClasses.add(FinalGradeScoreCriterionHibernateImpl.class);
        criterionClasses.add(WillExpireCriterionHibernateImpl.class);

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
        HashSet<CriteriaTemplate> values = new HashSet<CriteriaTemplate>();
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
        else if (WillExpireCriterionHibernateImpl.class.isAssignableFrom (criterion.getClass()))
            return gbWillExpireTemplate;

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

    protected Object doSecureGradebookAction(SecureGradebookActionCallback callback) throws Exception
    {
        final SecurityService securityService = getSecurityService();
        final String contextId = contextId();

        SecurityAdvisor yesMan = new SecurityAdvisor()
        {
            public SecurityAdvice isAllowed(String userId, String function, String reference)
            {
                String compTo;
                if (contextId.startsWith("/site/"))
                {
                    compTo = contextId;
                }
                else
                {
                    compTo = "/site/" + contextId;
                }

                if (reference.equals(compTo) && (PERM_VIEW_OWN_GRADES.equals(function) ||
                                                 PERM_EDIT_ASSIGNMENTS.equals(function)))
                {
                    return SecurityAdvice.ALLOWED;
                }
                else
                {
                    return SecurityAdvice.PASS;
                }
            }
        };

        try
        {
            securityService.pushAdvisor( yesMan );
            return callback.doSecureAction();
        }
        finally
        {
            securityService.popAdvisor();
        }
    }

    public boolean isCriterionMet(final Criterion criterion, final String userId, final String contextId)
        throws UnknownCriterionTypeException
    {
        if (!criterionClasses.contains(criterion.getClass()))
        {
            throw new UnknownCriterionTypeException (criterion.getClass().getName());
        }

        if (GreaterThanScoreCriterionHibernateImpl.class.isAssignableFrom(criterion.getClass()))
        {
            GreaterThanScoreCriterionHibernateImpl gischi = (GreaterThanScoreCriterionHibernateImpl)criterion;
            final GradebookService gbs = getGradebookService();
            final Long itemId = gischi.getItemId();

            if (itemId == null)
            {
                logger.error("isCriterionMet called on GreaterThanScoreCriterionHibernateImpl whose gradebook itemId is null");
                return false;
            }

            String score;

            try
            {
                score = (String) doSecureGradebookAction (new SecureGradebookActionCallback()
                {
                    public Object doSecureAction()
                    {
                        // pull the assignment from the gradebook to check the score
                        Assignment assn = gbs.getAssignment(contextId, itemId);
                        if (assn == null || !assn.isReleased())
                        {
                            logger.error("isCriterionMet couldn't retrieve the assignment; itemId = " + itemId);
                            return false;
                        }

                        return gbs.getAssignmentScoreString(contextId, itemId, userId);
                    }
                });
            }
            catch (Exception e)
            {
                logger.error("isCriterionMet on GreatherThanScoreCriterion - An exception was thrown while retrieving " + userId+"'s score for itemId: " + itemId, e);
                return false;
            }

            return (score != null && Math.round(Double.parseDouble(score)*100.0)/100.0 >= Double.parseDouble(gischi.getScore()));
        }
        else if (FinalGradeScoreCriterionHibernateImpl.class.isAssignableFrom(criterion.getClass()))
        {
        	FinalGradeScoreCriterionHibernateImpl fgschi = (FinalGradeScoreCriterionHibernateImpl)criterion;
        	final CertificateService certService = getCertificateService();
        	double score = 0;

        	try
        	{
	        	score =  (Double)doSecureGradebookAction(new SecureGradebookActionCallback()
	            {
	        		public Object doSecureAction()
	        		{
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
	        					for(Map.Entry<Long, Double> assgnScore : assgnScores.entrySet())
	        					{
	        						if(catWeights.containsKey(assgnScore.getKey()))
	        						{
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
                logger.error("isCriterionMet - Exception thrown while retrieving the final course grade for " + userId +" in contextId:" + contextId, e);
                return false;
            }

            return (score >= Double.parseDouble(fgschi.getScore()));
        }
        else if (DueDatePassedCriterionHibernateImpl.class.isAssignableFrom(criterion.getClass()))
        {
            DueDatePassedCriterionHibernateImpl ddpchi = (DueDatePassedCriterionHibernateImpl)criterion;
            final GradebookService gbs = getGradebookService();
            final Long itemId = ddpchi.getItemId();

            if (itemId == null)
            {
                logger.error("isCriterionMet called with DueDatePassedCriterion where itemId is null");
                return false;
            }

            Assignment assn;
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
                logger.error("isCriterionMet on DueDatePassedCriterion - An exception was thrown while retrieving a gradebook item; itemId: " + itemId, e);
                return false;
            }

            return (assn != null && (new Date()).compareTo(assn.getDueDate()) > 0);

        }
        else if (WillExpireCriterionHibernateImpl.class.isAssignableFrom(criterion.getClass()))
        {
            //we don't want to enforce this one
            return true;
        }
        else
        {
            throw new UnknownCriterionTypeException(criterion.getClass().getName());
        }
    }

    public Criterion createCriterion(CriteriaTemplate template, Map<String, String> bindings)
            throws InvalidBindingException, CriterionCreationException, UnknownCriterionTypeException
    {
        List<CriteriaTemplateVariable> variables = template.getTemplateVariables();
        final ResourceLoader rl = getResourceLoader();
        GradebookService gbs = getGradebookService();
        String contextId = getToolManager().getCurrentPlacement().getContext();

        for (CriteriaTemplateVariable variable : variables)
        {
            String value = bindings.get(variable.getVariableKey());
            if (value == null || !variable.isValid(value))
            {
                if (template instanceof DueDatePassedCriteriaTemplate)
                {
                    if (!gbs.isGradebookDefined(contextId))
                    {
                        //This site does not have a gradebook
                        InvalidBindingException ibe = new InvalidBindingException ();
                        ibe.setBindingKey(variable.getVariableKey());
                        ibe.setBindingValue(value);
                        ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_NO_GRADEBOOK, new Object[] {value} ));
                        throw ibe;
                    }
                    else if (gbs.getAssignments(contextId).isEmpty())
                    {
                        //This is an empty gradebook
                        InvalidBindingException ibe = new InvalidBindingException ();
                        ibe.setBindingKey(variable.getVariableKey());
                        ibe.setBindingValue(value);
                        ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_EMPTY_GRADEBOOK, new Object[] {value} ));
                        throw ibe;
                    }
                    else
                    {
                        //This gradebook is not empty, but there are no items with due dates
                        InvalidBindingException ibe = new InvalidBindingException ();
                        ibe.setBindingKey(variable.getVariableKey());
                        ibe.setBindingValue(value);
                        ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_NO_DUE_DATES, new Object[] {value} ));
                        throw ibe;
                    }
                }
                else if (variable.getVariableKey().equals(KEY_SCORE))
                {
                    InvalidBindingException ibe = new InvalidBindingException ();
                    ibe.setBindingKey(variable.getVariableKey());
                    ibe.setBindingValue(value);
                    ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_MIN_REQUIRED, new Object[] {value} ));
                    throw ibe;
                }
                else if (variable.getVariableKey().equals(KEY_EXPIRY_OFFSET))
                {
                    InvalidBindingException ibe = new InvalidBindingException ();
                    ibe.setBindingKey(variable.getVariableKey());
                    ibe.setBindingValue(value);
                    ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_EXPIRY_OFFSET_REQUIRED, new Object[] {value} ));
                    throw ibe;
                }
                else if (!gbs.isGradebookDefined(contextId))
                {
                    //This site does not have a gradebook
                    InvalidBindingException ibe = new InvalidBindingException ();
                    ibe.setBindingKey(variable.getVariableKey());
                    ibe.setBindingValue(value);
                    ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_NO_GRADEBOOK, new Object[] {value} ));
                    throw ibe;
                }
                else
                {
                    InvalidBindingException ibe = new InvalidBindingException ();
                    ibe.setBindingKey(variable.getVariableKey());
                    ibe.setBindingValue(value);
                    ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_EMPTY_GRADEBOOK, new Object[] {value} ));
                    throw ibe;
                }
            }
        }

        if (GreaterThanScoreCriteriaTemplate.class.isAssignableFrom(template.getClass()))
        {
            GreaterThanScoreCriterionHibernateImpl criterion = new GreaterThanScoreCriterionHibernateImpl();
            Long itemId = new Long(bindings.get(KEY_GRADEBOOK_ITEM));
            Assignment assn = gbs.getAssignment(contextId, itemId);
            String scoreStr = bindings.get(KEY_SCORE);

            criterion.setAssignment(assn);

            double score = -1;

            try
            {
                score = Double.parseDouble(scoreStr);
            }
            catch (NumberFormatException nfe)
            {
                InvalidBindingException ibe = new InvalidBindingException();
                ibe.setBindingKey(KEY_SCORE);
                ibe.setBindingValue(scoreStr);
                ibe.setLocalizedMessage (rl.getFormattedMessage(ERROR_NAN, new Object[] {scoreStr} ));
                throw ibe;
            }

            if (score < 0)
            {
                InvalidBindingException ibe = new InvalidBindingException();
                ibe.setBindingKey(KEY_SCORE);
                ibe.setBindingValue(scoreStr);
                ibe.setLocalizedMessage (rl.getFormattedMessage(ERROR_NEGATIVE_NUMBER, new Object[] {scoreStr}));
                throw ibe;
            }

            if (score > assn.getPoints())
            {
                InvalidBindingException ibe = new InvalidBindingException("" + assn.getPoints());
                ibe.setBindingKey(KEY_SCORE);
                ibe.setBindingValue(scoreStr);

                if (assn.getPoints()==0)
                {
                    ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_EMPTY_GRADEBOOK, new Object[] {scoreStr} ));
                }
                else
                {
                    ibe.setLocalizedMessage (rl.getFormattedMessage(ERROR_TOO_HIGH, new Object[] {scoreStr}));
                }

                throw ibe;
            }

            criterion.setScore(scoreStr);

            return criterion;
        }
        else if (FinalGradeScoreCriteriaTemplate.class.isAssignableFrom(template.getClass()))
        {
            if (!gbs.isGradebookDefined(contextId))
            {
                //This site does not have a gradebook
                InvalidBindingException ibe = new InvalidBindingException ();
                ibe.setLocalizedMessage(rl.getFormattedMessage(ERROR_EMPTY_GRADEBOOK, new Object[] {} ));
                throw ibe;
            }

            FinalGradeScoreCriterionHibernateImpl criterion = new FinalGradeScoreCriterionHibernateImpl();
            String scoreStr = bindings.get(KEY_SCORE);

            Map<Long,Double> catWeights = certService.getCategoryWeights(contextId);
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
	         		break;
	         	}
         	}

         double score = -1;

         try
         {
             score = Double.parseDouble(scoreStr);
         }
         catch (NumberFormatException nfe)
         {
             InvalidBindingException ibe = new InvalidBindingException();
             ibe.setBindingKey(KEY_SCORE);
             ibe.setBindingValue(scoreStr);
             ibe.setLocalizedMessage (rl.getFormattedMessage(ERROR_NAN, new Object[] {scoreStr}));
             throw ibe;
         }

         if (score < 0)
         {
             InvalidBindingException ibe = new InvalidBindingException();
             ibe.setBindingKey(KEY_SCORE);
             ibe.setBindingValue(scoreStr);
             ibe.setLocalizedMessage (rl.getFormattedMessage(ERROR_NEGATIVE_NUMBER, new Object[] {scoreStr}));
             throw ibe;
         }

         if (score > totalAvailable)
         {
             InvalidBindingException ibe = new InvalidBindingException("" + totalAvailable);
             ibe.setBindingKey(KEY_SCORE);
             ibe.setBindingValue(scoreStr);
             ibe.setLocalizedMessage (rl.getFormattedMessage(ERROR_TOO_HIGH, new Object[] {scoreStr}));
             throw ibe;
         }

         criterion.setScore(scoreStr);

         return criterion;
        }
        else if (DueDatePassedCriteriaTemplate.class.isAssignableFrom(template.getClass()))
        {
            DueDatePassedCriterionHibernateImpl criterion = new DueDatePassedCriterionHibernateImpl();

            Long itemId = new Long(bindings.get(KEY_GRADEBOOK_ITEM));
            Assignment assn = gbs.getAssignment(contextId, itemId);

            criterion.setAssignment(assn);

            return criterion;
        }
        else if (WillExpireCriteriaTemplate.class.isAssignableFrom(template.getClass()))
        {
            WillExpireCriterionHibernateImpl criterion = new WillExpireCriterionHibernateImpl();

            String strExpiryOffset = bindings.get(KEY_EXPIRY_OFFSET);
            criterion.setExpiryOffset(strExpiryOffset);
            return criterion;
        }
        throw new UnknownCriterionTypeException (template.getClass().getName());
    }

    public CriteriaTemplate getCriteriaTemplate (String id) throws UnknownCriterionTypeException
    {
        CriteriaTemplate template = criteriaTemplates.get(id);

        if (template == null)
        {
            throw new UnknownCriterionTypeException (id);
        }

        return template;
    }

    public Double getScore(final Long itemId, final String userId, final String contextId)
    {
        final GradebookService gbs = getGradebookService();
        try
        {
            return (Double) doSecureGradebookAction (new SecureGradebookActionCallback()
            {
                public Object doSecureAction()
                {
                    // pull the assignment from the gradebook to check the score
                    Assignment assn = gbs.getAssignment(contextId, itemId);

                    if (assn == null)
                    {
                        logger.error("getScore - could not retrieve assignment for " + userId +"; itemId: " + itemId);
                        return false;
                    }

                    if (!assn.isReleased())
                    {
                        return false;
                    }

                    return gbs.getAssignmentScore (contextId, itemId, userId);
                }
            });
        }
        catch (Exception e)
        {
            logger.error ("getScore - an exception occurred while retrieving the score for " + userId +"; itemId: " + itemId, e);
            return null;
        }
    }

    public Double getFinalScore(final String userId, final String contextId)
    {
        try
        {
            final CertificateService certService = getCertificateService();
            return (Double)doSecureGradebookAction(new SecureGradebookActionCallback()
            {
                public Object doSecureAction()
                {
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
                            for(Map.Entry<Long, Double> assgnScore : assgnScores.entrySet())
                            {
                                if(catWeights.containsKey(assgnScore.getKey()))
                                {
                                    Double score = assgnScore.getValue();
                                    Double points = assgnPoints.get(assgnScore.getKey());
                                    Double catWeight = catWeights.get(assgnScore.getKey());
                                    Double assgnWeight = assgnWeights.get(assgnScore.getKey());

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
            logger.error("getFinalScore - an exception occured while retrieving the final score for " + userId + " in " + contextId, e);
            return null;
        }
    }

    public Date getDateRecorded(final Long itemId, final String userId, final String contextId)
    {
        final GradebookService gbs = getGradebookService();

        try
        {
            GradeDefinition gradeDefn = gbs.getGradeDefinitionForStudentForItem(contextId, itemId, userId);
            return gradeDefn.getDateRecorded();
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public Date getFinalGradeDateRecorded(final String userId,final String contextId)
    {
        try
        {
            final CertificateService certService = getCertificateService();
            return (Date) doSecureGradebookAction(new SecureGradebookActionCallback()
            {
                public Object doSecureAction()
                {
                    //Just following the getFinalScore code, but ignoring grades and looking at dates

                    Map<Long,Double> catWeights = certService.getCategoryWeights(contextId);
                    Map<Long,Date> assgnDates = certService.getAssignmentDatesRecorded(contextId, userId);

                    Date lastDate = null;

                    int categoryType = certService.getCategoryType(contextId);

                    switch(categoryType)
                    {
                        case GradebookService.CATEGORY_TYPE_NO_CATEGORY:
                        {
                            for(Map.Entry<Long, Date> assgnDate : assgnDates.entrySet())
                            {
                                if (lastDate==null)
                                {
                                    lastDate = assgnDate.getValue();
                                }
                                else if (assgnDate.getValue() != null)
                                {
                                    if (assgnDate.getValue().after(lastDate))
                                    {
                                        lastDate = assgnDate.getValue();
                                    }
                                }
                            }
                            break;
                        }
                        case GradebookService.CATEGORY_TYPE_ONLY_CATEGORY:
                        {
                            for(Map.Entry<Long, Date> assgnDate : assgnDates.entrySet())
                            {
                                if(catWeights.containsKey(assgnDate.getKey()))
                                {
                                    if (lastDate==null)
                                    {
                                        lastDate = assgnDate.getValue();
                                    }
                                    else if (assgnDate.getValue() != null)
                                    {
                                        if (assgnDate.getValue().after(lastDate))
                                        {
                                            lastDate = assgnDate.getValue();
                                        }
                                    }

                                }
                            }
                            break;
                        }
                        case GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY:
                        {
                            for(Map.Entry<Long, Date> assgnDate : assgnDates.entrySet())
                            {
                                if(catWeights.containsKey(assgnDate.getKey()))
                                {
                                    if (lastDate == null)
                                    {
                                        lastDate = assgnDate.getValue();
                                    }
                                    else if (assgnDate.getValue() != null)
                                    {
                                        if (assgnDate.getValue().after(lastDate))
                                        {
                                            lastDate = assgnDate.getValue();
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                    return lastDate;
                }
            });
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public Date getDateIssued(final String userId, final String contextId, CertificateDefinition certDef)
    {
        Set<Criterion> criteria = certDef.getAwardCriteria();

        //The last date in chronological order will be selected
        Date lastDate = null;

        Iterator<Criterion> itCriteria = criteria.iterator();
        while (itCriteria.hasNext())
        {
            Criterion crit = itCriteria.next();
            try
            {
                if (!isCriterionMet(crit, userId, contextId))
                {
                    return null;
                }
            }
            catch (UnknownCriterionTypeException e)
            {
                return null;
            }

            Date date = crit.getDateMet(userId, contextId);
            if (lastDate == null)
            {
                lastDate = date;
            }
            else if (date != null && date.after(lastDate))
            {
                lastDate = date;
            }
        }

        return lastDate;
    }
}
