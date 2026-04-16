package com.joaopaulo.usuario.infrastructure.business.dto.in;

import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTOrequest {
    private String nome;
    private String email;
    private String senha;
    private List<EnderecoDTOrequest> enderecos;
    private List<TelefoneDTOrequest> telefones;
    private String fotoUrl;
}
