package com.rsmart.certification.api;

import com.rsmart.certification.api.criteria.CriterionCreationException;
import com.rsmart.certification.api.criteria.UnknownCriterionTypeException;
import com.rsmart.certification.api.criteria.CriteriaFactory;
import com.rsmart.certification.api.criteria.CriteriaTemplate;
import com.rsmart.certification.api.criteria.Criterion;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * This service manages the creation, update, and retrieval of CertificateDefinitions as well as the award of
 * certificates to users through the management of CertificateAward objects.
 *
 * The CertificateService depends on two other services to complete its work:
 *
 *  ConditionService is an implementation of an interface from the org.sakaiproject.condition.api from the
 *  sakai-kernel-api and is used to establish and check conditions for certificate award.
 *
 *  DocumentTemplateService is used to manage DocumentTemplate objects and to use those objects to render
 *  printable certificates.
 *
 * User: duffy
 * Date: Jun 7, 2011
 * Time: 4:40:50 PM
 */
public interface CertificateService
{
    public CertificateDefinition createCertificateDefinition (CertificateDefinition cd) throws IdUsedException;

    public CertificateDefinition updateCertificateDefinition (CertificateDefinition cd) throws IdUnusedException;
    
    public void setDocumentTemplateService (DocumentTemplateService dts);

    public DocumentTemplateService getDocumentTemplateService();

    public ContentHostingService getContentHostingService();

    public void deleteCertificateDefinition (String certificateDefinitionId)
        throws IdUnusedException, DocumentTemplateException;

    /**
     * Creates a CertificateDefinition with the minimal ammount of information required to store the object and
     * ensure it is unique. The CertificateDefinition must have a unique (name, siteId) combination. It will be
     * created in INCOMPLETE status. A call to activateCertificateDefinition(cd, true) will be required to validate
     * the final configuration of the CertificateDefinition and to set its status to ACTIVE.
     *
     * @param name
     * @param description
     * @param siteId
     *
     * @throws IdUsedException if the name is already in use for the given site.
     *
     * @return a new CertificateDefinition object
     */
    public CertificateDefinition createCertificateDefinition (String name, String description, String siteId)
        throws IdUsedException;

    public CertificateDefinition createCertificateDefinition (String name, String description, String siteId,
                                                              String fileName, String mimeType, InputStream template)
        throws IdUsedException, UnsupportedTemplateTypeException, DocumentTemplateException;

    public CertificateDefinition createCertificateDefinition (String name, String description, String siteId,
                                                              String fileName, String mimeType, InputStream template, 
                                                              String expiryOffset)
        throws IdUsedException, UnsupportedTemplateTypeException, DocumentTemplateException;

    /**
     * Populates the DocumentTemplate object for this CertificateDefinition.
     *
     * @param certificateDefinitionId
     * @param mimeType
     * @param template
     *
     * @return the new DocumentTemplate object
     */
    public DocumentTemplate setDocumentTemplate (String certificateDefinitionId, String name, String mimeType,
                                                 InputStream template)
        throws IdUnusedException, UnsupportedTemplateTypeException, DocumentTemplateException;

    /**
     * Populates the DocumentTemplate object for this CertificateDefinition. An attempt is made to determine the mime
     * type from the input stream.
     *
     * @param certificateDefinitionId
     * @param template
     *
     * @return the new DocumentTemplate object
     */
    public DocumentTemplate setDocumentTemplate (String certificateDefinitionId, String name, InputStream template)
        throws IdUnusedException, UnsupportedTemplateTypeException, DocumentTemplateException;

    /**
     * Gets inputstream for the DocumentTemplate set in content resource using the resourceId
     * @param resourceId
     * @return
     * @throws TemplateReadException
     */
    public InputStream getTemplateFileInputStream(String resourceId)
		throws TemplateReadException;
	
    /**
     * Sets the values to be used when populating the fields for the template during rendering of the printable
     * certificate.
     * 
     * @param certificateDefinitionId
     * @param fieldValues
     * @throws IdUnusedException
     */
    public void setFieldValues (String certificateDefinitionId, Map<String, String> fieldValues)
        throws IdUnusedException;

    /**
     * This sets the CertificateDefinitionStatus to ACTIVE or INACTIVE depending on the value of the boolean 'active'
     * parameter passed in. This method will validate whether the CertificateDefinition is complete before setting its
     * status. If the DocumentTemplate or AwardCriteria are null the status will be set to INCOMPLETE and an
     * IncompleteCertificateDefinitionException will be thrown.
     *
     * @param certificateDefinitionId
     * @param active
     *
     * throws IncompleteCertificateDefinitionException
     */
    public void activateCertificateDefinition (String certificateDefinitionId, boolean active)
            throws IncompleteCertificateDefinitionException, IdUnusedException;

    public CertificateDefinition getCertificateDefinitionByName (String siteId, String name)
        throws IdUnusedException;
    
    public CertificateDefinition getCertificateDefinition (String id)
        throws IdUnusedException;

    /**
     * @return All CertificateDefinition objects.
     */
    public Set<CertificateDefinition> getCertificateDefinitions ();

    /**
     * @param siteId
     * @return All CertificateDefinition objects for the given siteId.
     */
    public Set<CertificateDefinition> getCertificateDefinitionsForSite (String siteId);

    /**
     * @param siteId
     * @param statuses
     * @return All CertificateDefinition objects for the given siteId filtered by the supplied statuses.
     */
    public Set<CertificateDefinition> getCertificateDefinitionsForSite (String siteId, CertificateDefinitionStatus statuses[]);

    /**
     * This sets the AwardCriteria for the identified CertificateDefinition by using the supplied conditions. The
     * conditions Set can be populated with Conditions created by the ConditionService available from the
     * getConditionService() method.
     *
     * If the CertificateDefinition already has AwardCriteria set this method has the side effect of incrementing the
     * revision number so CertificateAward objects can be tracked to specific AwardCriteria.
     *
     * @param certificateDefinitionId
     * @param conditions
     * @return the new AwardCriteria object
     */
    public void setAwardCriteria (String certificateDefinitionId, Set<Criterion> conditions)
            throws IdUnusedException, UnmodifiableCertificateDefinitionException;

    public Criterion addAwardCriterion (String certificateDefinitionId, Criterion criterion)
            throws IdUnusedException, UnmodifiableCertificateDefinitionException;

    public void removeAwardCriterion (String certificateDefinitionId, String criterionId)
    		throws IdUnusedException, UnmodifiableCertificateDefinitionException;
    /**
     * This checks the current user's progress on AwardCriteria for a CertificateDefinition without the side effect
     * of actually awarding the certificate as would be the case with a call to getCertificateAward(...).
     *
     * @param certificateDefinitionId
     * @return the Conditions which the current user has not met for the supplied CertificateDefinition ID.
     */
    public Set<Criterion> getUnmetAwardConditions (String certificateDefinitionId)
            throws IdUnusedException, UnknownCriterionTypeException;

    /**
     * This checks the identified user's progress on AwardCriteria for a CertificateDefinition without the side effect
     * of actually awarding the certificate as would be the case with a call to getCertificateAward(...).
     *
     * @param certificateDefinitionId
     * @return the Conditions which the current user has not met for the supplied CertificateDefinition ID.
     */
    public Set<Criterion> getUnmetAwardConditionsForUser (String certificateDefinitionId, String userId)
            throws IdUnusedException, UnknownCriterionTypeException;

    public CertificateAward awardCertificate (String certificateDefinitionId)
            throws IdUnusedException, UnmetCriteriaException, UnknownCriterionTypeException;

    public CertificateAward awardCertificate (String certificateDefinitionId, String userId)
            throws IdUnusedException, UnmetCriteriaException, UnknownCriterionTypeException;

    /**
     * This looks for an existing CertificateAward for the current user. If no CertificateAward is found the
     * AwardCriteria are checked to see if the user has earned the certificate. If so a new CertificateAward is
     * created and returned. If not an UnmetCriteriaException is thrown containing the Set of Conditions which
     * failed.
     *
     * @param certificateDefinitionId
     * @return
     * @throws UnmetCriteriaException
     */
    public CertificateAward getCertificateAward (String certificateDefinitionId)
        throws IdUnusedException;

    /**
     * @return All CertificateAward objects
     */
    public Set<CertificateAward> getCertificateAwards();

    /**
     * This looks for an existing CertificateAward for the given user. If no CertificateAward is found the
     * AwardCriteria are checked to see if the user has earned the certificate. If so a new CertificateAward is
     * created and returned. If not an UnmetCriteriaException is thrown containing the Set of Conditions which
     * failed.
     *
     * @param certificateDefinitionId
     * @param userId
     * @return
     * @throws UnmetCriteriaException
     */
    public CertificateAward getCertificateAwardForUser (String certificateDefinitionId, String userId)
            throws IdUnusedException;

    /**
     * @return All CertificateAward objects for the given certificateDefinitionId
     */
    public Set<CertificateAward> getCertificateAwards(String certificateDefinitionId)
        throws IdUnusedException;

    /**
     * This method queries for CertificateAward objects for the current user for all of
     * the certificateDefinitionIds supplied. It will return a Map which relates those
     * IDs to the CertificateAwards. If a user has not received a CertificateAward for
     * a particular certificateDefinitionId that id <b>will not</b> appear in the returned
     * Map.
     *
     * @param certificateDefinitionIds
     * @return A Map from certificateDefinitionId to CertificateAward for the current user
     * @throws IdUnusedException if one fo the certificateDefinitionIds is invalid
     */
    public Map<String, CertificateAward> getCertificateAwardsForUser (String certificateDefinitionIds[])
        throws IdUnusedException;

    /**
     * @return All CertificateAward objects for the given userId
     */
    public Set<CertificateAward> getCertificateAwardsForUser (String userId);

    /**
     * Returns a Map whose key values are variable names that can be used to fill in template fields. The values in the
     * map contain text describing what the variable will be populated by during rendering.
     *
     * @return
     */
    public Map<String, String> getPredefinedTemplateVariables ();

    public InputStream getPrintableCertificateRendering (String certificateAwardId) throws IdUnusedException;

    public void registerCriteriaFactory (CriteriaFactory cFact);

    public Set<CriteriaTemplate> getCriteriaTemplates();

    public CertificateDefinition duplicateCertificateDefinition (String certificateDefinitionId) 
        throws IdUnusedException, IdUsedException, DocumentTemplateException, UnknownCriterionTypeException, CriterionCreationException;

    public CriteriaFactory getCriteriaFactory (String criteriaTemplateId);

    public int getCategoryType(final String gradebookId);
    
    public Map<Long, Double> getCategoryWeights(final String gradebookId);
    
	public Map<Long,Double> getAssignmentWeights(final String gradebookId);
	
	public Map<Long,Double> getAssignmentPoints(final String gradebookId);
	
	public Map<Long, Double> getCatOnlyAssignmentPoints(final String gradebookId);
	
	public Map<Long,Double> getAssignmentScores(final String gradebookId, final String studentId);
}
