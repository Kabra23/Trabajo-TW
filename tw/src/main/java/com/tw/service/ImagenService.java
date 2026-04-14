package com.tw.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImagenService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Guarda un archivo en la carpeta especificada
     * @param file archivo a guardar
     * @param subcarpeta subcarpeta dentro de uploadDir (ej: "platos", "categorias")
     * @return ruta relativa del archivo guardado
     */
    public String guardar(MultipartFile file, String subcarpeta) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        // Crear ruta base
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path subcarpetaPath = uploadPath.resolve(subcarpeta);

        // Crear directorios si no existen
        Files.createDirectories(subcarpetaPath);

        // Generar nombre único para evitar colisiones
        String nombreOriginal = file.getOriginalFilename();
        String extension = nombreOriginal != null ?
            nombreOriginal.substring(nombreOriginal.lastIndexOf(".")) : ".jpg";
        String nombreArchivo = UUID.randomUUID().toString() + extension;

        // Guardar archivo
        Path rutaArchivo = subcarpetaPath.resolve(nombreArchivo);
        Files.copy(file.getInputStream(), rutaArchivo);

        // Retornar ruta relativa para guardar en BD
        return subcarpeta + "/" + nombreArchivo;
    }

    /**
     * Elimina un archivo
     */
    public void eliminar(String rutaRelativa) throws IOException {
        if (rutaRelativa != null && !rutaRelativa.isEmpty()) {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path rutaArchivo = uploadPath.resolve(rutaRelativa);
            Files.deleteIfExists(rutaArchivo);
        }
    }

    /**
     * Valida que el archivo sea una imagen
     */
    public void validarImagen(MultipartFile file) throws IllegalArgumentException {
        if (file == null || file.isEmpty()) return;

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }

        long tamanioMaximo = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > tamanioMaximo) {
            throw new IllegalArgumentException("La imagen no debe superar 5MB");
        }
    }
}

