package com.ems.view;

import com.ems.proto.auth.UserInfo;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class UserAdvice {
    @ModelAttribute("user")
    public UserInfo user(HttpSession s) { return (UserInfo) s.getAttribute("user"); }
}
