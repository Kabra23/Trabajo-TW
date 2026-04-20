package com.tw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * CAMBIO PARA TOMCAT EXTERNO:
 * La clase principal debe extender SpringBootServletInitializer.
 * Esto permite que Tomcat arranque la aplicación Spring cuando
 * detecta el WAR en su directorio webapps/.
 *
 * Sin esta clase, Tomcat no sabría cómo inicializar Spring.
 */
@SpringBootApplication
public class TwApplication extends SpringBootServletInitializer {

    /**
     * REQUERIDO para Tomcat externo.
     * Configura la aplicación cuando es lanzada por Tomcat.
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(TwApplication.class);
    }

    /**
     * Punto de entrada cuando se ejecuta como JAR (desarrollo local).
     * Sigue funcionando normalmente con: mvn spring-boot:run
     */
    public static void main(String[] args) {
        SpringApplication.run(TwApplication.class, args);
    }
}
