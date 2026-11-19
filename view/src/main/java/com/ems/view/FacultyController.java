package com.ems.view;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ems.proto.courses.CourseServiceGrpc;
import com.ems.proto.courses.ListEnrollmentRequest;
import com.ems.proto.courses.ListSectionRequest;
import com.ems.proto.grades.GetGradesRequest;
import com.ems.proto.grades.GradesServiceGrpc;
import com.ems.proto.grades.ListGradableStudentsRequest;
import com.ems.proto.grades.UploadGradesRequest;

import io.grpc.StatusRuntimeException;
import jakarta.servlet.http.HttpSession;

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
    
    private static final List<Double> GRADE_OPTIONS = List.of(0.0, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0);
    @GetMapping("/grades")
    public String grades(@RequestParam(required = false) Long sectionId, Model model, HttpSession s) {
        model.addAttribute("gradeOptions", GRADE_OPTIONS);
        model.addAttribute("selectedSectionId", sectionId);
        try {
            var sectionReq = ListSectionRequest.newBuilder().setTerm("").setMineOnly(true).build();
            model.addAttribute("sections",
                    grpcAuth.withToken(courses, s).listSection(sectionReq).getSectionsList());

            if (sectionId != null) {
                var studentReq = ListGradableStudentsRequest.newBuilder().setSectionId(sectionId).build();
                model.addAttribute("students",
                        grpcAuth.withToken(grades, s).listGradableStudents(studentReq).getStudentsList());
            }
        } catch (StatusRuntimeException e) {
            model.addAttribute("error", "Could not load this page: " + e.getStatus().getCode());
        }
        return "pages/faculty/grades";
    }
    
    @PostMapping("/grades")
    public String uploadGrade(@RequestParam long sectionId, @RequestParam long enrollmentId, @RequestParam double grade, HttpSession s, RedirectAttributes ra) {
        try {
            grpcAuth.withToken(grades, s).uploadGrades(UploadGradesRequest.newBuilder()
                    .setEnrollmentId(enrollmentId)
                    .setGrade(grade)
                    .build());
            ra.addFlashAttribute("message", "Grade saved.");
        } catch (StatusRuntimeException e) {
            String why = e.getStatus().getDescription();
            ra.addFlashAttribute("error", why != null ? why : "Could not save grade.");
        }
        return "redirect:/faculty/grades?sectionId=" + sectionId;
    }
}
