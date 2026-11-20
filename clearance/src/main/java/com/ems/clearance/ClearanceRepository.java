package com.ems.clearance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClearanceRepository extends JpaRepository<Clearance, Long> {
  //*
  //  *StudentId simply checks for a studentId whether or not they
  //    have a clearance hold
  // 
  //  *StudentIdAndResolvedOnIsNull are specifically for studentIds
  //    THAT HAVE a clearance hold.
  //
  //  Note: it doesn't make sense to have findBy HoldType AND AdminID.
  //    Since, implicitly, the behavior is that we only have ONE admin
  //    per each department that grants clearance holds. That is,
  //    there is only one Accounting Admin that can grant 
  //    `financial_hold`. 
  // */
  List<Clearance> findByStudentId(Long studentId);
  List<Clearance> findByStudentIdAndResolvedOnIsNull(Long studentId);
  
  List<Clearance> findByHoldTypeAndResolvedOnIsNull(String holdType);
  List<Clearance> findByAdminIdAndResolvedOnIsNull(Long adminId);

  @Query(value="""
    SELECT EXISTS 
      (SELECT 1 
        FROM users 
        WHERE id = :studentId 
        AND role = 'student'
      )
    """,nativeQuery = true) boolean isStudent(@Param("studentId") Long StudentId);

  @Query(value = """
    SELECT full_name 
    FROM users 
    WHERE id = :userId""", nativeQuery = true) String findUserNameById(@Param("userId") Long userId);

  interface StudentRow {
    Long getId();
    String getFullName();
  }

  @Query(value = """
    SELECT id AS "id", full_name AS "fullName"
    FROM users
    WHERE role = 'student'
    ORDER BY id
    """, nativeQuery = true)
  List<StudentRow> findAllStudents();
}