package com.tw.service;

import com.tw.model.Categoria;
import com.tw.model.Restaurante;
import com.tw.model.Usuario;
import com.tw.repository.CategoriaRepository;
import com.tw.repository.RestauranteRepository;
import com.tw.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class RestauranteService {

    private final RestauranteRepository restauranteRepo;
    private final UsuarioRepository usuarioRepo;
    private final CategoriaRepository categoriaRepo;


    // ---- Lectura ----

    @Transactional(readOnly = true)
    public Restaurante buscarPorId(Long id) {
        return restauranteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Restaurante> listarTodos() {
        return restauranteRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Restaurante> buscar(String q) {
        return restauranteRepo.findByNombreContainingIgnoreCaseOrLocalidadContainingIgnoreCase(q, q);
    }

    @Transactional(readOnly = true)
    public List<Restaurante> buscarPorCategoria(Long categoriaId) {
        return restauranteRepo.findByCategorias_Id(categoriaId);
    }

    @Transactional(readOnly = true)
    public List<Restaurante> buscarPorPropietario(Usuario propietario) {
        return restauranteRepo.findByPropietario(propietario);
    }

    @Transactional(readOnly = true)
    public List<Restaurante> listarQueAceptanPedidos() {
        return restauranteRepo.findByAceptaPedidos(true);
    }

    @Transactional(readOnly = true)
    public List<Restaurante> listarQueNoAceptanPedidos() {
        return restauranteRepo.findByAceptaPedidos(false);
    }

    @Transactional(readOnly = true)
    public List<Restaurante> listarOrdenadosPorValoracion() {
        return restauranteRepo.findAllByOrderByMediaValoracionesDesc();
    }

    @Transactional(readOnly = true)
    public List<Restaurante> buscar(String q, Long categoriaId, String filtro, Integer valoracion, Integer precio, Boolean bikeFriendly, String orden) {
        return restauranteRepo.findAll((root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (q != null && !q.isBlank()) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nombre")), "%" + q.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("localidad")), "%" + q.toLowerCase() + "%")
                ));
            }

            if (categoriaId != null) {
                predicates.add(cb.equal(root.join("categorias").get("id"), categoriaId));
            }

            if (filtro != null) {
                if ("acepta".equals(filtro)) {
                    predicates.add(cb.isTrue(root.get("aceptaPedidos")));
                } else if ("noAcepta".equals(filtro)) {
                    predicates.add(cb.isFalse(root.get("aceptaPedidos")));
                }
            }

            if (valoracion != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("mediaValoraciones"), valoracion));
            }

            if (precio != null) {
                // Asumiendo que tienes un campo 'rangoPrecio' o similar
                // predicates.add(cb.equal(root.get("rangoPrecio"), precio));
            }

            if (bikeFriendly != null && bikeFriendly) {
                predicates.add(cb.isTrue(root.get("bikeFriendly")));
            }

            if (orden != null) {
                switch (orden) {
                    case "nombre_asc":
                        query.orderBy(cb.asc(root.get("nombre")));
                        break;
                    case "nombre_desc":
                        query.orderBy(cb.desc(root.get("nombre")));
                        break;
                    case "precio_asc":
                        query.orderBy(cb.asc(root.get("precioMin")));
                        break;
                    case "precio_desc":
                        query.orderBy(cb.desc(root.get("precioMin")));
                        break;
                    case "valoracion_desc":
                        query.orderBy(cb.desc(root.get("mediaValoraciones")));
                        break;
                }
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        });
    }


    // ---- Escritura ----

    public Restaurante crear(Restaurante restaurante, String emailPropietario, List<Long> categoriaIds) {
        Usuario propietario = obtenerUsuario(emailPropietario);
        restaurante.setPropietario(propietario);
        actualizarCategorias(restaurante, categoriaIds);
        return restauranteRepo.save(restaurante);
    }

    public Restaurante actualizar(Long id, Restaurante datos, String email, List<Long> categoriaIds) {
        Restaurante existente = buscarPorId(id);
        verificarPropietario(existente, email);

        existente.setNombre(datos.getNombre());
        existente.setDireccion(datos.getDireccion());
        existente.setLocalidad(datos.getLocalidad());
        existente.setTelefono(datos.getTelefono());
        existente.setAceptaPedidos(datos.getAceptaPedidos());

        if (datos.getImagen() != null) {
            existente.setImagen(datos.getImagen());
        }

        actualizarCategorias(existente, categoriaIds);
        return restauranteRepo.save(existente);
    }

    public void eliminar(Long id, String email) {
        Restaurante restaurante = buscarPorId(id);
        verificarPropietario(restaurante, email);
        restauranteRepo.delete(restaurante);
    }

    public void cambiarEstado(Long id, boolean aceptaPedidos, String email) {
        Restaurante restaurante = buscarPorId(id);
        verificarPropietario(restaurante, email);
        restaurante.setAceptaPedidos(aceptaPedidos);
        restauranteRepo.save(restaurante);
    }

    public void toggleFavorito(Long restauranteId, String email) {
        Usuario usuario = obtenerUsuario(email);
        Restaurante restaurante = buscarPorId(restauranteId);

        if (usuario.getFavoritos().contains(restaurante)) {
            usuario.getFavoritos().remove(restaurante);
        } else {
            usuario.getFavoritos().add(restaurante);
        }
        usuarioRepo.save(usuario);
    }


    // ---- Privados ----

    private Usuario obtenerUsuario(String email) {
        return usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));
    }

    private void verificarPropietario(Restaurante r, String email) {
        if (!r.getPropietario().getEmail().equals(email)) {
            throw new SecurityException("No tienes permiso para realizar esta acción");
        }
    }

    private void actualizarCategorias(Restaurante restaurante, List<Long> categoriaIds) {
        if (categoriaIds == null || categoriaIds.isEmpty()) {
            restaurante.getCategorias().clear();
            return;
        }
        List<Categoria> categorias = categoriaRepo.findAllById(categoriaIds);
        restaurante.setCategorias(new HashSet<>(categorias));
    }
}
