package com.tw.service;

import com.tw.model.Pedido;
import com.tw.model.Restaurante;
import com.tw.model.Usuario;
import com.tw.repository.PedidoRepository;
import com.tw.repository.RestauranteRepository;
import com.tw.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepo;
    private final UsuarioRepository usuarioRepo;
    private final RestauranteRepository restauranteRepo;

    public PedidoService(PedidoRepository pedidoRepo, 
                        UsuarioRepository usuarioRepo,
                        RestauranteRepository restauranteRepo) {
        this.pedidoRepo = pedidoRepo;
        this.usuarioRepo = usuarioRepo;
        this.restauranteRepo = restauranteRepo;
    }

    // ---- Lectura ----

    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long id) {
        return pedidoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarMisPedidos(String emailUsuario) {
        Usuario usuario = obtenerUsuario(emailUsuario);
        return pedidoRepo.findByUsuarioId(usuario.getId());
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPedidosDelRestaurante(Long restauranteId, String emailPropietario) {
        verificarPropietarioRestaurante(restauranteId, emailPropietario);
        return pedidoRepo.findByRestauranteId(restauranteId);
    }

    // ---- Escritura ----

    public Pedido crear(Pedido pedido, String emailUsuario, Long restauranteId) {
        Usuario usuario = obtenerUsuario(emailUsuario);
        Restaurante restaurante = restauranteRepo.findById(restauranteId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));

        if (!restaurante.getAceptaPedidos()) {
            throw new IllegalArgumentException("Este restaurante no acepta pedidos en este momento");
        }

        pedido.setUsuario(usuario);
        pedido.setRestaurante(restaurante);
        pedido.calcularTotal();

        return pedidoRepo.save(pedido);
    }

    public Pedido cambiarEstado(Long pedidoId, Pedido.EstadoPedido nuevoEstado, 
                                String emailUsuario, boolean esPropietario) {
        Pedido pedido = buscarPorId(pedidoId);

        if (esPropietario) {
            // Solo el propietario del restaurante puede cambiar el estado
            verificarPropietarioRestaurante(pedido.getRestaurante().getId(), emailUsuario);
        } else {
            // El usuario del pedido solo puede verlo
            if (!pedido.getUsuario().getEmail().equals(emailUsuario)) {
                throw new SecurityException("No tienes permiso para modificar este pedido");
            }
        }

        pedido.setEstado(nuevoEstado);
        return pedidoRepo.save(pedido);
    }

    public void eliminar(Long pedidoId, String emailUsuario) {
        Pedido pedido = buscarPorId(pedidoId);

        // Solo el usuario que creó el pedido puede eliminarlo
        if (!pedido.getUsuario().getEmail().equals(emailUsuario)) {
            throw new SecurityException("No tienes permiso para eliminar este pedido");
        }

        // No permitir eliminación de pedidos ya enviados o entregados
        if (pedido.getEstado() == Pedido.EstadoPedido.ENVIADO || 
            pedido.getEstado() == Pedido.EstadoPedido.ENTREGADO) {
            throw new IllegalArgumentException("No puedes eliminar pedidos ya enviados o entregados");
        }

        pedidoRepo.delete(pedido);
    }

    // ---- Privados ----

    private Usuario obtenerUsuario(String email) {
        return usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private void verificarPropietarioRestaurante(Long restauranteId, String emailPropietario) {
        Restaurante restaurante = restauranteRepo.findById(restauranteId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante no encontrado"));

        if (!restaurante.getPropietario().getEmail().equals(emailPropietario)) {
            throw new SecurityException("No tienes permiso para acceder a este restaurante");
        }
    }
}

