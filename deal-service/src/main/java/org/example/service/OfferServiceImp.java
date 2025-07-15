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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OfferServiceImp implements OfferService{
    private final RestTemplate restTemplate;
    private final PreScoringServiceProperties properties;

    public OfferServiceImp(RestTemplate restTemplate,
                        PreScoringServiceProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public List<LoanOfferDto> getLoanOffers(LoanStatementRequestDto requestDto) throws ServiceUnavailableException {
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

            return response.getBody().stream()
                    .sorted(Comparator.comparing(LoanOfferDto::getRate))
                    .collect(Collectors.toList());
        } catch (RestClientException e) {
            log.error("Ошибка при запросе предложений", e);
            throw new ServiceUnavailableException("Сервис калькулятора недоступен");
        } catch (ServiceUnavailableException e) {
            throw new RuntimeException(e);
        }
    }
}
