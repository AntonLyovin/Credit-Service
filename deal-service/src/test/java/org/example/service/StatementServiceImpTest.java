package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.config.PreScoringServiceProperties;
import org.example.model.AppliedOffer;
import org.example.model.StatusHistory;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.example.model.entity.Client;
import org.example.model.entity.Statement;
import org.example.model.enumerated.ApplicationStatus;
import org.example.model.enumerated.ChangeType;
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

import javax.naming.ServiceUnavailableException;
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

    @Mock
    private PreScoringServiceProperties preScoringServiceProperties;

    @InjectMocks
    private StatementServiceImp statementService;

    private LoanStatementRequestDto loanRequestDto;
    private LoanOfferDto loanOfferDto;
    private Client client;
    private Statement statement;

    @BeforeEach
    void setUp() {
        loanRequestDto = new LoanStatementRequestDto();
        loanRequestDto.setFirstName("Ivan");
        loanRequestDto.setLastName("Ivanov");
        loanRequestDto.setMiddleName("Ivanovich");
        loanRequestDto.setBirthdate(LocalDate.of(1990, 1, 1));
        loanRequestDto.setEmail("ivan@example.com");

        loanOfferDto = new LoanOfferDto();
        loanOfferDto.setStatementId(UUID.randomUUID());
        loanOfferDto.setRequestedAmount(BigDecimal.valueOf(100000));
        loanOfferDto.setTotalAmount(BigDecimal.valueOf(120000));
        loanOfferDto.setTerm(12);
        loanOfferDto.setMonthlyPayment(BigDecimal.valueOf(10000));
        loanOfferDto.setRate(BigDecimal.valueOf(10));
        loanOfferDto.setIsInsuranceEnabled(true);
        loanOfferDto.setIsSalaryClient(false);

        client = Client.builder()
                .clientId(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("ivan@example.com")
                .build();

        statement = Statement.builder()
                .statementId(UUID.randomUUID())
                .clientId(client)
                .creationDate(LocalDate.now())
                .status(ApplicationStatus.PREAPPROVAL)
                .statusHistory(new ArrayList<>())
                .build();
    }

    @Test
    void createStatement_ShouldSuccessfullyCreateStatement() throws ServiceUnavailableException {
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(statementRepository.save(any(Statement.class))).thenReturn(statement);

        List<LoanOfferDto> mockOffers = Collections.singletonList(loanOfferDto);
        ResponseEntity<List<LoanOfferDto>> responseEntity =
                new ResponseEntity<>(mockOffers, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        when(preScoringServiceProperties.getUrl()).thenReturn("http://calculator-service");

        List<LoanOfferDto> result = statementService.createStatement(loanRequestDto);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(loanOfferDto.getStatementId(), result.get(0).getStatementId());

        verify(clientRepository, times(1)).save(any(Client.class));
        verify(statementRepository, times(1)).save(any(Statement.class));
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void createStatement_ShouldThrowException_WhenRequestIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            statementService.createStatement(null);
        });
    }

    @Test
    void createStatement_ShouldThrowServiceUnavailableException_WhenCalculatorFails() {
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(statementRepository.save(any(Statement.class))).thenReturn(statement);
        when(preScoringServiceProperties.getUrl()).thenReturn("http://calculator-service");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new ResourceAccessException("Service unavailable"));

        assertThrows(ResourceAccessException.class, () -> {
            statementService.createStatement(loanRequestDto);
        });
    }


    @Test
    void selectOffer_ShouldThrowException_WhenStatementNotFound() {
        when(statementRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            statementService.selectOffer(loanOfferDto);
        });
    }

    @Test
    void convertToClient_ShouldMapAllFieldsCorrectly() {
        Client result = statementService.convertToClient(loanRequestDto);

        assertEquals(loanRequestDto.getFirstName(), result.getFirstName());
        assertEquals(loanRequestDto.getLastName(), result.getLastName());
        assertEquals(loanRequestDto.getMiddleName(), result.getMiddleName());
        assertEquals(loanRequestDto.getBirthdate(), result.getBirthDate());
        assertEquals(loanRequestDto.getEmail(), result.getEmail());
        assertNull(result.getGender());
        assertNull(result.getMaritalStatus());
        assertNull(result.getPassport());
        assertNull(result.getEmployment());
    }

    @Test
    void convertToAppliedOffer_ShouldMapAllFieldsCorrectly() {
        AppliedOffer result = statementService.convertToAppliedOffer(loanOfferDto);

        assertEquals(loanOfferDto.getStatementId(), result.getStatementId());
        assertEquals(loanOfferDto.getRequestedAmount(), result.getRequestedAmount());
        assertEquals(loanOfferDto.getTotalAmount(), result.getTotalAmount());
        assertEquals(loanOfferDto.getTerm(), result.getTerm());
        assertEquals(loanOfferDto.getMonthlyPayment(), result.getMonthlyPayment());
        assertEquals(loanOfferDto.getRate(), result.getRate());
        assertEquals(loanOfferDto.getIsInsuranceEnabled(), result.getIsInsuranceEnabled());
        assertEquals(loanOfferDto.getIsSalaryClient(), result.getIsSalaryClient());
    }

    @Test
    void createStatusHistory_ShouldCreateInitialHistory() {
        List<StatusHistory> result = statementService.createStatusHistory();

        assertEquals(1, result.size());
        StatusHistory history = result.get(0);
        assertEquals(ApplicationStatus.PREAPPROVAL, history.getStatus());
        assertEquals(ChangeType.AUTOMATIC, history.getChangeType());
        assertEquals(LocalDate.now(), history.getTime());
    }
}