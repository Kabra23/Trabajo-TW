package com.tw.controller;

import com.tw.model.Categoria;
import com.tw.model.Restaurante;
import com.tw.repository.CategoriaRepository;
import com.tw.repository.RestauranteRepository;
import com.tw.service.ImagenService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

/**
 * Controlador MVC para la gestión de categorías.
 *
 * Rutas:
 *   GET  /admin/categorias          → listado
 *   GET  /admin/categorias/nueva    → formulario nueva
 *   POST /admin/categorias/nueva    → guardar nueva
 *   GET  /admin/categorias/{id}/editar → formulario editar
 *   POST /admin/categorias/{id}/editar → guardar cambios
 *   POST /admin/categorias/{id}/eliminar → eliminar
 *
 * Solo accesible para ROLE_ADMIN.
 */
@Controller
@RequestMapping("/admin/categorias")
@PreAuthorize("hasRole('ADMIN')")
public class CategoriaAdminController {

    private final CategoriaRepository categoriaRepo;
    private final RestauranteRepository restauranteRepo;
    private final ImagenService imagenService;

    public CategoriaAdminController(CategoriaRepository categoriaRepo,
                                     RestauranteRepository restauranteRepo,
                                     ImagenService imagenService) {
        this.categoriaRepo = categoriaRepo;
        this.restauranteRepo = restauranteRepo;
        this.imagenService = imagenService;
    }

    // ── GET /admin/categorias → listado ──────────────────────────────────────
    @GetMapping
    public String listar(Model model) {
        List<Categoria> categorias = categoriaRepo.findAll();
        model.addAttribute("categorias", categorias);
        // Para cada categoría, cuántos restaurantes tiene
        model.addAttribute("nueva", new Categoria());
        return "admin/categorias";
    }

    // ── GET /admin/categorias/nueva → formulario ─────────────────────────────
    @GetMapping("/nueva")
    public String nuevaForm(Model model) {
        model.addAttribute("categoria", new Categoria());
        model.addAttribute("modo", "crear");
        return "admin/form-categoria";
    }

    // ── POST /admin/categorias/nueva → crear ─────────────────────────────────
    @PostMapping("/nueva")
    public String crear(@Valid @ModelAttribute("categoria") Categoria categoria,
                         BindingResult result,
                         @RequestParam(required = false) MultipartFile imagenFile,
                         Model model,
                         RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("modo", "crear");
            return "admin/form-categoria";
        }
        if (categoriaRepo.existsByNombre(categoria.getNombre())) {
            result.rejectValue("nombre", "nombre.duplicado",
                    "Ya existe una categoría con ese nombre");
            model.addAttribute("modo", "crear");
            return "admin/form-categoria";
        }
        subirImagenSiHay(categoria, imagenFile, flash);
        categoriaRepo.save(categoria);
        flash.addFlashAttribute("exito", "Categoría '" + categoria.getNombre() + "' creada correctamente");
        return "redirect:/admin/categorias";
    }

    // ── GET /admin/categorias/{id}/editar → formulario ───────────────────────
    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        Categoria cat = categoriaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        model.addAttribute("categoria", cat);
        model.addAttribute("modo", "editar");
        // Restaurantes que usan esta categoría (info útil para el admin)
        model.addAttribute("restaurantesConCat",
                restauranteRepo.findByCategorias_Id(id));
        return "admin/form-categoria";
    }

    // ── POST /admin/categorias/{id}/editar → guardar ─────────────────────────
    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                          @Valid @ModelAttribute("categoria") Categoria datos,
                          BindingResult result,
                          @RequestParam(required = false) MultipartFile imagenFile,
                          Model model,
                          RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("modo", "editar");
            return "admin/form-categoria";
        }
        Categoria existente = categoriaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        // Verificar duplicado solo si cambió el nombre
        if (!existente.getNombre().equalsIgnoreCase(datos.getNombre())
                && categoriaRepo.existsByNombre(datos.getNombre())) {
            result.rejectValue("nombre", "nombre.duplicado",
                    "Ya existe una categoría con ese nombre");
            model.addAttribute("modo", "editar");
            return "admin/form-categoria";
        }

        existente.setNombre(datos.getNombre().trim());
        existente.setDescripcion(datos.getDescripcion());
        subirImagenSiHay(existente, imagenFile, flash);
        categoriaRepo.save(existente);
        flash.addFlashAttribute("exito", "Categoría actualizada correctamente");
        return "redirect:/admin/categorias";
    }

    // ── POST /admin/categorias/{id}/eliminar → borrar ────────────────────────
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        Categoria cat = categoriaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        long numRestaurantes = restauranteRepo.findByCategorias_Id(id).size();
        if (numRestaurantes > 0) {
            flash.addFlashAttribute("error",
                    "No se puede eliminar: " + numRestaurantes +
                    " restaurante(s) usan esta categoría. Quítala de los restaurantes primero.");
            return "redirect:/admin/categorias";
        }
        String nombre = cat.getNombre();
        categoriaRepo.delete(cat);
        flash.addFlashAttribute("exito", "Categoría '" + nombre + "' eliminada");
        return "redirect:/admin/categorias";
    }

    // ── Privado ───────────────────────────────────────────────────────────────

    private void subirImagenSiHay(Categoria cat, MultipartFile file,
                                   RedirectAttributes flash) {
        if (file != null && !file.isEmpty()) {
            try {
                imagenService.validarImagen(file);
                String ruta = imagenService.guardar(file, "categorias");
                cat.setImagen(ruta);
            } catch (IOException e) {
                // No falla el guardado, solo avisa
                if (flash != null) {
                    flash.addFlashAttribute("advertencia",
                            "No se pudo subir la imagen: " + e.getMessage());
                }
            }
        }
    }
}
