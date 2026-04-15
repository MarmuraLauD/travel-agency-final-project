package com.epam.finaltask.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice(annotations = Controller.class)
public class UIExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("No handler found for UI request: {} {}", request.getMethod(), request.getRequestURI());

        ModelAndView mav = new ModelAndView("error/404");
        mav.setStatus(HttpStatus.NOT_FOUND);
        mav.addObject("status", 404);
        mav.addObject("error", "Page Not Found");
        mav.addObject("message", "The page you are looking for does not exist");
        mav.addObject("path", request.getRequestURI());
        return mav;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("No resource found for UI request: {}", request.getRequestURI());

        ModelAndView mav = new ModelAndView("error/404");
        mav.setStatus(HttpStatus.NOT_FOUND);
        mav.addObject("status", 404);
        mav.addObject("error", "Page Not Found");
        mav.addObject("message", "The page you are looking for does not exist");
        mav.addObject("path", request.getRequestURI());
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error in UI: ", ex);

        ModelAndView mav = new ModelAndView("error/500");
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        mav.addObject("status", 500);
        mav.addObject("error", "Internal Server Error");
        mav.addObject("message", "An unexpected error occurred");
        mav.addObject("path", request.getRequestURI());
        return mav;
    }
}
