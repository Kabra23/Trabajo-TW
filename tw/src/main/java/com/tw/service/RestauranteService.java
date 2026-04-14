package com.tw.service;

import com.tw.model.*;
import com.tw.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class RestauranteService {

    private final RestauranteRepository restauranteRepo;
    private final CategoriaRepository categoriaRepo;
    private final UsuarioRepository usuarioRepo;

    public RestauranteService(RestauranteRepository restauranteRepo,
                              CategoriaRepository categoriaRepo,
                              UsuarioRepository usuarioRepo) {
        this.restauranteRepo = restauranteRepo;
        this.categoriaRepo = categoriaRepo;
        this.usuarioRepo = usuarioRepo;
    }

    // ---- Lectura ----

    @Transactional(readOnly = true)
    public List<Restaurante> listarTodos() {
        return restauranteRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Restaurante> listarQueAceptanPedidos() {
        return restauranteRepo.findByAceptaPedidosTrue();
    }

    @Transactional(readOnly = true)
    public List<Restaurante> listarQueNoAceptanPedidos() {
        return restauranteRepo.findByAceptaPedidosFalse();
    }

    @Transactional(readOnly = true)
    public List<Restaurante> listarOrdenadosPorValoracion() {
        return restauranteRepo.findAllByOrderByMediaValoracionesDesc();
    }

    @Transactional(readOnly = true)
    public Restaurante buscarPorId(Long id) {
        return restauranteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Restaurante> buscar(String query) {
        if (query == null || query.isBlank()) return listarTodos();
        return restauranteRepo.buscarPorNombreOLocalidad(query.trim());
    }

    @Transactional(readOnly = true)
    public List<Restaurante> buscarPorCategoria(Long categoriaId) {
        return restauranteRepo.findByCategoriaId(categoriaId);
    }

    @Transactional(readOnly = true)
    public List<Restaurante> misRestaurantes(String email) {
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return restauranteRepo.findByPropietario(usuario);
    }

    // ---- Escritura ----

    public Restaurante crear(Restaurante restaurante, String emailPropietario, List<Long> categoriaIds) {
        Usuario propietario = usuarioRepo.findByEmail(emailPropietario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        restaurante.setPropietario(propietario);
        asignarCategorias(restaurante, categoriaIds);
        return restauranteRepo.save(restaurante);
    }

    public Restaurante actualizar(Long id, Restaurante datos, String emailUsuario, List<Long> categoriaIds) {
        Restaurante existente = verificarPropietario(id, emailUsuario);
        existente.setNombre(datos.getNombre());
        existente.setDireccion(datos.getDireccion());
        existente.setTelefono(datos.getTelefono());
        existente.setEmail(datos.getEmail());
        existente.setPrecioMin(datos.getPrecioMin());
        existente.setPrecioMax(datos.getPrecioMax());
        existente.setBikeFriendly(datos.getBikeFriendly());
        existente.setLocalidad(datos.getLocalidad());
        if (datos.getImagen() != null) existente.setImagen(datos.getImagen());
        asignarCategorias(existente, categoriaIds);
        return restauranteRepo.save(existente);
    }

    public void eliminar(Long id, String emailUsuario) {
        Restaurante restaurante = verificarPropietario(id, emailUsuario);
        restauranteRepo.delete(restaurante);
    }

    public void cambiarEstado(Long id, boolean aceptaPedidos, String emailUsuario) {
        Restaurante restaurante = verificarPropietario(id, emailUsuario);
        restaurante.setAceptaPedidos(aceptaPedidos);
        restauranteRepo.save(restaurante);
    }

    // ---- Favoritos (extra) ----

    public void toggleFavorito(Long restauranteId, String emailUsuario) {
        Usuario usuario = usuarioRepo.findByEmail(emailUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Restaurante restaurante = buscarPorId(restauranteId);
        List<Restaurante> favoritos = usuario.getFavoritos();
        if (favoritos.contains(restaurante)) {
            favoritos.remove(restaurante);
        } else {
            favoritos.add(restaurante);
        }
        usuarioRepo.save(usuario);
    }

    // ---- Privados ----

    private Restaurante verificarPropietario(Long id, String emailUsuario) {
        Restaurante restaurante = buscarPorId(id);
        if (!restaurante.getPropietario().getEmail().equals(emailUsuario)) {
            throw new SecurityException("No tienes permiso para modificar este restaurante");
        }
        return restaurante;
    }

    private void asignarCategorias(Restaurante restaurante, List<Long> categoriaIds) {
        if (categoriaIds != null && !categoriaIds.isEmpty()) {
            List<Categoria> categorias = categoriaRepo.findAllById(categoriaIds);
            restaurante.setCategorias(categorias);
        }
    }
}