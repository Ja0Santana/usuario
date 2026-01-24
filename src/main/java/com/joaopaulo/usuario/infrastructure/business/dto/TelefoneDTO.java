package com.joaopaulo.usuario.infrastructure.business.dto;

import lombok.Builder;

@Builder
public record TelefoneDTO(String numero, String ddd) {
}
