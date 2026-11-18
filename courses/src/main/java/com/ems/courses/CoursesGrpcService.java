package com.ems.courses;

import org.springframework.stereotype.Service;

import com.ems.proto.courses.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

@Service
public class CoursesGrpcService extends CourseServiceGrpc.CourseServiceImplBase {
    private final SectionRepository sections;
    
    public CoursesGrpcService(SectionRepository sections) {
        this.sections = sections;
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
            var rows = req.getTerm().isBlank() ? sections.findAll() : sections.findByTerm(req.getTerm());
            var response = ListSectionResponse.newBuilder();
            rows.forEach(s -> response.addSections(toProto(s)));
            out.onNext(response.build());
            out.onCompleted();
        } catch (Exception e) {
            out.onError(Status.INTERNAL.withDescription("Could not list sections").asRuntimeException());
        }
    }
    
    // @Override 
    // public void enrollCourse() {
        
    // }
    
    // @Override 
    // public void getEnrolledClasses() {
        
    // }
}
