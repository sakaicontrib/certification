package com.rsmart.certification.impl.hibernate.criteria.gradebook;

import java.io.Serializable;

public class CertGradeRecordObject implements Serializable {
    protected Long id;
    protected String studentId;
    protected CertGradebookObject gradableObject;

	/**
	 * @return Returns the id.
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return Returns the studentId.
	 */
	public String getStudentId() {
		return studentId;
	}
	/**
	 * @param studentId The studentId to set.
	 */
	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}
	public CertGradebookObject getGradableObject() {
		return gradableObject;
	}
	public void setGradableObject(CertGradebookObject gradableObject) {
		this.gradableObject = gradableObject;
	}

}



