package com.piaar.jwtsample.domain.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RouterController {
    @GetMapping("/login")
    public String LoginPage() {
        return "login.html";
    }
}
