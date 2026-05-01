package com.tw.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(basePackages = "com.tw.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleSecurity(SecurityException e, Model model) {
        model.addAttribute("codigo", "403");
        model.addAttribute("mensaje", "No tienes permiso para realizar esta acción");
        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(IllegalArgumentException e, Model model) {
        model.addAttribute("codigo", "404");
        model.addAttribute("mensaje", e.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception e, Model model, HttpServletRequest request) {
        model.addAttribute("codigo", "500");
        model.addAttribute("mensaje", "Ha ocurrido un error inesperado. Por favor, inténtalo de nuevo.");
        return "error";
    }
}
