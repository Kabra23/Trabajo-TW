package com.tw.controller;

import com.tw.model.Direccion;
import com.tw.model.Usuario;
import com.tw.repository.DireccionRepository;
import com.tw.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/perfil/direcciones")
public class DireccionController {

    private final DireccionRepository direccionRepo;
    private final UsuarioService usuarioService;

    public DireccionController(DireccionRepository direccionRepo, UsuarioService usuarioService) {
        this.direccionRepo = direccionRepo;
        this.usuarioService = usuarioService;
    }

    /** Añadir nueva dirección */
    @PostMapping("/nueva")
    public String nueva(@RequestParam String direccion,
                        @RequestParam(required = false) String etiqueta,
                        @RequestParam(required = false, defaultValue = "false") boolean principal,
                        @AuthenticationPrincipal UserDetails userDetails,
                        RedirectAttributes flash) {
        if (direccion == null || direccion.isBlank()) {
            flash.addFlashAttribute("error", "La dirección no puede estar vacía");
            return "redirect:/perfil";
        }

        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());

        // Si es principal, desmarcar las demás
        if (principal) {
            List<Direccion> existentes = direccionRepo.findByUsuarioId(usuario.getId());
            existentes.forEach(d -> d.setPrincipal(false));
            direccionRepo.saveAll(existentes);
        }

        Direccion nueva = Direccion.builder()
                .direccion(direccion.trim())
                .etiqueta(etiqueta != null && !etiqueta.isBlank() ? etiqueta.trim() : null)
                .principal(principal)
                .usuario(usuario)
                .build();

        // Si es la primera dirección, hacerla principal automáticamente
        if (direccionRepo.findByUsuarioId(usuario.getId()).isEmpty()) {
            nueva.setPrincipal(true);
        }

        direccionRepo.save(nueva);
        flash.addFlashAttribute("exito", "Dirección añadida correctamente");
        return "redirect:/perfil";
    }

    /** Marcar una dirección como principal */
    @PostMapping("/{id}/principal")
    public String marcarPrincipal(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes flash) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        Direccion direccion = direccionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada"));

        if (!direccion.getUsuario().getId().equals(usuario.getId())) {
            throw new SecurityException("No tienes permiso para modificar esta dirección");
        }

        // Desmarcar todas
        List<Direccion> existentes = direccionRepo.findByUsuarioId(usuario.getId());
        existentes.forEach(d -> d.setPrincipal(false));
        direccionRepo.saveAll(existentes);

        // Marcar la seleccionada
        direccion.setPrincipal(true);
        direccionRepo.save(direccion);

        flash.addFlashAttribute("exito", "Dirección principal actualizada");
        return "redirect:/perfil";
    }

    /** Eliminar una dirección */
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes flash) {
        Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        Direccion direccion = direccionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada"));

        if (!direccion.getUsuario().getId().equals(usuario.getId())) {
            throw new SecurityException("No tienes permiso para eliminar esta dirección");
        }

        boolean eraPrincipal = Boolean.TRUE.equals(direccion.getPrincipal());
        direccionRepo.delete(direccion);

        // Si era principal, asignar otra como principal automáticamente
        if (eraPrincipal) {
            List<Direccion> restantes = direccionRepo.findByUsuarioId(usuario.getId());
            if (!restantes.isEmpty()) {
                restantes.get(0).setPrincipal(true);
                direccionRepo.save(restantes.get(0));
            }
        }

        flash.addFlashAttribute("exito", "Dirección eliminada");
        return "redirect:/perfil";
    }
}
