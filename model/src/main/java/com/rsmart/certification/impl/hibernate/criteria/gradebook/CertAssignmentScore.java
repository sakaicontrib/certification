package com.rsmart.certification.impl.hibernate.criteria.gradebook;

import java.util.Date;

public class CertAssignmentScore extends CertGradeRecordObject {
    private Double pointsEarned;
    private Boolean excludedFromGrade;
    private Boolean excluded;
    private Date dateRecorded;

    public CertAssignmentScore() {
        super();
    }

    /**
     * The graderId and dateRecorded properties will be set explicitly by the
     * grade manager before the database is updated.
     * @param assignment The assignment this grade record is associated with
     * @param studentId The student id for whom this grade record belongs
     * @param grade The grade, or points earned
     */
    public CertAssignmentScore(CertAssignment assignment, String studentId, Double grade) {
        super();
        this.gradableObject = assignment;
        this.studentId = studentId;
        this.pointsEarned = grade;
    }

    /**
     * @return Returns the pointsEarned
     */
    public Double getPointsEarned() {
        return pointsEarned;
    }

    /**
     * @param pointsEarned The pointsEarned to set.
     */
    public void setPointsEarned(Double pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public Boolean isExcludedFromGrade() {
        return excludedFromGrade;
    }

    public Boolean getExcludedFromGrade() {
        return excludedFromGrade;
    }

    public void setExcludedFromGrade(Boolean isExcludedFromGrade) {
        this.excludedFromGrade = isExcludedFromGrade;
    }

    public Boolean isExcluded() {
        return this.excluded;
    }

    public void setExcluded(Boolean excluded) {
        this.excluded = excluded;
    }

    public void setDateRecorded(Date dateRecorded) 
    {
        this.dateRecorded = dateRecorded;
    }

    public Date getDateRecorded()
    {
        return dateRecorded;
    }
}
