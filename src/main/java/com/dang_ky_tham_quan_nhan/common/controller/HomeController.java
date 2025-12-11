package com.dang_ky_tham_quan_nhan.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/units")
    public String adminUnits() {
        return "admin/units";
    }

    @GetMapping("/admin/soldiers")
    public String adminSoldiers() {
        return "admin/soldiers";
    }
}
