package org.sakaiproject.certification.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.CategoryScoreData;
import org.sakaiproject.service.gradebook.shared.CommentDefinition;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.InvalidGradeException;
import org.sakaiproject.service.gradebook.shared.SortType;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;

public class MockGradebookService implements GradebookService
{
    @Override
    public Optional<CategoryScoreData> calculateCategoryScore( Long gradebookId, String studentUuid, Long categoryId, boolean includeNonReleasedItems )
    {
        return Optional.empty();
    }

    @Override
    public Optional<CategoryScoreData> calculateCategoryScore( Object gradebook, String studentUuid, CategoryDefinition category, List<Assignment> categoryAssignments, Map<Long, String> gradeMap, boolean includeNonReleasedItems )
    {
        return Optional.empty();
    }

    @Override
    public Map<String, CourseGrade> getCourseGradeForStudents(String gradebookUid, List<String> userUuids, Map<String, Double> schema)
    {
        return null;
    }

    @Override
    public Assignment getExternalAssignment( String gradebookUid, String externalId ) throws GradebookNotFoundException
    {
        return null;
    }

    public boolean isGradebookDefined(String gradebookUid)
    {
        return true;
    }

    public boolean isUserAbleToGradeItemForStudent(String gradebookUid, Long itemId, String studentUid)
    {
        return false;
    }

    public boolean isUserAbleToGradeItemForStudent(String gradebookUid, String itemName, String studentUid)
    {
        return false;
    }

    public boolean isUserAbleToViewItemForStudent(String gradebookUid, Long itemId, String studentUid)
    {
        return false;
    }

    public boolean isUserAbleToViewItemForStudent(String gradebookUid, String itemName, String studentUid)
    {
        return false;
    }

    public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, Long itemId, String studentUid)
    {
        return null;
    }

    public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, String itemName, String studentUid)
   {
        return null;
    }

    public List getAssignments(String gradebookUid) throws GradebookNotFoundException
    {
        Assignment assn1 = new Assignment();
        Assignment assn2 = new Assignment();
        Assignment assn3 = new Assignment();

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

        ArrayList assignments = new ArrayList(3);

        assignments.add(assn1);
        assignments.add(assn2);
        assignments.add(assn3);

        return assignments;
    }

    public Assignment getAssignment(String gradebookUid, String assignmentName) throws GradebookNotFoundException
    {
        return (Assignment)getAssignments(null).get(0);
    }

    public Assignment getAssignment(String gradebookUid, Long gbItemId) throws AssessmentNotFoundException
    {
        return (Assignment)getAssignments(null).get(0);
    }

    public Double getAssignmentScore(String gradebookUid, String assignmentName, String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException
    {
        return null;
    }

    public Double getAssignmentScore(String gradebookUid, Long gbItemId, String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException
    {
        return null;
    }

    public GradeDefinition getGradeDefinitionForStudentForItem(String gradebookUid, Long gbItemId, String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException
    {
        return null;
    }

    public List getGradeRecords(String gradbookUid, Collection studentUids)
    {
        return null;
    }

    public List getGradeRecords(String gradbookUid, Collection studentUids, Date cutoffDate)
    {
        return null;
    }

    public CommentDefinition getAssignmentScoreComment(String gradebookUid, String assignmentName, String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException
    {
        return null;
    }

    public CommentDefinition getAssignmentScoreComment(String gradebookUid, Long gbItemId, String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException
    {
        return null;
    }

    @Override
    public boolean getIsAssignmentExcused(String s, Long aLong, String s1) throws GradebookNotFoundException, AssessmentNotFoundException {
        return false;
    }

    @Override
    public boolean isValidNumericGrade(String grade)
    {
        return false;
    }

    public void setAssignmentScore(String gradebookUid, String assignmentName, String studentUid, Double score, String clientServiceDescription) throws GradebookNotFoundException, AssessmentNotFoundException {}

    public void setAssignmentScoreComment(String gradebookUid, String assignmentName, String studentUid, String comment) throws GradebookNotFoundException, AssessmentNotFoundException {}

    public boolean isAssignmentDefined(String gradebookUid, String assignmentTitle) throws GradebookNotFoundException
    {
        return false;
    }

    public String getGradebookDefinitionXml(String gradebookUid)
    {
        return null;
    }

    @Override
    public Map<String, String> transferGradebook( GradebookInformation gradebookInformation, List<Assignment> assignments, String toGradebookUid, String fromContext )
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    public void transferGradebookDefinitionXml(String fromGradebookUid, String toGradebookUid, String fromGradebookXml) {}

    public void mergeGradebookDefinitionXml(String toGradebookUid, String fromGradebookXml) {}

    public void removeAssignment(Long assignmentId) throws StaleObjectModificationException {}

    public List getCategories(Long gradebookId)
   {
        return null;
    }

    public List<CategoryDefinition> getCategoryDefinitions(String gradebookUid)
    {
        return null;
    }

    public void removeCategory(Long categoryId) throws StaleObjectModificationException {}

    public Long addAssignment(String gradebookUid, Assignment assignmentDefinition)
    {
        return 0L;
    }

    public void updateAssignment(String gradebookUid, String assignmentName, Assignment assignmentDefinition) {}

    public List<Assignment> getViewableAssignmentsForCurrentUser(String gradebookUid)
    {
        return null;
    }

    public Map<String, String> getViewableStudentsForItemForCurrentUser(String gradebookUid, Long gradableObjectId)
    {
        return null;
    }

    public Map<String, String> getViewableStudentsForItemForUser(String userUid, String gradebookUid, Long gradableObjectId)
    {
        return null;
    }

    public void addGradebook(String uid, String name) {}

    public void deleteGradebook(String uid) throws GradebookNotFoundException {}

    public void setAvailableGradingScales(Collection gradingScaleDefinitions) {}

    public void setDefaultGradingScale(String uid) {}

    public void addExternalAssessment(String gradebookUid, String externalId, String externalUrl, String title, Double points, Date dueDate, String externalServiceDescription, Boolean ungraded) throws GradebookNotFoundException, ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException {}

    public void addExternalAssessment(String gradebookUid, String externalId, String externalUrl, String title, double points, Date dueDate, String externalServiceDescription) throws GradebookNotFoundException, ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException {}

    public void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl, String title, double points, Date dueDate) throws GradebookNotFoundException, AssessmentNotFoundException, ConflictingAssignmentNameException, AssignmentHasIllegalPointsException {}

    public void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl, String title, Double points, Date dueDate) throws GradebookNotFoundException, AssessmentNotFoundException, ConflictingAssignmentNameException, AssignmentHasIllegalPointsException {}

    public void removeExternalAssessment(String gradebookUid, String externalId) throws GradebookNotFoundException, AssessmentNotFoundException {}

    public void updateExternalAssessmentScore(String gradebookUid, String externalId, String studentUid, Double points) throws GradebookNotFoundException, AssessmentNotFoundException {}

    public void updateExternalAssessmentScores(String gradebookUid, String externalId, Map studentUidsToScores) throws GradebookNotFoundException, AssessmentNotFoundException {}

    public boolean isExternalAssignmentDefined(String gradebookUid, String externalId) throws GradebookNotFoundException
    {
        return false;
    }

    public Map getImportCourseGrade(String gradebookUid)
    {
        return null;
    }

    public Object getGradebook(String uid) throws GradebookNotFoundException
    {
        return null;
    }

    public boolean checkStuendsNotSubmitted(String gradebookUid)
    {
        return false;
    }

    public boolean checkStudentsNotSubmitted(String gradebookUid)
    {
        return false;
    }

    public boolean isGradableObjectDefined(Long gradableObjectId)
    {
        return false;
    }

    public Map getViewableSectionUuidToNameMap(String gradebookUid)
    {
        return null;
    }

    public boolean currentUserHasGradeAllPerm(String gradebookUid)
    {
        return false;
    }

    public boolean isUserAllowedToGradeAll(String gradebookUid, String userUid)
    {
        return false;
    }

    public boolean currentUserHasGradingPerm(String gradebookUid)
    {
        return false;
    }

    public boolean isUserAllowedToGrade(String gradebookUid, String userUid)
    {
        return false;
    }

    public boolean currentUserHasEditPerm(String gradebookUid)
    {
        return false;
    }

    public boolean currentUserHasViewOwnGradesPerm(String gradebookUid)
    {
        return false;
    }

    public List<GradeDefinition> getGradesForStudentsForItem(String gradebookUid, Long gradableObjectId, List<String> studentIds)
    {
        return null;
    }

    public boolean isGradeValid(String gradebookUuid, String grade) throws GradebookNotFoundException
    {
        return false;
    }

    public List<String> identifyStudentsWithInvalidGrades(String gradebookUid, Map<String, String> studentIdToGradeMap) throws GradebookNotFoundException
    {
        return null;
    }

    public void saveGradeAndCommentForStudent(String gradebookUid, Long gradableObjectId, String studentId, String grade, String comment) throws InvalidGradeException, GradebookNotFoundException, AssessmentNotFoundException {}

    public void saveGradesAndComments(String gradebookUid, Long gradableObjectId, List<GradeDefinition> gradeDefList) throws InvalidGradeException, GradebookNotFoundException, AssessmentNotFoundException {}

    @Override
    public void saveGradeAndExcuseForStudent(String s, Long aLong, String s1, String s2, boolean b) throws InvalidGradeException, GradebookNotFoundException, AssessmentNotFoundException {

    }

    public Map getFixedGrade(String gradebookUid)
    {
        return null;
    }

    public Map getFixedPoint(String gradebookUid)
    {
        return null;
    }

    public Map getOldPoint(String gradebookUid)
    {
        return null;
    }

    public int getGradeEntryType(String gradebookUid) throws GradebookNotFoundException
    {
        return 0;
    }

    public Map getEnteredCourseGrade(String gradebookUid)
    {
        return null;
    }

    public Map getCalculatedCourseGrade(String gradebookUid)
    {
        return null;
    }

    public String getAssignmentScoreString(String gradebookUid, String assignmentName, String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException
    {
        return "75";
    }

    public String getAssignmentScoreString(String gradebookUid, Long gbItemId, String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException
    {
        return "75";
    }

    public void setAssignmentScoreString(String gradebookUid, String assignmentName, String studentUid, String score, String clientServiceDescription) throws GradebookNotFoundException, AssessmentNotFoundException {}

    public void finalizeGrades(String gradebookUid) throws GradebookNotFoundException {}

    public String getLowestPossibleGradeForGbItem(String gradebookUid, Long gradebookItemId)
    {
        return null;
    }

    public Object getCourseGrade(Long gradebookId)
    {
        return null;
    }

    public boolean canBeGraded(String userId, String siteId)
    {
        return false;
    }

    @Override
    public List<Assignment> getAssignments(String gradebookUid, SortType sortBy) throws GradebookNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setAssignmentScoreComment(String gradebookUid, Long assignmentId, String studentUid, String comment) throws GradebookNotFoundException, AssessmentNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GradebookInformation getGradebookInformation(String gradebookUid)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateAssignment(String gradebookUid, Long assignmentId, Assignment assignmentDefinition)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Assignment> getViewableAssignmentsForCurrentUser(String gradebookUid, SortType sortBy)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<Long, List<GradeDefinition>> getGradesWithoutCommentsForStudentsForItems(String gradebookUid, List<Long> gradableOjbectIds, List<String> studentIds)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setAssignmentScoreString(String gradebookUid, Long assignmentId, String studentUid, String score, String clientServiceDescription) throws GradebookNotFoundException, AssessmentNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PointsPossibleValidation isPointsPossibleValid(String gradebookUid, Assignment assignment, Double pointsPossible)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getAverageCourseGrade(String gradebookUid)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateAssignmentOrder(String gradebookUid, Long assignmentId, Integer order)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List getGradingEvents(String studentId, long assignmentId)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CourseGrade getCourseGradeForStudent(String gradebookUid, String userUuid)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, CourseGrade> getCourseGradeForStudents(String gradebookUid, List<String> userUuids)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List getViewableSections(String gradebookUid)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateGradebookSettings(String gradebookUid, GradebookInformation gbInfo)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set getGradebookGradeMappings(Long gradebookId)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set getGradebookGradeMappings(String gradebookUid)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateCourseGradeForStudent(String gradebookUid, String studentUuid, String grade)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateAssignmentCategorizedOrder(String gradebookUid, Long categoryId, Long assignmentId, Integer order)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List getGradingEvents(List<Long> assignmentIds, Date since)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getAssignmentScoreStringByNameOrId(String gradebookUid, String assignmentName, String studentUid)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean currentUserHasViewStudentNumbersPerm(String gradebookUid)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Assignment getAssignmentByNameOrId(String gradebookUid, String assignmentName)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
