package com.joaopaulo.usuario.infrastructure.business.dto.in;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetSenhaDTORequest {
    private String email;
    private String codigo;
    private String novaSenha;
}
