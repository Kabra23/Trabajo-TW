package com.tw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "categorias")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String nombre;

    private String descripcion;

    private String imagen; // ruta relativa para el extra de imágenes

    @ManyToMany(mappedBy = "categorias")
    private List<Restaurante> restaurantes;
}