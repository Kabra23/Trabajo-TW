package com.tw.config;

import com.tw.service.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService) {
        this.usuarioDetailsService = usuarioDetailsService;
    }

    // ── Cadena 1: API REST (/api/**) — httpBasic + stateless ─────────────────
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("GET", "/api/restaurantes/all").permitAll()
                .requestMatchers("GET", "/api/restaurantes/{id}").permitAll()
                .requestMatchers("GET", "/api/restaurantes/{id}/platos").permitAll()
                .requestMatchers("GET", "/api/restaurantes/{id}/valoraciones").permitAll()
                .requestMatchers("GET", "/api/categorias").permitAll()
                .requestMatchers("GET", "/api/categorias/{id}").permitAll()
                .requestMatchers("GET", "/api/categorias/{id}/restaurantes").permitAll()
                .requestMatchers("POST", "/api/usuarios").permitAll()
                .requestMatchers("POST", "/api/sesiones").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {})
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .csrf(csrf -> csrf.disable())
            .userDetailsService(usuarioDetailsService);

        return http.build();
    }

    // ── Cadena 2: MVC web — solo formLogin, sin httpBasic ────────────────────
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/imagenes/**", "/img/**", "/uploads/**").permitAll()
                .requestMatchers("/", "/restaurantes", "/restaurantes/{id}").permitAll()
                .requestMatchers("/busqueda").permitAll()
                .requestMatchers("/login", "/registro", "/registro-exitoso").permitAll()
                    .requestMatchers("/restaurantes/{id}/valoraciones", "/error-403").permitAll()
                // Admin MVC
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/categorias").hasRole("ADMIN")
                .requestMatchers("/categorias/nueva", "/categorias/*/editar", "/categorias/*/eliminar").hasRole("ADMIN")
                .requestMatchers("/usuarios", "/usuarios/*").hasRole("ADMIN")
                .requestMatchers("/usuarios/nuevo", "/usuarios/*/editar", "/usuarios/*/eliminar",
                                 "/usuarios/*/admin", "/usuarios/*/admin/revocar").hasRole("ADMIN")
                // Autenticados MVC
                .requestMatchers("/perfil/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/restaurantes", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                .accessDeniedPage("/error-403")
            )
            .httpBasic(basic -> basic.disable())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .userDetailsService(usuarioDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
