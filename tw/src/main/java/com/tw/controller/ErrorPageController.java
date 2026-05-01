package com.tw.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorPageController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusAttr instanceof Integer ? (Integer) statusAttr : 500;

        String mensaje;
        if (status == 403) {
            mensaje = "No tienes permiso para acceder a este recurso";
        } else if (status == 404) {
            mensaje = "Pagina no encontrada";
        } else {
            mensaje = "Ha ocurrido un error inesperado. Por favor, intentalo de nuevo.";
        }

        model.addAttribute("codigo", String.valueOf(status));
        model.addAttribute("mensaje", mensaje);
        return "error";
    }
}

