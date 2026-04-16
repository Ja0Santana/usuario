package com.joaopaulo.usuario.infrastructure.business;

import com.joaopaulo.usuario.infrastructure.entitiy.Usuario;
import com.joaopaulo.usuario.infrastructure.entitiy.VerificationToken;
import com.joaopaulo.usuario.infrastructure.exceptions.EmailVerificationException;
import com.joaopaulo.usuario.infrastructure.exceptions.UnauthorizedException;
import com.joaopaulo.usuario.infrastructure.repository.UsuarioRepository;
import com.joaopaulo.usuario.infrastructure.repository.VerificationTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private VerificationService verificationService;

    @Test
    @DisplayName("Deve criar código de verificação, deletar antigos e enviar e-mail")
    void deveCriarCodigoVerificacaoComSucesso() {
        Usuario usuario = Usuario.builder()
                .email("teste@email.com")
                .nome("Teste")
                .build();

        ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        verificationService.criarCodigoVerificacao(usuario);

        // Verifica limpeza
        verify(tokenRepository).deleteByUsuario(usuario);
        verify(tokenRepository).flush();

        // Verifica persistência do novo token
        verify(tokenRepository).save(tokenCaptor.capture());
        VerificationToken savedToken = tokenCaptor.getValue();
        
        assertThat(savedToken.getToken()).hasSize(6);
        assertThat(savedToken.getUsuario()).isEqualTo(usuario);
        assertThat(savedToken.getDataExpiracao()).isAfter(LocalDateTime.now());

        // Verifica envio de e-mail com o MESMO código salvo
        verify(emailService).enviarCodigoVerificacao(eq("teste@email.com"), codeCaptor.capture());
        assertThat(codeCaptor.getValue()).isEqualTo(savedToken.getToken());
    }

    @Test
    @DisplayName("Deve validar código com sucesso e atualizar usuário")
    void deveValidarCodigoComSucesso() {
        String email = "teste@email.com";
        String code = "123456";
        Usuario usuario = Usuario.builder().email(email).verificado(false).build();
        VerificationToken token = VerificationToken.builder()
                .token(code)
                .usuario(usuario)
                .dataExpiracao(LocalDateTime.now().plusMinutes(15))
                .build();

        when(tokenRepository.findByTokenAndUsuarioEmail(code, email)).thenReturn(Optional.of(token));

        verificationService.validarCodigo(email, code);

        assertThat(usuario.isVerificado()).isTrue();
        verify(usuarioRepository).save(usuario);
        verify(tokenRepository).delete(token);
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException quando código ou email não coincidem")
    void deveLancarErroQuandoTokenNaoEncontrado() {
        when(tokenRepository.findByTokenAndUsuarioEmail(anyString(), anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verificationService.validarCodigo("errado@email.com", "000000"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Código de verificação inválido ou e-mail incorreto.");
    }

    @Test
    @DisplayName("Deve lançar EmailVerificationException quando código está expirado")
    void deveLancarErroQuandoTokenExpirado() {
        String email = "teste@email.com";
        String code = "123456";
        VerificationToken token = VerificationToken.builder()
                .token(code)
                .dataExpiracao(LocalDateTime.now().minusMinutes(1)) // Expira no passado
                .build();

        when(tokenRepository.findByTokenAndUsuarioEmail(code, email)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> verificationService.validarCodigo(email, code))
                .isInstanceOf(EmailVerificationException.class)
                .hasMessageContaining("Código de verificação expirado.");
    }
}
