// ======================================================
// PlatoRepository.java
// ======================================================
package com.tw.repository;

import com.tw.model.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlatoRepository extends JpaRepository<Plato, Long> {
    List<Plato> findByRestauranteId(Long restauranteId);
    List<Plato> findByNombreContainingIgnoreCase(String nombre);

    @Modifying
    @Query("delete from Plato p where p.restaurante.id = :restauranteId")
    void deleteByRestauranteId(Long restauranteId);
}

