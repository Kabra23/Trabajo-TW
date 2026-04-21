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

import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * Búsqueda principal con todos los filtros (req. mínimo 6 y 7).
     */
    @Transactional(readOnly = true)
    public List<Restaurante> buscar(String q, Long categoriaId, String filtro,
                                    Integer valoracion, Integer precio,
                                    Boolean bikeFriendly, String orden) {
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
                predicates.add(cb.greaterThanOrEqualTo(root.get("mediaValoraciones"), (double) valoracion));
            }

            if (bikeFriendly != null && bikeFriendly) {
                predicates.add(cb.isTrue(root.get("bikeFriendly")));
            }

            if (orden != null) {
                switch (orden) {
                    case "nombre_asc" -> query.orderBy(cb.asc(root.get("nombre")));
                    case "nombre_desc" -> query.orderBy(cb.desc(root.get("nombre")));
                    case "precio_asc" -> query.orderBy(cb.asc(root.get("precioMin")));
                    case "precio_desc" -> query.orderBy(cb.desc(root.get("precioMin")));
                    case "valoracion_desc" -> query.orderBy(cb.desc(root.get("mediaValoraciones")));
                }
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        });
    }

    /**
     * Búsqueda AVANZADA (extra 3): busca en nombre, localidad, platos, comentarios y rango de precios.
     */
    @Transactional(readOnly = true)
    public List<Restaurante> busquedaAvanzada(String q, Long categoriaId, String localidad,
                                               Double precioMin, Double precioMax) {
        // Obtenemos todos y filtramos en memoria para poder cruzar con platos y valoraciones
        List<Restaurante> todos = restauranteRepo.findAll();

        return todos.stream().filter(r -> {
            // Filtro por texto (nombre, localidad, platos, comentarios/valoraciones)
            if (q != null && !q.isBlank()) {
                String ql = q.toLowerCase();
                boolean enNombre = r.getNombre() != null && r.getNombre().toLowerCase().contains(ql);
                boolean enLocalidad = r.getLocalidad() != null && r.getLocalidad().toLowerCase().contains(ql);
                boolean enPlatos = r.getPlatos() != null && r.getPlatos().stream().anyMatch(p ->
                        (p.getNombre() != null && p.getNombre().toLowerCase().contains(ql)) ||
                        (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(ql))
                );
                boolean enComentarios = r.getValoraciones() != null && r.getValoraciones().stream().anyMatch(v ->
                        v.getComentario() != null && v.getComentario().toLowerCase().contains(ql)
                );
                if (!enNombre && !enLocalidad && !enPlatos && !enComentarios) return false;
            }

            // Filtro por categoría
            if (categoriaId != null) {
                boolean tieneCategoria = r.getCategorias() != null &&
                        r.getCategorias().stream().anyMatch(c -> c.getId().equals(categoriaId));
                if (!tieneCategoria) return false;
            }

            // Filtro por localidad exacta (insensible a mayúsculas)
            if (localidad != null && !localidad.isBlank()) {
                if (r.getLocalidad() == null ||
                        !r.getLocalidad().toLowerCase().contains(localidad.toLowerCase())) return false;
            }

            // Filtro rango de precios
            if (precioMin != null && r.getPrecioMin() != null && r.getPrecioMax() != null) {
                if (r.getPrecioMax() < precioMin) return false;
            }
            if (precioMax != null && r.getPrecioMin() != null) {
                if (r.getPrecioMin() > precioMax) return false;
            }

            return true;
        }).collect(Collectors.toList());
    }

    /**
     * Restaurantes relacionados (extra 4): busca restaurantes similares al dado
     * en función de categoría, localidad, precio y valoración.
     * Devuelve máximo 4 resultados (excluye el propio restaurante).
     */
    @Transactional(readOnly = true)
    public List<Restaurante> buscarRelacionados(Restaurante restaurante) {
        Set<Long> categoriaIds = restaurante.getCategorias() == null ? Set.of() :
                restaurante.getCategorias().stream().map(Categoria::getId).collect(Collectors.toSet());

        List<Restaurante> todos = restauranteRepo.findAll();

        // Puntuación de similitud: más puntos = más relacionado
        Map<Restaurante, Integer> puntos = new LinkedHashMap<>();

        for (Restaurante r : todos) {
            if (r.getId().equals(restaurante.getId())) continue;

            int score = 0;

            // +3 por cada categoría en común
            if (r.getCategorias() != null) {
                for (Categoria c : r.getCategorias()) {
                    if (categoriaIds.contains(c.getId())) score += 3;
                }
            }

            // +2 por misma localidad
            if (restaurante.getLocalidad() != null && r.getLocalidad() != null
                    && restaurante.getLocalidad().equalsIgnoreCase(r.getLocalidad())) {
                score += 2;
            }

            // +1 si el rango de precio se solapa
            if (restaurante.getPrecioMin() != null && r.getPrecioMin() != null) {
                double minA = restaurante.getPrecioMin();
                double maxA = restaurante.getPrecioMax() != null ? restaurante.getPrecioMax() : minA + 10;
                double minB = r.getPrecioMin();
                double maxB = r.getPrecioMax() != null ? r.getPrecioMax() : minB + 10;
                if (minA <= maxB && minB <= maxA) score += 1;
            }

            // +1 si valoración similar (diferencia <= 1)
            if (restaurante.getMediaValoraciones() != null && r.getMediaValoraciones() != null) {
                if (Math.abs(restaurante.getMediaValoraciones() - r.getMediaValoraciones()) <= 1.0) {
                    score += 1;
                }
            }

            if (score > 0) {
                puntos.put(r, score);
            }
        }

        return puntos.entrySet().stream()
                .sorted(Map.Entry.<Restaurante, Integer>comparingByValue().reversed())
                .limit(4)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
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
        existente.setEmail(datos.getEmail());
        existente.setPrecioMin(datos.getPrecioMin());
        existente.setPrecioMax(datos.getPrecioMax());
        existente.setBikeFriendly(datos.getBikeFriendly());
        existente.setAceptaPedidos(datos.getAceptaPedidos());

        if (datos.getImagen() != null) {
            existente.setImagen(datos.getImagen());
        }
        String etiquetasNormalizadas = normalizarEtiquetasMenu(datos.getEtiquetasMenu());
        existente.setEtiquetasMenu(etiquetasNormalizadas);
        reasignarEtiquetasPlatos(existente, etiquetasNormalizadas);

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

    /** Guarda las etiquetas del menú (tabs editables por el propietario) */
    public void guardarEtiquetasMenu(Long id, String etiquetas, String email) {
        Restaurante restaurante = buscarPorId(id);
        verificarPropietario(restaurante, email);
        String etiquetasNormalizadas = normalizarEtiquetasMenu(etiquetas);
        restaurante.setEtiquetasMenu(etiquetasNormalizadas);
        reasignarEtiquetasPlatos(restaurante, etiquetasNormalizadas);
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
            throw new SecurityException("No tienes permiso para realizar esta accion");
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

    private String normalizarEtiquetasMenu(String etiquetasRaw) {
        if (etiquetasRaw == null || etiquetasRaw.isBlank()) {
            return null;
        }

        LinkedHashSet<String> unicas = Arrays.stream(etiquetasRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.length() > 40 ? s.substring(0, 40) : s)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (unicas.isEmpty()) {
            return null;
        }
        return String.join(",", unicas);
    }

    private void reasignarEtiquetasPlatos(Restaurante restaurante, String etiquetasRaw) {
        List<String> etiquetas = etiquetasRaw == null || etiquetasRaw.isBlank()
                ? restaurante.getEtiquetasMenuLista()
                : Arrays.stream(etiquetasRaw.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();

        if (etiquetas.isEmpty() || restaurante.getPlatos() == null) {
            return;
        }

        String etiquetaPorDefecto = etiquetas.get(0);
        for (var plato : restaurante.getPlatos()) {
            String actual = plato.getEtiquetaMenu();
            boolean valida = actual != null && etiquetas.stream().anyMatch(e -> e.equalsIgnoreCase(actual.trim()));
            if (!valida) {
                plato.setEtiquetaMenu(etiquetaPorDefecto);
            }
        }
    }
}
