package com.rsmart.certification.mock;

import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.CommentDefinition;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.InvalidGradeException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.service.gradebook.shared.SortType;

/**
 * User: duffy
 * Date: Jul 5, 2011
 * Time: 10:26:57 AM
 */
public class MockGradebookService
    implements GradebookService
{
    public boolean isGradebookDefined(String gradebookUid)
    {
        return true;
    }

    public boolean isUserAbleToGradeItemForStudent(String gradebookUid, Long itemId, String studentUid) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isUserAbleToGradeItemForStudent(String gradebookUid, String itemName, String studentUid) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isUserAbleToViewItemForStudent(String gradebookUid, Long itemId, String studentUid) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isUserAbleToViewItemForStudent(String gradebookUid, String itemName, String studentUid) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, Long itemId, String studentUid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, String itemName, String studentUid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List getAssignments(String gradebookUid)
        throws GradebookNotFoundException
    {
        Assignment
            assn1 = new Assignment(),
            assn2 = new Assignment(),
            assn3 = new Assignment();

        assn1.setId(new Long(1));
        assn1.setName("assignment 1");
        assn1.setPoints((double)100);
        assn1.setReleased(true);

        assn2.setId(new Long(2));
        assn2.setName("assignment 2");
        assn2.setPoints((double)150);
        assn2.setReleased(true);

        assn3.setId(new Long(3));
        assn3.setName("assignment 3");
        assn3.setPoints((double)150);
        assn3.setReleased(true);

        ArrayList
            assignments = new ArrayList(3);

        assignments.add(assn1);
        assignments.add(assn2);
        assignments.add(assn3);

        return assignments;
    }

    public Assignment getAssignment(String gradebookUid, String assignmentName) throws GradebookNotFoundException
    {
        return (Assignment)getAssignments(null).get(0);
    }

    public Assignment getAssignment(String gradebookUid, Long gbItemId) throws AssessmentNotFoundException {
        return (Assignment)getAssignments(null).get(0);
    }

    public Double getAssignmentScore(String gradebookUid, String assignmentName, String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Double getAssignmentScore(String gradebookUid, Long gbItemId, String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public GradeDefinition getGradeDefinitionForStudentForItem(String gradebookUid, Long gbItemId, String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List getGradeRecords(String gradbookUid, Collection studentUids) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List getGradeRecords(String gradbookUid, Collection studentUids, Date cutoffDate) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public CommentDefinition getAssignmentScoreComment(String gradebookUid, String assignmentName, String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public CommentDefinition getAssignmentScoreComment(String gradebookUid, Long gbItemId, String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAssignmentScore(String gradebookUid, String assignmentName, String studentUid, Double score, String clientServiceDescription) throws GradebookNotFoundException, AssessmentNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAssignmentScoreComment(String gradebookUid, String assignmentName, String studentUid, String comment) throws GradebookNotFoundException, AssessmentNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAssignmentDefined(String gradebookUid, String assignmentTitle) throws GradebookNotFoundException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getGradebookDefinitionXml(String gradebookUid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void transferGradebookDefinitionXml(String fromGradebookUid, String toGradebookUid, String fromGradebookXml) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void mergeGradebookDefinitionXml(String toGradebookUid, String fromGradebookXml) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeAssignment(Long assignmentId) throws StaleObjectModificationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public List getCategories(Long gradebookId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<CategoryDefinition> getCategoryDefinitions(String gradebookUid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeCategory(Long categoryId) throws StaleObjectModificationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Long addAssignment(String gradebookUid, Assignment assignmentDefinition) {
        return 0L;
    }

    public void updateAssignment(String gradebookUid, String assignmentName, Assignment assignmentDefinition) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Assignment> getViewableAssignmentsForCurrentUser(String gradebookUid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, String> getViewableStudentsForItemForCurrentUser(String gradebookUid, Long gradableObjectId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, String> getViewableStudentsForItemForUser(String userUid, String gradebookUid, Long gradableObjectId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addGradebook(String uid, String name) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void deleteGradebook(String uid) throws GradebookNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAvailableGradingScales(Collection gradingScaleDefinitions) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setDefaultGradingScale(String uid) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addExternalAssessment(String gradebookUid, String externalId, String externalUrl, String title, Double points, Date dueDate, String externalServiceDescription, Boolean ungraded) throws GradebookNotFoundException, ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addExternalAssessment(String gradebookUid, String externalId, String externalUrl, String title, double points, Date dueDate, String externalServiceDescription) throws GradebookNotFoundException, ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl, String title, double points, Date dueDate) throws GradebookNotFoundException, AssessmentNotFoundException, ConflictingAssignmentNameException, AssignmentHasIllegalPointsException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl, String title, Double points, Date dueDate) throws GradebookNotFoundException, AssessmentNotFoundException, ConflictingAssignmentNameException, AssignmentHasIllegalPointsException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeExternalAssessment(String gradebookUid, String externalId) throws GradebookNotFoundException, AssessmentNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateExternalAssessmentScore(String gradebookUid, String externalId, String studentUid, Double points) throws GradebookNotFoundException, AssessmentNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateExternalAssessmentScores(String gradebookUid, String externalId, Map studentUidsToScores) throws GradebookNotFoundException, AssessmentNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isExternalAssignmentDefined(String gradebookUid, String externalId) throws GradebookNotFoundException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getImportCourseGrade(String gradebookUid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getGradebook(String uid) throws GradebookNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean checkStuendsNotSubmitted(String gradebookUid) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean checkStudentsNotSubmitted(String gradebookUid) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isGradableObjectDefined(Long gradableObjectId) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getViewableSectionUuidToNameMap(String gradebookUid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean currentUserHasGradeAllPerm(String gradebookUid) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isUserAllowedToGradeAll(String gradebookUid, String userUid) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean currentUserHasGradingPerm(String gradebookUid) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isUserAllowedToGrade(String gradebookUid, String userUid) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean currentUserHasEditPerm(String gradebookUid) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean currentUserHasViewOwnGradesPerm(String gradebookUid) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<GradeDefinition> getGradesForStudentsForItem(String gradebookUid, Long gradableObjectId, List<String> studentIds) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isGradeValid(String gradebookUuid, String grade) throws GradebookNotFoundException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<String> identifyStudentsWithInvalidGrades(String gradebookUid, Map<String, String> studentIdToGradeMap) throws GradebookNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void saveGradeAndCommentForStudent(String gradebookUid, Long gradableObjectId, String studentId, String grade, String comment) throws InvalidGradeException, GradebookNotFoundException, AssessmentNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void saveGradesAndComments(String gradebookUid, Long gradableObjectId, List<GradeDefinition> gradeDefList) throws InvalidGradeException, GradebookNotFoundException, AssessmentNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getFixedGrade(String gradebookUid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getFixedPoint(String gradebookUid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getOldPoint(String gradebookUid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getGradeEntryType(String gradebookUid) throws GradebookNotFoundException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getEnteredCourseGrade(String gradebookUid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getCalculatedCourseGrade(String gradebookUid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getAssignmentScoreString(String gradebookUid, String assignmentName, String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException {
        return "75";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getAssignmentScoreString(String gradebookUid, Long gbItemId, String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException {
        return "75";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAssignmentScoreString(String gradebookUid, String assignmentName, String studentUid, String score, String clientServiceDescription) throws GradebookNotFoundException, AssessmentNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void finalizeGrades(String gradebookUid) throws GradebookNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getLowestPossibleGradeForGbItem(String gradebookUid, Long gradebookItemId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getCourseGrade(Long gradebookId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean canBeGraded(String userId, String siteId) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Assignment> getAssignments(String gradebookUid, SortType sortBy) throws GradebookNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAssignmentScoreComment(String gradebookUid, Long assignmentId, String studentUid, String comment) throws GradebookNotFoundException, AssessmentNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void transferGradebook(GradebookInformation gradebookInformation, List<Assignment> assignments, String toGradebookUid)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GradebookInformation getGradebookInformation(String gradebookUid)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateAssignment(String gradebookUid, Long assignmentId, Assignment assignmentDefinition)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Assignment> getViewableAssignmentsForCurrentUser(String gradebookUid, SortType sortBy)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<String, String> getImportCourseGrade(String gradebookUid, boolean useDefault)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<String, String> getImportCourseGrade(String gradebookUid, boolean useDefault, boolean mapTheGrades)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Long, List<GradeDefinition>> getGradesWithoutCommentsForStudentsForItems(String gradebookUid, List<Long> gradableOjbectIds, List<String> studentIds)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAssignmentScoreString(String gradebookUid, Long assignmentId, String studentUid, String score, String clientServiceDescription) throws GradebookNotFoundException, AssessmentNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PointsPossibleValidation isPointsPossibleValid(String gradebookUid, Assignment assignment, Double pointsPossible)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getAverageCourseGrade(String gradebookUid)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateAssignmentOrder(String gradebookUid, Long assignmentId, Integer order)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List getGradingEvents(String studentId, long assignmentId)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Double calculateCategoryScore(Long gradebookId, String studentUuid, Long categoryId)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Double calculateCategoryScore(Object gradebook, String studentUuid, CategoryDefinition category, List<Assignment> categoryAssignments, Map<Long, String> gradeMap)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CourseGrade getCourseGradeForStudent(String gradebookUid, String userUuid)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<String, CourseGrade> getCourseGradeForStudents(String gradebookUid, List<String> userUuids)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List getViewableSections(String gradebookUid)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateGradebookSettings(String gradebookUid, GradebookInformation gbInfo)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set getGradebookGradeMappings(Long gradebookId)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set getGradebookGradeMappings(String gradebookUid)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateCourseGradeForStudent(String gradebookUid, String studentUuid, String grade)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateAssignmentCategorizedOrder(String gradebookUid, Long categoryId, Long assignmentId, Integer order)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List getGradingEvents(List<Long> assignmentIds, Date since)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
