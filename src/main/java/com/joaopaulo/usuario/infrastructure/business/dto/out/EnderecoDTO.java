package com.joaopaulo.usuario.infrastructure.business.dto.out;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnderecoDTO {
    private Long id;
    private String rua;
    private Long numero;
    private String cidade;
    private String complemento;
    private String cep;
    private String estado;
}
