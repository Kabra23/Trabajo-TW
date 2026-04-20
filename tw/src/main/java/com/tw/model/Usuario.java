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

    // Sin @NotBlank porque la contraseña llega como @RequestParam separado
    @Column(nullable = false)
    private String password;

    private String fotoPerfil;

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

    /** Direcciones del usuario (extra: gestión de direcciones) */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Direccion> direcciones;

    /** Devuelve la dirección principal o null si no tiene ninguna */
    public Direccion getDireccionPrincipal() {
        if (direcciones == null || direcciones.isEmpty()) return null;
        return direcciones.stream()
                .filter(d -> Boolean.TRUE.equals(d.getPrincipal()))
                .findFirst()
                .orElse(direcciones.get(0));
    }
}
