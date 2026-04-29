package com.tw.api;

import com.tw.api.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones exclusivo para la API REST (/api/**).
 *
 * Garantiza que TODOS los errores de la API devuelvan JSON con estructura
 * consistente. Separa la gestión de errores MVC (HTML) de la API (JSON).
 *
 * Códigos de estado usados:
 *   400 Bad Request       - validación, argumento ilegal
 *   401 Unauthorized      - no autenticado
 *   403 Forbidden         - autenticado pero sin permiso
 *   404 Not Found         - recurso no existe
 *   409 Conflict          - duplicado, estado inválido
 *   500 Internal Error    - error inesperado (sin detalles internos)
 */
@RestControllerAdvice(basePackages = "com.tw.api")
public class ApiExceptionHandler {

    /** 400 - Errores de validación (@Valid) con detalle por campo */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
            errores.put(campo, error.getDefaultMessage());
        });

        Map<String, Object> body = new HashMap<>();
        body.put("status", 400);
        body.put("error", "Bad Request");
        body.put("mensaje", "Errores de validación en los datos enviados");
        body.put("errores", errores);
        body.put("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(body);
    }

    /** 400 - Argumento ilegal (datos inválidos de negocio) */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                400, "Bad Request", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest().body(error);
    }

    /** 403 - Acceso denegado por Spring Security */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                403, "Forbidden",
                "No tienes permiso para realizar esta acción",
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /** 403 - Acceso denegado por lógica propia */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurity(
            SecurityException ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                403, "Forbidden", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /** 409 - Conflicto (duplicados, estado inválido) */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            IllegalStateException ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                409, "Conflict", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * 500 - Error inesperado.
     * IMPORTANTE: No se exponen detalles internos para evitar fuga de información.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                500, "Internal Server Error",
                "Ha ocurrido un error interno. Por favor, inténtalo de nuevo.",
                request.getRequestURI());
        return ResponseEntity.internalServerError().body(error);
    }
}
