package com.joaopaulo.usuario.infrastructure.business.dto.in;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelefoneDTOrequest {
    private Long id;
    private String numero;
    private String ddd;
}
