package com.tw.controller;

import com.tw.model.LineaPedido;
import com.tw.model.Pedido;
import com.tw.model.Plato;
import com.tw.repository.CategoriaRepository;
import com.tw.service.PedidoService;
import com.tw.service.PlatoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {

    private final CategoriaRepository categoriaRepo;
    private final PedidoService pedidoService;
    private final PlatoService platoService;

    public WebController(CategoriaRepository categoriaRepo,
                         PedidoService pedidoService,
                         PlatoService platoService) {
        this.categoriaRepo = categoriaRepo;
        this.pedidoService = pedidoService;
        this.platoService = platoService;
    }

    @GetMapping("/")
    public String index(Model model) {
        // Pasar categorías para el menú de acceso rápido (extra)
        model.addAttribute("categorias", categoriaRepo.findAll());
        return "index";
    }

    /**
     * Recibe el formulario de la cesta y crea el pedido con sus líneas.
     * Requisito mínimo 4: Gestión de pedidos
     */
    @PostMapping("/pedidos/realizar")
    public String realizarPedido(@RequestParam Map<String, String> params,
                                 @RequestParam Long restauranteId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes flash) {
        try {
            // Recoger los platos seleccionados (plato_ID = cantidad)
            List<LineaPedido> lineas = new ArrayList<>();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getKey().startsWith("plato_")) {
                    String idStr = entry.getKey().replace("plato_", "");
                    int cantidad = 0;
                    try {
                        cantidad = Integer.parseInt(entry.getValue());
                    } catch (NumberFormatException ignored) {}

                    if (cantidad > 0) {
                        Long platoId = Long.parseLong(idStr);
                        Plato plato = platoService.buscarPorId(platoId);

                        LineaPedido linea = new LineaPedido();
                        linea.setNombrePlato(plato.getNombre()); // snapshot
                        linea.setPrecio(plato.getPrecio());       // snapshot
                        linea.setCantidad(cantidad);
                        linea.setPlato(plato);
                        lineas.add(linea);
                    }
                }
            }

            if (lineas.isEmpty()) {
                flash.addFlashAttribute("error", "No has añadido ningún plato al pedido");
                return "redirect:/restaurantes/" + restauranteId;
            }

            Pedido pedido = new Pedido();
            pedido.setLineas(lineas);
            // Asociar el pedido a cada línea
            lineas.forEach(l -> l.setPedido(pedido));

            pedidoService.crear(pedido, userDetails.getUsername(), restauranteId);
            flash.addFlashAttribute("exito", "¡Pedido realizado correctamente!");
            return "redirect:/pedidos/mis-pedidos";

        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al realizar el pedido: " + e.getMessage());
            return "redirect:/restaurantes/" + restauranteId;
        }
    }
}
