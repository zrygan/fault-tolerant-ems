package com.ems.courses;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sections")

public class Section {
    @Id private Long id;
    @Column(name = "course_code") private String courseCode;
    @Column(name = "course_title") private String courseTitle;
    private int units;
    @Column(name = "section_code") private String sectionCode;
    @Column(name = "faculty_name") private String facultyName;
    private int capacity;
    @Column(name = "enrolled_count") private int enrolledCount;
    private String term;
    
    public Long getId() { return id; }
    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseTitle; }
    public int getUnits() { return units; }
    public String getSectionCode() { return sectionCode; }
    public String getFacultyName() { return facultyName; }
    public int getCapacity() { return capacity; }
    public int getEnrolledCount() { return enrolledCount; }
    public String getTerm() { return term; }
}
