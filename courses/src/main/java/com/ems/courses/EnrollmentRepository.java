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
}
