package com.plug.security.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Representa un error de API en formato JSON.
 * Utilizado para respuestas de error en validaciones de seguridad.
 */
@Getter
@Setter
public class ApiError {
    
    @JsonProperty
    private String message;
    
    @JsonProperty
    private String error;

    public ApiError(String error, String message) {
        this.error = error;
        this.message = message;
    }
}
