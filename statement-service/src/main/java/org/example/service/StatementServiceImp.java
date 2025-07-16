package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.config.DealServiceProperties;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.naming.ServiceUnavailableException;
import java.util.List;

@Service
@Slf4j
public class StatementServiceImp implements StatementService{
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
                    new ParameterizedTypeReference<>() {}
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
    public void selectOffer(LoanOfferDto offerDto) throws ServiceUnavailableException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<LoanOfferDto> requestEntity = new HttpEntity<>(offerDto, headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    dealServiceProperties.getUrlSelect(),
                    HttpMethod.POST,
                    requestEntity,
                    void.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ServiceUnavailableException("Сервис сделки вернул ошибку");
            }

            ResponseEntity.status(response.getStatusCode()).build();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Ошибка выбора предложения: {}", e.getMessage());
            throw new ServiceUnavailableException("Сервис сделки недоступен");
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            throw new ServiceUnavailableException("Internal server error");
        }
    }
}
