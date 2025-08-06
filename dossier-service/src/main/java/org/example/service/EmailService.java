package org.example.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmailWithAttachment(String to, byte[] pdfBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Ваш документ");

            helper.setText("Пожалуйста, найдите вложенный документ.", "UTF-8");

            ByteArrayResource resource = new ByteArrayResource(pdfBytes) {
                @Override
                public String getFilename() {
                    return "document.pdf";
                }
            };

            helper.addAttachment("document.pdf", resource, "application/pdf");

            mailSender.send(message);
            log.info("Email с документом успешно отправлен на {}", to);

        } catch (MessagingException e) {
            log.error("Ошибка при отправке email на {}", to, e);
        }
    }
}


