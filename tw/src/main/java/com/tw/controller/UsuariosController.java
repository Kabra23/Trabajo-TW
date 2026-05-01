package com.tw.controller;

import com.tw.model.Usuario;
import com.tw.repository.UsuarioRepository;
import com.tw.service.UsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador MVC para la gestion de usuarios.
 *
 * GET  /usuarios              -> Lista de usuarios (autenticados)
 * GET  /usuarios/nuevo        -> Formulario crear usuario (ADMIN)
 * POST /usuarios/nuevo        -> Crear usuario (ADMIN)
 * GET  /usuarios/{id}/editar  -> Formulario editar usuario (ADMIN)
 * POST /usuarios/{id}/editar  -> Guardar cambios usuario (ADMIN)
 * POST /usuarios/{id}/eliminar -> Eliminar usuario (ADMIN)
 * POST /usuarios/{id}/admin   -> Conceder admin (ADMIN)
 * POST /usuarios/{id}/admin/revocar -> Revocar admin (ADMIN)
 */
@Controller
@RequestMapping("/usuarios")
public class UsuariosController {

    private final UsuarioRepository usuarioRepo;
    private final UsuarioService usuarioService;

    public UsuariosController(UsuarioRepository usuarioRepo, UsuarioService usuarioService) {
        this.usuarioRepo = usuarioRepo;
        this.usuarioService = usuarioService;
    }

    /* ------------------------------------------------------------------ */
    /*  LISTADO (todos los autenticados)                                    */
    /* ------------------------------------------------------------------ */

    @GetMapping
    public String listar(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<Usuario> usuarios = usuarioRepo.findAll();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("emailActual", userDetails.getUsername());
        return "usuarios";
    }

    /* ------------------------------------------------------------------ */
    /*  CREAR USUARIO (ADMIN)                                               */
    /* ------------------------------------------------------------------ */

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("modoEditar", false);
        return "usuario-form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/nuevo")
    public String crear(@RequestParam String nombre,
                        @RequestParam String apellidos,
                        @RequestParam String email,
                        @RequestParam String password,
                        @RequestParam(defaultValue = "false") boolean esAdmin,
                        RedirectAttributes flash) {
        if (nombre.isBlank() || apellidos.isBlank() || email.isBlank() || password.isBlank()) {
            flash.addFlashAttribute("error", "Todos los campos son obligatorios");
            return "redirect:/usuarios/nuevo";
        }
        try {
            usuarioService.crearUsuario(nombre, apellidos, email, password, esAdmin);
            flash.addFlashAttribute("exito", "Usuario creado correctamente");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/usuarios/nuevo";
        }
        return "redirect:/usuarios";
    }

    /* ------------------------------------------------------------------ */
    /*  EDITAR USUARIO (ADMIN)                                              */
    /* ------------------------------------------------------------------ */

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        model.addAttribute("usuario", u);
        model.addAttribute("modoEditar", true);
        return "usuario-form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         @RequestParam String nombre,
                         @RequestParam String apellidos,
                         @RequestParam String email,
                         @RequestParam(defaultValue = "false") boolean esAdmin,
                         RedirectAttributes flash) {
        if (nombre.isBlank() || apellidos.isBlank()) {
            flash.addFlashAttribute("error", "Nombre y apellidos son obligatorios");
            return "redirect:/usuarios/" + id + "/editar";
        }
        try {
            usuarioService.actualizarUsuarioAdmin(id, nombre, apellidos, email, esAdmin);
            flash.addFlashAttribute("exito", "Usuario actualizado correctamente");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/usuarios/" + id + "/editar";
        }
        return "redirect:/usuarios";
    }

    /* ------------------------------------------------------------------ */
    /*  ELIMINAR USUARIO (ADMIN)                                            */
    /* ------------------------------------------------------------------ */

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes flash) {
        Usuario u = usuarioRepo.findById(id).orElse(null);
        if (u == null) {
            flash.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/usuarios";
        }
        if (u.getEmail().equalsIgnoreCase(userDetails.getUsername())) {
            flash.addFlashAttribute("error", "No puedes eliminarte a ti mismo");
            return "redirect:/usuarios";
        }
        usuarioService.eliminarCuenta(id);
        flash.addFlashAttribute("exito", "Usuario eliminado correctamente");
        return "redirect:/usuarios";
    }

    /* ------------------------------------------------------------------ */
    /*  CONCEDER / REVOCAR ADMIN                                            */
    /* ------------------------------------------------------------------ */

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/admin")
    public String concederAdmin(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes flash) {
        usuarioRepo.findById(id).ifPresentOrElse(
                u -> {
                    usuarioService.concederAdmin(id);
                    flash.addFlashAttribute("exito", u.getNombre() + " ahora es administrador");
                },
                () -> flash.addFlashAttribute("error", "Usuario no encontrado")
        );
        return "redirect:/usuarios";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/admin/revocar")
    public String revocarAdmin(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes flash) {
        if (userDetails.getUsername().equals(
                usuarioRepo.findById(id).map(Usuario::getEmail).orElse(""))) {
            flash.addFlashAttribute("error", "No puedes revocar tus propios privilegios");
            return "redirect:/usuarios";
        }
        usuarioRepo.findById(id).ifPresentOrElse(
                u -> {
                    usuarioService.revocarAdmin(id);
                    flash.addFlashAttribute("exito", u.getNombre() + " ya no es administrador");
                },
                () -> flash.addFlashAttribute("error", "Usuario no encontrado")
        );
        return "redirect:/usuarios";
    }
}
