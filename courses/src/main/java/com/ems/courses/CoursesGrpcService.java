package com.ems.courses;

import org.springframework.stereotype.Service;

import com.ems.proto.courses.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.stream.Stream;
import java.util.List;

@Service
public class CoursesGrpcService extends CourseServiceGrpc.CourseServiceImplBase {
    private final SectionRepository sectionRepo;
    private final EnrollmentService enrollmentService;

    public CoursesGrpcService(SectionRepository sectionRepo, EnrollmentService enrollmentService) {
        this.sectionRepo = sectionRepo;
        this.enrollmentService = enrollmentService;
    }
    
    private SectionInfo toProto(SectionRepository.SectionRow s) {
        return SectionInfo.newBuilder()
                .setId(s.getId())
                .setCourseCode(s.getCourseCode())
                .setCourseTitle(s.getCourseTitle())
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
            List<SectionRepository.SectionRow> rows;
            if (req.getMineOnly()) {
                Long callerId = JwtServerInterceptor.USER_ID.get();
                if (callerId == null) {
                    out.onError(Status.UNAUTHENTICATED.asRuntimeException());
                    return;
                }
                rows = sectionRepo.findByFaculty(callerId, req.getTerm());
            } else {
                rows = sectionRepo.findSections(req.getTerm());
            }
            var response = ListSectionResponse.newBuilder();
            rows.forEach(r -> response.addSections(toProto(r)));
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
            var updatedSection = sectionRepo.findRowById(req.getSectionId()).orElseThrow();

           out.onNext(EnrollCourseResponse.newBuilder()
                .setEnrollmentId(enrollment.getId())
                .setSection(toProto(updatedSection))
                .build());
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

            var response = ListEnrollmentResponse.newBuilder();

            sectionRepo.findByStudent(studentId).forEach(r -> response.addSections(toProto(r)));
            out.onNext(response.build());
            out.onCompleted();

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

            enrollmentService.dropStudent(req.getSectionId(), studentId);
            var section = sectionRepo.findRowById(req.getSectionId()).orElseThrow();

            out.onNext(DropCourseResponse.newBuilder().setSection(toProto(section)).build());
            out.onCompleted();
        } catch (io.grpc.StatusRuntimeException e) {
            out.onError(e);
        } catch (Exception e) {
            out.onError(Status.INTERNAL.withDescription("Drop failed").asRuntimeException());
        }
    }
}
