package com.tw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "restaurantes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Campos requeridos por el enunciado ---

    @NotBlank
    @Column(nullable = false)
    private String nombre;          // Text

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String direccion;       // Textarea

    @NotBlank
    @Column(nullable = false)
    private String telefono;        // Tel

    @Email
    @NotBlank
    @Column(nullable = false)
    private String email;           // Email

    @Min(0)
    private Double precioMin;       // Number (rango precio)

    @Min(0)
    private Double precioMax;       // Number (rango precio)

    private Double mediaValoraciones; // calculado automáticamente

    private Boolean bikeFriendly;   // Radio button Sí/No

    // Estado para req. mínimo 7
    @Builder.Default
    private Boolean aceptaPedidos = true;

    // Extra: imagen del restaurante
    private String imagen;

    // Localidad para búsqueda (req. mínimo 6)
    private String localidad;

    // --- Relaciones ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id", nullable = false)
    private Usuario propietario;

    @ManyToMany
    @JoinTable(
            name = "restaurante_categorias",
            joinColumns = @JoinColumn(name = "restaurante_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private List<Categoria> categorias;

    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Plato> platos;

    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Valoracion> valoraciones;

    @ManyToMany(mappedBy = "favoritos")
    private List<Usuario> usuariosQueFavorecen;

     // Método de utilidad: recalcular media
     public void recalcularMedia() {
         if (valoraciones == null || valoraciones.isEmpty()) {
             this.mediaValoraciones = 0.0;
         } else {
             this.mediaValoraciones = valoraciones.stream()
                     .mapToDouble(Valoracion::getPuntuacion)
                     .average()
                     .orElse(0.0);
         }
     }
}