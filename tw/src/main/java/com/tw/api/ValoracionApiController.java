package com.tw.api;

import com.tw.api.dto.ErrorResponse;
import com.tw.api.dto.ValoracionDTO;
import com.tw.model.Restaurante;
import com.tw.model.Valoracion;
import com.tw.repository.RestauranteRepository;
import com.tw.repository.ValoracionRepository;
import com.tw.service.ValoracionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * API REST - Gestión de valoraciones.
 *
 * Endpoints:
 *   POST   /api/restaurantes/{id}/valoraciones  → Valorar restaurante
 *   GET    /api/restaurantes/{id}/valoraciones  → Listar valoraciones
 *
 * También incluye el filtro de ordenar por valoración en GET /api/restaurantes/all?sort=valoracionMedia
 * (implementado en RestauranteApiController)
 */
@RestController
@RequestMapping("/api/restaurantes")
public class ValoracionApiController {

    private final ValoracionService valoracionService;
    private final ValoracionRepository valoracionRepo;
    private final RestauranteRepository restauranteRepo;

    public ValoracionApiController(ValoracionService valoracionService,
                                    ValoracionRepository valoracionRepo,
                                    RestauranteRepository restauranteRepo) {
        this.valoracionService = valoracionService;
        this.valoracionRepo = valoracionRepo;
        this.restauranteRepo = restauranteRepo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/restaurantes/{id}/valoraciones → Crear valoración
    // Req. mínimo 5: max 1 valoración por usuario por restaurante
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/{id}/valoraciones")
    public ResponseEntity<?> crear(@PathVariable Long id,
                                    @Valid @RequestBody ValoracionDTO.ValoracionRequest dto,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized",
                            "Debes autenticarte para valorar", "/api/restaurantes/" + id + "/valoraciones"));
        }

        Restaurante restaurante = restauranteRepo.findById(id).orElse(null);
        if (restaurante == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Restaurante no encontrado", "/api/restaurantes/" + id + "/valoraciones"));
        }

        // El servicio lanza IllegalArgumentException si el usuario ya valoró
        // Ese error se mapea a 400 en ApiExceptionHandler
        Valoracion nueva = new Valoracion();
        nueva.setPuntuacion(dto.getPuntuacion());
        nueva.setComentario(dto.getComentario());

        try {
            Valoracion creada = valoracionService.crear(id, nueva, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(creada));
        } catch (IllegalArgumentException e) {
            // Ya valoró este restaurante → 409 Conflict
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(409, "Conflict", e.getMessage(),
                            "/api/restaurantes/" + id + "/valoraciones"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/restaurantes/{id}/valoraciones → Listar valoraciones (público)
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}/valoraciones")
    public ResponseEntity<?> listar(@PathVariable Long id) {
        Restaurante restaurante = restauranteRepo.findById(id).orElse(null);
        if (restaurante == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Restaurante no encontrado", "/api/restaurantes/" + id + "/valoraciones"));
        }

        List<ValoracionDTO.ValoracionResponse> valoraciones = valoracionRepo.findByRestauranteId(id)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(valoraciones);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Conversor Entity → Response DTO
    // No se expone el email del usuario por privacidad
    // ─────────────────────────────────────────────────────────────────────────
    private ValoracionDTO.ValoracionResponse toResponse(Valoracion v) {
        ValoracionDTO.ValoracionResponse dto = new ValoracionDTO.ValoracionResponse();
        dto.setId(v.getId());
        dto.setPuntuacion(v.getPuntuacion());
        dto.setComentario(v.getComentario());
        dto.setFecha(v.getFecha());
        dto.setRestauranteId(v.getRestaurante() != null ? v.getRestaurante().getId() : null);
        dto.setRestauranteNombre(v.getRestaurante() != null ? v.getRestaurante().getNombre() : null);
        dto.setUsuarioId(v.getUsuario() != null ? v.getUsuario().getId() : null);
        // Solo nombre, sin email (privacidad)
        dto.setUsuarioNombre(v.getUsuario() != null
                ? v.getUsuario().getNombre() + " " + v.getUsuario().getApellidos() : null);
        return dto;
    }
}
