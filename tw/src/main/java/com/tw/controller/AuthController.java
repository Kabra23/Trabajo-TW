package com.tw.controller;

import com.tw.model.Usuario;
import com.tw.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ---- Login ----

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error, Model model) {
        if (error != null) model.addAttribute("error", "Email o contraseña incorrectos");
        return "login";
    }

    // ---- Registro ----

    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("usuario") Usuario usuario,
                             BindingResult result,
                             @RequestParam String confirmPassword,
                             RedirectAttributes flash) {
        // Validar contraseña robusta
        if (!validarPassword(usuario.getPassword())) {
            result.rejectValue("password", "password.weak",
                "La contraseña debe tener al menos 8 caracteres, mayúscula, minúscula, número y carácter especial");
        }
        // Validar que coincidan
        if (!usuario.getPassword().equals(confirmPassword)) {
            result.rejectValue("password", "password.mismatch", "Las contraseñas no coinciden");
        }

        if (result.hasErrors()) return "registro";

        try {
            usuarioService.registrar(usuario);
            flash.addFlashAttribute("exito", "Cuenta creada correctamente. Inicia sesión.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "email.exists", e.getMessage());
            return "registro";
        }
    }

    // ---- Perfil ----

    @GetMapping("/perfil")
    public String perfil(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        model.addAttribute("usuario", usuario);
        return "perfil";
    }

    @PostMapping("/perfil/editar")
    public String editarPerfil(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String nombre,
                                @RequestParam String apellidos,
                                RedirectAttributes flash) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        usuarioService.actualizarPerfil(usuario.getId(), nombre, apellidos, null);
        flash.addFlashAttribute("exito", "Perfil actualizado correctamente");
        return "redirect:/perfil";
    }

    @PostMapping("/perfil/eliminar")
    public String eliminarCuenta(@AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes flash) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        usuarioService.eliminarCuenta(usuario.getId());
        // La sesión se invalida automáticamente por Spring Security al redirigir a logout
        return "redirect:/logout";
    }

    // ---- Validación de contraseña (req. mínimo 1) ----
    private boolean validarPassword(String pass) {
        if (pass == null || pass.length() < 8) return false;
        boolean tieneMayuscula  = pass.chars().anyMatch(Character::isUpperCase);
        boolean tieneMinuscula  = pass.chars().anyMatch(Character::isLowerCase);
        boolean tieneNumero     = pass.chars().anyMatch(Character::isDigit);
        boolean tieneEspecial   = pass.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
        return tieneMayuscula && tieneMinuscula && tieneNumero && tieneEspecial;
    }
}