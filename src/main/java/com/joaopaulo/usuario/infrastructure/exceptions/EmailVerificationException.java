package com.joaopaulo.usuario.infrastructure.exceptions;

public class EmailVerificationException extends BusinessException {
    public EmailVerificationException(String message) {
        super(message);
    }

    public EmailVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
