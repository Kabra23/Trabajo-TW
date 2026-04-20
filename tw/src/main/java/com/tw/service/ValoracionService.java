package com.tw.service;

import com.tw.model.Restaurante;
import com.tw.model.Usuario;
import com.tw.model.Valoracion;
import com.tw.repository.RestauranteRepository;
import com.tw.repository.UsuarioRepository;
import com.tw.repository.ValoracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ValoracionService {

    private final ValoracionRepository valoracionRepo;
    private final UsuarioRepository usuarioRepo;
    private final RestauranteRepository restauranteRepo;

    public ValoracionService(ValoracionRepository valoracionRepo,
                           UsuarioRepository usuarioRepo,
                           RestauranteRepository restauranteRepo) {
        this.valoracionRepo = valoracionRepo;
        this.usuarioRepo = usuarioRepo;
        this.restauranteRepo = restauranteRepo;
    }

    // ---- Lectura ----

    @Transactional(readOnly = true)
    public Valoracion buscarPorId(Long id) {
        return valoracionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Valoración no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<Valoracion> listarPorRestaurante(Long restauranteId) {
        return valoracionRepo.findByRestauranteId(restauranteId);
    }

    @Transactional(readOnly = true)
    public List<Valoracion> listarPorUsuario(String emailUsuario) {
        Usuario usuario = obtenerUsuario(emailUsuario);
        return valoracionRepo.findByUsuarioId(usuario.getId());
    }

    @Transactional(readOnly = true)
    public Optional<Valoracion> obtenerValoracionUsuario(String emailUsuario, Long restauranteId) {
        Usuario usuario = obtenerUsuario(emailUsuario);
        return valoracionRepo.findByUsuarioIdAndRestauranteId(usuario.getId(), restauranteId);
    }

    // ---- Escritura ----

    public Valoracion crear(Long restauranteId, Valoracion valoracion, String emailUsuario) {
        Usuario usuario = obtenerUsuario(emailUsuario);
        Restaurante restaurante = restauranteRepo.findById(restauranteId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));

        // Validar que el usuario no tenga ya una valoración de este restaurante
        Optional<Valoracion> existente = valoracionRepo
                .findByUsuarioIdAndRestauranteId(usuario.getId(), restauranteId);

        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya has valorado este restaurante. Puedes editarla.");
        }

        valoracion.setUsuario(usuario);
        valoracion.setRestaurante(restaurante);

        Valoracion guardada = valoracionRepo.save(valoracion);

        // Recalcular media del restaurante con la coleccion en memoria
        restaurante.getValoraciones().add(guardada);
        restaurante.recalcularMedia();
        restauranteRepo.save(restaurante);

        return guardada;
    }

    public Valoracion actualizar(Long valoracionId, Valoracion datos, String emailUsuario) {
        Valoracion existente = buscarPorId(valoracionId);

        // Solo el usuario que creó la valoración puede modificarla
        if (!existente.getUsuario().getEmail().equals(emailUsuario)) {
            throw new SecurityException("No tienes permiso para modificar esta valoración");
        }

        existente.setPuntuacion(datos.getPuntuacion());
        existente.setComentario(datos.getComentario());

        Valoracion actualizada = valoracionRepo.save(existente);

        // Recalcular media del restaurante
        Restaurante restaurante = existente.getRestaurante();
        restaurante.recalcularMedia();
        restauranteRepo.save(restaurante);

        return actualizada;
    }

    public void eliminar(Long valoracionId, String emailUsuario) {
        Valoracion valoracion = buscarPorId(valoracionId);

        // Solo el usuario que creó la valoración puede eliminarla
        if (!valoracion.getUsuario().getEmail().equals(emailUsuario)) {
            throw new SecurityException("No tienes permiso para eliminar esta valoración");
        }

        Restaurante restaurante = valoracion.getRestaurante();
        valoracionRepo.delete(valoracion);

        // Recalcular media del restaurante
        restaurante.getValoraciones().remove(valoracion);
        restaurante.recalcularMedia();
        restauranteRepo.save(restaurante);
    }

    // ---- Privados ----

    private Usuario obtenerUsuario(String email) {
        return usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }
}

