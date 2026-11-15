package com.example.Studio21.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

/**
 * Formato unico de error de toda la API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        //Esto seria el formato, pero ya en un JSON practicamente
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> errors) {

    public static ErrorResponse de(HttpStatus estado, String mensaje, String ruta) {
        return de(estado, mensaje, ruta, null);
    }

    public static ErrorResponse de(HttpStatus estado, String mensaje, String ruta, Map<String, String> errores) {
        return new ErrorResponse(Instant.now(), estado.value(), estado.getReasonPhrase(), mensaje, ruta, errores);
    }
}