package com.advisor.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {
    @RequestMapping(value = {"/", "/login", "/register", "/chat/**"})
    public String index() {
        return "forward:/index.html";
    }
}
