package com.tw.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Configuración para servir los archivos subidos por los usuarios.
 *
 * Por defecto Spring Boot sirve recursos estáticos desde /static,
 * pero los uploads se guardan en el sistema de ficheros externo.
 * Este configurer añade un handler que mapea /uploads/** a la carpeta
 * real donde se almacenan los ficheros.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Construye la ruta absoluta y asegura que termine en /
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize().toString();
        if (!uploadPath.endsWith("/")) {
            uploadPath = uploadPath + "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
