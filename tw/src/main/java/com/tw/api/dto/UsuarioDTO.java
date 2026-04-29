package com.tw.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTOs para operaciones con usuarios en la API REST.
 * Evita exponer campos sensibles como password en las respuestas.
 */
public class UsuarioDTO {

    // ══════════════════════════════ REQUEST ══════════════════════════════════

    /** Body de POST /api/usuarios (registro) */
    public static class RegistroRequest {
        @NotBlank(message = "El nombre es obligatorio")
        private String nombre;

        @NotBlank(message = "Los apellidos son obligatorios")
        private String apellidos;

        @Email(message = "Email no válido")
        @NotBlank(message = "El email es obligatorio")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,}$",
            message = "La contraseña debe contener mayúscula, minúscula, número y carácter especial"
        )
        private String password;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellidos() { return apellidos; }
        public void setApellidos(String apellidos) { this.apellidos = apellidos; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /** Body de PUT /api/usuarios/{id} (actualizar) */
    public static class ActualizarRequest {
        @NotBlank(message = "El nombre es obligatorio")
        private String nombre;

        @NotBlank(message = "Los apellidos son obligatorios")
        private String apellidos;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellidos() { return apellidos; }
        public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    }

    /** Body de POST /api/sesiones (login) */
    public static class LoginRequest {
        @Email(message = "Email no válido")
        @NotBlank(message = "El email es obligatorio")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // ══════════════════════════════ RESPONSE ══════════════════════════════════

    /** Respuesta de GET y POST /api/usuarios (sin password) */
    public static class UsuarioResponse {
        private Long id;
        private String nombre;
        private String apellidos;
        private String email;
        private String fotoPerfil;

        public UsuarioResponse() {}

        public UsuarioResponse(Long id, String nombre, String apellidos,
                               String email, String fotoPerfil) {
            this.id = id;
            this.nombre = nombre;
            this.apellidos = apellidos;
            this.email = email;
            this.fotoPerfil = fotoPerfil;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellidos() { return apellidos; }
        public void setApellidos(String apellidos) { this.apellidos = apellidos; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFotoPerfil() { return fotoPerfil; }
        public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }
    }

    /** Respuesta de POST /api/sesiones con token Basic Auth */
    public static class LoginResponse {
        private String token;
        private String tipo = "Bearer";
        private Long id;
        private String email;
        private String nombre;
        private String instrucciones = "Usa este token en la cabecera: Authorization: Basic <token>";

        public LoginResponse() {}

        public LoginResponse(String token, Long id, String email, String nombre) {
            this.token = token;
            this.id = id;
            this.email = email;
            this.nombre = nombre;
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getInstrucciones() { return instrucciones; }
        public void setInstrucciones(String instrucciones) { this.instrucciones = instrucciones; }
    }
}
