package com.tw.api;

import com.tw.api.dto.ErrorResponse;
import com.tw.api.dto.PlatoDTO;
import com.tw.model.Plato;
import com.tw.model.Restaurante;
import com.tw.repository.PlatoRepository;
import com.tw.repository.RestauranteRepository;
import com.tw.service.PlatoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * API REST - Gestión de platos.
 *
 * Endpoints:
 *   GET    /api/restaurantes/{id}/platos   → Listar platos (público)
 *   POST   /api/restaurantes/{id}/platos   → Crear plato (solo propietario)
 *   PUT    /api/platos/{id}               → Editar plato (solo propietario)
 *   DELETE /api/platos/{id}               → Eliminar plato (solo propietario)
 */
@RestController
@RequestMapping("/api")
public class PlatoApiController {

    private final PlatoService platoService;
    private final PlatoRepository platoRepo;
    private final RestauranteRepository restauranteRepo;

    public PlatoApiController(PlatoService platoService,
                               PlatoRepository platoRepo,
                               RestauranteRepository restauranteRepo) {
        this.platoService = platoService;
        this.platoRepo = platoRepo;
        this.restauranteRepo = restauranteRepo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/restaurantes/{id}/platos → Listar platos (público)
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/restaurantes/{id}/platos")
    public ResponseEntity<?> listarPlatos(@PathVariable Long id) {
        Restaurante restaurante = restauranteRepo.findById(id).orElse(null);
        if (restaurante == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Restaurante no encontrado", "/api/restaurantes/" + id + "/platos"));
        }

        List<PlatoDTO.PlatoResponse> platos = platoRepo.findByRestauranteId(id).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(platos);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/restaurantes/{id}/platos → Crear plato (solo propietario)
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/restaurantes/{id}/platos")
    public ResponseEntity<?> crearPlato(@PathVariable Long id,
                                         @Valid @RequestBody PlatoDTO.PlatoRequest dto,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized",
                            "Debes autenticarte", "/api/restaurantes/" + id + "/platos"));
        }

        Restaurante restaurante = restauranteRepo.findById(id).orElse(null);
        if (restaurante == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Restaurante no encontrado", "/api/restaurantes/" + id + "/platos"));
        }

        // Verificar propietario
        if (!restaurante.getPropietario().getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "Solo el propietario puede añadir platos",
                            "/api/restaurantes/" + id + "/platos"));
        }

        Plato plato = fromRequest(dto);
        Plato creado = platoService.crear(id, plato, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(creado));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/platos/{id} → Editar plato (solo propietario del restaurante)
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/platos/{id}")
    public ResponseEntity<?> editarPlato(@PathVariable Long id,
                                          @Valid @RequestBody PlatoDTO.PlatoRequest dto,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized",
                            "Debes autenticarte", "/api/platos/" + id));
        }

        Plato plato = platoRepo.findById(id).orElse(null);
        if (plato == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Plato no encontrado", "/api/platos/" + id));
        }

        // Verificar que el usuario es propietario del restaurante del plato
        if (!plato.getRestaurante().getPropietario().getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "Solo el propietario del restaurante puede editar este plato",
                            "/api/platos/" + id));
        }

        Plato datos = fromRequest(dto);
        Plato actualizado = platoService.actualizar(id, datos, userDetails.getUsername());

        return ResponseEntity.ok(toResponse(actualizado));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/platos/{id} → Eliminar plato (solo propietario)
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/platos/{id}")
    public ResponseEntity<?> eliminarPlato(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized",
                            "Debes autenticarte", "/api/platos/" + id));
        }

        Plato plato = platoRepo.findById(id).orElse(null);
        if (plato == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Plato no encontrado", "/api/platos/" + id));
        }

        if (!plato.getRestaurante().getPropietario().getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "Solo el propietario del restaurante puede eliminar este plato",
                            "/api/platos/" + id));
        }

        platoService.eliminar(id, userDetails.getUsername());
        return ResponseEntity.noContent().build(); // 204
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Conversores
    // ─────────────────────────────────────────────────────────────────────────

    private PlatoDTO.PlatoResponse toResponse(Plato p) {
        PlatoDTO.PlatoResponse dto = new PlatoDTO.PlatoResponse();
        dto.setId(p.getId());
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setPrecio(p.getPrecio());
        dto.setImagen(p.getImagen());
        dto.setEtiquetaMenu(p.getEtiquetaMenu());
        dto.setRestauranteId(p.getRestaurante() != null ? p.getRestaurante().getId() : null);
        return dto;
    }

    private Plato fromRequest(PlatoDTO.PlatoRequest dto) {
        Plato p = new Plato();
        p.setNombre(dto.getNombre());
        p.setDescripcion(dto.getDescripcion());
        p.setPrecio(dto.getPrecio());
        p.setEtiquetaMenu(dto.getEtiquetaMenu());
        return p;
    }
}
