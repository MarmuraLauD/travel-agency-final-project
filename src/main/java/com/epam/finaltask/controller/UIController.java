package com.epam.finaltask.controller;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        if (isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "auth/sign-in";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "5") int size,
                            @RequestParam(defaultValue = "title") String sortField,
                            @RequestParam(defaultValue = "asc") String sortDir,
                            Model model) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortField).ascending() : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<VoucherDTO> voucherPage = voucherService.findAll(pageable);

        model.addAttribute("vouchers", voucherPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", voucherPage.getTotalPages());
        model.addAttribute("totalItems", voucherPage.getTotalElements());

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        return "user/dashboard";
    }

    @GetMapping("/register")
    public String registerPage() {
        if (isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "auth/sign-up";
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return false;
        }
        return authentication.isAuthenticated();
    }
}
