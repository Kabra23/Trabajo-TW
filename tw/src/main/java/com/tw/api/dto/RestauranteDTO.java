package com.tw.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTOs para operaciones con restaurantes en la API REST.
 */
public class RestauranteDTO {

    // ══════════════════════════════ REQUEST ══════════════════════════════════

    /** Body de POST y PUT /api/restaurantes */
    public static class RestauranteRequest {
        @NotBlank(message = "El nombre es obligatorio")
        private String nombre;

        @NotBlank(message = "La dirección es obligatoria")
        private String direccion;

        private String localidad;

        @NotBlank(message = "El teléfono es obligatorio")
        private String telefono;

        @Email(message = "Email no válido")
        @NotBlank(message = "El email de contacto es obligatorio")
        private String email;

        @Min(value = 0, message = "El precio mínimo no puede ser negativo")
        private Double precioMin;

        @Min(value = 0, message = "El precio máximo no puede ser negativo")
        private Double precioMax;

        private Boolean bikeFriendly;
        private Boolean aceptaPedidos = true;
        private List<Long> categoriaIds;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }
        public String getLocalidad() { return localidad; }
        public void setLocalidad(String localidad) { this.localidad = localidad; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Double getPrecioMin() { return precioMin; }
        public void setPrecioMin(Double precioMin) { this.precioMin = precioMin; }
        public Double getPrecioMax() { return precioMax; }
        public void setPrecioMax(Double precioMax) { this.precioMax = precioMax; }
        public Boolean getBikeFriendly() { return bikeFriendly; }
        public void setBikeFriendly(Boolean bikeFriendly) { this.bikeFriendly = bikeFriendly; }
        public Boolean getAceptaPedidos() { return aceptaPedidos; }
        public void setAceptaPedidos(Boolean aceptaPedidos) { this.aceptaPedidos = aceptaPedidos; }
        public List<Long> getCategoriaIds() { return categoriaIds; }
        public void setCategoriaIds(List<Long> categoriaIds) { this.categoriaIds = categoriaIds; }
    }

    /** Body de PUT /api/restaurantes/{id}/estado */
    public static class EstadoRequest {
        @NotNull(message = "El campo aceptaPedidos es obligatorio")
        private Boolean aceptaPedidos;

        public Boolean getAceptaPedidos() { return aceptaPedidos; }
        public void setAceptaPedidos(Boolean aceptaPedidos) { this.aceptaPedidos = aceptaPedidos; }
    }

    // ══════════════════════════════ RESPONSE ══════════════════════════════════

    public static class RestauranteResponse {
        private Long id;
        private String nombre;
        private String direccion;
        private String localidad;
        private String telefono;
        private String email;
        private Double precioMin;
        private Double precioMax;
        private Double mediaValoraciones;
        private Boolean bikeFriendly;
        private Boolean aceptaPedidos;
        private String imagen;
        private List<CategoriaInfo> categorias;
        private PropietarioInfo propietario;

        public RestauranteResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }
        public String getLocalidad() { return localidad; }
        public void setLocalidad(String localidad) { this.localidad = localidad; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Double getPrecioMin() { return precioMin; }
        public void setPrecioMin(Double precioMin) { this.precioMin = precioMin; }
        public Double getPrecioMax() { return precioMax; }
        public void setPrecioMax(Double precioMax) { this.precioMax = precioMax; }
        public Double getMediaValoraciones() { return mediaValoraciones; }
        public void setMediaValoraciones(Double mediaValoraciones) { this.mediaValoraciones = mediaValoraciones; }
        public Boolean getBikeFriendly() { return bikeFriendly; }
        public void setBikeFriendly(Boolean bikeFriendly) { this.bikeFriendly = bikeFriendly; }
        public Boolean getAceptaPedidos() { return aceptaPedidos; }
        public void setAceptaPedidos(Boolean aceptaPedidos) { this.aceptaPedidos = aceptaPedidos; }
        public String getImagen() { return imagen; }
        public void setImagen(String imagen) { this.imagen = imagen; }
        public List<CategoriaInfo> getCategorias() { return categorias; }
        public void setCategorias(List<CategoriaInfo> categorias) { this.categorias = categorias; }
        public PropietarioInfo getPropietario() { return propietario; }
        public void setPropietario(PropietarioInfo propietario) { this.propietario = propietario; }
    }

    /** Info de categoría embebida en la respuesta (sin datos innecesarios) */
    public static class CategoriaInfo {
        private Long id;
        private String nombre;

        public CategoriaInfo() {}
        public CategoriaInfo(Long id, String nombre) { this.id = id; this.nombre = nombre; }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
    }

    /** Info del propietario (solo nombre, no email/password por privacidad) */
    public static class PropietarioInfo {
        private Long id;
        private String nombre;
        private String apellidos;

        public PropietarioInfo() {}
        public PropietarioInfo(Long id, String nombre, String apellidos) {
            this.id = id; this.nombre = nombre; this.apellidos = apellidos;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellidos() { return apellidos; }
        public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    }
}
