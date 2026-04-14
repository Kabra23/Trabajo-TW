package com.tw.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    private Double total;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaPedido> lineas;

     public void calcularTotal() {
         if (lineas == null || lineas.isEmpty()) {
             this.total = 0.0;
         } else {
             this.total = lineas.stream()
                     .mapToDouble(l -> l.getPrecio() * l.getCantidad())
                     .sum();
         }
     }

    public enum EstadoPedido {
        PENDIENTE, EN_PREPARACION, ENVIADO, ENTREGADO
    }
}