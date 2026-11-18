package com.ems.courses;

import org.springframework.stereotype.Service;

import com.ems.proto.courses.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.stream.Stream;

@Service
public class CoursesGrpcService extends CourseServiceGrpc.CourseServiceImplBase {
    private final SectionRepository sectionRepo;
    private final EnrollmentService enrollmentService;

    public CoursesGrpcService(SectionRepository sectionRepo, EnrollmentService enrollmentService) {
        this.sectionRepo = sectionRepo;
        this.enrollmentService = enrollmentService;
    }
    
    private SectionInfo toProto(Section s) {
        return SectionInfo.newBuilder()
                .setId(s.getId())
                .setCourseCode(s.getCourseCode())
                .setCourseTitle(s.getCourseName())
                .setUnits(s.getUnits())
                .setSectionCode(s.getSectionCode())
                .setFacultyName(s.getFacultyName())
                .setCapacity(s.getCapacity())
                .setEnrolledCount(s.getEnrolledCount())
                .setIsOpen(s.getEnrolledCount() < s.getCapacity())
                .setTerm(s.getTerm())
                .build();
    }
    
    @Override
    public void listSection(ListSectionRequest req, StreamObserver<ListSectionResponse> out) {
        try {
            var rows = req.getTerm().isBlank() ? sectionRepo.findAll(SectionRepository.SECTION_ORDER) : sectionRepo.findByTerm(req.getTerm(), SectionRepository.SECTION_ORDER);
            var response = ListSectionResponse.newBuilder();
            rows.forEach(s -> response.addSections(toProto(s)));
            out.onNext(response.build());
            out.onCompleted();
        } catch (Exception e) {
            out.onError(Status.INTERNAL.withDescription("Could not list sections").asRuntimeException());
        }
    }

    @Override
    public void enrollCourse(EnrollCourseRequest req, StreamObserver<EnrollCourseResponse> out) {
        try {
            if (!"student".equalsIgnoreCase(JwtServerInterceptor.ROLE.get())) {
                out.onError(Status.PERMISSION_DENIED.withDescription("Only students can enroll").asRuntimeException());
                return;
            }
            Long studentId = JwtServerInterceptor.USER_ID.get();

            if(studentId == null){
                out.onError(Status.UNAUTHENTICATED.withDescription("User context missing").asRuntimeException());

                return;
            }

            Enrollment enrollment = enrollmentService.enrollStudent(req.getSectionId(), studentId);

            Section updatedSection = sectionRepo.findById(req.getSectionId()).get();

            var response = EnrollCourseResponse.newBuilder()
                    .setEnrollmentId(enrollment.getId())
                    .setSection(toProto(updatedSection))
                    .build();

            out.onNext(response);
            out.onCompleted();

        } catch(io.grpc.StatusRuntimeException e){
            out.onError(e);
        } catch(Exception e){
            out.onError(Status.INTERNAL.withDescription("Enrollment failed").asRuntimeException());
        }


    }

    @Override
    public void listEnrollments(ListEnrollmentRequest req, StreamObserver<ListEnrollmentResponse> out) {
        try {
            Long studentId = JwtServerInterceptor.USER_ID.get();
            if (studentId == null) {
                out.onError(Status.UNAUTHENTICATED.withDescription("User context missing").asRuntimeException());
                return;
            }

            var enrollments = enrollmentService.getEnrollmentsForStudent(studentId);
            var response = ListEnrollmentResponse.newBuilder();

            for (Enrollment e : enrollments) {
                sectionRepo.findById(e.getSectionId()).ifPresent(s ->
                        response.addSections(toProto(s))
                );
            }

            out.onNext(response.build());
            out.onCompleted();
        } catch (Exception e) {

            out.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void dropCourse(DropCourseRequest req, StreamObserver<DropCourseResponse> out) {
        try {
            if (!"student".equalsIgnoreCase(JwtServerInterceptor.ROLE.get())) {
                out.onError(Status.PERMISSION_DENIED.withDescription("Only students can drop").asRuntimeException());
                return;
            }
            Long studentId = JwtServerInterceptor.USER_ID.get();
            if (studentId == null) {
                out.onError(Status.UNAUTHENTICATED.withDescription("User context missing").asRuntimeException());
                return;
            }

            Section section = enrollmentService.dropStudent(req.getSectionId(), studentId);

            out.onNext(DropCourseResponse.newBuilder().setSection(toProto(section)).build());
            out.onCompleted();
        } catch (io.grpc.StatusRuntimeException e) {
            out.onError(e);
        } catch (Exception e) {
            out.onError(Status.INTERNAL.withDescription("Drop failed").asRuntimeException());
        }
    }
}
