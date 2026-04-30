package com.tw.api;

import com.tw.api.dto.ErrorResponse;
import com.tw.model.Usuario;
import com.tw.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API REST — Gestión de administradores.
 *
 * Solo un usuario con ROLE_ADMIN (campo admin=true en BD) puede usar estos endpoints.
 *
 * Endpoints:
 *   GET    /api/admin/usuarios              → Listar todos los usuarios (solo admins)
 *   PUT    /api/admin/usuarios/{id}/admin   → Conceder rol admin
 *   DELETE /api/admin/usuarios/{id}/admin   → Revocar rol admin
 */
@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private final UsuarioRepository usuarioRepo;

    public AdminApiController(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    // ── GET /api/admin/usuarios → listar todos los usuarios ──────────────────
    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!esAdmin(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "Solo los administradores pueden ver la lista de usuarios",
                            "/api/admin/usuarios"));
        }

        List<Map<String, Object>> lista = usuarioRepo.findAll().stream()
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("nombre", u.getNombre());
                    m.put("apellidos", u.getApellidos());
                    m.put("email", u.getEmail());
                    m.put("admin", Boolean.TRUE.equals(u.getAdmin()));
                    // No se expone password ni fotoPerfil en este listado
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(lista);
    }

    // ── PUT /api/admin/usuarios/{id}/admin → conceder rol admin ──────────────
    @PutMapping("/usuarios/{id}/admin")
    public ResponseEntity<?> concederAdmin(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!esAdmin(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "Solo los administradores pueden conceder el rol de admin",
                            "/api/admin/usuarios/" + id + "/admin"));
        }

        Usuario objetivo = usuarioRepo.findById(id).orElse(null);
        if (objetivo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Usuario no encontrado",
                            "/api/admin/usuarios/" + id + "/admin"));
        }

        if (Boolean.TRUE.equals(objetivo.getAdmin())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(409, "Conflict",
                            "El usuario ya es administrador",
                            "/api/admin/usuarios/" + id + "/admin"));
        }

        objetivo.setAdmin(true);
        usuarioRepo.save(objetivo);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Rol de administrador concedido correctamente");
        respuesta.put("usuarioId", objetivo.getId());
        respuesta.put("email", objetivo.getEmail());
        respuesta.put("admin", true);
        respuesta.put("nota", "El usuario tendrá acceso de admin en su próxima sesión");

        return ResponseEntity.ok(respuesta);
    }

    // ── DELETE /api/admin/usuarios/{id}/admin → revocar rol admin ────────────
    @DeleteMapping("/usuarios/{id}/admin")
    public ResponseEntity<?> revocarAdmin(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!esAdmin(userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "Solo los administradores pueden revocar el rol de admin",
                            "/api/admin/usuarios/" + id + "/admin"));
        }

        Usuario objetivo = usuarioRepo.findById(id).orElse(null);
        if (objetivo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found",
                            "Usuario no encontrado",
                            "/api/admin/usuarios/" + id + "/admin"));
        }

        // Un admin no puede quitarse a sí mismo el rol
        if (objetivo.getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Bad Request",
                            "No puedes quitarte el rol de administrador a ti mismo",
                            "/api/admin/usuarios/" + id + "/admin"));
        }

        if (!Boolean.TRUE.equals(objetivo.getAdmin())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(409, "Conflict",
                            "El usuario no tiene rol de administrador",
                            "/api/admin/usuarios/" + id + "/admin"));
        }

        objetivo.setAdmin(false);
        usuarioRepo.save(objetivo);

        return ResponseEntity.noContent().build(); // 204
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private boolean esAdmin(UserDetails u) {
        return u != null && u.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
