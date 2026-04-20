package com.tw.service;

import com.tw.model.Usuario;
import com.tw.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    // Cambia este email por el tuyo de admin
    private static final String ADMIN_EMAIL = "admin@cume.es";

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // Si el email coincide con el admin, asignar ROLE_ADMIN ademas de ROLE_USER
        List<SimpleGrantedAuthority> authorities;
        if (ADMIN_EMAIL.equalsIgnoreCase(email)) {
            authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_ADMIN")
            );
        } else {
            authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new User(usuario.getEmail(), usuario.getPassword(), authorities);
    }
}
