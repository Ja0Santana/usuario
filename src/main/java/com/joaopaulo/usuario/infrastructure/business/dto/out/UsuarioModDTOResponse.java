package com.joaopaulo.usuario.infrastructure.business.dto.out;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioModDTOResponse {
    private String nome;
    private String email;
    private String senha;
}
