// ======================================================
// LineaPedidoRepository.java
// ======================================================
package com.tw.repository;

import com.tw.model.LineaPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LineaPedidoRepository extends JpaRepository<LineaPedido, Long> {
    List<LineaPedido> findByPedidoId(Long pedidoId);
    List<LineaPedido> findByPlatoId(Long platoId);
}

