package com.ems.grades;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    interface GradeRow {
        Long getEnrollmentId();
        String getCourseCode();
        String getCourseTitle();
        String getSectionCode();
        String getTerm();
        BigDecimal getGrade(); //null wehn not graded
    }
    
    interface StudentRow {
        Long getEnrollmentId();
        String getStudentName();
        BigDecimal getCurrentGrade();
    }
    
    // get student grade based on enrollmwnt
    @Query(value = """
            SELECT e.id AS "enrollmentId", c.code AS "courseCode", c.title AS "courseTitle",
                   s.section_code AS "sectionCode", s.term AS "term", g.grade AS "grade"
            FROM enrollments e
            JOIN sections s ON s.id = e.section_id
            JOIN courses c ON c.id = s.course_id
            LEFT JOIN grades g ON g.enrollment_id = e.id
            WHERE e.student_id = :studentId
            ORDER BY s.term DESC, c.code
            """, nativeQuery = true)
    List<GradeRow> findByStudent(@Param("studentId") Long studentId);
    

}