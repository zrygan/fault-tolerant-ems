package com.ems.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import io.grpc.StatusRuntimeException;

import com.ems.proto.courses.*;
import com.ems.proto.grades.*;
import com.ems.view.GrpcAuth.*;

@Controller
@RequestMapping("/faculty")
public class FacultyController {
    private final CourseServiceGrpc.CourseServiceBlockingStub courses;
    private final GradesServiceGrpc.GradesServiceBlockingStub grades;
    private final GrpcAuth grpcAuth;
    
    public FacultyController(CourseServiceGrpc.CourseServiceBlockingStub courses,
                             GradesServiceGrpc.GradesServiceBlockingStub grades,
                             GrpcAuth grpcAuth) {
        this.courses = courses; this.grades = grades; this.grpcAuth = grpcAuth;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession s) {
        try {
            model.addAttribute("grades", grpcAuth.withToken(grades, s)
                .getGrades(GetGradesRequest.getDefaultInstance()).getGradesList());
            model.addAttribute("schedule", grpcAuth.withToken(courses, s)
                .listEnrollments(ListEnrollmentRequest.getDefaultInstance()).getSectionsList());
        } catch (StatusRuntimeException e) {
            model.addAttribute("error", "Some data is temporarily unavailable.");
        }
        return "pages/faculty/dashboard";
    }
}
