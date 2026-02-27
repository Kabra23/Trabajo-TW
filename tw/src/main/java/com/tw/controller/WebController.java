package com.tw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(
            @RequestParam("nombre") String nombre,
            @RequestParam("apellidos") String apellidos,
            @RequestParam("emailRegistro") String email,
            @RequestParam("passwordRegistro") String password,
            Model model) {

        // Aquí guardarías el usuario en la base de datos
        // Por ahora solo pasamos los datos a la vista

        model.addAttribute("nombre", nombre);
        model.addAttribute("apellidos", apellidos);
        model.addAttribute("email", email);

        return "registro-exitoso";
    }

    @GetMapping("/restaurantes")
    public String restaurantes() {
        return "restaurantes";
    }

    @GetMapping("/detalle-restaurante")
    public String detalleRestaurante() {
        return "detalle-restaurante";
    }

    @GetMapping("/editar-restaurante")
    public String editarRestaurante() {
        return "editar-restaurante";
    }
}

