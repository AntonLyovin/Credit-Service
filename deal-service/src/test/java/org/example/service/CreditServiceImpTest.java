package org.example.service;

import org.example.config.ScoringServiceProperties;
import org.example.exception.custom.*;
import org.example.model.AppliedOffer;
import org.example.model.StatusHistory;
import org.example.model.dto.CreditDto;
import org.example.model.dto.FinishRegistrationRequestDto;
import org.example.model.dto.ScoringDataDto;
import org.example.model.entity.Client;
import org.example.model.entity.Credit;
import org.example.model.entity.Statement;
import org.example.model.enumerated.ApplicationStatus;
import org.example.model.enumerated.ChangeType;
import org.example.model.enumerated.Gender;
import org.example.model.enumerated.MaritalStatus;
import org.example.repository.CreditRepository;
import org.example.repository.StatementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditServiceImpTest {

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ScoringServiceProperties scoringServiceProperties;

    @InjectMocks
    private CreditServiceImp creditService;

    private FinishRegistrationRequestDto requestDto;
    private Statement statement;
    private CreditDto creditDto;
    private final String validStatementId = "550e8400-e29b-41d4-a716-446655440000";

    @BeforeEach
    void setUp() {
        requestDto = new FinishRegistrationRequestDto();
        requestDto.setGender(Gender.MALE);
        requestDto.setPassportSeries("1234");
        requestDto.setPassportNumber("567890");
        requestDto.setPassportIssueDate(LocalDate.now());
        requestDto.setPassportIssueBranch("UFMS");
        requestDto.setMaritalStatus(MaritalStatus.SINGLE);
        requestDto.setDependentAmount(1);
        requestDto.setAccountNumber("1234567890");
        requestDto.setIsInsuranceEnabled(true);
        requestDto.setIsSalaryClient(false);

        Client client = Client.builder()
                .clientId(UUID.randomUUID())
                .firstName("Иван")
                .lastName("Иванов")
                .middleName("Иванович")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        AppliedOffer appliedOffer = new AppliedOffer();
        appliedOffer.setRequestedAmount(BigDecimal.valueOf(100000.0));
        appliedOffer.setTerm(12);
        appliedOffer.setMonthlyPayment(BigDecimal.valueOf(8500.0));
        appliedOffer.setRate(BigDecimal.valueOf(15.0));
        appliedOffer.setIsInsuranceEnabled(true);

        statement = Statement.builder()
                .statementId(UUID.fromString(validStatementId))
                .clientId(client)
                .status(ApplicationStatus.PREAPPROVAL)
                .creationDate(LocalDate.now())
                .appliedOffer(appliedOffer)
                .statusHistory(new ArrayList<>())
                .build();

        creditDto = new CreditDto();
        creditDto.setAmount(BigDecimal.valueOf(100000.0));
        creditDto.setTerm(12);
        creditDto.setMonthlyPayment(BigDecimal.valueOf(8500.0));
        creditDto.setRate(BigDecimal.valueOf(15.0));
        creditDto.setPsk(BigDecimal.valueOf(102000.0));
        creditDto.setIsInsuranceEnabled(true);
        creditDto.setIsSalaryClient(false);
        creditDto.setPaymentSchedule(new ArrayList<>());
    }

    @Test
    void finishCalculateCredit_ShouldSuccessfullyProcessValidRequest() {
        when(statementRepository.findById(any(UUID.class))).thenReturn(Optional.of(statement));
        when(scoringServiceProperties.getUrl()).thenReturn("http://scoring-service");
        when(restTemplate.postForEntity(anyString(), any(ScoringDataDto.class), eq(CreditDto.class)))
                .thenReturn(new ResponseEntity<>(creditDto, HttpStatus.OK));

        ResponseEntity<Void> response = creditService.finishCalculateCredit(requestDto, validStatementId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(statementRepository, times(1)).findById(UUID.fromString(validStatementId));
        verify(restTemplate, times(1)).postForEntity(anyString(), any(ScoringDataDto.class), eq(CreditDto.class));
        verify(creditRepository, times(1)).save(any(Credit.class));
        verify(statementRepository, times(1)).save(statement);
    }

    @Test
    void finishCalculateCredit_ShouldThrowInvalidInputException_WhenRequestIsNull() {
        assertThrows(InvalidInputException.class, () -> {
            creditService.finishCalculateCredit(null, validStatementId);
        });
    }

    @Test
    void finishCalculateCredit_ShouldThrowInvalidInputException_WhenStatementIdIsInvalid() {
        assertThrows(InvalidInputException.class, () -> {
            creditService.finishCalculateCredit(requestDto, "invalid-uuid");
        });
    }

    @Test
    void finishCalculateCredit_ShouldThrowStatementNotFoundException_WhenStatementNotFound() {
        when(statementRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(StatementNotFoundException.class, () -> {
            creditService.finishCalculateCredit(requestDto, validStatementId);
        });
    }


    @Test
    void finishCalculateCredit_ShouldThrowScoringServiceException_WhenServiceReturnsError() {
        when(statementRepository.findById(any(UUID.class))).thenReturn(Optional.of(statement));
        when(scoringServiceProperties.getUrl()).thenReturn("http://scoring-service");
        when(restTemplate.postForEntity(anyString(), any(ScoringDataDto.class), eq(CreditDto.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(ScoringServiceException.class, () -> {
            creditService.finishCalculateCredit(requestDto, validStatementId);
        });
    }


    @Test
    void createAndSaveCredit_ShouldCorrectlyMapAndSaveCredit() {
        creditService.createAndSaveCredit(creditDto, statement);

        verify(creditRepository, times(1)).save(any(Credit.class));
    }

    @Test
    void updateStatementStatus_ShouldUpdateStatusCorrectly() {
        statement.setStatusHistory(new ArrayList<>());

        creditService.updateStatementStatus(statement);

        assertEquals(ApplicationStatus.CREDIT_ISSUED, statement.getStatus());
        assertEquals(1, statement.getStatusHistory().size());
        StatusHistory history = statement.getStatusHistory().get(0);
        assertEquals(ApplicationStatus.CREDIT_ISSUED, history.getStatus());
        assertEquals(ChangeType.AUTOMATIC, history.getChangeType());
        verify(statementRepository, times(1)).save(statement);
    }
}