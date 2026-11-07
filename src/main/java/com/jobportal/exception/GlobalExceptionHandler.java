package com.jobportal.exception;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Custom Resource Not Found Exception
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(
            ResourceNotFoundException ex,
            Model model,
            HttpServletResponse response) {

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute("errorMessage", ex.getMessage());

        return "error/404";
    }

    // Invalid URL / Page Not Found
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFound(
            NoResourceFoundException ex,
            Model model,
            HttpServletResponse response) {

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute(
                "errorMessage",
                "The page you are looking for does not exist."
        );

        return "error/404";
    }

    // Other Internal Server Errors
    @ExceptionHandler(Exception.class)
    public String handleGlobalException(
            Exception ex,
            Model model,
            HttpServletResponse response) {

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        model.addAttribute(
                "errorMessage",
                "Something went wrong. Please try again later."
        );

        return "error/500";
    }
}