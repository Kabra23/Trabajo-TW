package com.tw.controller;

import com.tw.model.Valoracion;
import com.tw.service.ValoracionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;

@Controller
public class ValoracionController {

    private final ValoracionService valoracionService;

    public ValoracionController(ValoracionService valoracionService) {
        this.valoracionService = valoracionService;
    }

    // -------------------------------------------------------
    // Crear valoración
    // -------------------------------------------------------
    @PostMapping("/restaurantes/{id}/valorar")
    public String crear(@PathVariable Long id,
                       @Valid @ModelAttribute("valoracion") Valoracion valoracion,
                       BindingResult result,
                       @AuthenticationPrincipal UserDetails userDetails,
                       RedirectAttributes flash) {
        try {
            if (result.hasErrors()) {
                flash.addFlashAttribute("error", "Error en los datos de la valoración");
                return "redirect:/restaurantes/" + id;
            }

            valoracionService.crear(id, valoracion, userDetails.getUsername());
            flash.addFlashAttribute("exito", "Valoración creada correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/restaurantes/" + id;
    }

    // -------------------------------------------------------
    // Actualizar valoración
    // -------------------------------------------------------
    @PostMapping("/valoraciones/{id}/editar")
    public String editar(@PathVariable Long id,
                        @Valid @ModelAttribute("valoracion") Valoracion datos,
                        BindingResult result,
                        @AuthenticationPrincipal UserDetails userDetails,
                        RedirectAttributes flash) {
        try {
            if (result.hasErrors()) {
                flash.addFlashAttribute("error", "Error en los datos de la valoración");
                Valoracion actual = valoracionService.buscarPorId(id);
                return "redirect:/restaurantes/" + actual.getRestaurante().getId();
            }

            Valoracion actualizada = valoracionService.actualizar(id, datos, userDetails.getUsername());
            flash.addFlashAttribute("exito", "Valoración actualizada correctamente");
            return "redirect:/restaurantes/" + actualizada.getRestaurante().getId();
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            Valoracion actual = valoracionService.buscarPorId(id);
            return "redirect:/restaurantes/" + actual.getRestaurante().getId();
        }
    }

    // -------------------------------------------------------
    // Eliminar valoración
    // -------------------------------------------------------
    @PostMapping("/valoraciones/{id}/eliminar")
    public String eliminar(@PathVariable Long id,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes flash) {
        try {
            Valoracion valoracion = valoracionService.buscarPorId(id);
            Long restauranteId = valoracion.getRestaurante().getId();
            valoracionService.eliminar(id, userDetails.getUsername());
            flash.addFlashAttribute("exito", "Valoración eliminada correctamente");
            return "redirect:/restaurantes/" + restauranteId;
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            Valoracion valoracion = valoracionService.buscarPorId(id);
            return "redirect:/restaurantes/" + valoracion.getRestaurante().getId();
        }
    }

    // -------------------------------------------------------
    // Listar valoraciones de un restaurante
    // -------------------------------------------------------
    @GetMapping("/restaurantes/{id}/valoraciones")
    public String listar(@PathVariable Long id,
                        Model model) {
        List<Valoracion> valoraciones = valoracionService.listarPorRestaurante(id);
        model.addAttribute("valoraciones", valoraciones);
        model.addAttribute("restauranteId", id);
        return "valoraciones";
    }

    // -------------------------------------------------------
    // Mis valoraciones
    // -------------------------------------------------------
    @GetMapping("/mis-valoraciones")
    public String misValoraciones(@AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        List<Valoracion> valoraciones = valoracionService.listarPorUsuario(userDetails.getUsername());
        model.addAttribute("valoraciones", valoraciones);
        return "mis-valoraciones";
    }
}

