package com.tw.controller;

import com.tw.model.Usuario;
import com.tw.service.ImagenService;
import com.tw.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final ImagenService imagenService;

    public AuthController(UsuarioService usuarioService, ImagenService imagenService) {
        this.usuarioService = usuarioService;
        this.imagenService = imagenService;
    }

    // ---- Login ----

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Email o contraseña incorrectos");
        }
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
                             Model model) {
        // Validar contraseña robusta
        if (!validarPassword(usuario.getPassword())) {
            result.rejectValue("password", "password.weak",
                    "La contraseña debe tener al menos 8 caracteres, mayúscula, minúscula, número y carácter especial");
        }
        // Validar que coincidan
        if (usuario.getPassword() != null && !usuario.getPassword().equals(confirmPassword)) {
            result.rejectValue("password", "password.mismatch", "Las contraseñas no coinciden");
        }

        if (result.hasErrors()) {
            return "registro";
        }

        try {
            usuarioService.registrar(usuario);
            // Req. mínimo 1: mostrar página de éxito con los datos del usuario
            model.addAttribute("nombre", usuario.getNombre());
            model.addAttribute("apellidos", usuario.getApellidos());
            model.addAttribute("email", usuario.getEmail());
            return "registro-exitoso";
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
                                @RequestParam(required = false) MultipartFile fotoFile,
                                RedirectAttributes flash) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());

        // Subir foto de perfil si se proporcionó (extra imágenes)
        String rutaFoto = null;
        if (fotoFile != null && !fotoFile.isEmpty()) {
            try {
                imagenService.validarImagen(fotoFile);
                rutaFoto = imagenService.guardar(fotoFile, "usuarios");
            } catch (IOException e) {
                flash.addFlashAttribute("error", "Error al subir la foto: " + e.getMessage());
                return "redirect:/perfil";
            }
        }

        usuarioService.actualizarPerfil(usuario.getId(), nombre, apellidos, rutaFoto);
        flash.addFlashAttribute("exito", "Perfil actualizado correctamente");
        return "redirect:/perfil";
    }

    @PostMapping("/perfil/eliminar")
    public String eliminarCuenta(@AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes flash) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        usuarioService.eliminarCuenta(usuario.getId());
        return "redirect:/logout";
    }

    // ---- Validación de contraseña (req. mínimo 1) ----
    private boolean validarPassword(String pass) {
        if (pass == null || pass.length() < 8) return false;
        boolean tieneMayuscula = pass.chars().anyMatch(Character::isUpperCase);
        boolean tieneMinuscula = pass.chars().anyMatch(Character::isLowerCase);
        boolean tieneNumero    = pass.chars().anyMatch(Character::isDigit);
        boolean tieneEspecial  = pass.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
        return tieneMayuscula && tieneMinuscula && tieneNumero && tieneEspecial;
    }
}
