package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.example.model.dto.EmailMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import javax.naming.ServiceUnavailableException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final SendResponseService sendResponseService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "finish-registration", groupId = "my_consumer")
    public void processFinishRegistration(String message) {
        processMessage(message, "registration");
    }

    @KafkaListener(topics = "create-documents", groupId = "my_consumer")
    public void processCreateDocuments(String message) {processMessage(message, "documents");}

    @KafkaListener(topics = "send-documents", groupId = "my_consumer")
    public void processSendDocuments(String message) {



        processMessage(message, "send");}

    @KafkaListener(topics = "send-ses", groupId = "my_consumer")
    public void processSendSes(String message) {
        processMessage(message, "ses");
    }

    @KafkaListener(topics = "credit-issued", groupId = "my_consumer")
    public void processVerifySes(String message) {
        processMessage(message, "issued");
    }

    private void processMessage(String message, String messageType) {
        try {
            EmailMessage emailMessage = objectMapper.readValue(message, EmailMessage.class);
            log.info("Получено сообщение типа {}: {}", messageType, message);

            validateEmailMessage(emailMessage);

            byte[] documentBytes = createPdfDocument(emailMessage, messageType);
            emailService.sendEmailWithAttachment(emailMessage.getAddress(), documentBytes);

            log.info("Email успешно отправлен на {}", emailMessage.getAddress());
        } catch (JsonProcessingException e) {
            log.error("Ошибка парсинга сообщения: {}", message, e);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка генерации PDF", e);
        } catch (MessagingException e) {
            throw new RuntimeException("Ошибка отправки email", e);
        }
    }

    private void validateEmailMessage(EmailMessage message) {
        if (message.getAddress() == null || message.getAddress().isEmpty()) {
            throw new IllegalArgumentException("Требуется email");
        }
    }

    byte[] createPdfDocument(EmailMessage message, String messageType) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            InputStream fontStream = getClass().getResourceAsStream("/fonts/arial.ttf");
            if (fontStream == null) {
                throw new IOException("Шрифт arial.ttf не найден в ресурсах");
            }

            try {
                PDFont font = PDType0Font.load(document, fontStream);
                PDPageContentStream contentStream = new PDPageContentStream(document, page);

                float margin = 50;
                float width = page.getMediaBox().getWidth() - 2 * margin;
                float fontSize = 12;
                float leading = fontSize * 1.5f;

                contentStream.setFont(font, fontSize);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, page.getMediaBox().getHeight() - margin - leading);
                contentStream.setLeading(leading);

                String header = "Дорогой пользователь:\n\n" +
                        message.getLastName() + " " +
                        message.getFirstName() + " " +
                        message.getMiddleName() + "\n\n" +
                        message.getText() + "\n\n";

                addTextWithWrapping(contentStream, font, fontSize, width, header);

                if ("registration".equals(messageType) && message.getAppliedOffer() != null) {
                    String offerText = "Вы выбрали предложение:\n" +
                            "Сумма: " + message.getAppliedOffer().getTotalAmount() + "\n" +
                            "Срок: " + message.getAppliedOffer().getTerm() + " месяцев\n" +
                            "Ежемесячный платеж: " + message.getAppliedOffer().getMonthlyPayment() + "\n\n";

                    addTextWithWrapping(contentStream, font, fontSize, width, offerText);
                } else if ("send".equals(messageType) && message.getCredit() != null) {
                    String creditText = "Детали кредита:\n" +
                            "Сумма: " + message.getCredit().getAmount() + "\n" +
                            "Срок: " + message.getCredit().getTerm() + " месяцев\n" +
                            "Ежемесячный платеж: " + message.getCredit().getMonthlyPayment() + "\n" +
                            "Ставка: " + message.getCredit().getRate() + "\n" +
                            "ПСК: " + message.getCredit().getPsk() + "\n";
                    addTextWithWrapping(contentStream, font, fontSize, width, creditText);
                    sendResponseService.processSendDocumentsStatus(UUID.fromString((message.getStatementId())));

                    if (message.getCredit().getPaymentSchedule() != null) {
                        String schedule = "График платежей:\n" +
                                message.getCredit().getPaymentSchedule().stream()
                                        .map(ps -> ps.toString())
                                        .collect(Collectors.joining("\n"));

                        addTextWithWrapping(contentStream, font, fontSize, width, schedule);
                    }
                } else if ("ses".equals(messageType) && message.getSesCode() != null) {
                    String sesCode = "Код: " + message.getSesCode() + "\n";
                    addTextWithWrapping(contentStream, font, fontSize, width, sesCode);
                } else if ("issued".equals(messageType)) {
                    String issuedCredit = "Процесс выдачи кредита окончен" + "\n";
                    addTextWithWrapping(contentStream, font, fontSize, width, issuedCredit);
                }

                addTextWithWrapping(contentStream, font, fontSize, width, "\n\nСпасибо!");

                contentStream.endText();
                contentStream.close();

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                document.save(byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            } catch (ServiceUnavailableException e) {
                throw new RuntimeException(e);
            } finally {
                fontStream.close();
            }
        }
    }

    private void addTextWithWrapping(PDPageContentStream contentStream, PDFont font,
                                     float fontSize, float width, String text) throws IOException {
        for (String line : wrapText(text, font, fontSize, width)) {
            contentStream.showText(line);
            contentStream.newLine();
        }
    }

    private List<String> wrapText(String text, PDFont font, float fontSize, float width) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;

        String[] paragraphs = text.split("\n");
        for (String paragraph : paragraphs) {
            int lastSpace = -1;
            int start = 0;
            float currentWidth = 0;

            for (int i = 0; i < paragraph.length(); i++) {
                char c = paragraph.charAt(i);
                float charWidth = font.getStringWidth(String.valueOf(c)) / 1000 * fontSize;

                if (Character.isWhitespace(c)) {
                    lastSpace = i;
                }

                if (currentWidth + charWidth > width) {
                    if (lastSpace >= start) {
                        lines.add(paragraph.substring(start, lastSpace));
                        start = lastSpace + 1;
                        lastSpace = -1;
                    } else {
                        lines.add(paragraph.substring(start, i));
                        start = i;
                    }
                    currentWidth = 0;
                    i = start - 1;
                    continue;
                }

                currentWidth += charWidth;
            }

            if (start < paragraph.length()) {
                lines.add(paragraph.substring(start));
            }
        }

        return lines;
    }
}
