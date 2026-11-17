package com.ems.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import io.grpc.StatusRuntimeException;

import com.ems.proto.auth.AuthServiceGrpc;
import com.ems.proto.auth.LoginRequest;
import com.ems.proto.auth.LogoutRequest;
import com.ems.proto.auth.Role;

@Controller
public class AuthController {
    private final AuthServiceGrpc.AuthServiceBlockingStub auth;
    private final GrpcAuth grpcAuth;

    public AuthController(AuthServiceGrpc.AuthServiceBlockingStub auth, GrpcAuth grpcAuth) {
        this.auth = auth;
        this.grpcAuth = grpcAuth;
    }
    
    @GetMapping({"/login"})
    public String loginPage() { return "pages/login"; }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpSession session, RedirectAttributes ra) {
        try {
            var res = auth.login(LoginRequest.newBuilder()
                    .setEmail(email).setPassword(password).build());
            session.setAttribute("token", res.getToken());
            session.setAttribute("user", res.getUser());
            return res.getUser().getRole() == Role.ROLE_FACULTY
                    ? "redirect:/faculty/dashboard" : "redirect:/student/dashboard";
        } catch (StatusRuntimeException e) {
            System.err.println("Login failed: " + e.getStatus());
            ra.addFlashAttribute("error", "Login failed: " + e.getStatus().getCode());
            return "redirect:/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        try {
            grpcAuth.withToken(auth, session).logout(LogoutRequest.getDefaultInstance());
        } catch (StatusRuntimeException ignored) {
        }
        session.invalidate();
        return "redirect:/login";
    }
}
