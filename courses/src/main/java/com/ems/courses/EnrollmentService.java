package com.ems.courses;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.grpc.Status;

@Service
public class EnrollmentService {
    private final SectionRepository sectionRepo;
    private final EnrollmentRepository enrollmentRepo;

    public EnrollmentService(SectionRepository sectionRepo, EnrollmentRepository enrollmentRepo){
        this.sectionRepo = sectionRepo;
        this.enrollmentRepo = enrollmentRepo;
    }

    @Transactional
    public Enrollment enrollStudent(Long sectionId, Long studentId){
        if(enrollmentRepo.existsByStudentIdAndSectionId(studentId, sectionId)){
            throw Status.ALREADY_EXISTS.withDescription("You are already enrolled in this section.").asRuntimeException();
        }

        Section section = sectionRepo.findById(sectionId).orElseThrow(() -> Status.NOT_FOUND.withDescription("Section not found").asRuntimeException());

        if(section.getEnrolledCount() >= section.getCapacity()){
            throw Status.FAILED_PRECONDITION.withDescription("Section is full").asRuntimeException();
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setSectionId(sectionId);
        enrollment.setEnrolledAt(java.time.OffsetDateTime.now());

        int newCount = section.getEnrolledCount() + 1;
        section.setEnrolledCount(newCount);

        if(newCount>= section.getCapacity()){
            section.setIsOpen(false);
        }

        sectionRepo.save(section);
        return enrollmentRepo.save(enrollment);
    }

    @Transactional
    public Section dropStudent(Long sectionId, Long studentId){
        Enrollment enrollment = enrollmentRepo.findByStudentIdAndSectionId(studentId, sectionId).orElseThrow(() -> Status.NOT_FOUND.withDescription("You are not enrolled in this section.").asRuntimeException());
        if (enrollmentRepo.hasGrade(enrollment.getId())) {
            throw Status.FAILED_PRECONDITION
                    .withDescription("This course already has a grade and can no longer be dropped.")
                    .asRuntimeException();
        }

        Section section = sectionRepo.findById(sectionId).orElseThrow(() -> Status.NOT_FOUND.withDescription("Section not found").asRuntimeException());

        boolean wasFull = section.getEnrolledCount() >= section.getCapacity();

        enrollmentRepo.delete(enrollment);
        section.setEnrolledCount(Math.max(0, section.getEnrolledCount() - 1));
        if (wasFull) section.setIsOpen(true);   // only undo the auto-close from enrollStudent

        return sectionRepo.save(section);
    }

    public java.util.List<Enrollment> getEnrollmentsForStudent(Long studentId) {
        return enrollmentRepo.findByStudentId(studentId);
    }

}
