package com.ems.courses;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {

    interface SectionRow {
        Long getId();
        String getCourseCode();
        String getCourseTitle();
        Integer getUnits();
        String getSectionCode();
        String getFacultyName();
        Integer getCapacity();
        Integer getEnrolledCount();
        Boolean getIsOpen();
        String getTerm();
    }

    String BASE = """
            SELECT s.id AS "id", c.code AS "courseCode", c.title AS "courseTitle",
                   CAST(c.units AS INTEGER) AS "units", s.section_code AS "sectionCode",
                   COALESCE(u.full_name, '') AS "facultyName",
                   CAST(s.capacity AS INTEGER) AS "capacity",
                   CAST(s.enrolled_count AS INTEGER) AS "enrolledCount",
                   s.is_open AS "isOpen", s.term AS "term"
            FROM sections s
            JOIN courses c ON c.id = s.course_id
            LEFT JOIN users u ON u.id = s.faculty_id
            """;
            
    // all sections
    @Query(value = BASE + " WHERE (:term = '' OR s.term = :term) ORDER BY c.code, s.section_code",
           nativeQuery = true)
    List<SectionRow> findSections(@Param("term") String term);
    
    // what sections a faculty teaches
   @Query(value = BASE + " WHERE s.faculty_id = :facultyId AND (:term = '' OR s.term = :term) ORDER BY c.code, s.section_code",
           nativeQuery = true)
    List<SectionRow> findByFaculty(@Param("facultyId") Long facultyId, @Param("term") String term);
    
    // get a section
    @Query(value = BASE + " WHERE s.id = :id", nativeQuery = true)
    Optional<SectionRow> findRowById(@Param("id") Long id);

    // all sections a student is enrolled in
    @Query(value = BASE + " JOIN enrollments e ON e.section_id = s.id WHERE e.student_id = :studentId ORDER BY c.code",
           nativeQuery = true)
    List<SectionRow> findByStudent(@Param("studentId") Long studentId);
}