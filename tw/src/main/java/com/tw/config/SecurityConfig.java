package com.tw.config;

import com.tw.service.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService) {
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── Autorización de rutas ──────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Estáticos y uploads
                .requestMatchers("/css/**", "/js/**", "/imagenes/**", "/img/**", "/uploads/**").permitAll()
                // Páginas MVC públicas
                .requestMatchers("/", "/restaurantes", "/restaurantes/{id}").permitAll()
                .requestMatchers("/busqueda").permitAll()
                .requestMatchers("/categorias").permitAll()
                .requestMatchers("/login", "/registro", "/registro-exitoso").permitAll()
                // Admin
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Autenticados MVC
                .requestMatchers("/perfil/**").authenticated()

                // ── API REST ──────────────────────────────────────────────
                // Públicos (no requieren auth)
                .requestMatchers("GET", "/api/restaurantes/all").permitAll()
                .requestMatchers("GET", "/api/restaurantes/{id}").permitAll()
                .requestMatchers("GET", "/api/restaurantes/{id}/platos").permitAll()
                .requestMatchers("GET", "/api/restaurantes/{id}/valoraciones").permitAll()
                // Categorías: listado y detalle público
                .requestMatchers("GET",  "/api/categorias").permitAll()
                .requestMatchers("GET",  "/api/categorias/{id}").permitAll()
                .requestMatchers("GET",  "/api/categorias/{id}/restaurantes").permitAll()

                // Registro y login no requieren auth
                .requestMatchers("POST", "/api/usuarios").permitAll()
                .requestMatchers("POST", "/api/sesiones").permitAll()
                // Gestión de admins: solo ROLE_ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // El resto de la API requiere autenticación
                .requestMatchers("/api/**").authenticated()

                .anyRequest().authenticated()
            )

            // ── Formulario de login MVC ────────────────────────────────────
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/restaurantes", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // ── Logout ────────────────────────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )

            // ── HTTP Basic para la API REST ────────────────────────────────
            // Permite autenticar con cabecera Authorization: Basic <base64(email:pass)>
            .httpBasic(basic -> {})

            // ── CSRF ──────────────────────────────────────────────────────
            // Deshabilitar solo para rutas /api/** (los clientes REST no usan sesión)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )

            .userDetailsService(usuarioDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean AuthenticationManager necesario para el endpoint POST /api/sesiones (login).
     * Permite autenticar manualmente en el controlador.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
