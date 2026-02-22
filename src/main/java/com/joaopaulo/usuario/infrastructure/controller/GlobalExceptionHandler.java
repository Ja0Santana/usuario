package com.joaopaulo.usuario.infrastructure.controller;

import com.joaopaulo.usuario.infrastructure.exceptions.ConflictException;
import com.joaopaulo.usuario.infrastructure.exceptions.IllegalArgumentException;
import com.joaopaulo.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.joaopaulo.usuario.infrastructure.exceptions.UnauthorizedException;
import com.joaopaulo.usuario.infrastructure.exceptions.dtos.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException ex,
                                                                            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(builderError(HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.NOT_FOUND.getReasonPhrase()));
    }

    @ExceptionHandler(ConflictException.class)
    public  ResponseEntity<ErrorResponseDTO> handleConflictException(ConflictException ex,
                                                                     HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(builderError(HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.CONFLICT.getReasonPhrase()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorizedException(UnauthorizedException ex,
                                                                        HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(builderError(HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex,
                                                                           HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(builderError(HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.getReasonPhrase()));
    }

    public ErrorResponseDTO builderError(int status, String message, String path, String error) {
        return ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .message(message)
                .error(error)
                .status(status)
                .path(path)
                .build();
    }
}
