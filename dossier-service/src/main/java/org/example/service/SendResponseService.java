package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.config.DealServiceProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.naming.ServiceUnavailableException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendResponseService {
    private final RestTemplate restTemplate;
    private final DealServiceProperties dealServiceProperties;


    public ResponseEntity<Void> processSendDocumentsStatus(UUID statementId) throws ServiceUnavailableException {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = dealServiceProperties.getUrlAdmin() + "/{statementId}/status";

            return restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    null,
                    Void.class,
                    statementId
            );

        } catch (RestClientException e) {
            log.error("Ошибка при обработке кредитного расчета. ID заявки: {}", statementId, e);
            throw new ServiceUnavailableException("Сервис расчета кредита недоступен");
        }
    }
}
