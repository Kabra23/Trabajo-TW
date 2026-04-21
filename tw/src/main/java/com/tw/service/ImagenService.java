package com.tw.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImagenService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Guarda un archivo en la carpeta especificada y devuelve la ruta relativa.
     *
     * La ruta devuelta es relativa DENTRO de uploadDir, por ejemplo:
     *   "usuarios/abc123.jpg"
     *
     * En Thymeleaf se usa como: th:src="@{'/uploads/' + ${usuario.fotoPerfil}}"
     * El WebMvcConfig mapea /uploads/** al directorio físico de uploads.
     *
     * @param file      archivo a guardar
     * @param subcarpeta subcarpeta dentro de uploadDir (ej: "usuarios", "restaurantes")
     * @return ruta relativa dentro de uploadDir
     */
    public String guardar(MultipartFile file, String subcarpeta) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo esta vacio");
        }

        // Resolver la ruta absoluta del directorio base
        Path baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path subDir  = baseDir.resolve(subcarpeta);

        // Crear directorios si no existen
        Files.createDirectories(subDir);

        // Generar nombre único para evitar colisiones
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        } else {
            extension = ".jpg";
        }
        String nombreArchivo = UUID.randomUUID().toString() + extension;

        // Guardar con REPLACE_EXISTING por si hubiera una colisión (muy improbable con UUID)
        Path destino = subDir.resolve(nombreArchivo);
        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        // Devolver ruta relativa: "subcarpeta/nombreArchivo"
        return subcarpeta + "/" + nombreArchivo;
    }

    /**
     * Elimina un archivo a partir de su ruta relativa.
     */
    public void eliminar(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) return;
        try {
            Path baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path archivo = baseDir.resolve(rutaRelativa).normalize();
            // Seguridad: verificar que el archivo esté dentro de baseDir
            if (archivo.startsWith(baseDir)) {
                Files.deleteIfExists(archivo);
            }
        } catch (IOException e) {
            System.err.println("No se pudo eliminar la imagen: " + e.getMessage());
        }
    }

    /**
     * Valida que el archivo sea una imagen con tipo MIME correcto y tamaño aceptable.
     */
    public void validarImagen(MultipartFile file) {
        if (file == null || file.isEmpty()) return;

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen (JPEG, PNG o WebP)");
        }

        // Rechazar SVG y otros formatos potencialmente peligrosos
        if (contentType.contains("svg") || contentType.contains("xml")) {
            throw new IllegalArgumentException("Formato de imagen no permitido");
        }

        long maxSize = 5L * 1024 * 1024; // 5 MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("La imagen no debe superar 5 MB");
        }
    }
}
