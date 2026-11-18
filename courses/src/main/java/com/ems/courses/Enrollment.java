package com.ems.courses;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "enrollments")

public class Enrollment {
    @Id private Long id;
    @Column(name="student_id", nullable=false) private Long studentId;
    @Column(name="section_id", nullable=false) private Long sectionId;
    @Column(name = "enrolled_at", nullable = false) private OffsetDateTime enrolledAt;

    protected Enrollment() {}

    public Long getId() { return id; }
    public Long getStudentId() { return studentId; }
    public Long getSectionId() { return sectionId; }
    public OffsetDateTime getEnrolledAt() { return enrolledAt; }
    public void setStudentId(Long newStudent) { this.studentId = newStudent; }
    public void setSectionId(Long newSection) { this.sectionId = newSection; }
    public void setEnrolledAt(OffsetDateTime enrolledAt) { this.enrolledAt = enrolledAt; }
}
