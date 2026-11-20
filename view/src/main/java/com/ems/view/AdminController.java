package com.ems.view;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ems.proto.auth.UserInfo;
import com.ems.proto.clearance.ClearanceServiceGrpc;
import com.ems.proto.clearance.ListStudentClearancesByHoldTypeRequest;
import com.ems.proto.clearance.ListStudentClearancesByPlacedByRequest;
import com.ems.proto.clearance.ListStudentClearancesRequest;
import com.ems.proto.clearance.ListStudentsRequest;
import com.ems.proto.clearance.PlaceHoldRequest;
import com.ems.proto.clearance.ResolveHoldRequest;

import io.grpc.StatusRuntimeException;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final List<String> HOLD_TYPES = List.of(
            "incomplete_records",
            "disciplinary_hold",
            "library_hold",
            "financial_hold");

    private final ClearanceServiceGrpc.ClearanceServiceBlockingStub clearance;
    private final GrpcAuth grpcAuth;

    public AdminController(ClearanceServiceGrpc.ClearanceServiceBlockingStub clearance, GrpcAuth grpcAuth) {
        this.clearance = clearance;
        this.grpcAuth = grpcAuth;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String holdType,
            Model model,
            HttpSession s) {
        model.addAttribute("holdTypes", HOLD_TYPES);
        model.addAttribute("selectedStudentId", studentId);
        model.addAttribute("selectedHoldType", holdType);

        try {
            var stub = grpcAuth.withToken(clearance, s);
            model.addAttribute("students",
                    stub.listStudents(ListStudentsRequest.getDefaultInstance()).getStudentsList());
            if (studentId != null) {
                var req = ListStudentClearancesRequest.newBuilder()
                        .setStudentId(studentId)
                        .setUnresolvedOnly(false)
                        .build();
                model.addAttribute("clearances", stub.listStudentClearances(req).getClearancesList());
            } else if (holdType != null && !holdType.isBlank()) {
                var req = ListStudentClearancesByHoldTypeRequest.newBuilder()
                        .setHoldType(holdType)
                        .build();
                model.addAttribute("clearances", stub.listStudentClearancesByHoldType(req).getClearancesList());
            } else {
                var user = (UserInfo) s.getAttribute("user");
                var req = ListStudentClearancesByPlacedByRequest.newBuilder()
                        .setPlacedBy(user.getId())
                        .build();
                model.addAttribute("clearances", stub.listStudentClearancesByPlacedBy(req).getClearancesList());
            }
        } catch (StatusRuntimeException e) {
            model.addAttribute("error", "Clearances are temporarily unavailable: " + e.getStatus().getCode());
        }
        return "pages/admin/dashboard";
    }

    @PostMapping("/place")
    public String placeHold(
            @RequestParam long studentId,
            @RequestParam String holdType,
            HttpSession s,
            RedirectAttributes ra) {
        try {
            grpcAuth.withToken(clearance, s).placeHold(PlaceHoldRequest.newBuilder()
                    .setStudentId(studentId)
                    .setHoldType(holdType)
                    .build());
            ra.addFlashAttribute("message", "Clearance hold placed.");
        } catch (StatusRuntimeException e) {
            ra.addFlashAttribute("error", "Could not place hold: " + e.getStatus().getCode());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/resolve")
    public String resolveHold(
            @RequestParam long clearanceId,
            HttpSession s,
            RedirectAttributes ra) {
        try {
            grpcAuth.withToken(clearance, s).resolveHold(ResolveHoldRequest.newBuilder()
                    .setClearanceId(clearanceId)
                    .build());
            ra.addFlashAttribute("message", "Clearance hold resolved.");
        } catch (StatusRuntimeException e) {
            ra.addFlashAttribute("error", "Could not resolve hold: " + e.getStatus().getCode());
        }
        return "redirect:/admin/dashboard";
    }
}
