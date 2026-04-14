// ======================================================
// RestauranteRepository.java
// ======================================================
package com.tw.repository;

import com.tw.model.Restaurante;
import com.tw.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {
    List<Restaurante> findByLocalidad(String localidad);
    List<Restaurante> findByAceptaPedidosTrue();
    List<Restaurante> findByAceptaPedidosFalse();
    List<Restaurante> findByPropietarioId(Long propietarioId);
    List<Restaurante> findByNombreContainingIgnoreCase(String nombre);
    List<Restaurante> findByPropietario(Usuario propietario);
    List<Restaurante> findAllByOrderByMediaValoracionesDesc();

    @Query("SELECT r FROM Restaurante r WHERE " +
           "LOWER(r.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.localidad) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Restaurante> buscarPorNombreOLocalidad(@Param("query") String query);

    @Query("SELECT DISTINCT r FROM Restaurante r " +
           "JOIN r.categorias c WHERE c.id = :categoriaId")
    List<Restaurante> findByCategoriaId(@Param("categoriaId") Long categoriaId);
}

