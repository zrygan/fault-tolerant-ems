package com.ems.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String login() { return "pages/login"; }

    @GetMapping("/signup")
    public String signup() { return "pages/signup"; }
}
