package com.tw.api;

import com.tw.api.dto.ErrorResponse;
import com.tw.api.dto.PedidoDTO;
import com.tw.model.LineaPedido;
import com.tw.model.Pedido;
import com.tw.model.Plato;
import com.tw.model.Usuario;
import com.tw.repository.PedidoRepository;
import com.tw.repository.PlatoRepository;
import com.tw.repository.RestauranteRepository;
import com.tw.repository.UsuarioRepository;
import com.tw.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API REST - Gestión de pedidos.
 *
 * Endpoints:
 *   POST   /api/pedidos                    → Realizar pedido (autenticado)
 *   GET    /api/usuarios/{id}/pedidos      → Historial del usuario
 *   GET    /api/pedidos/{id}               → Detalle de un pedido
 */
@RestController
@RequestMapping("/api")
public class PedidoApiController {

    private final PedidoService pedidoService;
    private final PedidoRepository pedidoRepo;
    private final UsuarioRepository usuarioRepo;
    private final PlatoRepository platoRepo;
    private final RestauranteRepository restauranteRepo;

    public PedidoApiController(PedidoService pedidoService,
                                PedidoRepository pedidoRepo,
                                UsuarioRepository usuarioRepo,
                                PlatoRepository platoRepo,
                                RestauranteRepository restauranteRepo) {
        this.pedidoService = pedidoService;
        this.pedidoRepo = pedidoRepo;
        this.usuarioRepo = usuarioRepo;
        this.platoRepo = platoRepo;
        this.restauranteRepo = restauranteRepo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/pedidos → Realizar pedido (autenticado)
    // Req. mínimo 4
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/pedidos")
    public ResponseEntity<?> crear(@Valid @RequestBody PedidoDTO.PedidoRequest dto,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized",
                            "Debes autenticarte para realizar pedidos", "/api/pedidos"));
        }

        // Verificar que el restaurante existe
        var restaurante = restauranteRepo.findById(dto.getRestauranteId()).orElse(null);
        if (restaurante == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Restaurante no encontrado", "/api/pedidos"));
        }

        // Verificar que el restaurante acepta pedidos
        if (!restaurante.getAceptaPedidos()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(409, "Conflict",
                            "Este restaurante no acepta pedidos en este momento", "/api/pedidos"));
        }

        // Construir las líneas del pedido
        List<LineaPedido> lineas = new ArrayList<>();
        for (PedidoDTO.LineaRequest lineaDto : dto.getLineas()) {
            Plato plato = platoRepo.findById(lineaDto.getPlatoId()).orElse(null);
            if (plato == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(404, "Not Found",
                                "Plato con ID " + lineaDto.getPlatoId() + " no encontrado",
                                "/api/pedidos"));
            }

            // Verificar que el plato pertenece al restaurante del pedido
            if (!plato.getRestaurante().getId().equals(dto.getRestauranteId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse(400, "Bad Request",
                                "El plato '" + plato.getNombre() + "' no pertenece a este restaurante",
                                "/api/pedidos"));
            }

            LineaPedido linea = new LineaPedido();
            linea.setNombrePlato(plato.getNombre());   // snapshot
            linea.setPrecio(plato.getPrecio());          // snapshot
            linea.setCantidad(lineaDto.getCantidad());
            linea.setPlato(plato);
            lineas.add(linea);
        }

        Pedido pedido = new Pedido();
        pedido.setLineas(lineas);
        lineas.forEach(l -> l.setPedido(pedido));

        Pedido creado = pedidoService.crear(pedido, userDetails.getUsername(), dto.getRestauranteId());

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(creado));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/usuarios/{id}/pedidos → Historial de pedidos del usuario
    // Req. mínimo 4
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/usuarios/{id}/pedidos")
    public ResponseEntity<?> listarPedidosUsuario(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized",
                            "Debes autenticarte", "/api/usuarios/" + id + "/pedidos"));
        }

        // Verificar que el usuario existe
        Usuario usuario = usuarioRepo.findById(id).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Usuario no encontrado", "/api/usuarios/" + id + "/pedidos"));
        }

        // Solo puede ver sus propios pedidos (no acceso a pedidos de otros)
        if (!usuario.getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "No puedes ver los pedidos de otro usuario",
                            "/api/usuarios/" + id + "/pedidos"));
        }

        List<Pedido> pedidos = pedidoRepo.findByUsuarioId(id);
        return ResponseEntity.ok(pedidos.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/pedidos/{id} → Detalle de un pedido
    // Visible por el usuario que lo hizo o el propietario del restaurante
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/pedidos/{id}")
    public ResponseEntity<?> detallePedido(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized",
                            "Debes autenticarte", "/api/pedidos/" + id));
        }

        Pedido pedido = pedidoRepo.findById(id).orElse(null);
        if (pedido == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Pedido no encontrado", "/api/pedidos/" + id));
        }

        String emailActual = userDetails.getUsername();
        boolean esUsuario = pedido.getUsuario().getEmail().equals(emailActual);
        boolean esPropietario = pedido.getRestaurante().getPropietario().getEmail().equals(emailActual);

        if (!esUsuario && !esPropietario) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "No tienes acceso a este pedido", "/api/pedidos/" + id));
        }

        return ResponseEntity.ok(toResponse(pedido));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Conversor Entity → Response DTO
    // ─────────────────────────────────────────────────────────────────────────
    private PedidoDTO.PedidoResponse toResponse(Pedido p) {
        PedidoDTO.PedidoResponse dto = new PedidoDTO.PedidoResponse();
        dto.setId(p.getId());
        dto.setFecha(p.getFecha());
        dto.setTotal(p.getTotal());
        dto.setEstado(p.getEstado() != null ? p.getEstado().name() : null);
        dto.setRestauranteId(p.getRestaurante() != null ? p.getRestaurante().getId() : null);
        dto.setRestauranteNombre(p.getRestaurante() != null ? p.getRestaurante().getNombre() : null);
        dto.setUsuarioId(p.getUsuario() != null ? p.getUsuario().getId() : null);
        // No exponer email del usuario en la respuesta pública
        dto.setUsuarioNombre(p.getUsuario() != null
                ? p.getUsuario().getNombre() + " " + p.getUsuario().getApellidos() : null);

        if (p.getLineas() != null) {
            dto.setLineas(p.getLineas().stream().map(l -> {
                PedidoDTO.LineaResponse linea = new PedidoDTO.LineaResponse();
                linea.setId(l.getId());
                linea.setNombrePlato(l.getNombrePlato());
                linea.setPrecio(l.getPrecio());
                linea.setCantidad(l.getCantidad());
                linea.setSubtotal(l.getPrecio() != null && l.getCantidad() != null
                        ? l.getPrecio() * l.getCantidad() : 0.0);
                return linea;
            }).collect(Collectors.toList()));
        }

        return dto;
    }
}
