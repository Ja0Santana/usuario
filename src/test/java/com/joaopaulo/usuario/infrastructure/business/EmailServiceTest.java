package com.joaopaulo.usuario.infrastructure.business;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "plannit@test.com");
    }

    @Test
    @DisplayName("Deve enviar e-mail de verificação com sucesso")
    void deveEnviarEmailComSucesso() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        emailService.enviarCodigoVerificacao("user@test.com", "123456");

        verify(mailSender).send(any(MimeMessage.class));
    }
}
