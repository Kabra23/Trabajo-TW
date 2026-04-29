package com.tw.api;

import com.tw.api.dto.ErrorResponse;
import com.tw.api.dto.UsuarioDTO;
import com.tw.model.Usuario;
import com.tw.repository.UsuarioRepository;
import com.tw.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

/**
 * API REST - Gestión de usuarios.
 *
 * Endpoints:
 *   POST   /api/usuarios           → Registro
 *   GET    /api/usuarios/{id}      → Ver perfil (autenticado)
 *   PUT    /api/usuarios/{id}      → Editar perfil (solo el propio)
 *   DELETE /api/usuarios/{id}      → Eliminar cuenta (solo el propio)
 *   POST   /api/sesiones           → Login (devuelve token Basic)
 */
@RestController
@RequestMapping("/api")
public class UsuarioApiController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepo;
    private final AuthenticationManager authManager;

    public UsuarioApiController(UsuarioService usuarioService,
                                UsuarioRepository usuarioRepo,
                                AuthenticationManager authManager) {
        this.usuarioService = usuarioService;
        this.usuarioRepo = usuarioRepo;
        this.authManager = authManager;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/usuarios  →  Registro de nuevo usuario
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/usuarios")
    public ResponseEntity<?> registro(@Valid @RequestBody UsuarioDTO.RegistroRequest dto) {
        // Verificar duplicado de email (409 Conflict)
        if (usuarioRepo.existsByEmail(dto.getEmail())) {
            ErrorResponse error = new ErrorResponse(
                    409, "Conflict", "Ya existe una cuenta con ese email", "/api/usuarios");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombre(dto.getNombre());
        nuevo.setApellidos(dto.getApellidos());
        nuevo.setEmail(dto.getEmail());

        Usuario guardado = usuarioService.registrar(nuevo, dto.getPassword(), null);

        UsuarioDTO.UsuarioResponse respuesta = toResponse(guardado);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/usuarios/{id}  →  Ver perfil (solo el propio usuario)
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<?> getPerfil(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized", "Debes autenticarte", "/api/usuarios/" + id));
        }

        Usuario usuario = usuarioRepo.findById(id)
                .orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found", "Usuario no encontrado", "/api/usuarios/" + id));
        }

        // Solo puede ver su propio perfil (no fuga de información de otros)
        if (!usuario.getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "No puedes acceder al perfil de otro usuario", "/api/usuarios/" + id));
        }

        return ResponseEntity.ok(toResponse(usuario));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/usuarios/{id}  →  Actualizar perfil (solo el propio)
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> actualizarPerfil(@PathVariable Long id,
                                               @Valid @RequestBody UsuarioDTO.ActualizarRequest dto,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized", "Debes autenticarte", "/api/usuarios/" + id));
        }

        Usuario usuario = usuarioRepo.findById(id).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found", "Usuario no encontrado", "/api/usuarios/" + id));
        }

        if (!usuario.getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "No puedes modificar el perfil de otro usuario", "/api/usuarios/" + id));
        }

        // Carga datos actuales antes de editar (requisito: ver datos previos)
        Usuario actualizado = usuarioService.actualizarPerfil(id, dto.getNombre(), dto.getApellidos(), null);
        return ResponseEntity.ok(toResponse(actualizado));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/usuarios/{id}  →  Eliminar cuenta (solo el propio)
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> eliminarCuenta(@PathVariable Long id,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized", "Debes autenticarte", "/api/usuarios/" + id));
        }

        Usuario usuario = usuarioRepo.findById(id).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Not Found", "Usuario no encontrado", "/api/usuarios/" + id));
        }

        if (!usuario.getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(403, "Forbidden",
                            "No puedes eliminar la cuenta de otro usuario", "/api/usuarios/" + id));
        }

        usuarioService.eliminarCuenta(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/sesiones  →  Login (EXTRA +0.5 pts)
    // Devuelve token Basic Auth codificado en Base64 para usar en cabeceras
    // Authorization: Basic <token>
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/sesiones")
    public ResponseEntity<?> login(@Valid @RequestBody UsuarioDTO.LoginRequest dto) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            // Token Basic: Base64(email:password)
            String credenciales = dto.getEmail() + ":" + dto.getPassword();
            String token = Base64.getEncoder().encodeToString(credenciales.getBytes());

            UsuarioDTO.LoginResponse respuesta = new UsuarioDTO.LoginResponse(
                    token, usuario.getId(), usuario.getEmail(), usuario.getNombre());

            return ResponseEntity.ok(respuesta);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized",
                            "Email o contraseña incorrectos", "/api/sesiones"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilidad: Entity → Response DTO (no exponer password)
    // ─────────────────────────────────────────────────────────────────────────
    private UsuarioDTO.UsuarioResponse toResponse(Usuario u) {
        return new UsuarioDTO.UsuarioResponse(
                u.getId(), u.getNombre(), u.getApellidos(), u.getEmail(), u.getFotoPerfil());
    }
}
