package com.tw.service;

import com.tw.model.Categoria;
import com.tw.model.Plato;
import com.tw.model.Restaurante;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

@Service
public class VistaImagenService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String categoria(Categoria categoria) {
        if (categoria == null) {
            return "/img/logo.png";
        }

        String ruta = resolverRuta(categoria.getImagen(), "logo.png");
        if (ruta != null) return ruta;

        String n = normalizar(categoria.getNombre());
        if (contiene(n, "desayuno", "brunch", "tostada")) return "/img/tostada.jpg";
        if (contiene(n, "bocadillo", "montadito")) return "/img/montadito.png";
        if (contiene(n, "pizza")) return "/img/pizza.png";
        if (contiene(n, "hamburguesa", "burger", "americana")) return "/img/americana.png";
        if (contiene(n, "asiatica", "asia", "wok", "sushi")) return "/img/asiatica.png";
        if (contiene(n, "mexicana", "mexico", "taco")) return "/img/mexicana.png";
        if (contiene(n, "italiana", "italia", "pasta")) return "/img/italiana.png";
        if (contiene(n, "ensalada", "saludable", "veg")) return "/img/ensalada.png";
        if (contiene(n, "bebida", "cafe", "cafeteria")) return "/img/cafe.png";
        if (contiene(n, "tapa", "racion")) return "/img/tortilla.png";

        return "/img/logo.png";
    }

    public String restaurante(Restaurante restaurante) {
        if (restaurante == null) {
            return "/img/banner.png";
        }

        String ruta = resolverRuta(restaurante.getImagen(), "banner.png");
        if (ruta != null) return ruta;
        ruta = resolverRuta(restaurante.getImagenBanner(), "banner.png");
        if (ruta != null) return ruta;

        String n = normalizar(restaurante.getNombre());
        if (contiene(n, "pizza", "pizzeria")) return "/img/pizza.png";
        if (contiene(n, "burger", "hamburg")) return "/img/americana.png";
        if (contiene(n, "asia", "wok", "sushi", "ramen")) return "/img/asiatica.png";
        if (contiene(n, "mex", "taco")) return "/img/mexicana.png";
        if (contiene(n, "cafe", "cafeteria", "brunch", "desay")) return "/img/cafe.png";
        if (contiene(n, "ital", "pasta")) return "/img/italiana.png";
        if (contiene(n, "ensalada", "verde")) return "/img/ensalada.png";
        if (contiene(n, "rincon", "tapa", "maria")) return "/img/tortilla.png";

        Set<Categoria> categorias = restaurante.getCategorias();
        if (categorias != null) {
            for (Categoria cat : categorias) {
                String rutaCategoria = categoria(cat);
                if (!"/img/logo.png".equals(rutaCategoria)) {
                    return rutaCategoria;
                }
            }
        }

        return "/img/banner.png";
    }

    public String plato(Plato plato) {
        if (plato == null) {
            return "/img/pizza.png";
        }

        String ruta = resolverRuta(plato.getImagen(), "pizza.png");
        if (ruta != null) return ruta;

        String n = normalizar(plato.getNombre());
        if (contiene(n, "tostada")) return "/img/tostada.jpg";
        if (contiene(n, "cafe")) return "/img/cafe.png";
        if (contiene(n, "zumo", "jugo", "batido")) return "/img/zumo.png";
        if (contiene(n, "bocadillo", "montadito", "sandwich")) return "/img/montadito.png";
        if (contiene(n, "pizza")) return "/img/pizza.png";
        if (contiene(n, "pasta", "lasana", "ravioli")) return "/img/italiana.png";
        if (contiene(n, "burger", "hamburguesa")) return "/img/americana.png";
        if (contiene(n, "pollo", "chicken")) return "/img/pollo.png";
        if (contiene(n, "arroz", "wok", "teriyaki", "rollito")) return "/img/asiatica.png";
        if (contiene(n, "ensalada")) return "/img/ensalada.png";
        if (contiene(n, "tortilla", "croqueta", "brava", "patata", "iberic")) return "/img/tortilla.png";

        return "/img/pizza.png";
    }

    private String resolverRuta(String almacenada, String porDefecto) {
        if (!tieneTexto(almacenada)) {
            return null; // sin imagen → usar fallback por nombre
        }

        String limpia = almacenada.trim();

        // URLs absolutas o rutas ya prefijadas → devolver tal cual
        if (limpia.startsWith("http://") || limpia.startsWith("https://")
                || limpia.startsWith("/img/") || limpia.startsWith("/uploads/")) {
            return limpia;
        }

        if (limpia.contains("/")) {
            // Ruta relativa dentro de uploads (ej: "platos/abc123.jpg")
            // Verificar que el archivo existe físicamente antes de devolverla
            try {
                Path archivo = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(limpia);
                if (Files.exists(archivo)) {
                    return "/uploads/" + limpia;
                }
            } catch (Exception ignored) { }
            return null; // archivo no encontrado → usar fallback por nombre
        }

        return "/img/" + limpia;
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    private String normalizar(String valor) {
        if (valor == null) return "";
        String nfd = Normalizer.normalize(valor, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }

    private boolean contiene(String fuente, String... claves) {
        for (String clave : claves) {
            if (fuente.contains(clave)) {
                return true;
            }
        }
        return false;
    }
}

