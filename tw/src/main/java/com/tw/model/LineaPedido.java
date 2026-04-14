package com.tw.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lineas_pedido")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LineaPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombrePlato;  // snapshot del nombre en el momento del pedido

    @Column(nullable = false)
    private Double precio;       // snapshot del precio

    @Column(nullable = false)
    private Integer cantidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plato_id")
    private Plato plato;         // puede ser null si el plato fue eliminado
}