// ======================================================
// PlatoRepository.java
// ======================================================
package com.tw.repository;

import com.tw.model.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlatoRepository extends JpaRepository<Plato, Long> {
    List<Plato> findByRestauranteId(Long restauranteId);
    List<Plato> findByNombreContainingIgnoreCase(String nombre);
}

