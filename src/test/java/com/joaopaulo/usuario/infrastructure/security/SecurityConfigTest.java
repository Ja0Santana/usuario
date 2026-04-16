package com.joaopaulo.usuario.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired(required = false)
    private SecurityFilterChain securityFilterChain;

    @Test
    @DisplayName("Deve carregar beans de segurança corretamente")
    void deveCarregarBeansSeguranca() {
        assertThat(passwordEncoder).isNotNull();
        assertThat(authenticationManager).isNotNull();
        assertThat(securityFilterChain).isNotNull();
    }
}
