// ======================================================
// ValoracionRepository.java
// ======================================================
package com.tw.repository;

import com.tw.model.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {
    List<Valoracion> findByRestauranteId(Long restauranteId);
    List<Valoracion> findByUsuarioId(Long usuarioId);
    Optional<Valoracion> findByUsuarioIdAndRestauranteId(Long usuarioId, Long restauranteId);
}

