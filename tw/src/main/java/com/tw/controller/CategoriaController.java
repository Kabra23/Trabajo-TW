package com.tw.controller;

import com.tw.model.Categoria;
import com.tw.repository.CategoriaRepository;
import com.tw.service.ImagenService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/**
 * Rutas auxiliares de categorías accesibles desde el formulario de restaurante.
 * El CRUD completo de categorías se gestiona en CategoriaAdminController (/categorias).
 */
@Controller
public class CategoriaController {

    private final CategoriaRepository categoriaRepo;
    private final ImagenService imagenService;

    public CategoriaController(CategoriaRepository categoriaRepo,
                                ImagenService imagenService) {
        this.categoriaRepo = categoriaRepo;
        this.imagenService = imagenService;
    }

    // -------------------------------------------------------
    // Rutas auxiliares: crear/eliminar categoría desde el
    // formulario de restaurante (solo ADMIN)
    // -------------------------------------------------------

    @PostMapping("/restaurantes/categorias/nueva")
    @PreAuthorize("hasRole('ADMIN')")
    public String nueva(@Valid @ModelAttribute("categoria") Categoria categoria,
                        BindingResult result,
                        @RequestParam(required = false) MultipartFile imagenFile,
                        RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", "El nombre de la categoría no puede estar vacío");
            return "redirect:/restaurantes/nuevo";
        }
        if (categoriaRepo.existsByNombre(categoria.getNombre())) {
            flash.addFlashAttribute("error", "Ya existe una categoría con ese nombre");
            return "redirect:/restaurantes/nuevo";
        }
        if (imagenFile != null && !imagenFile.isEmpty()) {
            try {
                imagenService.validarImagen(imagenFile);
                String ruta = imagenService.guardar(imagenFile, "categorias");
                categoria.setImagen(ruta);
            } catch (IOException e) {
                flash.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
                return "redirect:/restaurantes/nuevo";
            }
        }
        categoriaRepo.save(categoria);
        flash.addFlashAttribute("exito", "Categoría '" + categoria.getNombre() + "' creada correctamente");
        return "redirect:/restaurantes/nuevo";
    }

    @PostMapping("/restaurantes/categorias/{id}/eliminar")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        categoriaRepo.findById(id).ifPresent(cat -> {
            categoriaRepo.delete(cat);
            flash.addFlashAttribute("exito", "Categoría eliminada");
        });
        return "redirect:/restaurantes";
    }
}
