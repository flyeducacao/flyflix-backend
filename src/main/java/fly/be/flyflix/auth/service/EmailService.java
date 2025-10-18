package fly.be.flyflix.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

    @Service
    @RequiredArgsConstructor
    public class EmailService {

        private final JavaMailSender mailSender;

        public void enviarEmail(String to, String subject, String content) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                // Definir o remetente correto
                helper.setFrom("atendimento@flyeducacao.org");

                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(content, true); // true = HTML

                mailSender.send(message);
            } catch (MessagingException e) {
                throw new RuntimeException("Erro ao enviar e-mail", e);
            }
        }
    }