package com.tw.controller;

import com.tw.model.*;
import com.tw.repository.CategoriaRepository;
import com.tw.service.ImagenService;
import com.tw.service.RestauranteService;
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
import java.util.List;

@Controller
public class RestauranteController {

    private final RestauranteService restauranteService;
    private final CategoriaRepository categoriaRepo;
    private final UsuarioService usuarioService;
    private final ImagenService imagenService;

    public RestauranteController(RestauranteService restauranteService,
                                 CategoriaRepository categoriaRepo,
                                 UsuarioService usuarioService,
                                 ImagenService imagenService) {
        this.restauranteService = restauranteService;
        this.categoriaRepo = categoriaRepo;
        this.usuarioService = usuarioService;
        this.imagenService = imagenService;
    }

    // -------------------------------------------------------
    // Listado (req. mínimo 6, 7 y extra ordenar por valoración)
    // -------------------------------------------------------
    @GetMapping("/restaurantes")
    public String listar(@RequestParam(required = false) String q,
                         @RequestParam(required = false, defaultValue = "todos") String filtro,
                         @RequestParam(required = false) Long categoria,
                         Model model) {
        List<Restaurante> restaurantes;

        if (q != null && !q.isBlank()) {
            restaurantes = restauranteService.buscar(q);
        } else if (categoria != null) {
            restaurantes = restauranteService.buscarPorCategoria(categoria);
        } else {
            restaurantes = switch (filtro) {
                case "acepta"     -> restauranteService.listarQueAceptanPedidos();
                case "noAcepta"   -> restauranteService.listarQueNoAceptanPedidos();
                case "valoracion" -> restauranteService.listarOrdenadosPorValoracion();
                default           -> restauranteService.listarTodos();
            };
        }

        model.addAttribute("restaurantes", restaurantes);
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("filtro", filtro);
        model.addAttribute("q", q);
        return "restaurantes";
    }

    // -------------------------------------------------------
    // Detalle
    // -------------------------------------------------------
    @GetMapping("/restaurantes/{id}")
    public String detalle(@PathVariable Long id,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        Restaurante restaurante = restauranteService.buscarPorId(id);
        model.addAttribute("restaurante", restaurante);

        if (userDetails != null) {
            boolean esPropietario = restaurante.getPropietario()
                    .getEmail().equals(userDetails.getUsername());
            model.addAttribute("esPropietario", esPropietario);
            model.addAttribute("usuarioLogueado", true);
        } else {
            model.addAttribute("esPropietario", false);
            model.addAttribute("usuarioLogueado", false);
        }
        return "detalle-restaurante";
    }

    // -------------------------------------------------------
    // Crear restaurante
    // -------------------------------------------------------
    @GetMapping("/restaurantes/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("restaurante", new Restaurante());
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("modo", "crear");
        return "form-restaurante";
    }

    @PostMapping("/restaurantes/nuevo")
    public String crear(@Valid @ModelAttribute("restaurante") Restaurante restaurante,
                        BindingResult result,
                        @RequestParam(required = false) List<Long> categoriaIds,
                        @RequestParam(required = false) MultipartFile imagenFile,
                        @AuthenticationPrincipal UserDetails userDetails,
                        Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaRepo.findAll());
            model.addAttribute("modo", "crear");
            return "form-restaurante";
        }
        // Subir imagen si se proporcionó
        subirImagen(restaurante, imagenFile);

        restauranteService.crear(restaurante, userDetails.getUsername(), categoriaIds);
        flash.addFlashAttribute("exito", "Restaurante creado correctamente");
        return "redirect:/restaurantes";
    }

    // -------------------------------------------------------
    // Editar restaurante (solo propietario — req. mínimo 2)
    // -------------------------------------------------------
    @GetMapping("/restaurantes/{id}/editar")
    public String editarForm(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        Restaurante restaurante = restauranteService.buscarPorId(id);
        verificarPropietario(restaurante, userDetails.getUsername());
        model.addAttribute("restaurante", restaurante);
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("modo", "editar");
        return "form-restaurante";
    }

    @PostMapping("/restaurantes/{id}/editar")
    public String editar(@PathVariable Long id,
                         @Valid @ModelAttribute("restaurante") Restaurante datos,
                         BindingResult result,
                         @RequestParam(required = false) List<Long> categoriaIds,
                         @RequestParam(required = false) MultipartFile imagenFile,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaRepo.findAll());
            model.addAttribute("modo", "editar");
            return "form-restaurante";
        }
        try {
            subirImagen(datos, imagenFile);
            restauranteService.actualizar(id, datos, userDetails.getUsername(), categoriaIds);
            flash.addFlashAttribute("exito", "Restaurante actualizado correctamente");
        } catch (SecurityException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/restaurantes/" + id;
    }

    // -------------------------------------------------------
    // Eliminar (solo propietario)
    // -------------------------------------------------------
    @PostMapping("/restaurantes/{id}/eliminar")
    public String eliminar(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes flash) {
        try {
            restauranteService.eliminar(id, userDetails.getUsername());
            flash.addFlashAttribute("exito", "Restaurante eliminado");
        } catch (SecurityException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/restaurantes";
    }

    // -------------------------------------------------------
    // Cambiar estado acepta/no acepta pedidos (req. mínimo 7)
    // -------------------------------------------------------
    @PostMapping("/restaurantes/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam boolean aceptaPedidos,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes flash) {
        try {
            restauranteService.cambiarEstado(id, aceptaPedidos, userDetails.getUsername());
            String msg = aceptaPedidos
                    ? "El restaurante ahora acepta pedidos"
                    : "El restaurante ya no acepta pedidos";
            flash.addFlashAttribute("exito", msg);
        } catch (SecurityException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/restaurantes/" + id;
    }

    // -------------------------------------------------------
    // Favoritos (extra)
    // -------------------------------------------------------
    @PostMapping("/restaurantes/{id}/favorito")
    public String toggleFavorito(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes flash) {
        restauranteService.toggleFavorito(id, userDetails.getUsername());
        return "redirect:/restaurantes/" + id;
    }

    // ---- utilidades ----

    private void verificarPropietario(Restaurante r, String email) {
        if (!r.getPropietario().getEmail().equals(email)) {
            throw new SecurityException("No tienes permiso");
        }
    }

    private void subirImagen(Restaurante restaurante, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                imagenService.validarImagen(file);
                String ruta = imagenService.guardar(file, "restaurantes");
                restaurante.setImagen(ruta);
            } catch (IOException e) {
                System.err.println("Error al subir imagen del restaurante: " + e.getMessage());
            }
        }
    }
}
