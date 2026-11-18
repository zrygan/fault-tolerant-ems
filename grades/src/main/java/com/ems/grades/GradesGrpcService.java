package com.ems.grades;

import org.springframework.stereotype.Service;

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
    
    // @Override 
    // public void getGrades() {
        
    // }
    
    // @Override 
    // public void uploadGrades() {
        
    // }
    
    // @Override 
    // public void listStudents() {
        
    // }
}
