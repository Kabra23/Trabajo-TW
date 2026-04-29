package com.tw.api.dto;

import java.time.LocalDateTime;

/**
 * DTO estándar para respuestas de error de la API REST.
 */
public class ErrorResponse {

    private int status;
    private String error;
    private String mensaje;
    private String path;
    private LocalDateTime timestamp;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String mensaje, String path) {
        this();
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.path = path;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
