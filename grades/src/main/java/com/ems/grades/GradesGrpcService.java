package com.ems.grades;

import org.springframework.stereotype.Service;

import com.ems.proto.courses.ListSectionResponse;
import com.ems.proto.courses.SectionInfo;
import com.ems.proto.grades.*;
import com.google.apps.card.v1.Card.Section;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

@Service 
public class GradesGrpcService extends GradesServiceGrpc.GradesServiceImplBase {
    private final GradeRepository grades;
    
    public GradesGrpcService(GradeRepository grades) {
        this.grades = grades;
    }
    
    private static boolean isFaculty() {
        return "FACULTY".equalsIgnoreCase(JwtServerInterceptor.ROLE.get());
    }
    
    private GradeRecord toProto(Grade g) {
        return GradeRecord.newBuilder()
        .build();
    }
    
    @Override 
    public void getGrades(GetGradesRequest req, StreamObserver<GetGradesResponse> out) {
        try {
            
        } catch(Exception e) {
            out.onError(Status.INTERNAL.withDescription("Could not get grades").asRuntimeException());
        }
    }
    
    // @Override 
    // public void uploadGrades() {
        
    // }
    
    // @Override 
    // public void listGradableStudents() {
        
    // }
}
