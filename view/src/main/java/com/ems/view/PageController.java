package com.ems.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/", "/student/dashboard"})
    public String dashboard() { return "pages/student/dashboard"; }

    @GetMapping("/student/enroll")
    public String enroll() { return "pages/student/enroll"; }

    @GetMapping("/student/grades")
    public String grades() { return "pages/student/grades"; }

    @GetMapping("/login")
    public String login() { return "pages/login"; }

    @GetMapping("/signup")
    public String signup() { return "pages/signup"; }
}
