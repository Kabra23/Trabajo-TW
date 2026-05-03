package com.tw.api;

import com.tw.api.dto.ErrorResponse;
import com.tw.api.dto.RestauranteDTO;
import com.tw.model.Categoria;
import com.tw.model.Restaurante;
import com.tw.model.Usuario;
import com.tw.repository.CategoriaRepository;
import com.tw.repository.RestauranteRepository;
import com.tw.repository.UsuarioRepository;
import com.tw.service.RestauranteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * API REST - Gestión de restaurantes.
 *
 * Endpoints:
 *   GET    /api/restaurantes/all              → Todos los restaurantes (público)
 *   GET    /api/restaurantes                  → Restaurantes del usuario autenticado
 *   GET    /api/restaurantes/{id}             → Detalle (público)
 *   POST   /api/restaurantes                  → Crear (autenticado)
 *   PUT    /api/restaurantes/{id}             → Editar (solo propietario)
 *   DELETE /api/restaurantes/{id}             → Eliminar (solo propietario)
 *   PUT    /api/restaurantes/{id}/estado      → Cambiar estado (solo propietario)
 *
 * Query params en GET /api/restaurantes/all:
 *   ?estado=acepta|no-acepta   → filtrar por estado (req. 7)
 *   ?filtro=acepta|noAcepta    → alias de ?estado (compatibilidad)
 *   ?q=texto                   → buscar por nombre o localidad (req. 6)
 *   ?localidad=Merida          → filtrar exacto por localidad
 *   ?sort=valoracionMedia      → ordenar por media de valoraciones (req. 5)
 */
@RestController
@RequestMapping("/api/restaurantes")
public class RestauranteApiController {

    private final RestauranteService restauranteService;
    private final RestauranteRepository restauranteRepo;
    private final UsuarioRepository usuarioRepo;
    private final CategoriaRepository categoriaRepo;

    public RestauranteApiController(RestauranteService restauranteService,
                                    RestauranteRepository restauranteRepo,
                                    UsuarioRepository usuarioRepo,
                                    CategoriaRepository categoriaRepo) {
        this.restauranteService = restauranteService;
        this.restauranteRepo = restauranteRepo;
        this.usuarioRepo = usuarioRepo;
        this.categoriaRepo = categoriaRepo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/restaurantes/all → Listado completo con filtros (público)
    // Req. mínimo 6 (búsqueda) y 7 (estado)
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/all")
    public ResponseEntity<List<RestauranteDTO.RestauranteResponse>> listarTodos(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String localidad,
            @RequestParam(required = false) String filtro,   // alias legacy
            @RequestParam(required = false) String estado,   // ?estado=acepta | no-acepta
            @RequestParam(required = false) String sort) {

        // Normalizar: 'estado' tiene prioridad sobre 'filtro' (alias por compatibilidad)
        String estadoEfectivo = (estado != null) ? estado : filtro;

        List<Restaurante> restaurantes;

        // Búsqueda por texto (nombre o localidad)
        if (q != null && !q.isBlank()) {
            restaurantes = restauranteRepo
                    .findByNombreContainingIgnoreCaseOrLocalidadContainingIgnoreCase(q, q);
        } else if (localidad != null && !localidad.isBlank()) {
            restaurantes = restauranteRepo
                    .findByNombreContainingIgnoreCaseOrLocalidadContainingIgnoreCase(localidad, localidad);
        } else if ("acepta".equalsIgnoreCase(estadoEfectivo)) {
            restaurantes = restauranteRepo.findByAceptaPedidos(true);
        } else if ("no-acepta".equalsIgnoreCase(estadoEfectivo)
                || "noAcepta".equalsIgnoreCase(estadoEfectivo)) {
            restaurantes = restauranteRepo.findByAceptaPedidos(false);
        } else if ("valoracionMedia".equals(sort)) {
            restaurantes = restauranteRepo.findAllByOrderByMediaValoracionesDesc();
        } else {
            restaurantes = restauranteRepo.findAll();
        }

        List<RestauranteDTO.RestauranteResponse> respuesta = restaurantes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/restaurantes → Restaurantes del usuario autenticado
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> misRestaurantes(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized",
                            "Debes autenticarte para ver tus restaurantes", "/api/restaurantes"));
        }

        Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername()).orElseThrow();
        List<Restaurante> restaurantes = restauranteRepo.findByPropietario(usuario);

        return ResponseEntity.ok(restaurantes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/restaurantes/{id} → Detalle de un restaurante (público)
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getRestaurante(@PathVariable Long id) {
        Restaurante restaurante = restauranteRepo.findById(id).orElse(null);

        if (restaurante == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Restaurante no encontrado", "/api/restaurantes/" + id));
        }

        return ResponseEntity.ok(toResponse(restaurante));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/restaurantes → Crear restaurante (autenticado)
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody RestauranteDTO.RestauranteRequest dto,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized",
                            "Debes autenticarte para crear un restaurante", "/api/restaurantes"));
        }

        Restaurante restaurante = fromRequest(dto);
        Restaurante creado = restauranteService.crear(restaurante,
                userDetails.getUsername(), dto.getCategoriaIds());

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(creado));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/restaurantes/{id} → Editar restaurante (solo propietario)
    // Requisito: debe devolver datos previamente cargados para editar
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id,
                                     @Valid @RequestBody RestauranteDTO.RestauranteRequest dto,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized",
                            "Debes autenticarte", "/api/restaurantes/" + id));
        }

        Restaurante existente = restauranteRepo.findById(id).orElse(null);
        if (existente == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Restaurante no encontrado", "/api/restaurantes/" + id));
        }

        // Verificar propietario (403 si no es el dueño)
        if (!existente.getPropietario().getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "Solo el propietario puede editar este restaurante",
                            "/api/restaurantes/" + id));
        }

        Restaurante datos = fromRequest(dto);
        Restaurante actualizado = restauranteService.actualizar(id, datos,
                userDetails.getUsername(), dto.getCategoriaIds());

        return ResponseEntity.ok(toResponse(actualizado));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/restaurantes/{id} → Eliminar restaurante (solo propietario)
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized",
                            "Debes autenticarte", "/api/restaurantes/" + id));
        }

        Restaurante existente = restauranteRepo.findById(id).orElse(null);
        if (existente == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Restaurante no encontrado", "/api/restaurantes/" + id));
        }

        if (!existente.getPropietario().getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "Solo el propietario puede eliminar este restaurante",
                            "/api/restaurantes/" + id));
        }

        restauranteService.eliminar(id, userDetails.getUsername());
        return ResponseEntity.noContent().build(); // 204
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/restaurantes/{id}/estado → Cambiar estado acepta/no acepta
    // Req. mínimo 7
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id,
                                            @Valid @RequestBody RestauranteDTO.EstadoRequest dto,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized",
                            "Debes autenticarte", "/api/restaurantes/" + id + "/estado"));
        }

        Restaurante existente = restauranteRepo.findById(id).orElse(null);
        if (existente == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Restaurante no encontrado", "/api/restaurantes/" + id + "/estado"));
        }

        if (!existente.getPropietario().getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "Solo el propietario puede cambiar el estado",
                            "/api/restaurantes/" + id + "/estado"));
        }

        restauranteService.cambiarEstado(id, dto.getAceptaPedidos(), userDetails.getUsername());
        Restaurante actualizado = restauranteRepo.findById(id).orElseThrow();
        return ResponseEntity.ok(toResponse(actualizado));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Conversores Entity ↔ DTO
    // ─────────────────────────────────────────────────────────────────────────

    private RestauranteDTO.RestauranteResponse toResponse(Restaurante r) {
        RestauranteDTO.RestauranteResponse dto = new RestauranteDTO.RestauranteResponse();
        dto.setId(r.getId());
        dto.setNombre(r.getNombre());
        dto.setDireccion(r.getDireccion());
        dto.setLocalidad(r.getLocalidad());
        dto.setTelefono(r.getTelefono());
        dto.setEmail(r.getEmail());
        dto.setPrecioMin(r.getPrecioMin());
        dto.setPrecioMax(r.getPrecioMax());
        dto.setMediaValoraciones(r.getMediaValoraciones());
        dto.setBikeFriendly(r.getBikeFriendly());
        dto.setAceptaPedidos(r.getAceptaPedidos());
        dto.setImagen(r.getImagen());

        // Categorías (sin datos sensibles)
        if (r.getCategorias() != null) {
            dto.setCategorias(r.getCategorias().stream()
                    .map(c -> new RestauranteDTO.CategoriaInfo(c.getId(), c.getNombre()))
                    .collect(Collectors.toList()));
        }

        // Propietario (solo nombre, no email/password)
        if (r.getPropietario() != null) {
            dto.setPropietario(new RestauranteDTO.PropietarioInfo(
                    r.getPropietario().getId(),
                    r.getPropietario().getNombre(),
                    r.getPropietario().getApellidos()));
        }

        return dto;
    }

    private Restaurante fromRequest(RestauranteDTO.RestauranteRequest dto) {
        Restaurante r = new Restaurante();
        r.setNombre(dto.getNombre());
        r.setDireccion(dto.getDireccion());
        r.setLocalidad(dto.getLocalidad());
        r.setTelefono(dto.getTelefono());
        r.setEmail(dto.getEmail());
        r.setPrecioMin(dto.getPrecioMin());
        r.setPrecioMax(dto.getPrecioMax());
        r.setBikeFriendly(dto.getBikeFriendly());
        r.setAceptaPedidos(dto.getAceptaPedidos() != null ? dto.getAceptaPedidos() : true);
        return r;
    }
}
