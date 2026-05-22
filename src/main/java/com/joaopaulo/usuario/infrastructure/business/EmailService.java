package com.joaopaulo.usuario.infrastructure.business;

import com.joaopaulo.usuario.infrastructure.exceptions.EmailVerificationException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void enviarCodigoVerificacao(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verificação de Conta - Plannit");

            String htmlContent = "<div style=\"font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f6f6ff; padding: 40px 20px; color: #272e42;\">" +
                "<div style=\"max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 24px; box-shadow: 0 8px 30px rgba(74, 64, 224, 0.1); overflow: hidden;\">" +
                "  <div style=\"background-color: #4a40e0; padding: 40px 20px; text-align: center;\">" +
                "    <h1 style=\"color: #ffffff; margin: 0; font-size: 28px; font-weight: 900; letter-spacing: 2px;\">Plannit</h1>" +
                "  </div>" +
                "  <div style=\"padding: 40px 30px; text-align: center;\">" +
                "    <h2 style=\"margin-top: 0; color: #14007e; font-size: 24px; font-weight: 800;\">Verifique seu e-mail</h2>" +
                "    <p style=\"color: #535b71; line-height: 1.6; font-size: 16px; margin-bottom: 35px;\">Olá! Falta muito pouco para ter acesso total ao Plannit. Use o código abaixo para confirmar sua identidade.</p>" +
                "    <div style=\"background-color: #e2e7ff; padding: 25px; border-radius: 16px; margin: 0 auto 35px auto; max-width: 250px; border: 2px solid #c7d4fa;\">" +
                "      <span style=\"display: block; font-size: 34px; font-weight: 900; letter-spacing: 6px; color: #4a40e0;\">" + code + "</span>" +
                "    </div>" +
                "    <p style=\"color: #a5adc6; font-size: 14px; margin: 0; font-weight: 600;\">Este código expira em 15 minutos.</p>" +
                "    <hr style=\"border: none; border-top: 1px solid #eef0ff; margin: 30px 0;\">" +
                "    <p style=\"color: #a5adc6; font-size: 12px; margin: 0;\">Se você não solicitou este código, por favor, apenas ignore este e-mail.</p>" +
                "  </div>" +
                "</div>" +
                "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new EmailVerificationException("Falha ao enviar e-mail HTML de verificação.", e);
        }
    }

    public void enviarEmailRecuperacaoSenha(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Recuperação de Senha - Plannit");

            String htmlContent = "<div style=\"font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f6f6ff; padding: 40px 20px; color: #272e42;\">" +
                "<div style=\"max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 24px; box-shadow: 0 8px 30px rgba(74, 64, 224, 0.1); overflow: hidden;\">" +
                "  <div style=\"background-color: #4a40e0; padding: 40px 20px; text-align: center;\">" +
                "    <h1 style=\"color: #ffffff; margin: 0; font-size: 28px; font-weight: 900; letter-spacing: 2px;\">Plannit</h1>" +
                "  </div>" +
                "  <div style=\"padding: 40px 30px; text-align: center;\">" +
                "    <h2 style=\"margin-top: 0; color: #14007e; font-size: 24px; font-weight: 800;\">Recuperação de Senha</h2>" +
                "    <p style=\"color: #535b71; line-height: 1.6; font-size: 16px; margin-bottom: 35px;\">Você solicitou a recuperação de senha. Use o código abaixo para definir uma nova senha para sua conta.</p>" +
                "    <div style=\"background-color: #e2e7ff; padding: 25px; border-radius: 16px; margin: 0 auto 35px auto; max-width: 250px; border: 2px solid #c7d4fa;\">" +
                "      <span style=\"display: block; font-size: 34px; font-weight: 900; letter-spacing: 6px; color: #4a40e0;\">" + code + "</span>" +
                "    </div>" +
                "    <p style=\"color: #a5adc6; font-size: 14px; margin: 0; font-weight: 600;\">Este código expira em 10 minutos por razões de segurança.</p>" +
                "    <hr style=\"border: none; border-top: 1px solid #eef0ff; margin: 30px 0;\">" +
                "    <p style=\"color: #a5adc6; font-size: 12px; margin: 0;\">Se você não solicitou a recuperação de senha, por favor, altere sua senha atual imediatamente ou entre em contato com o suporte.</p>" +
                "  </div>" +
                "</div>" +
                "</div>";


            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new EmailVerificationException("Falha ao enviar e-mail HTML de recuperação de senha.", e);
        }
    }
}
