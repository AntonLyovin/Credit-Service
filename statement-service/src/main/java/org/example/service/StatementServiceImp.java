package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.config.DealServiceProperties;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import javax.naming.ServiceUnavailableException;
import java.util.List;

@Service
@Slf4j
public class StatementServiceImp implements StatementService {
    private final RestTemplate restTemplate;
    private final DealServiceProperties dealServiceProperties;

    public StatementServiceImp(RestTemplate restTemplate, DealServiceProperties dealServiceProperties) {
        this.restTemplate = restTemplate;
        this.dealServiceProperties = dealServiceProperties;
    }

    @Override
    public List<LoanOfferDto> calculateOffers(LoanStatementRequestDto requestDto) throws ServiceUnavailableException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<LoanStatementRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);

            ResponseEntity<List<LoanOfferDto>> response = restTemplate.exchange(
                    dealServiceProperties.getUrlStatement(),
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {
                    }
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ServiceUnavailableException("Сервис сделки вернул ошибку");
            }

            return response.getBody();
        } catch (RestClientException e) {
            log.error("Ошибка при запросе предложений", e);
            throw new ServiceUnavailableException("Сервис сделки недоступен");
        } catch (ServiceUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<Void> selectOffer(LoanOfferDto offerDto) throws ServiceUnavailableException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<LoanOfferDto> requestEntity = new HttpEntity<>(offerDto, headers);

            // Добавьте логирование перед отправкой
            log.info("Sending to {}: {}", dealServiceProperties.getUrlSelect(), offerDto);

            ResponseEntity<Void> response = restTemplate.exchange(
                    dealServiceProperties.getUrlSelect(),
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );

            // Добавьте логирование ответа
            log.info("Received response: {}", response.getStatusCode());
            return response;

        } catch (HttpClientErrorException e) {
            log.error("Client error ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ServiceUnavailableException("Client error: " + e.getStatusCode());
        } catch (HttpServerErrorException e) {
            log.error("Server error ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ServiceUnavailableException("Server error: " + e.getStatusCode());
        } catch (ResourceAccessException e) {
            log.error("Connection failed: {}", e.getMessage());
            throw new ServiceUnavailableException("Connection failed");
        }
    }
}
