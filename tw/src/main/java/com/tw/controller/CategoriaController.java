package com.tw.controller;

import com.tw.model.Categoria;
import com.tw.repository.CategoriaRepository;
import com.tw.service.ImagenService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaRepository categoriaRepo;
    private final ImagenService imagenService;

    public CategoriaController(CategoriaRepository categoriaRepo, ImagenService imagenService) {
        this.categoriaRepo = categoriaRepo;
        this.imagenService = imagenService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaRepo.findAll());
        return "categorias";
    }

    @GetMapping("/nueva")
    public String nuevaForm(Model model) {
        model.addAttribute("categoria", new Categoria());
        model.addAttribute("modo", "crear");
        return "form-categoria.html";
    }

    @PostMapping("/nueva")
    public String crear(@ModelAttribute Categoria categoria,
                        @RequestParam(required = false) MultipartFile imagenFile,
                        RedirectAttributes flash) {
        if (categoriaRepo.existsByNombre(categoria.getNombre())) {
            flash.addFlashAttribute("error", "Ya existe una categoría con ese nombre");
            return "redirect:/categorias/nueva";
        }
        subirImagen(categoria, imagenFile);
        categoriaRepo.save(categoria);
        flash.addFlashAttribute("exito", "Categoría creada correctamente");
        return "redirect:/categorias";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        Categoria categoria = categoriaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        model.addAttribute("categoria", categoria);
        model.addAttribute("modo", "editar");
        return "form-categoria.html";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         @ModelAttribute Categoria datos,
                         @RequestParam(required = false) MultipartFile imagenFile,
                         RedirectAttributes flash) {
        Categoria existente = categoriaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        subirImagen(existente, imagenFile);
        categoriaRepo.save(existente);
        flash.addFlashAttribute("exito", "Categoría actualizada");
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        categoriaRepo.deleteById(id);
        flash.addFlashAttribute("exito", "Categoría eliminada");
        return "redirect:/categorias";
    }

    private void subirImagen(Categoria categoria, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                String ruta = imagenService.guardar(file, "categorias");
                categoria.setImagen(ruta);
            } catch (IOException e) {
                // Si falla la imagen no se bloquea el guardado
                System.err.println("Error al subir imagen de categoría: " + e.getMessage());
            }
        }
    }
}