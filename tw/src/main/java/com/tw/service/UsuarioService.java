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

    /** Registra un nuevo usuario. Lanza excepción si el email ya existe. */
    public Usuario registrar(Usuario usuario, String password, String direccion) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese email");
        }
        // Hash de la contraseña antes de guardar
        usuario.setPassword(passwordEncoder.encode(password));
        return usuarioRepository.save(usuario);
    }

    /** Obtiene el usuario por email (usado en controladores tras autenticación). */
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

    /** Actualiza los datos del perfil (nombre, apellidos, etc.). */
    public Usuario actualizarPerfil(Long id, String nombre, String apellidos, String fotoPerfil) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        if (fotoPerfil != null) usuario.setFotoPerfil(fotoPerfil);
        return usuarioRepository.save(usuario);
    }

    /** Cambia la contraseña del usuario después de verificar la actual. */
    public void cambiarPassword(Long id, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }
        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
    }

    /** Elimina la cuenta del usuario y todos sus datos asociados. */
    public void eliminarCuenta(Long id) {
        usuarioRepository.deleteById(id);
    }
}