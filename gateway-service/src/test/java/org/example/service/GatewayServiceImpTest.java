package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.config.DealServiceProperties;
import org.example.config.StatementServiceProperties;
import org.example.model.dto.FinishRegistrationRequestDto;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.*;

import javax.naming.ServiceUnavailableException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class GatewayServiceImpTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private StatementServiceProperties statementServiceProperties ;

    @Mock
    private DealServiceProperties dealServiceProperties;

    @InjectMocks
    private GatewayServiceImp gatewayService;

    private static final String TEST_BASE_STATEMENT_URL = "http://localhost:8888";
    private static final String TEST_STATEMENT_URL = TEST_BASE_STATEMENT_URL + "/statement";
    private static final String TEST_OFFER_URL = TEST_BASE_STATEMENT_URL + "/statement/offer";
    private static final String TEST_BASE_DEAL_URL = "http://localhost:8081";
    private static final String TEST_REGISTRATION_URL = TEST_BASE_DEAL_URL + "/deal/calculate";
    private static final String TEST_DOCUMENT_URL = TEST_BASE_DEAL_URL + "/deal/document";


    @Test
    void calculateOffers_SuccessfulResponse_ReturnsLoanOffers() throws ServiceUnavailableException {
        LoanStatementRequestDto requestDto = new LoanStatementRequestDto();
        List<LoanOfferDto> expectedOffers = List.of(new LoanOfferDto());

        when(statementServiceProperties.getFullStatementUrl()).thenReturn(TEST_STATEMENT_URL);

        ResponseEntity<List<LoanOfferDto>> responseEntity = new ResponseEntity<>(
                expectedOffers, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(TEST_STATEMENT_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        List<LoanOfferDto> result = gatewayService.calculateOffers(requestDto);

        assertEquals(expectedOffers, result);
        verify(restTemplate).exchange(
                eq(TEST_STATEMENT_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class));
    }

    @Test
    void selectOffer_SuccessfulResponse_ReturnsResponseEntity() throws ServiceUnavailableException {
        LoanOfferDto offerDto = new LoanOfferDto();
        ResponseEntity<Void> expectedResponse = new ResponseEntity<>(HttpStatus.OK);

        when(statementServiceProperties.getFullOfferUrl()).thenReturn(TEST_OFFER_URL);
        when(restTemplate.exchange(
                eq(TEST_OFFER_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Void> response = gatewayService.selectOffer(offerDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate).exchange(
                eq(TEST_OFFER_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class));
    }

    @Test
    void selectOffer_HttpClientErrorException_ThrowsServiceUnavailableException() {
        LoanOfferDto offerDto = new LoanOfferDto();
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request");

        when(statementServiceProperties.getFullOfferUrl()).thenReturn(TEST_OFFER_URL);
        when(restTemplate.exchange(
                eq(TEST_OFFER_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)))
                .thenThrow(exception);

        ServiceUnavailableException thrown = assertThrows(ServiceUnavailableException.class,
                () -> gatewayService.selectOffer(offerDto));

        assertEquals("Client error: 400 BAD_REQUEST", thrown.getMessage());
    }

    @Test
    void selectOffer_HttpServerErrorException_ThrowsServiceUnavailableException() {
        LoanOfferDto offerDto = new LoanOfferDto();
        HttpServerErrorException exception = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error");

        when(statementServiceProperties.getFullOfferUrl()).thenReturn(TEST_OFFER_URL);
        when(restTemplate.exchange(
                eq(TEST_OFFER_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)))
                .thenThrow(exception);

        ServiceUnavailableException thrown = assertThrows(ServiceUnavailableException.class,
                () -> gatewayService.selectOffer(offerDto));

        assertEquals("Server error: 500 INTERNAL_SERVER_ERROR", thrown.getMessage());
    }

    @Test
    void selectOffer_ResourceAccessException_ThrowsServiceUnavailableException() {
        LoanOfferDto offerDto = new LoanOfferDto();
        ResourceAccessException exception = new ResourceAccessException("Connection timed out");

        when(statementServiceProperties.getFullOfferUrl()).thenReturn(TEST_OFFER_URL);
        when(restTemplate.exchange(
                eq(TEST_OFFER_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)))
                .thenThrow(exception);

        ServiceUnavailableException thrown = assertThrows(ServiceUnavailableException.class,
                () -> gatewayService.selectOffer(offerDto));

        assertEquals("Connection failed", thrown.getMessage());
    }

    @Test
    void processCreditCalculation_SuccessfulRequest_ReturnsResponseEntity() throws ServiceUnavailableException {
        UUID statementId = UUID.randomUUID();
        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto();
        ResponseEntity<Void> expectedResponse = new ResponseEntity<>(HttpStatus.OK);
        String addUrl = "/{statementId}";

        when(dealServiceProperties.getFullRegistrationUrl()).thenReturn(TEST_REGISTRATION_URL);

        when(restTemplate.exchange(
                eq(TEST_REGISTRATION_URL + addUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class),
                eq(statementId)))
                .thenReturn(expectedResponse);

        ResponseEntity<Void> response = gatewayService.processCreditCalculation(requestDto, statementId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate).exchange(
                eq(TEST_REGISTRATION_URL + addUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class),
                eq(statementId));
    }

    @Test
    void processCreditCalculation_RestClientException_ThrowsServiceUnavailableException() {
        UUID statementId = UUID.randomUUID();
        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto();
        String addUrl = "/{statementId}";

        when(dealServiceProperties.getFullRegistrationUrl()).thenReturn(TEST_REGISTRATION_URL);

        when(restTemplate.exchange(
                eq(TEST_REGISTRATION_URL + addUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class),
                eq(statementId)))
                .thenThrow(new RestClientException("Connection failed"));

        ServiceUnavailableException exception = assertThrows(
                ServiceUnavailableException.class,
                () -> gatewayService.processCreditCalculation(requestDto, statementId));

        assertEquals("Сервис расчета кредита недоступен", exception.getMessage());
    }

    @Test
    void processCreditCalculation_VerifyRequestHeadersAndUrl() throws ServiceUnavailableException {
        UUID statementId = UUID.randomUUID();
        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto();
        String addUrl = "/{statementId}";

        when(dealServiceProperties.getFullRegistrationUrl()).thenReturn(TEST_REGISTRATION_URL);
        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                eq(Void.class),
                any(UUID.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        gatewayService.processCreditCalculation(requestDto, statementId);

        verify(dealServiceProperties).getFullRegistrationUrl();

        verify(restTemplate).exchange(
                eq(TEST_REGISTRATION_URL + addUrl),
                eq(HttpMethod.POST),
                argThat((HttpEntity<FinishRegistrationRequestDto> entity) ->
                        entity.getHeaders().getContentType().equals(MediaType.APPLICATION_JSON) &&
                                entity.getBody().equals(requestDto)),
                eq(Void.class),
                eq(statementId));
    }

    @Test
    void processSendDocuments_SuccessfulRequest_ReturnsOkResponse() throws ServiceUnavailableException {
        UUID statementId = UUID.randomUUID();
        String addUrl = "/{statementId}/send";

        when(dealServiceProperties.getFullDocumentUrl()).thenReturn(TEST_DOCUMENT_URL);
        when(restTemplate.exchange(
                eq(TEST_DOCUMENT_URL + addUrl),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                eq(statementId.toString())))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<Void> response = gatewayService.processSendDocuments(statementId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate).exchange(
                eq(TEST_DOCUMENT_URL + addUrl),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                eq(statementId.toString()));
    }

    @Test
    void processSendDocuments_RestClientException_ThrowsServiceUnavailable() {
        UUID statementId = UUID.randomUUID();

        when(dealServiceProperties.getFullDocumentUrl()).thenReturn(TEST_DOCUMENT_URL);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                anyString()))
                .thenThrow(new RestClientException("Connection error"));

        ServiceUnavailableException exception = assertThrows(
                ServiceUnavailableException.class,
                () -> gatewayService.processSendDocuments(statementId));

        assertEquals("Сервис расчета кредита недоступен", exception.getMessage());
    }

    @Test
    void processSendDocuments_VerifyUrlConstruction() throws ServiceUnavailableException {
        UUID statementId = UUID.randomUUID();
        String addUrl = "/{statementId}/send";

        when(dealServiceProperties.getFullDocumentUrl()).thenReturn(TEST_DOCUMENT_URL);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        gatewayService.processSendDocuments(statementId);

        verify(restTemplate).exchange(
                eq(TEST_DOCUMENT_URL + addUrl),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                eq(statementId.toString()));
    }

    @Test
    void processSendDocuments_VerifyHeadersNotSetWhenBodyIsNull() throws ServiceUnavailableException {
        UUID statementId = UUID.randomUUID();

        when(dealServiceProperties.getFullDocumentUrl()).thenReturn(TEST_DOCUMENT_URL);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                argThat(entity -> entity == null || entity.getHeaders() == null),
                eq(Void.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        gatewayService.processSendDocuments(statementId);

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                anyString());
    }

    @Test
    void processSignDocuments_SuccessfulRequest_ReturnsOkResponse() throws ServiceUnavailableException {
        UUID statementId = UUID.randomUUID();
        String addUrl = "/{statementId}/sign";

        when(dealServiceProperties.getFullDocumentUrl()).thenReturn(TEST_DOCUMENT_URL);
        when(restTemplate.exchange(
                eq(TEST_DOCUMENT_URL + addUrl),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                eq(statementId.toString())))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<Void> response = gatewayService.processSignDocuments(statementId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate).exchange(
                eq(TEST_DOCUMENT_URL + addUrl),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                eq(statementId.toString()));
    }

    @Test
    void processSignDocuments_RestClientException_ThrowsServiceUnavailableException() {
        UUID statementId = UUID.randomUUID();
        String addUrl = "/{statementId}/sign";

        when(dealServiceProperties.getFullDocumentUrl()).thenReturn(TEST_DOCUMENT_URL);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                anyString()))
                .thenThrow(new RestClientException("Connection failed"));

        ServiceUnavailableException exception = assertThrows(
                ServiceUnavailableException.class,
                () -> gatewayService.processSignDocuments(statementId));

        assertEquals("Сервис расчета кредита недоступен", exception.getMessage());
        verify(restTemplate).exchange(
                eq(TEST_DOCUMENT_URL + addUrl),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                eq(statementId.toString()));
    }

    @Test
    void processSignDocuments_VerifyUrlConstruction() throws ServiceUnavailableException {
        UUID statementId = UUID.randomUUID();
        String addUrl = "/{statementId}/sign";

        when(dealServiceProperties.getFullDocumentUrl()).thenReturn(TEST_DOCUMENT_URL);
        when(restTemplate.exchange(
                eq(TEST_DOCUMENT_URL + addUrl),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        gatewayService.processSignDocuments(statementId);

        verify(restTemplate).exchange(
                eq(TEST_DOCUMENT_URL + addUrl),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                eq(statementId.toString()));
    }

    @Test
    void processSignDocuments_VerifyNullBodyAndNoHeaders() throws ServiceUnavailableException {
        UUID statementId = UUID.randomUUID();

        when(dealServiceProperties.getFullDocumentUrl()).thenReturn(TEST_DOCUMENT_URL);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                argThat(entity -> entity == null),
                eq(Void.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        gatewayService.processSignDocuments(statementId);

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                anyString());
    }

    @Test
    void processVerifySesCode_SuccessfulRequest_ReturnsOkResponse() throws ServiceUnavailableException {
        UUID statementId = UUID.randomUUID();
        String sesCode = "123456";
        String addUrl = "/{statementId}/code?sesCode={sesCode}";

        when(dealServiceProperties.getFullDocumentUrl()).thenReturn(TEST_DOCUMENT_URL);
        when(restTemplate.exchange(
                eq(TEST_DOCUMENT_URL + addUrl),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                eq(statementId.toString()),
                eq(sesCode)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<Void> response = gatewayService.processVerifySesCode(statementId, sesCode);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate).exchange(
                eq(TEST_DOCUMENT_URL + "/{statementId}/code?sesCode={sesCode}"),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                eq(statementId.toString()),
                eq(sesCode));
    }

    @Test
    void processVerifySesCode_RestClientException_ThrowsServiceUnavailableException() {
        UUID statementId = UUID.randomUUID();
        String sesCode = "654321";
        String addUrl = "/{statementId}/code?sesCode={sesCode}";

        when(dealServiceProperties.getFullDocumentUrl()).thenReturn(TEST_DOCUMENT_URL);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                anyString(),
                anyString()))
                .thenThrow(new RestClientException("Connection timeout"));

        ServiceUnavailableException exception = assertThrows(
                ServiceUnavailableException.class,
                () -> gatewayService.processVerifySesCode(statementId, sesCode));

        assertEquals("Сервис расчета кредита недоступен", exception.getMessage());
        verify(restTemplate).exchange(
                eq(TEST_DOCUMENT_URL + addUrl),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                eq(statementId.toString()),
                eq(sesCode));
    }

    @Test
    void processVerifySesCode_VerifyUrlAndParametersConstruction() throws ServiceUnavailableException {
        UUID statementId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        String sesCode = "987654";
        String addUrl = "/{statementId}/code?sesCode={sesCode}";

        when(dealServiceProperties.getFullDocumentUrl()).thenReturn(TEST_DOCUMENT_URL);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                anyString(),
                anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        gatewayService.processVerifySesCode(statementId, sesCode);

        verify(restTemplate).exchange(
                eq(TEST_DOCUMENT_URL + addUrl),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                eq("550e8400-e29b-41d4-a716-446655440000"),
                eq("987654"));
    }

    @Test
    void processVerifySesCode_VerifyHeadersNotSetWhenBodyIsNull() throws ServiceUnavailableException {
        UUID statementId = UUID.randomUUID();
        String sesCode = "111222";

        when(dealServiceProperties.getFullDocumentUrl()).thenReturn(TEST_DOCUMENT_URL);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                argThat(entity -> entity == null),
                eq(Void.class),
                anyString(),
                anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        gatewayService.processVerifySesCode(statementId, sesCode);

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                anyString(),
                anyString());
    }

    @Test
    void processVerifySesCode_VerifyEmptySesCodeHandling() throws ServiceUnavailableException {
        UUID statementId = UUID.randomUUID();
        String emptySesCode = "";
        String addUrl = "/{statementId}/code?sesCode={sesCode}";

        when(dealServiceProperties.getFullDocumentUrl()).thenReturn(TEST_DOCUMENT_URL);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                anyString(),
                eq("")))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Act
        ResponseEntity<Void> response = gatewayService.processVerifySesCode(statementId, emptySesCode);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(restTemplate).exchange(
                eq(TEST_DOCUMENT_URL + addUrl),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                eq(statementId.toString()),
                eq(""));
    }

}