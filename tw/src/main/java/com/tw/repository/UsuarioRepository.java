// ======================================================
// UsuarioRepository.java
// ======================================================
package com.tw.repository;

import com.tw.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.restaurantes WHERE u.email = :email")
    Optional<Usuario> findByEmailWithRestaurantes(@Param("email") String email);

    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.favoritos WHERE u.email = :email")
    Optional<Usuario> findByEmailWithFavoritos(@Param("email") String email);

    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}
