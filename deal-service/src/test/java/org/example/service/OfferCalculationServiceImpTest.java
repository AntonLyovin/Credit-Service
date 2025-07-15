package org.example.service;

import org.example.config.PreScoringServiceProperties;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.naming.ServiceUnavailableException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OfferCalculationServiceImpTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PreScoringServiceProperties properties;

    @InjectMocks
    private OfferCalculationServiceImp service;

    @Test
    void calculateOffers_Success() throws ServiceUnavailableException {
        LoanStatementRequestDto request = new LoanStatementRequestDto();
        List<LoanOfferDto> expectedOffers = List.of(new LoanOfferDto(), new LoanOfferDto());

        when(properties.getUrl()).thenReturn("http://localhost:8080/calculator/offers");
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))
        ).thenReturn(ResponseEntity.ok(expectedOffers));

        List<LoanOfferDto> result = service.calculateOffers(request);

        assertEquals(expectedOffers.size(), result.size());
        verify(restTemplate).exchange(
                eq("http://localhost:8080/calculator/offers"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }


    @Test
    void calculateOffers_RestClientException() {
        LoanStatementRequestDto request = new LoanStatementRequestDto();

        when(properties.getUrl()).thenReturn("http://localhost:8080/calculator/offers");
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class))
        ).thenThrow(new RestClientException("Connection failed"));

        assertThrows(ServiceUnavailableException.class, () -> service.calculateOffers(request));
    }

}