package com.ems.courses;

import jakarta.persistence.*;

@Entity
@Table(name = "sections")

public class Section {
    @Id private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false) private Course course;

    @ManyToOne
    @JoinColumn(name = "faculty_id") private User faculty;

    private int capacity;

    @Column(name = "section_code") private String sectionCode;

    @Column(name = "enrolled_count") private int enrolledCount;

    private String term;

    @Column(name = "is_open")
    private boolean isOpen;

    public Long getId() { return id; }
    public String getCourseCode() { return course.getCode(); }
    public String getCourseName() { return course.getTitle(); }
    public int getUnits() { return course.getUnits(); }
    public String getSectionCode() { return sectionCode; }
    public String getFacultyName() { return faculty != null ? faculty.getFullName() : "TBA"; }
    public int getCapacity() { return capacity; }
    public int getEnrolledCount() { return enrolledCount; }
    public String getTerm() { return term; }
    public boolean getIsOpen() { return isOpen; }

    public void setEnrolledCount(int newEnrolledCount) {this.enrolledCount = newEnrolledCount; }
    public void setIsOpen(boolean isOpen) { this.isOpen = isOpen; }
}
