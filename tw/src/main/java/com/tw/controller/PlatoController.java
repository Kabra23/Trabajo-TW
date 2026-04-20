package com.tw.controller;

import com.tw.model.Plato;
import com.tw.model.Restaurante;
import com.tw.service.ImagenService;
import com.tw.service.PlatoService;
import com.tw.service.RestauranteService;
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
@RequestMapping("/restaurantes")
public class PlatoController {

    private final PlatoService platoService;
    private final RestauranteService restauranteService;
    private final ImagenService imagenService;

    public PlatoController(PlatoService platoService,
                         RestauranteService restauranteService,
                         ImagenService imagenService) {
        this.platoService = platoService;
        this.restauranteService = restauranteService;
        this.imagenService = imagenService;
    }

    // -------------------------------------------------------
    // Formulario de nuevo plato
    // -------------------------------------------------------
    @GetMapping("/{id}/platos/nuevo")
    public String nuevoForm(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        Restaurante restaurante = restauranteService.buscarPorId(id);
        verificarPropietario(restaurante, userDetails.getUsername());

        model.addAttribute("restaurante", restaurante);
        model.addAttribute("plato", new Plato());
        model.addAttribute("modo", "crear");
        return "form-plato";
    }

    // -------------------------------------------------------
    // Crear plato
    // -------------------------------------------------------
    @PostMapping("/{id}/platos/nuevo")
    public String crear(@PathVariable Long id,
                       @Valid @ModelAttribute("plato") Plato plato,
                       BindingResult result,
                       @RequestParam(required = false) MultipartFile imagenFile,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model,
                       RedirectAttributes flash) {
        Restaurante restaurante = restauranteService.buscarPorId(id);
        verificarPropietario(restaurante, userDetails.getUsername());

        if (result.hasErrors()) {
            model.addAttribute("restaurante", restaurante);
            model.addAttribute("modo", "crear");
            return "form-plato";
        }

        try {
            subirImagen(plato, imagenFile);
            platoService.crear(id, plato, userDetails.getUsername());
            flash.addFlashAttribute("exito", "Plato creado correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al crear el plato: " + e.getMessage());
        }

        return "redirect:/restaurantes/" + id;
    }

    // -------------------------------------------------------
    // Formulario de edición de plato
    // -------------------------------------------------------
    @GetMapping("/{rid}/platos/{pid}/editar")
    public String editarForm(@PathVariable Long rid,
                            @PathVariable Long pid,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        Restaurante restaurante = restauranteService.buscarPorId(rid);
        verificarPropietario(restaurante, userDetails.getUsername());

        Plato plato = platoService.buscarPorId(pid);
        if (!plato.getRestaurante().getId().equals(rid)) {
            throw new IllegalArgumentException("El plato no pertenece a este restaurante");
        }

        model.addAttribute("restaurante", restaurante);
        model.addAttribute("plato", plato);
        model.addAttribute("modo", "editar");
        return "form-plato";
    }

    // -------------------------------------------------------
    // Actualizar plato
    // -------------------------------------------------------
    @PostMapping("/{rid}/platos/{pid}/editar")
    public String editar(@PathVariable Long rid,
                        @PathVariable Long pid,
                        @Valid @ModelAttribute("plato") Plato datos,
                        BindingResult result,
                        @RequestParam(required = false) MultipartFile imagenFile,
                        @AuthenticationPrincipal UserDetails userDetails,
                        Model model,
                        RedirectAttributes flash) {
        Restaurante restaurante = restauranteService.buscarPorId(rid);
        verificarPropietario(restaurante, userDetails.getUsername());

        if (result.hasErrors()) {
            model.addAttribute("restaurante", restaurante);
            model.addAttribute("modo", "editar");
            return "form-plato";
        }

        try {
            Plato existente = platoService.buscarPorId(pid);
            if (!existente.getRestaurante().getId().equals(rid)) {
                throw new IllegalArgumentException("El plato no pertenece a este restaurante");
            }

            subirImagen(datos, imagenFile);
            platoService.actualizar(pid, datos, userDetails.getUsername());
            flash.addFlashAttribute("exito", "Plato actualizado correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al actualizar el plato: " + e.getMessage());
        }

        return "redirect:/restaurantes/" + rid;
    }

    // -------------------------------------------------------
    // Eliminar plato
    // -------------------------------------------------------
    @PostMapping("/{rid}/platos/{pid}/eliminar")
    public String eliminar(@PathVariable Long rid,
                          @PathVariable Long pid,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes flash) {
        Restaurante restaurante = restauranteService.buscarPorId(rid);
        verificarPropietario(restaurante, userDetails.getUsername());

        try {
            Plato plato = platoService.buscarPorId(pid);
            if (!plato.getRestaurante().getId().equals(rid)) {
                throw new IllegalArgumentException("El plato no pertenece a este restaurante");
            }
            platoService.eliminar(pid, userDetails.getUsername());
            flash.addFlashAttribute("exito", "Plato eliminado correctamente");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al eliminar el plato: " + e.getMessage());
        }

        return "redirect:/restaurantes/" + rid;
    }

    // ---- Utilidades ----

    private void verificarPropietario(Restaurante r, String email) {
        if (!r.getPropietario().getEmail().equals(email)) {
            throw new SecurityException("No tienes permiso para acceder a este restaurante");
        }
    }

    private void subirImagen(Plato plato, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            try {
                imagenService.validarImagen(file);
                String ruta = imagenService.guardar(file, "platos");
                plato.setImagen(ruta);
            } catch (IOException e) {
                throw new IOException("Error al subir la imagen", e);
            }
        }
    }
}
