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

import com.ems.proto.auth.*;
import com.ems.proto.courses.*;
import com.ems.proto.grades.*;

import com.ems.view.GrpcAuth.*;

@Controller
@RequestMapping("/student")
public class StudentController {
    private final CourseServiceGrpc.CourseServiceBlockingStub courses;
    private final GradesServiceGrpc.GradesServiceBlockingStub grades;
    private final GrpcAuth grpcAuth;

    public StudentController(CourseServiceGrpc.CourseServiceBlockingStub courses,
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
        return "pages/student/dashboard";
    }

    @GetMapping("/grades")
    public String gradesPage(Model model, HttpSession s) {
        try {
            model.addAttribute("grades", grpcAuth.withToken(grades, s)
                .getGrades(GetGradesRequest.getDefaultInstance()).getGradesList());
        } catch (StatusRuntimeException e) {
            model.addAttribute("error", "Grades are temporarily unavailable.");
        }
        return "pages/student/grades";
    }

    @GetMapping("/enroll")
    public String enroll(@RequestParam(defaultValue = "") String term, Model model, HttpSession s) {
        try {
            var req = ListSectionRequest.newBuilder().setTerm(term).setMineOnly(false).build();
            model.addAttribute("sections", grpcAuth.withToken(courses, s).listSection(req).getSectionsList());
        } catch (StatusRuntimeException e) {
            model.addAttribute("error", "Courses are temporarily unavailable.");
        }
        return "pages/student/enroll";
    }

    @PostMapping("/enroll")
    public String doEnroll(@RequestParam long sectionId, HttpSession s, RedirectAttributes ra) {
        try {
            grpcAuth.withToken(courses, s).enrollCourse(
                EnrollCourseRequest.newBuilder().setSectionId(sectionId).build());
            ra.addFlashAttribute("message", "Enrolled!");
        } catch (StatusRuntimeException e) {
            ra.addFlashAttribute("error", "Could not enroll: " + e.getStatus().getDescription());
        }
        return "redirect:/student/enroll";
    }
}