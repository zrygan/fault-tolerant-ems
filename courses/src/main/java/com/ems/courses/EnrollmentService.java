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

        section.setEnrolledCount(section.getEnrolledCount() + 1);

        sectionRepo.save(section);
        return enrollmentRepo.save(enrollment);
    }

}
