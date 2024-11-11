package eu.minted.eos.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);


        log.info("Siunčiamas el. laiškas adresu: {}", to);
        try {
            mailSender.send(message);
            log.info("Laiškas sėkmingai išsiųstas adresu: {}", to);
        } catch (MailException e) {
            log.error("Klaida siunčiant el. laišką adresu {}: {}", to, e.getMessage());
            throw e;
        }
    }
}

