package com.tw.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Column(nullable = false)
    private String apellidos;

    @Email(message = "El correo electrónico no es válido")
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fotoPerfil;

    /**
     * Indica si el usuario tiene rol de administrador.
     * Solo un admin puede conceder o revocar este privilegio a otro usuario.
     * La columna se añade automáticamente a la BD gracias a ddl-auto=update.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean admin = false;

    @OneToMany(mappedBy = "propietario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Restaurante> restaurantes;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pedido> pedidos;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Valoracion> valoraciones;

    @ManyToMany
    @JoinTable(
            name = "favoritos",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "restaurante_id")
    )
    @ToString.Exclude
    private List<Restaurante> favoritos;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Direccion> direcciones;

    public Direccion getDireccionPrincipal() {
        if (direcciones == null || direcciones.isEmpty()) return null;
        return direcciones.stream()
                .filter(d -> Boolean.TRUE.equals(d.getPrincipal()))
                .findFirst()
                .orElse(direcciones.get(0));
    }

    /** Comodidad: ¿es admin? Nunca null */
    public boolean esAdmin() {
        return Boolean.TRUE.equals(this.admin);
    }
}
