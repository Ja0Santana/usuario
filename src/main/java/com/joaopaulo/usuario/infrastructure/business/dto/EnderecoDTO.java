package com.joaopaulo.usuario.infrastructure.business.dto;

import lombok.Builder;

@Builder
public record EnderecoDTO(String rua, Long numero, String complemento, String cidade, String estado, String cep) {
}
