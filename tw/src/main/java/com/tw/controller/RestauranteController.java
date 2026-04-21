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

    // ---- Listado principal con filtros (req. mínimo 6 y 7) ----
    @GetMapping("/restaurantes")
    public String listar(
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer valoracion,
            @RequestParam(required = false) Integer precio,
            @RequestParam(required = false) Boolean bikeFriendly,
            @RequestParam(required = false) String orden,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        List<Restaurante> restaurantes = restauranteService.buscar(q, categoria, filtro, valoracion, precio, bikeFriendly, orden);

        model.addAttribute("restaurantes", restaurantes);
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("filtro", filtro);
        model.addAttribute("categoria", categoria);
        model.addAttribute("q", q);
        model.addAttribute("valoracion", valoracion);
        model.addAttribute("precio", precio);
        model.addAttribute("bikeFriendly", bikeFriendly);
        model.addAttribute("orden", orden);

        if (userDetails != null) {
            try {
                Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
                Direccion dirPrincipal = usuario.getDireccionPrincipal();
                if (dirPrincipal != null) {
                    model.addAttribute("direccionUsuario", dirPrincipal.getDireccion());
                }
            } catch (Exception ignored) {}
        }

        return "restaurantes";
    }

    // ---- Búsqueda avanzada (extra 3) ----
    @GetMapping("/busqueda")
    public String busquedaAvanzada(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false) String localidad,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            Model model) {

        List<Restaurante> resultados = null;
        boolean buscado = q != null || categoria != null || localidad != null
                || precioMin != null || precioMax != null;

        if (buscado) {
            resultados = restauranteService.busquedaAvanzada(q, categoria, localidad, precioMin, precioMax);
        }

        model.addAttribute("resultados", resultados);
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("q", q);
        model.addAttribute("categoria", categoria);
        model.addAttribute("localidad", localidad);
        model.addAttribute("precioMin", precioMin);
        model.addAttribute("precioMax", precioMax);
        model.addAttribute("buscado", buscado);
        return "busqueda-avanzada";
    }

    // ---- Detalle del restaurante ----
    @GetMapping("/restaurantes/{id}")
    public String detalle(@PathVariable Long id,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        Restaurante restaurante = restauranteService.buscarPorId(id);
        model.addAttribute("restaurante", restaurante);

        List<Restaurante> relacionados = restauranteService.buscarRelacionados(restaurante);
        model.addAttribute("relacionados", relacionados);

        if (userDetails != null) {
            boolean esPropietario = restaurante.getPropietario()
                    .getEmail().equals(userDetails.getUsername());
            model.addAttribute("esPropietario", esPropietario);
        } else {
            model.addAttribute("esPropietario", false);
        }
        return "detalle-restaurante";
    }

    // ---- Guardar etiquetas del menú ----
    @PostMapping("/restaurantes/{id}/etiquetas")
    public String guardarEtiquetas(@PathVariable Long id,
                                   @RequestParam String etiquetasMenu,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes flash) {
        try {
            restauranteService.guardarEtiquetasMenu(id, etiquetasMenu, userDetails.getUsername());
            flash.addFlashAttribute("exito", "Etiquetas del menu actualizadas");
        } catch (SecurityException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/restaurantes/" + id;
    }

    // ---- Formulario nuevo restaurante ----
    @GetMapping("/restaurantes/nuevo")
    public String nuevoForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("restaurante", new Restaurante());
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("modo", "crear");
        if (userDetails != null) {
            Usuario usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
            model.addAttribute("misRestaurantes", restauranteService.buscarPorPropietario(usuario));
        }
        return "form-restaurante";
    }

    @PostMapping("/restaurantes/nuevo")
    public String crear(@Valid @ModelAttribute("restaurante") Restaurante restaurante,
                        BindingResult result,
                        @RequestParam(required = false) List<Long> categoriaIds,
                        @RequestParam(required = false) MultipartFile imagenFile,
                        @RequestParam(required = false) MultipartFile imagenBannerFile,
                        @AuthenticationPrincipal UserDetails userDetails,
                        Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaRepo.findAll());
            model.addAttribute("modo", "crear");
            return "form-restaurante";
        }
        // Subir imagen principal (tarjeta)
        subirImagenPrincipal(restaurante, imagenFile);
        // Subir imagen banner
        subirImagenBanner(restaurante, imagenBannerFile);

        restauranteService.crear(restaurante, userDetails.getUsername(), categoriaIds);
        flash.addFlashAttribute("exito", "Restaurante creado correctamente");
        return "redirect:/restaurantes";
    }

    // ---- Editar restaurante ----
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
                         @RequestParam(required = false) MultipartFile imagenBannerFile,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaRepo.findAll());
            model.addAttribute("modo", "editar");
            return "form-restaurante";
        }
        try {
            // Subir imagen principal si se proporcionó una nueva
            subirImagenPrincipal(datos, imagenFile);
            // Subir imagen banner si se proporcionó una nueva
            subirImagenBanner(datos, imagenBannerFile);

            restauranteService.actualizar(id, datos, userDetails.getUsername(), categoriaIds);
            flash.addFlashAttribute("exito", "Restaurante actualizado correctamente");
        } catch (SecurityException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/restaurantes/" + id;
    }

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

    // ---- Estado acepta/no acepta pedidos (req. mínimo 7) ----
    @PostMapping("/restaurantes/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam boolean aceptaPedidos,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes flash) {
        try {
            restauranteService.cambiarEstado(id, aceptaPedidos, userDetails.getUsername());
            flash.addFlashAttribute("exito", aceptaPedidos
                    ? "El restaurante ahora acepta pedidos"
                    : "El restaurante ya no acepta pedidos");
        } catch (SecurityException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/restaurantes/" + id;
    }

    // ---- Favoritos ----
    @PostMapping("/restaurantes/{id}/favorito")
    public String toggleFavorito(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes flash) {
        restauranteService.toggleFavorito(id, userDetails.getUsername());
        return "redirect:/restaurantes/" + id;
    }

    // ---- Utilidades ----

    private void verificarPropietario(Restaurante r, String email) {
        if (!r.getPropietario().getEmail().equals(email)) {
            throw new SecurityException("No tienes permiso para editar este restaurante");
        }
    }

    /**
     * Sube la imagen principal del restaurante (la que aparece en el listado/tarjeta).
     * Solo actualiza si se envía un archivo no vacío.
     */
    private void subirImagenPrincipal(Restaurante restaurante, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                imagenService.validarImagen(file);
                String ruta = imagenService.guardar(file, "restaurantes");
                restaurante.setImagen(ruta);
            } catch (IOException e) {
                System.err.println("Error al subir imagen principal: " + e.getMessage());
            }
        }
    }

    /**
     * Sube la imagen banner del restaurante (la que aparece en el hero del detalle).
     * Solo actualiza si se envía un archivo no vacío.
     */
    private void subirImagenBanner(Restaurante restaurante, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                imagenService.validarImagen(file);
                String ruta = imagenService.guardar(file, "restaurantes");
                restaurante.setImagenBanner(ruta);
            } catch (IOException e) {
                System.err.println("Error al subir imagen banner: " + e.getMessage());
            }
        }
    }
}
