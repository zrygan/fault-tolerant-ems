package com.ems.grades;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

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
    
    // get list of students in a section faculty teaches
    @Query(value = """
            SELECT e.id AS "enrollmentId", u.full_name AS "studentName", g.grade AS "currentGrade"
            FROM enrollments e
            JOIN sections s ON s.id = e.section_id
            JOIN users u ON u.id = e.student_id
            LEFT JOIN grades g ON g.enrollment_id = e.id
            WHERE e.section_id = :sectionId AND s.faculty_id = :facultyId
            ORDER BY u.full_name
            """, nativeQuery = true)
    List<StudentRow> findGradableStudents(@Param("sectionId") Long sectionId,
                                          @Param("facultyId") Long facultyId);
    
    // check if faculty member teaches specific section
    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM enrollments e
                JOIN sections s ON s.id = e.section_id
                WHERE e.id = :enrollmentId AND s.faculty_id = :facultyId
            )
            """, nativeQuery = true)
    boolean facultyTeaches(@Param("enrollmentId") Long enrollmentId,
                           @Param("facultyId") Long facultyId);

    // grade insert or modification
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO grades (enrollment_id, grade, uploaded_by, uploaded_at)
            VALUES (:enrollmentId, :grade, :uploadedBy, now())
            ON CONFLICT (enrollment_id)
            DO UPDATE SET grade = EXCLUDED.grade,
                          uploaded_by = EXCLUDED.uploaded_by,
                          uploaded_at = now()
            """, nativeQuery = true)
    int upsertGrade(@Param("enrollmentId") Long enrollmentId,
                    @Param("grade") BigDecimal grade,
                    @Param("uploadedBy") Long uploadedBy);
}