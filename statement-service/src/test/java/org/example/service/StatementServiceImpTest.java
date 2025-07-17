package org.example.service;

import org.example.config.DealServiceProperties;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.*;

import javax.naming.ServiceUnavailableException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class StatementServiceImpTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private DealServiceProperties dealServiceProperties;

    @InjectMocks
    private StatementServiceImp statementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(dealServiceProperties.getUrlStatement()).thenReturn("http://localhost:8081/deal/statement");
        when(dealServiceProperties.getUrlSelect()).thenReturn("http://localhost:8081/deal/offer/select");
    }

    @Test
    void calculateOffers_success() throws ServiceUnavailableException {
        LoanStatementRequestDto requestDto = new LoanStatementRequestDto();
        List<LoanOfferDto> offers = Arrays.asList(new LoanOfferDto(), new LoanOfferDto());

        ResponseEntity<List<LoanOfferDto>> responseEntity = new ResponseEntity<>(offers, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://localhost:8081/deal/statement"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<LoanOfferDto>>>any()
        )).thenReturn(responseEntity);

        List<LoanOfferDto> result = statementService.calculateOffers(requestDto);
        assertEquals(2, result.size());
    }

    @Test
    void calculateOffers_responseNullBody_throws() {
        ResponseEntity<List<LoanOfferDto>> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<LoanOfferDto>>>any()
        )).thenReturn(responseEntity);
    }

    @Test
    void calculateOffers_restClientException_throws() {
        when(restTemplate.exchange(
                anyString(),
                any(),
                any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<LoanOfferDto>>>any()
        )).thenThrow(new RestClientException("Error"));

        assertThrows(ServiceUnavailableException.class, () -> {
            statementService.calculateOffers(new LoanStatementRequestDto());
        });
    }

    @Test
    void selectOffer_success() throws ServiceUnavailableException {
        LoanOfferDto offer = new LoanOfferDto();

        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class)))
                .thenReturn(response);

        ResponseEntity<Void> result = statementService.selectOffer(offer);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void selectOffer_clientError_throws() {
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class)))
                .thenThrow(exception);

        assertThrows(ServiceUnavailableException.class, () -> {
            statementService.selectOffer(new LoanOfferDto());
        });
    }

    @Test
    void selectOffer_serverError_throws() {
        HttpServerErrorException exception = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class)))
                .thenThrow(exception);

        assertThrows(ServiceUnavailableException.class, () -> {
            statementService.selectOffer(new LoanOfferDto());
        });
    }

    @Test
    void selectOffer_resourceAccessException_throws() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Void.class)))
                .thenThrow(new ResourceAccessException("Connection failed"));

        assertThrows(ServiceUnavailableException.class, () -> {
            statementService.selectOffer(new LoanOfferDto());
        });
    }
}