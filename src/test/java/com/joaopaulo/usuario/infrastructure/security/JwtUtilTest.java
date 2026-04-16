package com.joaopaulo.usuario.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        
        // Gera uma chave segura dinâmica para o teste (HS256 necessita de 256 bits)
        javax.crypto.SecretKey key = io.jsonwebtoken.Jwts.SIG.HS256.key().build();
        String dynamicSecret = java.util.Base64.getEncoder().encodeToString(key.getEncoded());
        
        // Injeta a chave secreta no campo privado usando ReflectionTestUtils
        ReflectionTestUtils.setField(jwtUtil, "secretKey", dynamicSecret);
    }

    @Test
    @DisplayName("Deve gerar um token JWT com sucesso")
    void deveGerarTokenComSucesso() {
        String username = "teste@email.com";
        String token = jwtUtil.generateToken(username, false);

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // Formato JWT: header.payload.signature
    }

    @Test
    @DisplayName("Deve extrair o username correto do token")
    void deveExtrairUsernameDoToken() {
        String username = "usuario.teste@provedor.com";
        String token = jwtUtil.generateToken(username, false);

        String extractedUsername = jwtUtil.extractUsername(token);

        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("Deve validar um token correto e não expirado")
    void deveValidarTokenComSucesso() {
        String username = "joao@email.com";
        String token = jwtUtil.generateToken(username, false);

        boolean isValid = jwtUtil.validateToken(token, username);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Deve invalidar token se o username for diferente")
    void deveInvalidarTokenComUsernameDiferente() {
        String usernameOriginal = "joao@email.com";
        String usernameErrado = "maria@email.com";
        String token = jwtUtil.generateToken(usernameOriginal, false);

        boolean isValid = jwtUtil.validateToken(token, usernameErrado);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve identificar se o token não está expirado")
    void deveVerificarSeTokenNaoEstaExpirado() {
        String token = jwtUtil.generateToken("teste", false);

        boolean isExpired = jwtUtil.isTokenExpired(token);

        assertThat(isExpired).isFalse();
    }
}
