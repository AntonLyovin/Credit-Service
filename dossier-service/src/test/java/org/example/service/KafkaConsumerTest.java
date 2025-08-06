package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.example.model.dto.EmailMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaConsumerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KafkaConsumer kafkaConsumer;

    @Test
    void processFinishRegistration_ValidMessage_ShouldSendEmail() throws Exception {
        String jsonMessage = "{\"address\":\"test@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\"}";
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setAddress("test@example.com");

        Message<String> message = MessageBuilder.withPayload(jsonMessage)
                .setHeader(KafkaHeaders.RECEIVED_TOPIC, "finish-registration")
                .build();

        when(objectMapper.readValue(jsonMessage, EmailMessage.class)).thenReturn(emailMessage);
        doNothing().when(emailService).sendEmailWithAttachment(anyString(), any(byte[].class));

        kafkaConsumer.processFinishRegistration(jsonMessage);

        verify(emailService, times(1)).sendEmailWithAttachment(anyString(), any(byte[].class));
    }

    @Test
    void processCreateDocuments_ValidMessage_ShouldGeneratePdf() throws Exception {
        String jsonMessage = "{\"address\":\"test@example.com\",\"text\":\"Test document\"}";
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setAddress("test@example.com");
        emailMessage.setText("Test document");

        when(objectMapper.readValue(jsonMessage, EmailMessage.class)).thenReturn(emailMessage);
        doNothing().when(emailService).sendEmailWithAttachment(anyString(), any(byte[].class));

        kafkaConsumer.processCreateDocuments(jsonMessage);

        verify(emailService, times(1)).sendEmailWithAttachment(eq("test@example.com"), any(byte[].class));
    }

    @Test
    void processMessage_InvalidJson_ShouldLogError() throws Exception {
        String invalidJson = "invalid json";

        when(objectMapper.readValue(invalidJson, EmailMessage.class))
                .thenThrow(new JsonProcessingException("Invalid JSON") {
                });

        kafkaConsumer.processFinishRegistration(invalidJson);

        verify(emailService, never()).sendEmailWithAttachment(anyString(), any(byte[].class));
    }

    @Test
    void processMessage_MissingEmail_ShouldThrowException() throws Exception {
        String jsonMessage = "{\"firstName\":\"John\"}";
        EmailMessage emailMessage = new EmailMessage(); // email not set

        when(objectMapper.readValue(jsonMessage, EmailMessage.class)).thenReturn(emailMessage);

        assertThrows(IllegalArgumentException.class, () -> {
            kafkaConsumer.processFinishRegistration(jsonMessage);
        });

        verify(emailService, never()).sendEmailWithAttachment(anyString(), any(byte[].class));
    }

    @Test
    void createPdfDocument_ValidInput_ShouldGeneratePdf() throws Exception {
        EmailMessage message = new EmailMessage();
        message.setFirstName("John");
        message.setLastName("Doe");
        message.setText("Test content");

        byte[] result = kafkaConsumer.createPdfDocument(message, "test");

        assertNotNull(result);
        assertTrue(result.length > 0);

        try (PDDocument doc = PDDocument.load(result)) {
            assertEquals(1, doc.getNumberOfPages());
        }
    }

    @Test
    void processMessage_EmailSendingFailed_ShouldThrowException() throws Exception {
        String jsonMessage = "{\"address\":\"test@example.com\"}";
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setAddress("test@example.com");

        when(objectMapper.readValue(jsonMessage, EmailMessage.class)).thenReturn(emailMessage);
        doThrow(new MessagingException("SMTP error"))
                .when(emailService).sendEmailWithAttachment(anyString(), any(byte[].class));

        assertThrows(RuntimeException.class, () -> {
            kafkaConsumer.processFinishRegistration(jsonMessage);
        });
    }

    @Test
    void wrapText_ShouldHandleLongText() throws Exception {
        String longText = "This is a very long text that should be wrapped properly when it exceeds the page width.";
        EmailMessage message = new EmailMessage();
        message.setText(longText);

        byte[] pdf = kafkaConsumer.createPdfDocument(message, "test");
        assertNotNull(pdf);
    }

    @Test
    void processAllMessageTypes_ShouldHandleCorrectly() throws Exception {
        testMessageType("finish-registration");
        reset(emailService); // Сбрасываем счетчик вызовов между тестами
        testMessageType("create-documents");
        reset(emailService);
        testMessageType("send-documents");
        reset(emailService);
        testMessageType("send-ses");
        reset(emailService);
        testMessageType("credit-issued");
    }

    private void testMessageType(String topic) throws Exception {
        String jsonMessage = "{\"address\":\"test@example.com\"}";
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setAddress("test@example.com");

        when(objectMapper.readValue(jsonMessage, EmailMessage.class)).thenReturn(emailMessage);
        doNothing().when(emailService).sendEmailWithAttachment(anyString(), any(byte[].class));

        switch (topic) {
            case "finish-registration":
                kafkaConsumer.processFinishRegistration(jsonMessage);
                break;
            case "create-documents":
                kafkaConsumer.processCreateDocuments(jsonMessage);
                break;
            case "send-documents":
                kafkaConsumer.processSendDocuments(jsonMessage);
                break;
            case "send-ses":
                kafkaConsumer.processSendSes(jsonMessage);
                break;
            case "credit-issued":
                kafkaConsumer.processVerifySes(jsonMessage);
                break;
        }

        verify(emailService, times(1)).sendEmailWithAttachment(eq("test@example.com"), any(byte[].class));
    }
}
