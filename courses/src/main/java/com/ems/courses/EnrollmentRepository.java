package com.ems.courses;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
  List<Enrollment> findByStudentId(Long studentId);

  boolean existsByStudentIdAndSectionId(Long studentId, Long sectionId);

  Optional<Enrollment> findByStudentIdAndSectionId(Long studentId, Long sectionId);

  @Query(value = "SELECT EXISTS (SELECT 1 FROM grades WHERE enrollment_id = :enrollmentId)", nativeQuery = true)
  boolean hasGrade(@Param("enrollmentId") Long enrollmentId);

  @Query("""
      SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END
      FROM Enrollment e, Section s
      WHERE e.sectionId = s.id
        AND e.studentId = :studentId
        AND s.course.code = :courseCode
      """)
  boolean existsByStudentAndCourseCode(@Param("studentId") Long studentId,
      @Param("courseCode") String courseCode);

  interface RowsWithActiveHold {
    String getHoldType();

    String getAdminName();
  }

  // ===
  // BONUS: CLEARANCE HOLDING
  // ===
  // Queries self if self has a clearance hold.
  // 
  // Used in enrollStudent@courses/EnrollmentServices
  // Refer to clearance/
  @Query(value = """
      SELECT c.hold_type AS "holdType", u.full_name AS "adminName"
      FROM clearances c
      JOIN users u ON u.id = c.admin_id
      WHERE c.student_id = :studentId
        AND c.resolved_on IS NULL
      ORDER BY c.placed_on
      """, nativeQuery = true)
  List<RowsWithActiveHold> findActiveHolds(@Param("studentId") Long studentId);
}
