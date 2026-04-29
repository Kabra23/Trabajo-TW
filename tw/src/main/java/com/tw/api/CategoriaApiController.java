package com.tw.api;

import com.tw.api.dto.ErrorResponse;
import com.tw.model.Categoria;
import com.tw.model.Restaurante;
import com.tw.repository.CategoriaRepository;
import com.tw.repository.RestauranteRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * API REST - Gestión de categorías (Extra 1 — hasta 1 punto).
 *
 * Endpoints:
 *   GET    /api/categorias              → Listar todas (público)
 *   GET    /api/categorias/{id}         → Detalle (público)
 *   POST   /api/categorias              → Crear (solo ADMIN)
 *   PUT    /api/categorias/{id}         → Editar (solo ADMIN)
 *   DELETE /api/categorias/{id}         → Eliminar (solo ADMIN)
 *
 *   GET    /api/categorias/{id}/restaurantes          → Restaurantes de esa categoría
 *   POST   /api/restaurantes/{id}/categorias/{catId}  → Añadir categoría a restaurante
 *   DELETE /api/restaurantes/{id}/categorias/{catId}  → Quitar categoría de restaurante
 */
@RestController
@RequestMapping("/api")
public class CategoriaApiController {

    private final CategoriaRepository categoriaRepo;
    private final RestauranteRepository restauranteRepo;

    public CategoriaApiController(CategoriaRepository categoriaRepo,
                                   RestauranteRepository restauranteRepo) {
        this.categoriaRepo = categoriaRepo;
        this.restauranteRepo = restauranteRepo;
    }

    // ── GET /api/categorias → listar todas ───────────────────────────────────
    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaResponse>> listar() {
        List<CategoriaResponse> lista = categoriaRepo.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    // ── GET /api/categorias/{id} → detalle ───────────────────────────────────
    @GetMapping("/categorias/{id}")
    public ResponseEntity<?> getCategoria(@PathVariable Long id) {
        Categoria cat = categoriaRepo.findById(id).orElse(null);
        if (cat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Categoría no encontrada", "/api/categorias/" + id));
        }
        return ResponseEntity.ok(toResponse(cat));
    }

    // ── POST /api/categorias → crear (ADMIN) ─────────────────────────────────
    @PostMapping("/categorias")
    public ResponseEntity<?> crear(@Valid @RequestBody CategoriaRequest dto,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (!esAdmin(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "Solo los administradores pueden crear categorías", "/api/categorias"));
        }
        if (categoriaRepo.existsByNombre(dto.getNombre())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(409, "Conflict",
                            "Ya existe una categoría con ese nombre", "/api/categorias"));
        }
        Categoria nueva = new Categoria();
        nueva.setNombre(dto.getNombre().trim());
        nueva.setDescripcion(dto.getDescripcion());
        Categoria guardada = categoriaRepo.save(nueva);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(guardada));
    }

    // ── PUT /api/categorias/{id} → editar (ADMIN) ────────────────────────────
    @PutMapping("/categorias/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id,
                                     @Valid @RequestBody CategoriaRequest dto,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        if (!esAdmin(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "Solo los administradores pueden editar categorías",
                            "/api/categorias/" + id));
        }
        Categoria cat = categoriaRepo.findById(id).orElse(null);
        if (cat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Categoría no encontrada", "/api/categorias/" + id));
        }
        // Verificar nombre duplicado (excepto la misma categoría)
        if (!cat.getNombre().equals(dto.getNombre())
                && categoriaRepo.existsByNombre(dto.getNombre())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(409, "Conflict",
                            "Ya existe una categoría con ese nombre", "/api/categorias/" + id));
        }
        cat.setNombre(dto.getNombre().trim());
        cat.setDescripcion(dto.getDescripcion());
        return ResponseEntity.ok(toResponse(categoriaRepo.save(cat)));
    }

    // ── DELETE /api/categorias/{id} → eliminar (ADMIN) ───────────────────────
    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        if (!esAdmin(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "Solo los administradores pueden eliminar categorías",
                            "/api/categorias/" + id));
        }
        Categoria cat = categoriaRepo.findById(id).orElse(null);
        if (cat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Categoría no encontrada", "/api/categorias/" + id));
        }
        categoriaRepo.delete(cat);
        return ResponseEntity.noContent().build();
    }

    // ── GET /api/categorias/{id}/restaurantes → acceso rápido ────────────────
    @GetMapping("/categorias/{id}/restaurantes")
    public ResponseEntity<?> restaurantesPorCategoria(@PathVariable Long id) {
        Categoria cat = categoriaRepo.findById(id).orElse(null);
        if (cat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Categoría no encontrada",
                            "/api/categorias/" + id + "/restaurantes"));
        }
        List<RestauranteResumen> lista = restauranteRepo.findByCategorias_Id(id)
                .stream().map(r -> new RestauranteResumen(
                        r.getId(), r.getNombre(), r.getLocalidad(),
                        r.getMediaValoraciones(), r.getAceptaPedidos()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    // ── POST /api/restaurantes/{rid}/categorias/{cid} → añadir ───────────────
    @PostMapping("/restaurantes/{rid}/categorias/{cid}")
    public ResponseEntity<?> addCategoria(@PathVariable Long rid,
                                           @PathVariable Long cid,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized",
                            "Debes autenticarte", "/api/restaurantes/" + rid + "/categorias/" + cid));
        }
        Restaurante rest = restauranteRepo.findById(rid).orElse(null);
        if (rest == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Restaurante no encontrado",
                            "/api/restaurantes/" + rid + "/categorias/" + cid));
        }
        if (!rest.getPropietario().getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "Solo el propietario puede modificar las categorías del restaurante",
                            "/api/restaurantes/" + rid + "/categorias/" + cid));
        }
        Categoria cat = categoriaRepo.findById(cid).orElse(null);
        if (cat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Categoría no encontrada",
                            "/api/restaurantes/" + rid + "/categorias/" + cid));
        }
        if (rest.getCategorias().contains(cat)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(409, "Conflict",
                            "El restaurante ya tiene asignada esta categoría",
                            "/api/restaurantes/" + rid + "/categorias/" + cid));
        }
        rest.getCategorias().add(cat);
        restauranteRepo.save(rest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new java.util.HashMap<String, Object>() {{
                    put("mensaje", "Categoría añadida correctamente");
                    put("restauranteId", rid);
                    put("categoriaId", cid);
                    put("categoriaNombre", cat.getNombre());
                }});
    }

    // ── DELETE /api/restaurantes/{rid}/categorias/{cid} → quitar ─────────────
    @DeleteMapping("/restaurantes/{rid}/categorias/{cid}")
    public ResponseEntity<?> removeCategoria(@PathVariable Long rid,
                                              @PathVariable Long cid,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized",
                            "Debes autenticarte", "/api/restaurantes/" + rid + "/categorias/" + cid));
        }
        Restaurante rest = restauranteRepo.findById(rid).orElse(null);
        if (rest == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Restaurante no encontrado",
                            "/api/restaurantes/" + rid + "/categorias/" + cid));
        }
        if (!rest.getPropietario().getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "Solo el propietario puede modificar las categorías del restaurante",
                            "/api/restaurantes/" + rid + "/categorias/" + cid));
        }
        Categoria cat = categoriaRepo.findById(cid).orElse(null);
        if (cat == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Categoría no encontrada",
                            "/api/restaurantes/" + rid + "/categorias/" + cid));
        }
        rest.getCategorias().remove(cat);
        restauranteRepo.save(rest);
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean esAdmin(UserDetails u) {
        return u != null && u.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private CategoriaResponse toResponse(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion(), c.getImagen());
    }

    // ── DTOs internos (ligeros, sin fichero separado) ─────────────────────────

    public static class CategoriaRequest {
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        private String nombre;
        private String descripcion;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    }

    public static class CategoriaResponse {
        private Long id;
        private String nombre;
        private String descripcion;
        private String imagen;

        public CategoriaResponse() {}
        public CategoriaResponse(Long id, String nombre, String descripcion, String imagen) {
            this.id = id; this.nombre = nombre;
            this.descripcion = descripcion; this.imagen = imagen;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public String getImagen() { return imagen; }
        public void setImagen(String imagen) { this.imagen = imagen; }
    }

    public static class RestauranteResumen {
        private Long id;
        private String nombre;
        private String localidad;
        private Double mediaValoraciones;
        private Boolean aceptaPedidos;

        public RestauranteResumen() {}
        public RestauranteResumen(Long id, String nombre, String localidad,
                                   Double mediaValoraciones, Boolean aceptaPedidos) {
            this.id = id; this.nombre = nombre; this.localidad = localidad;
            this.mediaValoraciones = mediaValoraciones; this.aceptaPedidos = aceptaPedidos;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getLocalidad() { return localidad; }
        public void setLocalidad(String localidad) { this.localidad = localidad; }
        public Double getMediaValoraciones() { return mediaValoraciones; }
        public void setMediaValoraciones(Double v) { this.mediaValoraciones = v; }
        public Boolean getAceptaPedidos() { return aceptaPedidos; }
        public void setAceptaPedidos(Boolean aceptaPedidos) { this.aceptaPedidos = aceptaPedidos; }
    }
}
