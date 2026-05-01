package com.tw.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UsuarioDTO {

    public static class RegistroRequest {
        @NotBlank(message = "El nombre es obligatorio")
        private String nombre;
        @NotBlank(message = "Los apellidos son obligatorios")
        private String apellidos;
        @Email(message = "Email no valido")
        @NotBlank(message = "El email es obligatorio")
        private String email;
        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,}$",
                 message = "La contrasena debe contener mayuscula, minuscula, numero y caracter especial")
        private String password;

        public String getNombre() { return nombre; }
        public void setNombre(String n) { this.nombre = n; }
        public String getApellidos() { return apellidos; }
        public void setApellidos(String a) { this.apellidos = a; }
        public String getEmail() { return email; }
        public void setEmail(String e) { this.email = e; }
        public String getPassword() { return password; }
        public void setPassword(String p) { this.password = p; }
    }

    public static class ActualizarRequest {
        @NotBlank(message = "El nombre es obligatorio")
        private String nombre;
        @NotBlank(message = "Los apellidos son obligatorios")
        private String apellidos;
        @Email(message = "El email no tiene un formato valido")
        private String email;

        public String getNombre() { return nombre; }
        public void setNombre(String n) { this.nombre = n; }
        public String getApellidos() { return apellidos; }
        public void setApellidos(String a) { this.apellidos = a; }
        public String getEmail() { return email; }
        public void setEmail(String e) { this.email = e; }
    }

    public static class LoginRequest {
        @Email(message = "Email no valido")
        @NotBlank(message = "El email es obligatorio")
        private String email;
        @NotBlank(message = "La contrasena es obligatoria")
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String e) { this.email = e; }
        public String getPassword() { return password; }
        public void setPassword(String p) { this.password = p; }
    }

    public static class UsuarioResponse {
        private Long id;
        private String nombre;
        private String apellidos;
        private String email;
        private String fotoPerfil;

        public UsuarioResponse() {}
        public UsuarioResponse(Long id, String nombre, String apellidos, String email, String fotoPerfil) {
            this.id = id; this.nombre = nombre; this.apellidos = apellidos;
            this.email = email; this.fotoPerfil = fotoPerfil;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String n) { this.nombre = n; }
        public String getApellidos() { return apellidos; }
        public void setApellidos(String a) { this.apellidos = a; }
        public String getEmail() { return email; }
        public void setEmail(String e) { this.email = e; }
        public String getFotoPerfil() { return fotoPerfil; }
        public void setFotoPerfil(String f) { this.fotoPerfil = f; }
    }

    public static class LoginResponse {
        private String token;
        private String tipo = "Basic";
        private Long id;
        private String email;
        private String nombre;
        private String instrucciones = "Usa este token en la cabecera: Authorization: Basic <token>";

        public LoginResponse() {}
        public LoginResponse(String token, Long id, String email, String nombre) {
            this.token = token; this.id = id; this.email = email; this.nombre = nombre;
        }

        public String getToken() { return token; }
        public void setToken(String t) { this.token = t; }
        public String getTipo() { return tipo; }
        public void setTipo(String t) { this.tipo = t; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String e) { this.email = e; }
        public String getNombre() { return nombre; }
        public void setNombre(String n) { this.nombre = n; }
        public String getInstrucciones() { return instrucciones; }
        public void setInstrucciones(String i) { this.instrucciones = i; }
    }
}
