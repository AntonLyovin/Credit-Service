package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.enumerated.Theme;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(Object message, Theme theme) {

        String topic;
        switch (theme) {
            case FINISH_REGISTRATION:
                topic = "finish-registration";
                break;
            case CREATE_DOCUMENTS:
                topic = "create-documents";
                break;
            case SEND_DOCUMENTS:
                topic = "send-documents";
                break;
            case SEND_SES:
                topic = "send-ses";
                break;
            case CREDIT_ISSUED:
                topic = "credit-issued";
                break;
            case STATEMENT_DENIED:
                topic = "statement-denied";
                break;
            default:
                throw new IllegalArgumentException("Неизвестная тема: " + theme);
        }

        try {
            kafkaTemplate.send(topic, message);
        } catch (Exception e) {
            log.info("ошибка отправки сообщения в кафку", e);
        }

    }
}
