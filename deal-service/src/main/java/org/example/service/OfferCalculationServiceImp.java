package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.config.PreScoringServiceProperties;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.naming.ServiceUnavailableException;
import java.util.List;

@Service
@Slf4j
public class OfferCalculationServiceImp implements OfferCalculationService {
    private final RestTemplate restTemplate;
    private final PreScoringServiceProperties properties;

    public OfferCalculationServiceImp(RestTemplate restTemplate,
                                       PreScoringServiceProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public List<LoanOfferDto> calculateOffers(LoanStatementRequestDto requestDto) throws ServiceUnavailableException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<LoanStatementRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);

            ResponseEntity<List<LoanOfferDto>> response = restTemplate.exchange(
                    properties.getUrl(),
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ServiceUnavailableException("Сервис калькулятора вернул ошибку");
            }

            return response.getBody();
        } catch (RestClientException e) {
            log.error("Ошибка при запросе предложений", e);
            throw new ServiceUnavailableException("Сервис калькулятора недоступен");
        } catch (ServiceUnavailableException e) {
            throw new RuntimeException(e);
        }
    }
}
