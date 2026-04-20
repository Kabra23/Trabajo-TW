package com.tw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "restaurantes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    @Column(nullable = false)
    private String telefono;

    @Email(message = "El correo no es válido")
    @NotBlank(message = "El correo de contacto es obligatorio")
    @Column(nullable = false)
    private String email;

    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double precioMin;

    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double precioMax;

    private Double mediaValoraciones;

    private Boolean bikeFriendly;

    @Builder.Default
    private Boolean aceptaPedidos = true;

    private String imagen;
    private String imagenBanner;

    private String localidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id", nullable = false)
    private Usuario propietario;

    // CORRECCIÓN PROBLEMA 2: EAGER para evitar LazyInitializationException
    // cuando Thymeleaf accede a categorias fuera de la sesión JPA
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "restaurante_categorias",
            joinColumns = @JoinColumn(name = "restaurante_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    @Builder.Default
    private Set<Categoria> categorias = new HashSet<>();

    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<Plato> platos = new HashSet<>();

    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Valoracion> valoraciones = new HashSet<>();

    @ManyToMany(mappedBy = "favoritos")
    @Builder.Default
    private Set<Usuario> usuariosQueFavorecen = new HashSet<>();

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
