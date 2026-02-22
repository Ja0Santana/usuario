package com.joaopaulo.usuario.infrastructure.exceptions.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private String error;
    private int status;
    private String message;
    private String path;
}
