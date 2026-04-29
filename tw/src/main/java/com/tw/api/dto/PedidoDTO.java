package com.tw.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs para operaciones con pedidos en la API REST.
 */
public class PedidoDTO {

    // ====== REQUEST ======

    public static class LineaRequest {
        @NotNull(message = "El ID del plato es obligatorio")
        private Long platoId;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        private Integer cantidad;

        public Long getPlatoId() { return platoId; }
        public void setPlatoId(Long platoId) { this.platoId = platoId; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    }

    public static class PedidoRequest {
        @NotNull(message = "El restaurante es obligatorio")
        private Long restauranteId;

        @NotNull(message = "Debe incluir al menos un plato")
        @Size(min = 1, message = "Debe incluir al menos un plato")
        private List<LineaRequest> lineas;

        public Long getRestauranteId() { return restauranteId; }
        public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }
        public List<LineaRequest> getLineas() { return lineas; }
        public void setLineas(List<LineaRequest> lineas) { this.lineas = lineas; }
    }

    // ====== RESPONSE ======

    public static class LineaResponse {
        private Long id;
        private String nombrePlato;
        private Double precio;
        private Integer cantidad;
        private Double subtotal;

        public LineaResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombrePlato() { return nombrePlato; }
        public void setNombrePlato(String nombrePlato) { this.nombrePlato = nombrePlato; }
        public Double getPrecio() { return precio; }
        public void setPrecio(Double precio) { this.precio = precio; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        public Double getSubtotal() { return subtotal; }
        public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    }

    public static class PedidoResponse {
        private Long id;
        private LocalDateTime fecha;
        private Double total;
        private String estado;
        private Long restauranteId;
        private String restauranteNombre;
        private Long usuarioId;
        private String usuarioNombre;
        private List<LineaResponse> lineas;

        public PedidoResponse() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public LocalDateTime getFecha() { return fecha; }
        public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
        public Double getTotal() { return total; }
        public void setTotal(Double total) { this.total = total; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
        public Long getRestauranteId() { return restauranteId; }
        public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }
        public String getRestauranteNombre() { return restauranteNombre; }
        public void setRestauranteNombre(String restauranteNombre) { this.restauranteNombre = restauranteNombre; }
        public Long getUsuarioId() { return usuarioId; }
        public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
        public String getUsuarioNombre() { return usuarioNombre; }
        public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
        public List<LineaResponse> getLineas() { return lineas; }
        public void setLineas(List<LineaResponse> lineas) { this.lineas = lineas; }
    }
}
