package com.tw.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTOs para operaciones con platos en la API REST.
 */
public class PlatoDTO {

    // ====== REQUEST ======

    public static class PlatoRequest {
        @NotBlank(message = "El nombre del plato es obligatorio")
        private String nombre;

        private String descripcion;

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
        private Double precio;

        private String etiquetaMenu;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public Double getPrecio() { return precio; }
        public void setPrecio(Double precio) { this.precio = precio; }
        public String getEtiquetaMenu() { return etiquetaMenu; }
        public void setEtiquetaMenu(String etiquetaMenu) { this.etiquetaMenu = etiquetaMenu; }
    }

    // ====== RESPONSE ======

    public static class PlatoResponse {
        private Long id;
        private String nombre;
        private String descripcion;
        private Double precio;
        private String imagen;
        private String etiquetaMenu;
        private Long restauranteId;

        public PlatoResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public Double getPrecio() { return precio; }
        public void setPrecio(Double precio) { this.precio = precio; }
        public String getImagen() { return imagen; }
        public void setImagen(String imagen) { this.imagen = imagen; }
        public String getEtiquetaMenu() { return etiquetaMenu; }
        public void setEtiquetaMenu(String etiquetaMenu) { this.etiquetaMenu = etiquetaMenu; }
        public Long getRestauranteId() { return restauranteId; }
        public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }
    }
}
