package com.tw.controller;

import com.tw.model.Categoria;
import com.tw.repository.CategoriaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categorias")
public class CategoriaAdminController {

    private final CategoriaRepository categoriaRepo;

    public CategoriaAdminController(CategoriaRepository categoriaRepo) {
        this.categoriaRepo = categoriaRepo;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("nueva", new Categoria());
        return "categorias";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/nueva")
    public String nueva(@RequestParam String nombre,
                        @RequestParam(required = false) String descripcion,
                        RedirectAttributes flash) {
        if (nombre == null || nombre.isBlank()) {
            flash.addFlashAttribute("error", "El nombre es obligatorio");
            return "redirect:/categorias";
        }
        if (categoriaRepo.existsByNombre(nombre.trim())) {
            flash.addFlashAttribute("error", "Ya existe una categoria con ese nombre");
            return "redirect:/categorias";
        }
        Categoria cat = new Categoria();
        cat.setNombre(nombre.trim());
        cat.setDescripcion(descripcion != null && !descripcion.isBlank() ? descripcion.trim() : null);
        categoriaRepo.save(cat);
        flash.addFlashAttribute("exito", "Categoria creada correctamente");
        return "redirect:/categorias";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        Categoria cat = categoriaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada"));
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("editar", cat);
        model.addAttribute("nueva", new Categoria());
        return "categorias";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         @RequestParam String nombre,
                         @RequestParam(required = false) String descripcion,
                         RedirectAttributes flash) {
        Categoria cat = categoriaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada"));
        cat.setNombre(nombre.trim());
        cat.setDescripcion(descripcion != null && !descripcion.isBlank() ? descripcion.trim() : null);
        categoriaRepo.save(cat);
        flash.addFlashAttribute("exito", "Categoria actualizada correctamente");
        return "redirect:/categorias";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        categoriaRepo.findById(id).ifPresent(cat -> {
            categoriaRepo.delete(cat);
            flash.addFlashAttribute("exito", "Categoria eliminada");
        });
        return "redirect:/categorias";
    }
}
