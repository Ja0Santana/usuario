package com.joaopaulo.usuario.infrastructure.business.dto.in;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationDTORequest {
    private String email;
    private String codigo;
}
