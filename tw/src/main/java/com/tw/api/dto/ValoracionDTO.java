package com.tw.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTOs para operaciones con valoraciones en la API REST.
 */
public class ValoracionDTO {

    // ====== REQUEST ======

    public static class ValoracionRequest {
        @NotNull(message = "La puntuación es obligatoria")
        @Min(value = 1, message = "La puntuación mínima es 1")
        @Max(value = 5, message = "La puntuación máxima es 5")
        private Integer puntuacion;

        private String comentario;

        public Integer getPuntuacion() { return puntuacion; }
        public void setPuntuacion(Integer puntuacion) { this.puntuacion = puntuacion; }
        public String getComentario() { return comentario; }
        public void setComentario(String comentario) { this.comentario = comentario; }
    }

    // ====== RESPONSE ======

    public static class ValoracionResponse {
        private Long id;
        private Integer puntuacion;
        private String comentario;
        private LocalDateTime fecha;
        private Long restauranteId;
        private String restauranteNombre;
        private Long usuarioId;
        private String usuarioNombre; // Solo nombre, no email (privacidad)

        public ValoracionResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Integer getPuntuacion() { return puntuacion; }
        public void setPuntuacion(Integer puntuacion) { this.puntuacion = puntuacion; }
        public String getComentario() { return comentario; }
        public void setComentario(String comentario) { this.comentario = comentario; }
        public LocalDateTime getFecha() { return fecha; }
        public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
        public Long getRestauranteId() { return restauranteId; }
        public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }
        public String getRestauranteNombre() { return restauranteNombre; }
        public void setRestauranteNombre(String restauranteNombre) { this.restauranteNombre = restauranteNombre; }
        public Long getUsuarioId() { return usuarioId; }
        public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
        public String getUsuarioNombre() { return usuarioNombre; }
        public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
    }
}
