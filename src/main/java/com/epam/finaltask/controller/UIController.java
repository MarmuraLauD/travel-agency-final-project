package com.epam.finaltask.controller;

import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class UIController {

    private final VoucherService voucherService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/sign-in";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("vouchers", voucherService.findAll());
        return "user/dashboard";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/sign-up";
    }
}
