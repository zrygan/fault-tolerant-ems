package com.ems.grades;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "grades")

public class Grade {
    @Id @Column(name = "enrollment_id") private Long enrollmentId;
    @Column(name = "grade", precision = 2, scale = 1) private BigDecimal grade;
    @Column(name = "uploaded_by", nullable = false) private Long uploadedBy;
    @Column(name = "uploaded_at", nullable = false) private OffsetDateTime uploadedAt;
    
    protected Grade() {}
    
     public Grade(Long enrollmentId, BigDecimal grade, Long uploadedBy) {
        this.enrollmentId = enrollmentId;
        update(grade, uploadedBy);
    }

    public void update(BigDecimal grade, Long uploadedBy) {
        this.grade = grade;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = OffsetDateTime.now();
    }
    
    public Long getEnrolledId() { return enrollmentId; }
    public BigDecimal getGrade() { return grade; }
    public Long getUploadedBy() { return uploadedBy; }
    public OffsetDateTime getUploadedAt() { return uploadedAt; }
}
