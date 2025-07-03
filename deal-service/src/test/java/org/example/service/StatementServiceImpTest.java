package org.example.service;

import calculatorApp.calculator.model.dto.LoanOfferDto;
import calculatorApp.calculator.model.dto.LoanStatementRequestDto;
import jakarta.persistence.EntityNotFoundException;
import org.example.model.AppliedOffer;
import org.example.model.entity.Client;
import org.example.model.entity.Statement;
import org.example.model.enumerated.ApplicationStatus;
import org.example.repository.ClientRepository;
import org.example.repository.StatementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatementServiceImpTest {

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private StatementServiceImp statementService;

    private LoanStatementRequestDto validRequestDto;
    private Client testClient;
    private Statement testStatement;
    private List<LoanOfferDto> testOffers;

    @BeforeEach
    void setUp() {
        validRequestDto = new LoanStatementRequestDto();
        validRequestDto.setLastName("Иванов");
        validRequestDto.setFirstName("Иван");
        validRequestDto.setMiddleName("Иванович");
        validRequestDto.setBirthdate(LocalDate.of(1990, 1, 1));
        validRequestDto.setEmail("ivanov@example.com");

        testClient = new Client();
        testClient.setClientId(UUID.randomUUID());

        testStatement = new Statement();
        testStatement.setStatementId(UUID.randomUUID());
        testStatement.setClientId(testClient);
        testStatement.setCreationDate(LocalDate.now());

        LoanOfferDto offer1 = new LoanOfferDto();
        offer1.setRate(BigDecimal.valueOf(10.5));
        offer1.setTerm(12);
        offer1.setRequestedAmount(BigDecimal.valueOf(100000L));
        offer1.setTotalAmount(BigDecimal.valueOf(110000L));

        LoanOfferDto offer2 = new LoanOfferDto();
        offer2.setRate(BigDecimal.valueOf(9.5));
        offer2.setTerm(12);
        offer2.setRequestedAmount(BigDecimal.valueOf(100000L));
        offer2.setTotalAmount(BigDecimal.valueOf(109000L));

        testOffers = Arrays.asList(offer1, offer2);
    }

    @Test
    void createStatement_WithValidRequest_ReturnsSortedOffers() {

        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(statementRepository.save(any(Statement.class))).thenReturn(testStatement);

        ResponseEntity<List<LoanOfferDto>> responseEntity =
                new ResponseEntity<>(testOffers, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        List<LoanOfferDto> result = statementService.createStatement(validRequestDto);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(BigDecimal.valueOf(9.5), result.get(0).getRate());
        assertEquals(BigDecimal.valueOf(10.5), result.get(1).getRate());

        verify(clientRepository, times(1)).save(any(Client.class));
        verify(statementRepository, times(1)).save(any(Statement.class));
    }

    @Test
    void createStatement_WithNullRequest_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            statementService.createStatement(null);
        });
    }

    @Test
    void createStatement_WhenCalculatorServiceFails_ThrowsResourceAccessException() {

        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(statementRepository.save(any(Statement.class))).thenReturn(testStatement);

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new ResourceAccessException("Сервис недоступен"));

        assertThrows(ResourceAccessException.class, () -> {
            statementService.createStatement(validRequestDto);
        });
    }

    @Test
    void selectOffer_WithValidOffer_UpdatesStatement() {

        UUID statementId = UUID.randomUUID();
        LoanOfferDto offerDto = new LoanOfferDto();
        offerDto.setStatementId(statementId);
        offerDto.setRate(BigDecimal.valueOf(10.5));
        offerDto.setTerm(12);
        offerDto.setRequestedAmount(BigDecimal.valueOf(100000L));

        Statement existingStatement = new Statement();
        existingStatement.setStatementId(statementId);
        existingStatement.setStatus(ApplicationStatus.PREAPPROVAL);

        when(statementRepository.findById(statementId)).thenReturn(Optional.of(existingStatement));
        when(statementRepository.save(any(Statement.class))).thenReturn(existingStatement);

        statementService.selectOffer(offerDto);

        verify(statementRepository, times(1)).findById(statementId);
        verify(statementRepository, times(1)).save(any(Statement.class));

        assertEquals(ApplicationStatus.APPROVED, existingStatement.getStatus());
        assertNotNull(existingStatement.getAppliedOffer());
        assertEquals(1, existingStatement.getStatusHistory().size());
    }

    @Test
    void selectOffer_WithNonExistingStatement_ThrowsEntityNotFoundException() {

        UUID nonExistingId = UUID.randomUUID();
        LoanOfferDto offerDto = new LoanOfferDto();
        offerDto.setStatementId(nonExistingId);

        when(statementRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            statementService.selectOffer(offerDto);
        });

        verify(statementRepository, never()).save(any(Statement.class));
    }

    @Test
    void selectOffer_WhenUpdatingExistingOffer_PreservesAppliedOffer() {

        UUID statementId = UUID.randomUUID();
        LoanOfferDto offerDto = new LoanOfferDto();
        offerDto.setStatementId(statementId);
        offerDto.setRate(BigDecimal.valueOf(10.5));
        offerDto.setTerm(12);
        offerDto.setRequestedAmount(BigDecimal.valueOf(100000L));

        Statement existingStatement = new Statement();
        existingStatement.setStatementId(statementId);
        existingStatement.setStatus(ApplicationStatus.PREAPPROVAL);

        AppliedOffer existingAppliedOffer = new AppliedOffer();
        existingAppliedOffer.setStatementId(statementId);
        existingAppliedOffer.setRate(BigDecimal.valueOf(12.0));
        existingStatement.setAppliedOffer(existingAppliedOffer);

        when(statementRepository.findById(statementId)).thenReturn(Optional.of(existingStatement));
        when(statementRepository.save(any(Statement.class))).thenReturn(existingStatement);

        statementService.selectOffer(offerDto);

        assertEquals(BigDecimal.valueOf(10.5), existingStatement.getAppliedOffer().getRate());
        assertEquals(statementId, existingStatement.getAppliedOffer().getStatementId());
    }

    @Test
    void convertToAppliedOffer_ReturnsCorrectObject() {

        LoanOfferDto dto = new LoanOfferDto();
        dto.setStatementId(UUID.randomUUID());
        dto.setRequestedAmount(BigDecimal.valueOf(100000L));
        dto.setTotalAmount(BigDecimal.valueOf(110000L));
        dto.setTerm(12);
        dto.setMonthlyPayment(BigDecimal.valueOf(9166L));
        dto.setRate(BigDecimal.valueOf(10.5));
        dto.setIsInsuranceEnabled(true);
        dto.setIsSalaryClient(false);

        AppliedOffer result = statementService.convertToAppliedOffer(dto);

        assertNotNull(result);
        assertEquals(dto.getStatementId(), result.getStatementId());
        assertEquals(dto.getRequestedAmount(), result.getRequestedAmount());
        assertEquals(dto.getTotalAmount(), result.getTotalAmount());
        assertEquals(dto.getTerm(), result.getTerm());
        assertEquals(dto.getMonthlyPayment(), result.getMonthlyPayment());
        assertEquals(dto.getRate(), result.getRate());
        assertEquals(dto.getIsInsuranceEnabled(), result.getIsInsuranceEnabled());
        assertEquals(dto.getIsSalaryClient(), result.getIsSalaryClient());
    }
}
