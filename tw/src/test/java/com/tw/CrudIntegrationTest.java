package com.tw;

import com.tw.model.*;
import com.tw.repository.*;
import com.tw.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración CRUD para verificar que la base de datos funciona correctamente.
 * Cubre: Usuario, Restaurante, Plato, Pedido, Valoracion, Categoria
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CrudIntegrationTest {

    @Autowired UsuarioRepository usuarioRepo;
    @Autowired RestauranteRepository restauranteRepo;
    @Autowired PlatoRepository platoRepo;
    @Autowired PedidoRepository pedidoRepo;
    @Autowired ValoracionRepository valoracionRepo;
    @Autowired CategoriaRepository categoriaRepo;
    @Autowired PasswordEncoder passwordEncoder;

    @Autowired UsuarioService usuarioService;
    @Autowired RestauranteService restauranteService;
    @Autowired PlatoService platoService;
    @Autowired ValoracionService valoracionService;

    @BeforeEach
    void limpiarDatos() {
        valoracionRepo.deleteAll();
        pedidoRepo.deleteAll();
        platoRepo.deleteAll();
        restauranteRepo.deleteAll();
        categoriaRepo.deleteAll();
        usuarioRepo.deleteAll();
    }

    // ================================================================
    // CRUD USUARIO
    // ================================================================

    @Test
    @Order(1)
    @DisplayName("CRUD Usuario - CREATE: Registrar usuario nuevo")
    void testUsuario_Create() {
        Usuario u = new Usuario();
        u.setNombre("Juan");
        u.setApellidos("García López");
        u.setEmail("juan.test@example.com");
        u.setPassword(passwordEncoder.encode("Test@1234"));

        Usuario guardado = usuarioRepo.save(u);

        assertNotNull(guardado.getId(), "El ID debe generarse automáticamente");
        assertEquals("Juan", guardado.getNombre());
        assertEquals("juan.test@example.com", guardado.getEmail());

        // Limpieza
        usuarioRepo.delete(guardado);
    }

    @Test
    @Order(2)
    @DisplayName("CRUD Usuario - READ: Buscar por email")
    void testUsuario_Read() {
        // Crear
        Usuario u = new Usuario();
        u.setNombre("Ana");
        u.setApellidos("Martínez");
        u.setEmail("ana.test@example.com");
        u.setPassword(passwordEncoder.encode("Test@1234"));
        usuarioRepo.save(u);

        // Leer
        Optional<Usuario> encontrado = usuarioRepo.findByEmail("ana.test@example.com");
        assertTrue(encontrado.isPresent(), "El usuario debe encontrarse por email");
        assertEquals("Ana", encontrado.get().getNombre());

        // Limpieza
        usuarioRepo.delete(encontrado.get());
    }

    @Test
    @Order(3)
    @DisplayName("CRUD Usuario - UPDATE: Actualizar nombre y apellidos")
    void testUsuario_Update() {
        // Crear
        Usuario u = new Usuario();
        u.setNombre("Pedro");
        u.setApellidos("Sánchez");
        u.setEmail("pedro.test@example.com");
        u.setPassword(passwordEncoder.encode("Test@1234"));
        Usuario guardado = usuarioRepo.save(u);

        // Actualizar
        usuarioService.actualizarPerfil(guardado.getId(), "Pedro Actualizado", "Sánchez Nuevo", null);

        // Verificar
        Usuario actualizado = usuarioRepo.findById(guardado.getId()).orElseThrow();
        assertEquals("Pedro Actualizado", actualizado.getNombre());
        assertEquals("Sánchez Nuevo", actualizado.getApellidos());

        // Limpieza
        usuarioRepo.delete(actualizado);
    }

    @Test
    @Order(4)
    @DisplayName("CRUD Usuario - DELETE: Eliminar cuenta")
    void testUsuario_Delete() {
        // Crear
        Usuario u = new Usuario();
        u.setNombre("Luis");
        u.setApellidos("Fernández");
        u.setEmail("luis.test@example.com");
        u.setPassword(passwordEncoder.encode("Test@1234"));
        Usuario guardado = usuarioRepo.save(u);
        Long id = guardado.getId();

        // Eliminar
        usuarioRepo.deleteById(id);

        // Verificar
        assertFalse(usuarioRepo.existsById(id), "El usuario debe haber sido eliminado");
    }

    @Test
    @Order(5)
    @DisplayName("CRUD Usuario - Email duplicado lanza excepción")
    void testUsuario_EmailDuplicado() {
        Usuario u1 = new Usuario();
        u1.setNombre("Carlos");
        u1.setApellidos("Ruiz");
        u1.setEmail("carlos.test@example.com");
        u1.setPassword(passwordEncoder.encode("Test@1234"));
        usuarioRepo.save(u1);

        Usuario u2 = new Usuario();
        u2.setNombre("Carlos2");
        u2.setApellidos("Ruiz2");
        u2.setEmail("carlos.test@example.com"); // mismo email
        u2.setPassword(passwordEncoder.encode("Test@1234"));

        // Debe lanzar excepción de unicidad
        assertThrows(Exception.class, () -> {
            usuarioRepo.saveAndFlush(u2);
        }, "Debe fallar con email duplicado");

        // Limpieza
        usuarioRepo.findByEmail("carlos.test@example.com").ifPresent(usuarioRepo::delete);
    }

    // ================================================================
    // CRUD RESTAURANTE
    // ================================================================

    @Test
    @Order(6)
    @DisplayName("CRUD Restaurante - CREATE: Crear restaurante con propietario")
    @Transactional
    void testRestaurante_Create() {
        // Crear propietario
        Usuario propietario = new Usuario();
        propietario.setNombre("Chef");
        propietario.setApellidos("Restaurantero");
        propietario.setEmail("chef.test@example.com");
        propietario.setPassword(passwordEncoder.encode("Test@1234"));
        usuarioRepo.save(propietario);

        // Crear restaurante
        Restaurante r = new Restaurante();
        r.setNombre("Restaurante Test");
        r.setDireccion("Calle Test 1, Mérida");
        r.setTelefono("+34 924 00 00 00");
        r.setEmail("rest.test@example.com");
        r.setPrecioMin(5.0);
        r.setPrecioMax(20.0);
        r.setBikeFriendly(true);
        r.setAceptaPedidos(true);
        r.setLocalidad("Mérida");
        r.setPropietario(propietario);

        Restaurante guardado = restauranteRepo.save(r);

        assertNotNull(guardado.getId());
        assertEquals("Restaurante Test", guardado.getNombre());
        assertTrue(guardado.getAceptaPedidos());

        // Limpieza
        restauranteRepo.delete(guardado);
        usuarioRepo.delete(propietario);
    }

    @Test
    @Order(7)
    @DisplayName("CRUD Restaurante - READ: Buscar por localidad")
    @Transactional
    void testRestaurante_Read() {
        // Crear propietario
        Usuario propietario = new Usuario();
        propietario.setNombre("Chef2");
        propietario.setApellidos("Test");
        propietario.setEmail("chef2.test@example.com");
        propietario.setPassword(passwordEncoder.encode("Test@1234"));
        usuarioRepo.save(propietario);

        // Crear restaurante en Badajoz
        Restaurante r = new Restaurante();
        r.setNombre("Bar Badajoz");
        r.setDireccion("Calle Mayor 1");
        r.setTelefono("+34 924 11 11 11");
        r.setEmail("bar.test@example.com");
        r.setLocalidad("Badajoz");
        r.setAceptaPedidos(true);
        r.setPropietario(propietario);
        restauranteRepo.save(r);

        // Buscar por localidad
        List<Restaurante> resultados = restauranteRepo.findByNombreContainingIgnoreCaseOrLocalidadContainingIgnoreCase("Badajoz", "Badajoz");
        assertFalse(resultados.isEmpty(), "Debe encontrar restaurantes en Badajoz");
        assertTrue(resultados.stream().anyMatch(res -> res.getNombre().equals("Bar Badajoz")));

        // Limpieza
        restauranteRepo.delete(r);
        usuarioRepo.delete(propietario);
    }

    @Test
    @Order(8)
    @DisplayName("CRUD Restaurante - UPDATE: Cambiar estado acepta pedidos")
    @Transactional
    void testRestaurante_Update_Estado() {
        // Crear propietario y restaurante
        Usuario propietario = new Usuario();
        propietario.setNombre("Chef3");
        propietario.setApellidos("Test");
        propietario.setEmail("chef3.test@example.com");
        propietario.setPassword(passwordEncoder.encode("Test@1234"));
        usuarioRepo.save(propietario);

        Restaurante r = new Restaurante();
        r.setNombre("Restaurante Estado Test");
        r.setDireccion("Calle 1");
        r.setTelefono("+34 924 22 22 22");
        r.setEmail("estado.test@example.com");
        r.setAceptaPedidos(true);
        r.setPropietario(propietario);
        Restaurante guardado = restauranteRepo.save(r);

        // Cambiar estado
        Restaurante rest = restauranteService.buscarPorId(guardado.getId());
        rest.setAceptaPedidos(false);
        restauranteService.actualizar(rest.getId(), rest, propietario.getEmail(), null);


        // Verificar
        Restaurante actualizado = restauranteRepo.findById(guardado.getId()).orElseThrow();
        assertFalse(actualizado.getAceptaPedidos(), "Debe estar cerrado");

        // Limpieza
        restauranteRepo.delete(actualizado);
        usuarioRepo.delete(propietario);
    }

    @Test
    @Order(9)
    @DisplayName("CRUD Restaurante - DELETE: Eliminar restaurante (solo propietario)")
    @Transactional
    void testRestaurante_Delete() {
        Usuario propietario = new Usuario();
        propietario.setNombre("Chef4");
        propietario.setApellidos("Test");
        propietario.setEmail("chef4.test@example.com");
        propietario.setPassword(passwordEncoder.encode("Test@1234"));
        usuarioRepo.save(propietario);

        Restaurante r = new Restaurante();
        r.setNombre("Restaurante Para Borrar");
        r.setDireccion("Calle Delete 1");
        r.setTelefono("+34 924 33 33 33");
        r.setEmail("delete.test@example.com");
        r.setAceptaPedidos(true);
        r.setPropietario(propietario);
        Restaurante guardado = restauranteRepo.save(r);
        Long id = guardado.getId();

        // Eliminar
        restauranteService.eliminar(id, propietario.getEmail());

        // Verificar
        assertFalse(restauranteRepo.existsById(id), "El restaurante debe haberse eliminado");

        // Limpieza
        usuarioRepo.delete(propietario);
    }

    @Test
    @Order(10)
    @DisplayName("CRUD Restaurante - Seguridad: No propietario no puede eliminar")
    @Transactional
    void testRestaurante_Seguridad_NoPropietario() {
        Usuario propietario = new Usuario();
        propietario.setNombre("Chef5");
        propietario.setApellidos("Test");
        propietario.setEmail("chef5.test@example.com");
        propietario.setPassword(passwordEncoder.encode("Test@1234"));
        usuarioRepo.save(propietario);

        Usuario otro = new Usuario();
        otro.setNombre("Otro");
        otro.setApellidos("Usuario");
        otro.setEmail("otro.test@example.com");
        otro.setPassword(passwordEncoder.encode("Test@1234"));
        usuarioRepo.save(otro);

        Restaurante r = new Restaurante();
        r.setNombre("Restaurante Seguridad");
        r.setDireccion("Calle Seg 1");
        r.setTelefono("+34 924 44 44 44");
        r.setEmail("seg.test@example.com");
        r.setAceptaPedidos(true);
        r.setPropietario(propietario);
        Restaurante guardado = restauranteRepo.save(r);

        // Otro usuario NO puede eliminar
        assertThrows(SecurityException.class,
                () -> restauranteService.eliminar(guardado.getId(), otro.getEmail()),
                "Debe lanzar SecurityException si no eres el propietario");

        // Limpieza
        restauranteRepo.delete(guardado);
        usuarioRepo.delete(propietario);
        usuarioRepo.delete(otro);
    }

    // ================================================================
    // CRUD PLATO
    // ================================================================

    @Test
    @Order(11)
    @DisplayName("CRUD Plato - CREATE + READ + UPDATE + DELETE completo")
    @Transactional
    void testPlato_CrudCompleto() {
        // Setup
        Usuario propietario = new Usuario();
        propietario.setNombre("Chef6");
        propietario.setApellidos("Test");
        propietario.setEmail("chef6.test@example.com");
        propietario.setPassword(passwordEncoder.encode("Test@1234"));
        usuarioRepo.save(propietario);

        Restaurante r = new Restaurante();
        r.setNombre("Rest Platos");
        r.setDireccion("Calle P 1");
        r.setTelefono("+34 924 55 55 55");
        r.setEmail("plato.rest@example.com");
        r.setAceptaPedidos(true);
        r.setPropietario(propietario);
        restauranteRepo.save(r);

        // CREATE
        Plato plato = new Plato();
        plato.setNombre("Tortilla Española");
        plato.setDescripcion("Tortilla casera con patata y cebolla");
        plato.setPrecio(5.50);
        Plato guardado = platoService.crear(r.getId(), plato, propietario.getEmail());
        assertNotNull(guardado.getId(), "El plato debe tener ID");

        // READ
        List<Plato> platos = platoRepo.findByRestauranteId(r.getId());
        assertFalse(platos.isEmpty());
        assertEquals("Tortilla Española", platos.get(0).getNombre());

        // UPDATE
        Plato datosActualizados = new Plato();
        datosActualizados.setNombre("Tortilla con Jamón");
        datosActualizados.setDescripcion("Tortilla mejorada");
        datosActualizados.setPrecio(6.50);
        platoService.actualizar(guardado.getId(), datosActualizados, propietario.getEmail());
        Plato actualizado = platoRepo.findById(guardado.getId()).orElseThrow();
        assertEquals("Tortilla con Jamón", actualizado.getNombre());
        assertEquals(6.50, actualizado.getPrecio());

        // DELETE
        platoService.eliminar(guardado.getId(), propietario.getEmail());
        assertFalse(platoRepo.existsById(guardado.getId()), "El plato debe haberse eliminado");

        // Limpieza
        restauranteRepo.delete(r);
        usuarioRepo.delete(propietario);
    }

    // ================================================================
    // CRUD VALORACION
    // ================================================================

    @Test
    @Order(12)
    @DisplayName("CRUD Valoración - CREATE + READ + DELETE")
    @Transactional
    void testValoracion_CrudCompleto() {
        // Setup
        Usuario propietario = new Usuario();
        propietario.setNombre("Prop");
        propietario.setApellidos("Test");
        propietario.setEmail("prop.val@example.com");
        propietario.setPassword(passwordEncoder.encode("Test@1234"));
        usuarioRepo.save(propietario);

        Usuario cliente = new Usuario();
        cliente.setNombre("Cliente");
        cliente.setApellidos("Test");
        cliente.setEmail("cliente.val@example.com");
        cliente.setPassword(passwordEncoder.encode("Test@1234"));
        usuarioRepo.save(cliente);

        Restaurante r = new Restaurante();
        r.setNombre("Rest Val");
        r.setDireccion("Calle V 1");
        r.setTelefono("+34 924 66 66 66");
        r.setEmail("val.rest@example.com");
        r.setAceptaPedidos(true);
        r.setPropietario(propietario);
        restauranteRepo.save(r);

        // CREATE valoración
        Valoracion val = new Valoracion();
        val.setPuntuacion(5);
        val.setComentario("Excelente restaurante, muy recomendable");
        Valoracion guardada = valoracionService.crear(r.getId(), val, cliente.getEmail());

        assertNotNull(guardada.getId());
        assertEquals(5, guardada.getPuntuacion());

        // READ - La media del restaurante debe actualizarse
        Restaurante actualizado = restauranteRepo.findById(r.getId()).orElseThrow();
        assertEquals(5.0, actualizado.getMediaValoraciones(), 0.01, "La media debe ser 5.0");

        // No puede valorar dos veces el mismo restaurante
        Valoracion val2 = new Valoracion();
        val2.setPuntuacion(3);
        assertThrows(IllegalArgumentException.class,
                () -> valoracionService.crear(r.getId(), val2, cliente.getEmail()),
                "No puede valorar dos veces el mismo restaurante");

        // DELETE
        valoracionService.eliminar(guardada.getId(), cliente.getEmail());
        assertFalse(valoracionRepo.existsById(guardada.getId()));

        // Limpieza
        restauranteRepo.delete(r);
        usuarioRepo.delete(propietario);
        usuarioRepo.delete(cliente);
    }

    // ================================================================
    // CRUD CATEGORIA
    // ================================================================

    @Test
    @Order(13)
    @DisplayName("CRUD Categoría - CREATE + READ + UPDATE + DELETE")
    void testCategoria_CrudCompleto() {
        // CREATE
        Categoria cat = new Categoria();
        cat.setNombre("Comida Test " + System.currentTimeMillis());
        cat.setDescripcion("Descripción de prueba");
        Categoria guardada = categoriaRepo.save(cat);
        assertNotNull(guardada.getId());

        // READ
        Optional<Categoria> encontrada = categoriaRepo.findById(guardada.getId());
        assertTrue(encontrada.isPresent());
        assertEquals(cat.getNombre(), encontrada.get().getNombre());

        // UPDATE
        encontrada.get().setDescripcion("Descripción actualizada");
        categoriaRepo.save(encontrada.get());
        Categoria actualizada = categoriaRepo.findById(guardada.getId()).orElseThrow();
        assertEquals("Descripción actualizada", actualizada.getDescripcion());

        // DELETE
        categoriaRepo.deleteById(guardada.getId());
        assertFalse(categoriaRepo.existsById(guardada.getId()));
    }

    // ================================================================
    // CRUD BÚSQUEDA
    // ================================================================

    @Test
    @Order(14)
    @DisplayName("Búsqueda - Buscar restaurante por nombre o localidad (req. mínimo 6)")
    @Transactional
    void testBusqueda_PorNombreOLocalidad() {
        Usuario propietario = new Usuario();
        propietario.setNombre("Busca");
        propietario.setApellidos("Test");
        propietario.setEmail("busca.test@example.com");
        propietario.setPassword(passwordEncoder.encode("Test@1234"));
        usuarioRepo.save(propietario);

        Restaurante r = new Restaurante();
        r.setNombre("La Taberna del Puerto");
        r.setDireccion("Puerto 1");
        r.setTelefono("+34 924 77 77 77");
        r.setEmail("busca.rest@example.com");
        r.setLocalidad("Cáceres");
        r.setAceptaPedidos(true);
        r.setPropietario(propietario);
        restauranteRepo.save(r);

        // Buscar por nombre
        List<Restaurante> porNombre = restauranteRepo.findByNombreContainingIgnoreCaseOrLocalidadContainingIgnoreCase("taberna", "taberna");
        assertTrue(porNombre.stream().anyMatch(res -> res.getNombre().contains("Taberna")));

        // Buscar por localidad
        List<Restaurante> porLocalidad = restauranteRepo.findByNombreContainingIgnoreCaseOrLocalidadContainingIgnoreCase("Cáceres", "Cáceres");
        assertTrue(porLocalidad.stream().anyMatch(res -> "Cáceres".equals(res.getLocalidad())));

        // Limpieza
        restauranteRepo.delete(r);
        usuarioRepo.delete(propietario);
    }

    @Test
    @Order(15)
    @DisplayName("Filtro - Restaurantes que aceptan vs no aceptan pedidos (req. mínimo 7)")
    @Transactional
    void testFiltro_AceptaPedidos() {
        Usuario propietario = new Usuario();
        propietario.setNombre("Filtro");
        propietario.setApellidos("Test");
        propietario.setEmail("filtro.test@example.com");
        propietario.setPassword(passwordEncoder.encode("Test@1234"));
        usuarioRepo.save(propietario);

        Restaurante abierto = new Restaurante();
        abierto.setNombre("Rest Abierto");
        abierto.setDireccion("Calle A 1");
        abierto.setTelefono("+34 924 88 88 88");
        abierto.setEmail("abierto@example.com");
        abierto.setAceptaPedidos(true);
        abierto.setPropietario(propietario);
        restauranteRepo.save(abierto);

        Restaurante cerrado = new Restaurante();
        cerrado.setNombre("Rest Cerrado");
        cerrado.setDireccion("Calle C 1");
        cerrado.setTelefono("+34 924 99 99 99");
        cerrado.setEmail("cerrado@example.com");
        cerrado.setAceptaPedidos(false);
        cerrado.setPropietario(propietario);
        restauranteRepo.save(cerrado);

        // Verificar filtros
        List<Restaurante> aceptan = restauranteRepo.findByAceptaPedidos(true);
        List<Restaurante> noAceptan = restauranteRepo.findByAceptaPedidos(false);

        assertTrue(aceptan.stream().anyMatch(r -> r.getNombre().equals("Rest Abierto")));
        assertTrue(noAceptan.stream().anyMatch(r -> r.getNombre().equals("Rest Cerrado")));
        assertFalse(aceptan.stream().anyMatch(r -> r.getNombre().equals("Rest Cerrado")));

        // Limpieza
        restauranteRepo.delete(abierto);
        restauranteRepo.delete(cerrado);
        usuarioRepo.delete(propietario);
    }
}
