package com.joaopaulo.usuario.infrastructure.exceptions;

import com.joaopaulo.usuario.infrastructure.exceptions.dtos.ErrorResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {

    @Test
    @DisplayName("Deve testar instancialização de todas as exceções customizadas")
    void deveTestarExcecoes() {
        assertThat(new BusinessException("Error")).isNotNull();
        assertThat(new ConflictException("Error")).isNotNull();
        assertThat(new EmailVerificationException("Error")).isNotNull();
        assertThat(new EmailVerificationException("Error", new RuntimeException())).isNotNull();
        assertThat(new IllegalArgumentException("Error")).isNotNull();
        assertThat(new JsonConversionException("Error", new RuntimeException())).isNotNull();
        assertThat(new ResourceNotFoundException("Error")).isNotNull();
        assertThat(new UnauthorizedException("Error")).isNotNull();
        assertThat(new UnauthorizedException("Error", new RuntimeException())).isNotNull();
    }

    @Test
    @DisplayName("Deve testar ErrorResponseDTO")
    void deveTestarErrorResponseDTO() {
        LocalDateTime now = LocalDateTime.now();
        ErrorResponseDTO dto = ErrorResponseDTO.builder()
                .timestamp(now)
                .status(400)
                .error("Bad Request")
                .message("Message")
                .path("/path")
                .build();

        assertThat(dto.getTimestamp()).isEqualTo(now);
        assertThat(dto.getStatus()).isEqualTo(400);
        assertThat(dto.getError()).isEqualTo("Bad Request");
        assertThat(dto.getMessage()).isEqualTo("Message");
        assertThat(dto.getPath()).isEqualTo("/path");

        ErrorResponseDTO emptyDto = new ErrorResponseDTO();
        emptyDto.setStatus(200);
        assertThat(emptyDto.getStatus()).isEqualTo(200);
    }
}
