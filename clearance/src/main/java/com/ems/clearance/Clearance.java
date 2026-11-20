package com.ems.clearance;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "clearances")

public class Clearance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="student_id", nullable=false) private Long studentId;
    @Column(name="admin_id", nullable=false) private Long adminId;
    @Column(name="hold_type", nullable=false) private String holdType;
    @Column(name="placed_on", nullable=false) private OffsetDateTime placedOn = OffsetDateTime.now();
    @Column(name="resolved_on", nullable=true) private OffsetDateTime resolvedOn;

    protected Clearance() {}

    public Clearance(Long studentId, Long adminId, String holdType) {
        this.studentId = studentId;
        this.adminId = adminId;
        this.holdType = holdType;
        this.placedOn = OffsetDateTime.now();
    }

    public void resolve() {
        this.resolvedOn = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Long getStudentId() { return studentId; }
    public Long getAdminId() { return adminId; }
    public String getHoldType() { return holdType; }
    public OffsetDateTime getPlacedOn() { return placedOn; }
    public OffsetDateTime getResolvedOn() { return resolvedOn; }
    public void setResolvedOn(OffsetDateTime resolvedOn) { this.resolvedOn = resolvedOn; }
}
