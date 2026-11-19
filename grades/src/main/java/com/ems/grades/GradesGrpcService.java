package com.ems.grades;

import org.springframework.stereotype.Service;

import com.ems.proto.courses.ListSectionResponse;
import com.ems.proto.courses.SectionInfo;
import com.ems.proto.grades.*;
import com.google.apps.card.v1.Card.Section;

import java.math.BigDecimal;
import java.util.Set;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

@Service 
public class GradesGrpcService extends GradesServiceGrpc.GradesServiceImplBase {
    private static final Set<Double> VALID_GRADES = Set.of(0.0, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0);
    private final GradeRepository grades;
    
    public GradesGrpcService(GradeRepository grades) {
        this.grades = grades;
    }
    
    private static boolean isFaculty() {
        return "FACULTY".equalsIgnoreCase(JwtServerInterceptor.ROLE.get());
    }
    
    private GradeRecord toProto(GradeRepository.GradeRow g) {
        var b = GradeRecord.newBuilder()
                .setEnrollmentId(g.getEnrollmentId())
                .setCourseCode(g.getCourseCode())
                .setCourseTitle(g.getCourseTitle())
                .setSectionCode(g.getSectionCode())
                .setTerm(g.getTerm());
                
        if (g.getGrade() != null) {
            b.setGrade(g.getGrade().doubleValue());
        }
        return b.build();
    }
    
    @Override 
    public void getGrades(GetGradesRequest req, StreamObserver<GetGradesResponse> out) {
        Long callerId = JwtServerInterceptor.USER_ID.get();
        
        if (callerId == null) {
            out.onError(Status.UNAUTHENTICATED.asRuntimeException());
            return;
        }
        
        try {
            var response = GetGradesResponse.newBuilder();
            grades.findByStudent(callerId).forEach(r -> response.addGrades(toProto(r)));
            out.onNext(response.build());
            out.onCompleted();
        } catch(Exception e) {
            out.onError(Status.INTERNAL.withDescription("Could not get grades").asRuntimeException());
        }
    }
    
    @Override 
    public void uploadGrades(UploadGradesRequest req, StreamObserver<UploadGradesResponse> out) {
        Long callerId = JwtServerInterceptor.USER_ID.get();
        
        if (callerId == null || !isFaculty()) {
            out.onError(Status.PERMISSION_DENIED
                    .withDescription("Only faculty can upload grades").asRuntimeException());
            return;
        }
        
        if (!VALID_GRADES.contains(req.getGrade())) {
            out.onError(Status.INVALID_ARGUMENT
                    .withDescription("Grade uploaded must be [0.0, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, or 4.0]")
                    .asRuntimeException());
            return;
        }
        
        grades.upsertGrade(req.getEnrollmentId(), BigDecimal.valueOf(req.getGrade()), callerId);

        out.onNext(UploadGradesResponse.newBuilder()
                .setEnrollmentId(req.getEnrollmentId())
                .setGrade(req.getGrade())
                .build());
        out.onCompleted();
        try {
            if (!grades.facultyTeaches(req.getEnrollmentId(), callerId)) {
                out.onError(Status.PERMISSION_DENIED.withDescription("You do not teach this student's section").asRuntimeException());
                return;
            }
        } catch(Exception e) {
            out.onError(Status.INTERNAL.withDescription("Could not save grade").asRuntimeException());
        }
    }
    
    @Override 
    public void listGradableStudents(ListGradableStudentsRequest req, StreamObserver<ListGradableStudentsResponse> out) {
        Long callerId = JwtServerInterceptor.USER_ID.get();
        if (callerId == null || !isFaculty()) {
            out.onError(Status.PERMISSION_DENIED
                    .withDescription("Only faculty can list students").asRuntimeException());
            return;
        }
        try {
            var response = ListGradableStudentsResponse.newBuilder();
            grades.findGradableStudents(req.getSectionId(), callerId).forEach(r -> {
                var s = GradableStudent.newBuilder()
                        .setEnrollmentId(r.getEnrollmentId())
                        .setStudentName(r.getStudentName());
                if (r.getCurrentGrade() != null) {
                    s.setCurrentGrade(r.getCurrentGrade().doubleValue());
                }
                response.addStudents(s.build());
            });
            out.onNext(response.build());
            out.onCompleted();
        } catch (Exception e) {
            out.onError(Status.INTERNAL.withDescription("Could not list students").asRuntimeException());
        }
    }
}
