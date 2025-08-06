package org.example.service;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {EmailService.class, EmailServiceTest.TestConfig.class})
class EmailServiceTest {
    private GreenMail greenMail;

    @Autowired
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.start();
        greenMail.setUser("test@example.com", "test", "password");
    }

    @AfterEach
    void tearDown() {
        greenMail.stop();
    }


    @Configuration
    static class TestConfig {
        @Bean
        public JavaMailSenderImpl mailSender() {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("localhost");
            mailSender.setPort(3025);
            mailSender.setUsername("test");
            mailSender.setPassword("password");
            mailSender.setDefaultEncoding("UTF-8");

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "false");
            props.put("mail.debug", "true");
            props.put("mail.from", "noreply@example.com");

            return mailSender;
        }
    }

    @Test
    void sendEmailWithAttachment_ShouldSendEmailCorrectly() throws Exception {
        String to = "recipient@example.com";
        byte[] pdfContent = "Test PDF content".getBytes();

        emailService.sendEmailWithAttachment(to, pdfContent);

        assertTrue(greenMail.waitForIncomingEmail(5000, 1));

        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
        assertEquals(1, receivedMessages.length);

        MimeMessage message = receivedMessages[0];
        assertEquals("Ваш документ", message.getSubject());
        assertEquals(to, message.getRecipients(Message.RecipientType.TO)[0].toString());

        MimeMultipart multipart = (MimeMultipart) message.getContent();
        assertEquals(2, multipart.getCount());

        String attachmentFileName = multipart.getBodyPart(1).getFileName();
        assertEquals("document.pdf", attachmentFileName);
    }

    @Test
    void sendEmailWithAttachment_ShouldHandleLargeFiles() throws Exception {
        byte[] largePdf = new byte[1024 * 1024];
        new Random().nextBytes(largePdf);

        emailService.sendEmailWithAttachment("large@test.com", largePdf);

        assertTrue(greenMail.waitForIncomingEmail(10000, 1));
        MimeMessage message = greenMail.getReceivedMessages()[0];
        MimeMultipart multipart = (MimeMultipart) message.getContent();
        assertTrue(multipart.getBodyPart(1).getSize() > 1024 * 1024); // >1MB
    }

    @Test
    void sendEmailWithAttachment_ShouldLogSuccess() {
        String to = "log@example.com";
        byte[] pdfContent = "Log test".getBytes();

        emailService.sendEmailWithAttachment(to, pdfContent);

        assertDoesNotThrow(() -> emailService.sendEmailWithAttachment(to, pdfContent));
    }

    @Test
    void sendEmailWithAttachment_ShouldHandleSmtpError() {
        ServerSetup brokenSetup = new ServerSetup(3026, null, ServerSetup.PROTOCOL_SMTP);
        GreenMail brokenMail = new GreenMail(brokenSetup);
        brokenMail.start();

        JavaMailSenderImpl brokenSender = new JavaMailSenderImpl();
        brokenSender.setHost("localhost");
        brokenSender.setPort(3026);

        EmailService service = new EmailService(brokenSender);

        assertDoesNotThrow(() ->
                service.sendEmailWithAttachment("error@test.com", "test".getBytes()));

        brokenMail.stop();
    }

}