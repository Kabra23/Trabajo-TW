package com.tw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    // Nota: Las demás rutas están en controladores especializados:
    // - /login, /registro, /perfil → AuthController
    // - /restaurantes, /detalle-restaurante → RestauranteController
    // - /categorias → CategoriaController
    // - /platos → PlatoController
    // - /pedidos → PedidoController
    // - /valoraciones → ValoracionController
}

