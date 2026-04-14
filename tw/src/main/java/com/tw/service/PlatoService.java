package com.tw.service;

import com.tw.model.Plato;
import com.tw.model.Restaurante;
import com.tw.repository.PlatoRepository;
import com.tw.repository.RestauranteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class PlatoService {

    private final PlatoRepository platoRepo;
    private final RestauranteRepository restauranteRepo;

    public PlatoService(PlatoRepository platoRepo, RestauranteRepository restauranteRepo) {
        this.platoRepo = platoRepo;
        this.restauranteRepo = restauranteRepo;
    }

    // ---- Lectura ----

    @Transactional(readOnly = true)
    public Plato buscarPorId(Long id) {
        return platoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plato no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Plato> listarPorRestaurante(Long restauranteId) {
        return platoRepo.findByRestauranteId(restauranteId);
    }

    @Transactional(readOnly = true)
    public List<Plato> buscar(String nombre) {
        return platoRepo.findByNombreContainingIgnoreCase(nombre);
    }

    // ---- Escritura ----

    public Plato crear(Long restauranteId, Plato plato, String emailPropietario) {
        Restaurante restaurante = verificarPropietario(restauranteId, emailPropietario);
        plato.setRestaurante(restaurante);
        return platoRepo.save(plato);
    }

    public Plato actualizar(Long platoId, Plato datos, String emailPropietario) {
        Plato existente = buscarPorId(platoId);
        verificarPropietario(existente.getRestaurante().getId(), emailPropietario);
        
        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        existente.setPrecio(datos.getPrecio());
        if (datos.getImagen() != null) {
            existente.setImagen(datos.getImagen());
        }
        
        return platoRepo.save(existente);
    }

    public void eliminar(Long platoId, String emailPropietario) {
        Plato plato = buscarPorId(platoId);
        verificarPropietario(plato.getRestaurante().getId(), emailPropietario);
        platoRepo.delete(plato);
    }

    // ---- Privados ----

    private Restaurante verificarPropietario(Long restauranteId, String emailPropietario) {
        Restaurante restaurante = restauranteRepo.findById(restauranteId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));
        
        if (!restaurante.getPropietario().getEmail().equals(emailPropietario)) {
            throw new SecurityException("No tienes permiso para modificar este restaurante");
        }
        
        return restaurante;
    }
}

