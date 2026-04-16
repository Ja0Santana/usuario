package com.joaopaulo.usuario.infrastructure.business.dto.in;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioModDTOrequest {
    private String nome;
    private String email;
    private String senha;
    private String fotoUrl;
}
