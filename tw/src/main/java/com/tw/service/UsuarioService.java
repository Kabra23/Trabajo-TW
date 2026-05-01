package com.tw.service;

import com.tw.model.Usuario;
import com.tw.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrar(Usuario usuario, String password, String direccion) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese email");
        }
        usuario.setPassword(passwordEncoder.encode(password));
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmailConRestaurantes(String email) {
        return usuarioRepository.findByEmailWithRestaurantes(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmailConFavoritos(String email) {
        return usuarioRepository.findByEmailWithFavoritos(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    public Usuario actualizarPerfil(Long id, String nombre, String apellidos, String fotoPerfil) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        if (fotoPerfil != null) usuario.setFotoPerfil(fotoPerfil);
        return usuarioRepository.save(usuario);
    }

    /**
     * Actualiza el email. Lanza IllegalArgumentException si ya existe.
     * Devuelve true si el email cambio (requiere re-login).
     */
    public boolean actualizarEmail(Long id, String nuevoEmail) {
        if (nuevoEmail == null || nuevoEmail.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacio");
        }
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        String emailNorm = nuevoEmail.trim().toLowerCase();
        if (usuario.getEmail().equalsIgnoreCase(emailNorm)) {
            return false;
        }
        if (usuarioRepository.existsByEmail(emailNorm)) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese email");
        }
        usuario.setEmail(emailNorm);
        usuarioRepository.save(usuario);
        return true;
    }

    public void cambiarPassword(Long id, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new IllegalArgumentException("La contrasena actual no es correcta");
        }
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
    }

    public void eliminarCuenta(Long id) {
        usuarioRepository.deleteById(id);
    }

    /** Concede privilegios de administrador al usuario indicado. */
    public void concederAdmin(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuario.setAdmin(true);
        usuarioRepository.save(usuario);
    }

    /** Revoca los privilegios de administrador del usuario indicado. */
    public void revocarAdmin(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuario.setAdmin(false);
        usuarioRepository.save(usuario);
    }

    /** Crea un nuevo usuario con todos sus datos (solo ADMIN). */
    public Usuario crearUsuario(String nombre, String apellidos, String email,
                                 String password, boolean esAdmin) {
        if (usuarioRepository.existsByEmail(email.trim().toLowerCase())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese email");
        }
        Usuario u = new Usuario();
        u.setNombre(nombre.trim());
        u.setApellidos(apellidos.trim());
        u.setEmail(email.trim().toLowerCase());
        u.setPassword(passwordEncoder.encode(password));
        u.setAdmin(esAdmin);
        return usuarioRepository.save(u);
    }

    /** Actualiza nombre, apellidos y rol admin de un usuario (solo ADMIN). */
    public void actualizarUsuarioAdmin(Long id, String nombre, String apellidos,
                                        String email, boolean esAdmin) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuario.setNombre(nombre.trim());
        usuario.setApellidos(apellidos.trim());
        usuario.setAdmin(esAdmin);
        if (email != null && !email.isBlank()) {
            String emailNorm = email.trim().toLowerCase();
            if (!usuario.getEmail().equalsIgnoreCase(emailNorm)) {
                if (usuarioRepository.existsByEmail(emailNorm)) {
                    throw new IllegalArgumentException("Ya existe una cuenta con ese email");
                }
                usuario.setEmail(emailNorm);
            }
        }
        usuarioRepository.save(usuario);
    }
}
