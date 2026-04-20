package com.tw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "direcciones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La dirección no puede estar vacía")
    @Column(nullable = false)
    private String direccion;

    private String etiqueta; // ej: "Casa", "Trabajo"

    /** Si es true, es la dirección predeterminada del usuario */
    @Builder.Default
    private Boolean principal = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
