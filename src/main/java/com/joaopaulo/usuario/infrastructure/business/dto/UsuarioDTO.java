package com.joaopaulo.usuario.infrastructure.business.dto;

import lombok.Builder;

import java.util.List;
@Builder
public record UsuarioDTO(String nome, String email, String senha, List<EnderecoDTO> endereco, List<TelefoneDTO> telefones) {
}
