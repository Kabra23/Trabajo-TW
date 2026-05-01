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

    @PostMapping("/usuarios")
    public ResponseEntity<?> registro(@Valid @RequestBody UsuarioDTO.RegistroRequest dto) {
        if (usuarioRepo.existsByEmail(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(409, "Conflict", "Ya existe una cuenta con ese email", "/api/usuarios"));
        }
        Usuario nuevo = new Usuario();
        nuevo.setNombre(dto.getNombre());
        nuevo.setApellidos(dto.getApellidos());
        nuevo.setEmail(dto.getEmail());
        Usuario guardado = usuarioService.registrar(nuevo, dto.getPassword(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(guardado));
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<?> getPerfil(@PathVariable Long id,
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
                            "No puedes acceder al perfil de otro usuario", "/api/usuarios/" + id));
        }
        return ResponseEntity.ok(toResponse(usuario));
    }

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
        usuarioService.actualizarPerfil(id, dto.getNombre(), dto.getApellidos(), null);
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            try {
                usuarioService.actualizarEmail(id, dto.getEmail());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorResponse(409, "Conflict", e.getMessage(), "/api/usuarios/" + id));
            }
        }
        Usuario actualizado = usuarioRepo.findById(id).orElseThrow();
        return ResponseEntity.ok(toResponse(actualizado));
    }

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
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sesiones")
    public ResponseEntity<?> login(@Valid @RequestBody UsuarioDTO.LoginRequest dto) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            Usuario usuario = usuarioRepo.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            String credenciales = dto.getEmail() + ":" + dto.getPassword();
            String token = Base64.getEncoder().encodeToString(credenciales.getBytes());
            return ResponseEntity.ok(new UsuarioDTO.LoginResponse(
                    token, usuario.getId(), usuario.getEmail(), usuario.getNombre()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized",
                            "Email o contrasena incorrectos", "/api/sesiones"));
        }
    }

    private UsuarioDTO.UsuarioResponse toResponse(Usuario u) {
        return new UsuarioDTO.UsuarioResponse(
                u.getId(), u.getNombre(), u.getApellidos(), u.getEmail(), u.getFotoPerfil());
    }
}
