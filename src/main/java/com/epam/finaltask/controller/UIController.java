package com.epam.finaltask.controller;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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



    @GetMapping("/admin")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(defaultValue = "title") String sortField,
                            @RequestParam(defaultValue = "asc") String sortDir,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) String tourType,
                            @RequestParam(required = false) String transferType,
                            @RequestParam(required = false) String hotelType,
                            @RequestParam(required = false) Boolean hot,
                            Model model) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortField).ascending() : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<VoucherDTO> voucherPage;

        boolean hasFilters = (search != null && !search.trim().isEmpty()) ||
                (tourType != null && !tourType.isEmpty() && !tourType.equals("ALL")) ||
                (transferType != null && !transferType.isEmpty() && !transferType.equals("ALL")) ||
                (hotelType != null && !hotelType.isEmpty() && !hotelType.equals("ALL")) ||
                (hot != null && hot);

        if (hasFilters) {
            voucherPage = voucherService.searchWithFilters(
                    search, tourType, transferType, hotelType, hot, "REGISTERED", pageable);
        } else {
            voucherPage = voucherService.findAllByStatus("REGISTERED", pageable);
        }

        model.addAttribute("vouchers", voucherPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", voucherPage.getTotalPages());
        model.addAttribute("totalItems", voucherPage.getTotalElements());

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("tourType", tourType != null ? tourType : "ALL");
        model.addAttribute("transferType", transferType != null ? transferType : "ALL");
        model.addAttribute("hotelType", hotelType != null ? hotelType : "ALL");
        model.addAttribute("hot", hot != null && hot);

        return "user/dashboard";
    }

    @GetMapping("/profile")
    public String showProfilePage() {
        return "user/profile";
    }



}
