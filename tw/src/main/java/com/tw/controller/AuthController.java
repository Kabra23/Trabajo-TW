package com.tw.controller;

import com.tw.model.Direccion;
import com.tw.model.Usuario;
import com.tw.repository.DireccionRepository;
import com.tw.service.ImagenService;
import com.tw.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final DireccionRepository direccionRepo;

    public AuthController(UsuarioService usuarioService,
                          ImagenService imagenService,
                          DireccionRepository direccionRepo) {
        this.usuarioService = usuarioService;
        this.imagenService = imagenService;
        this.direccionRepo = direccionRepo;
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error, Model model) {
        if (error != null) model.addAttribute("error", "Email o contrasena incorrectos");
        return "login";
    }

    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("usuario") Usuario usuario,
                             BindingResult result,
                             @RequestParam String password,
                             @RequestParam String confirmPassword,
                             @RequestParam(required = false) String direccionPrincipal,
                             Model model) {
        if (!validarPassword(password)) {
            result.rejectValue("password", "password.weak",
                    "La contrasena debe tener al menos 8 caracteres, mayuscula, minuscula, numero y caracter especial");
        }
        if (password != null && !password.equals(confirmPassword)) {
            result.rejectValue("password", "password.mismatch", "Las contrasenas no coinciden");
        }
        if (result.hasErrors()) return "registro";

        try {
            Usuario guardado = usuarioService.registrar(usuario, password, direccionPrincipal);
            if (direccionPrincipal != null && !direccionPrincipal.isBlank()) {
                Direccion dir = Direccion.builder()
                        .direccion(direccionPrincipal.trim())
                        .etiqueta("Principal")
                        .principal(true)
                        .usuario(guardado)
                        .build();
                direccionRepo.save(dir);
            }
            model.addAttribute("nombre", usuario.getNombre());
            model.addAttribute("apellidos", usuario.getApellidos());
            model.addAttribute("email", usuario.getEmail());
            return "registro-exitoso";
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "email.exists", e.getMessage());
            return "registro";
        }
    }

    @GetMapping("/perfil")
    public String perfil(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario usuario = usuarioService.buscarPorEmailConFavoritos(userDetails.getUsername());
        model.addAttribute("usuario", usuario);
        model.addAttribute("direcciones", direccionRepo.findByUsuarioId(usuario.getId()));
        return "perfil";
    }

    @PostMapping("/perfil/editar")
    public String editarPerfil(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String nombre,
                                @RequestParam String apellidos,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) MultipartFile fotoFile,
                                HttpServletRequest request,
                                RedirectAttributes flash) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());

        String rutaFoto = null;
        if (fotoFile != null && !fotoFile.isEmpty()) {
            try {
                rutaFoto = imagenService.guardar(fotoFile, "usuarios");
            } catch (IOException e) {
                flash.addFlashAttribute("error", "Error al subir la foto: " + e.getMessage());
                return "redirect:/perfil";
            }
        }
        usuarioService.actualizarPerfil(usuario.getId(), nombre, apellidos, rutaFoto);

        if (email != null && !email.isBlank() && !email.equalsIgnoreCase(usuario.getEmail())) {
            try {
                boolean cambio = usuarioService.actualizarEmail(usuario.getId(), email);
                if (cambio) {
                    SecurityContextHolder.clearContext();
                    request.getSession().invalidate();
                    flash.addFlashAttribute("exito",
                            "Email actualizado. Inicia sesion de nuevo con tu nuevo email.");
                    return "redirect:/login";
                }
            } catch (IllegalArgumentException e) {
                flash.addFlashAttribute("error", e.getMessage());
                return "redirect:/perfil";
            }
        }

        flash.addFlashAttribute("exito", "Perfil actualizado correctamente");
        return "redirect:/perfil";
    }

    @PostMapping("/perfil/eliminar")
    public String eliminarCuenta(@AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        usuarioService.eliminarCuenta(usuario.getId());
        return "redirect:/logout";
    }

    @GetMapping("/mis-restaurantes")
    public String misRestaurantes(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        model.addAttribute("restaurantes", usuario.getRestaurantes());
        return "mis-restaurantes";
    }

    private boolean validarPassword(String pass) {
        if (pass == null || pass.length() < 8) return false;
        return pass.chars().anyMatch(Character::isUpperCase)
            && pass.chars().anyMatch(Character::isLowerCase)
            && pass.chars().anyMatch(Character::isDigit)
            && pass.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
    }
}
