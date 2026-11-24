package com.jousting.rpg.api.dto;

import java.time.LocalDateTime;

/**
 * Estructura estándar de respuesta para errores de la API.
 */
public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp
) {
    public ErrorResponse(String code, String message) {
        this(code, message, LocalDateTime.now());
    }
}
