package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.dto.EmailMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "finish-registration", groupId = "my_consumer")
    public void listen(String message) throws JsonProcessingException {
        EmailMessage emailMessage = objectMapper.readValue(message, EmailMessage.class);
        log.info("Получено сообщение. Текст сообщения: {}", message);
        byte[] documentBytes = createDocumentForSelectOffer(emailMessage);

        emailService.sendEmailWithAttachment(emailMessage.getAddress(),documentBytes);
    }


    private byte[] createDocumentForSelectOffer(EmailMessage message) throws JsonProcessingException {

        String content = "Дорогой пользователь:\n\n" +
                message.getLastName() + " " +
                message.getFirstName() + " " +
                message.getMiddleName() + " " +
                " Вы выбрали предложение:\n" +
                objectMapper.writeValueAsString(message.getAppliedOffer()) +
                message.getText() +
                "\n\nСпасибо!";
        return content.getBytes(StandardCharsets.UTF_8);
    }
}
