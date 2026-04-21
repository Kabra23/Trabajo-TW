// ======================================================
// PedidoRepository.java
// ======================================================
package com.tw.repository;

import com.tw.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioId(Long usuarioId);
    List<Pedido> findByRestauranteId(Long restauranteId);
    List<Pedido> findByUsuarioIdAndRestauranteId(Long usuarioId, Long restauranteId);

    @Modifying
    @Query("delete from Pedido p where p.restaurante.id = :restauranteId")
    void deleteByRestauranteId(Long restauranteId);
}

