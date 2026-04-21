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

    @NotBlank(message = "La direccion es obligatoria")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String direccion;

    @NotBlank(message = "El telefono es obligatorio")
    @Column(nullable = false)
    private String telefono;

    @Email(message = "El correo no es valido")
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

    /**
     * Etiquetas del menú personalizadas por el propietario.
     * Se almacenan como texto separado por comas, ej: "Destacados,Desayunos,Bocadillos,Bebidas"
     * Si es null o vacío, se usan las etiquetas por defecto.
     */
    @Column(columnDefinition = "TEXT")
    private String etiquetasMenu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id", nullable = false)
    private Usuario propietario;

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

    /**
     * Devuelve las etiquetas como lista. Si no hay personalizadas, devuelve las por defecto.
     */
    public java.util.List<String> getEtiquetasMenuLista() {
        if (etiquetasMenu != null && !etiquetasMenu.isBlank()) {
            return java.util.Arrays.stream(etiquetasMenu.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
        }
        return java.util.List.of("Destacados", "Desayunos", "Bocadillos y Montaditos", "Bebidas", "Ensaladas");
    }
}
