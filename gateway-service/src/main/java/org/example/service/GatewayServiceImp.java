package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.config.DealServiceProperties;
import org.example.config.StatementServiceProperties;
import org.example.model.dto.FinishRegistrationRequestDto;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import javax.naming.ServiceUnavailableException;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class GatewayServiceImp implements GatewayService {
    private final RestTemplate restTemplate;
    private final StatementServiceProperties statementServiceProperties;
    private final DealServiceProperties dealServiceProperties;

    @Override
    public List<LoanOfferDto> calculateOffers(LoanStatementRequestDto requestDto) throws ServiceUnavailableException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<LoanStatementRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);

            ResponseEntity<List<LoanOfferDto>> response = restTemplate.exchange(
                    statementServiceProperties.getUrlStatement(),
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

            log.info("Sending to {}: {}", statementServiceProperties.getUrlOffer(), offerDto);

            ResponseEntity<Void> response = restTemplate.exchange(
                    statementServiceProperties.getUrlOffer(),
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );

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

    @Override
    public ResponseEntity<Void> processCreditCalculation(FinishRegistrationRequestDto requestDto, UUID statementId)
            throws ServiceUnavailableException {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = dealServiceProperties.getUrlRegistration() + "/{statementId}";

            HttpEntity<FinishRegistrationRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);

            return restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Void.class,
                    statementId
            );

        } catch (RestClientException e) {
            log.error("Ошибка при обработке кредитного расчета. ID заявки: {}", statementId, e);
            throw new ServiceUnavailableException("Сервис расчета кредита недоступен");
        }
    }

    @Override
    public ResponseEntity<Void> processSendDocuments(UUID statementId) throws ServiceUnavailableException {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = dealServiceProperties.getUrlDocument() + "/{statementId}/send";

            return restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    null,
                    Void.class,
                    statementId.toString()
            );

        } catch (RestClientException e) {
            log.error("Ошибка при обработке кредитного расчета. ID заявки: {}", statementId, e);
            throw new ServiceUnavailableException("Сервис расчета кредита недоступен");
        }
    }

    @Override
    public ResponseEntity<Void> processSignDocuments(UUID statementId) throws ServiceUnavailableException {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = dealServiceProperties.getUrlDocument() + "/{statementId}/sign";

            return restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    null,
                    Void.class,
                    statementId.toString()
            );

        } catch (RestClientException e) {
            log.error("Ошибка при обработке кредитного расчета. ID заявки: {}", statementId, e);
            throw new ServiceUnavailableException("Сервис расчета кредита недоступен");
        }
    }

    @Override
    public ResponseEntity<Void> processVerifySesCode(UUID statementId, String sesCode) throws ServiceUnavailableException {

        try {
            String url = dealServiceProperties.getUrlDocument() + "/{statementId}/code?sesCode={sesCode}";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);


            return restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    null,
                    Void.class,
                    statementId.toString(),
                    sesCode
            );

        } catch (RestClientException e) {
            log.error("Ошибка при обработке кредитного расчета. ID заявки: {}", statementId, e);
            throw new ServiceUnavailableException("Сервис расчета кредита недоступен");
        }

    }


}
