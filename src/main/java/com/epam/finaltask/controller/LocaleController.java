package com.epam.finaltask.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Slf4j
@Controller
public class LocaleController {

    private final LocaleResolver localeResolver;

    public LocaleController(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @GetMapping("/locale")
    public String changeLocale(@RequestParam("lang") String lang,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        Locale locale = switch (lang.toLowerCase()) {
            case "uk" -> new Locale("uk");
            default -> Locale.ENGLISH;
        };

        localeResolver.setLocale(request, response, locale);

        log.info("Language changed to: {}", locale);

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }
}
