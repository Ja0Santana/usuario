package com.joaopaulo.usuario.infrastructure.business.dto.in;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GoogleLoginDTORequest {
    private String idToken;
    private Boolean lembrarMe;
}
