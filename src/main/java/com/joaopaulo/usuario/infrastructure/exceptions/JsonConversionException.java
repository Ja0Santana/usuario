package com.joaopaulo.usuario.infrastructure.exceptions;

public class JsonConversionException extends RuntimeException {
    public JsonConversionException(String message) {
        super(message);
    }
    public JsonConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
