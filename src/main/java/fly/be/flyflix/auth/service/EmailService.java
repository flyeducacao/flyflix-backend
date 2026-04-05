package fly.be.flyflix.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public void enviarEmail(String to, String subject, String content) {
        try {
            log.info("Iniciando envio de email para '{}' com assunto '{}'", to, subject);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("dev_educ@flyeducacao.org");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true = HTML

            mailSender.send(message);
            log.info("Email enviado com sucesso para '{}'", to);
        } catch (MessagingException e) {
            log.error("Falha ao montar email para '{}': {}", to, e.getMessage(), e);
            throw new RuntimeException("Erro ao enviar e-mail", e);
        } catch (Exception e) {
            log.error("Falha ao enviar email para '{}': {}", to, e.getMessage(), e);
            throw new RuntimeException("Erro ao enviar e-mail", e);
        }
    }
}
