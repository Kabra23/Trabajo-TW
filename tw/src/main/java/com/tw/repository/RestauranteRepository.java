package com.tw.repository;

import com.tw.model.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RestauranteRepository extends JpaRepository<Restaurante, Long>, JpaSpecificationExecutor<Restaurante> {

    List<Restaurante> findByAceptaPedidos(boolean aceptaPedidos);

    List<Restaurante> findByNombreContainingIgnoreCaseOrLocalidadContainingIgnoreCase(String nombre, String localidad);

    List<Restaurante> findAllByOrderByMediaValoracionesDesc();

    /**
     * Spring Data JPA genera automaticamente el JOIN por la relacion
     * @ManyToMany entre Restaurante y Categoria usando el nombre del campo
     * 'categorias' y el atributo 'id' de Categoria.
     */
    List<Restaurante> findByCategorias_Id(Long categoriaId);

    List<Restaurante> findByPropietario(com.tw.model.Usuario propietario);

    @Modifying
    @Query(value = "DELETE FROM favoritos WHERE restaurante_id = :restauranteId", nativeQuery = true)
    void deleteFavoritosByRestauranteId(Long restauranteId);

    @Modifying
    @Query(value = "DELETE FROM restaurante_categorias WHERE restaurante_id = :restauranteId", nativeQuery = true)
    void deleteCategoriasByRestauranteId(Long restauranteId);

    @Modifying
    @Query("delete from Restaurante r where r.id = :restauranteId")
    void deleteByIdDirect(Long restauranteId);
}
