package com.tw.controller;

import com.tw.model.LineaPedido;
import com.tw.model.Pedido;
import com.tw.service.PedidoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // -------------------------------------------------------
    // Mis pedidos (usuario)
    // -------------------------------------------------------
    @GetMapping("/mis-pedidos")
    public String misPedidos(@AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        List<Pedido> pedidos = pedidoService.listarMisPedidos(userDetails.getUsername());
        long pedidosEnCurso = 0;
        long pedidosEntregados = 0;
        double totalGastado = 0.0;

        for (Pedido pedido : pedidos) {
            if (pedido == null) {
                continue;
            }

            Pedido.EstadoPedido estado = pedido.getEstado();
            if (estado == Pedido.EstadoPedido.ENTREGADO) {
                pedidosEntregados++;
            }
            if (estado == Pedido.EstadoPedido.PENDIENTE
                    || estado == Pedido.EstadoPedido.EN_PREPARACION
                    || estado == Pedido.EstadoPedido.ENVIADO) {
                pedidosEnCurso++;
            }

            if (pedido.getTotal() != null) {
                totalGastado += pedido.getTotal();
            }
        }

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("pedidosEnCurso", pedidosEnCurso);
        model.addAttribute("pedidosEntregados", pedidosEntregados);
        model.addAttribute("totalGastado", totalGastado);
        model.addAttribute("esPropio", true);
        return "mis-pedidos";
    }

    // -------------------------------------------------------
    // Detalle del pedido
    // -------------------------------------------------------
    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        Pedido pedido = pedidoService.buscarPorId(id);

        // Verificar que el usuario sea el dueño del pedido o el propietario del restaurante
        boolean esPropio = pedido.getUsuario().getEmail().equals(userDetails.getUsername());
        boolean esPropietario = pedido.getRestaurante().getPropietario().getEmail()
                .equals(userDetails.getUsername());

        if (!esPropio && !esPropietario) {
            throw new SecurityException("No tienes permiso para ver este pedido");
        }

        model.addAttribute("pedido", pedido);
        model.addAttribute("esPropio", esPropio);
        model.addAttribute("esPropietario", esPropietario);
        return "detalle-pedido";
    }

    // -------------------------------------------------------
    // Crear pedido (en el formulario del restaurante)
    // -------------------------------------------------------
    @PostMapping("/crear")
    public String crear(@RequestParam Long restauranteId,
                       @AuthenticationPrincipal UserDetails userDetails,
                       RedirectAttributes flash) {
        try {
            Pedido pedido = new Pedido();
            // Aquí se añadirían líneas del pedido (platos seleccionados)
            pedidoService.crear(pedido, userDetails.getUsername(), restauranteId);
            flash.addFlashAttribute("exito", "Pedido creado correctamente");
            return "redirect:/pedidos/mis-pedidos";
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/restaurantes/" + restauranteId;
        }
    }

    // -------------------------------------------------------
    // Cambiar estado del pedido (solo propietario del restaurante)
    // -------------------------------------------------------
    @PostMapping("/{id}/cambiar-estado")
    public String cambiarEstado(@PathVariable Long id,
                               @RequestParam Pedido.EstadoPedido estado,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes flash) {
        try {
            pedidoService.cambiarEstado(id, estado, userDetails.getUsername(), true);
            flash.addFlashAttribute("exito", "Estado del pedido actualizado");
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }

    // -------------------------------------------------------
    // Eliminar pedido (solo el usuario que lo creó)
    // -------------------------------------------------------
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes flash) {
        try {
            Pedido pedido = pedidoService.buscarPorId(id);
            pedidoService.eliminar(id, userDetails.getUsername());
            flash.addFlashAttribute("exito", "Pedido eliminado correctamente");
            return "redirect:/pedidos/mis-pedidos";
        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos/" + id;
        }
    }

    // -------------------------------------------------------
    // Pedidos del restaurante (para el propietario)
    // -------------------------------------------------------
    @GetMapping("/restaurante/{id}")
    public String pedidosDelRestaurante(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       Model model) {
        try {
            List<Pedido> pedidos = pedidoService.listarPedidosDelRestaurante(id, userDetails.getUsername());
            model.addAttribute("pedidos", pedidos);
            model.addAttribute("restauranteId", id);
            return "pedidos-restaurante";
        } catch (SecurityException e) {
            throw new SecurityException("No tienes permiso para ver estos pedidos");
        }
    }
}

