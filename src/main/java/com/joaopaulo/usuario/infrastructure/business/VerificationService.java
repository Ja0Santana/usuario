package com.joaopaulo.usuario.infrastructure.business;

import com.joaopaulo.usuario.infrastructure.entitiy.Usuario;
import com.joaopaulo.usuario.infrastructure.entitiy.VerificationToken;
import com.joaopaulo.usuario.infrastructure.exceptions.EmailVerificationException;
import com.joaopaulo.usuario.infrastructure.exceptions.UnauthorizedException;
import com.joaopaulo.usuario.infrastructure.repository.UsuarioRepository;
import com.joaopaulo.usuario.infrastructure.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private static final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void criarCodigoVerificacao(Usuario usuario) {
        tokenRepository.deleteByUsuario(usuario);
        tokenRepository.flush();

        String code = String.format("%06d", secureRandom.nextInt(1000000));

        VerificationToken verificationToken = VerificationToken.builder()
                .token(code)
                .usuario(usuario)
                .dataExpiracao(LocalDateTime.now().plusMinutes(15))
                .build();

        tokenRepository.save(verificationToken);
        
        emailService.enviarCodigoVerificacao(usuario.getEmail(), code);
    }

    @Transactional
    public void criarCodigoRecuperacao(Usuario usuario) {
        tokenRepository.deleteByUsuario(usuario);
        tokenRepository.flush();

        String code = String.format("%06d", secureRandom.nextInt(1000000));

        VerificationToken verificationToken = VerificationToken.builder()
                .token(code)
                .usuario(usuario)
                .dataExpiracao(LocalDateTime.now().plusMinutes(10))
                .build();

        tokenRepository.save(verificationToken);
        
        emailService.enviarEmailRecuperacaoSenha(usuario.getEmail(), code);
    }

    @Transactional
    public void validarCodigo(String email, String code) {
        VerificationToken verificationToken = tokenRepository.findByTokenAndUsuarioEmail(code, email)
                .orElseThrow(() -> new UnauthorizedException("Código de verificação inválido ou e-mail incorreto."));

        if (verificationToken.isExpirado()) {
            throw new EmailVerificationException("Código de verificação expirado.");
        }

        Usuario usuario = verificationToken.getUsuario();
        usuario.setVerificado(true);
        usuarioRepository.save(usuario);

        tokenRepository.delete(verificationToken);
    }

    @Transactional
    public void validarCodigoRecuperacao(String email, String code) {
        VerificationToken verificationToken = tokenRepository.findByTokenAndUsuarioEmail(code, email)
                .orElseThrow(() -> new UnauthorizedException("Código de recuperação inválido ou e-mail incorreto."));

        if (verificationToken.isExpirado()) {
            throw new EmailVerificationException("Código de recuperação expirado.");
        }

        tokenRepository.delete(verificationToken);
    }
}
