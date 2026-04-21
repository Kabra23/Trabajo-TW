package com.tw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "platos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Plato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @DecimalMin("0.0")
    @Column(nullable = false)
    private Double precio;

    private String imagen; // extra imágenes

    @Column(length = 80)
    private String etiquetaMenu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;
}