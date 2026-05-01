package com.tw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorPageController {

    @RequestMapping("/error-403")
    public String accessDenied(Model model) {
        model.addAttribute("codigo", "403");
        model.addAttribute("mensaje", "No tienes permiso para acceder a este recurso");
        return "error";
    }
}
