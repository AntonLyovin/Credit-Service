package org.example.service;

import calculatorApp.calculator.model.dto.CreditDto;
import calculatorApp.calculator.model.dto.EmploymentDto;
import calculatorApp.calculator.model.dto.PaymentScheduleElementDto;
import calculatorApp.calculator.model.dto.ScoringDataDto;
import calculatorApp.calculator.model.enumerated.EmploymentStatusEnum;
import calculatorApp.calculator.model.enumerated.Gender;
import calculatorApp.calculator.model.enumerated.MartialStatus;
import calculatorApp.calculator.model.enumerated.Position;
import jakarta.persistence.EntityNotFoundException;
import org.example.model.AppliedOffer;
import org.example.model.StatusHistory;
import org.example.model.dto.FinishRegistrationRequestDto;
import org.example.model.entity.Client;
import org.example.model.entity.Credit;
import org.example.model.entity.Statement;
import org.example.model.enumerated.ApplicationStatus;
import org.example.model.enumerated.ChangeType;
import org.example.repository.CreditRepository;
import org.example.repository.StatementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import javax.naming.ServiceUnavailableException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @InjectMocks
    private CreditServiceImp creditService;

    private FinishRegistrationRequestDto requestDto;
    private Statement testStatement;
    private CreditDto testCreditDto;
    private final String statementId = "550e8400-e29b-41d4-a716-446655440000";

    @BeforeEach
    void setUp() {

        Client client = Client.builder()
                .clientId(UUID.randomUUID())
                .lastName("Ivanov")
                .firstName("Ivan")
                .middleName("Ivanovich")
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("ivanov@example.com")
                .gender(Gender.MALE)
                .martialStatus(MartialStatus.SINGLE)
                .dependentAmount(1)
                .build();

        AppliedOffer appliedOffer = new AppliedOffer();
        appliedOffer.setRequestedAmount(BigDecimal.valueOf(100000L));
        appliedOffer.setTerm(12);

        EmploymentDto employmentDto = new EmploymentDto();
        employmentDto.setEmploymentStatus(EmploymentStatusEnum.SELF_EMPLOYED);
        employmentDto.setEmployerINN("1234567890");
        employmentDto.setSalary(BigDecimal.valueOf(100000));
        employmentDto.setPosition(Position.WORKER);
        employmentDto.setWorkExperienceTotal(60);
        employmentDto.setGetWorkExperienceCurrent(24);

        requestDto = new FinishRegistrationRequestDto();
        requestDto.setGender(Gender.MALE);
        requestDto.setPassportSeries("1234");
        requestDto.setPassportNumber("567890");
        requestDto.setPassportIssueDate(LocalDate.now().minusYears(5));
        requestDto.setPassportIssueBranch("UFMS");
        requestDto.setMaritalStatus(MartialStatus.SINGLE);
        requestDto.setDependentAmount(1);
        requestDto.setEmployment(employmentDto);
        requestDto.setAccountNumber("40817810099910004312");
        requestDto.setIsInsuranceEnabled(true);
        requestDto.setIsSalaryClient(false);

        testStatement = Statement.builder()
                .statementId(UUID.fromString(statementId))
                .status(ApplicationStatus.APPROVED)
                .appliedOffer(appliedOffer)
                .clientId(client)
                .build();

        testCreditDto = new CreditDto();
        testCreditDto.setAmount(BigDecimal.valueOf(100000));
        testCreditDto.setTerm(12);
        testCreditDto.setMonthlyPayment(BigDecimal.valueOf(9166.67));
        testCreditDto.setRate(BigDecimal.valueOf(10.5));
        testCreditDto.setPsk(BigDecimal.valueOf(110000));
        testCreditDto.setIsInsuranceEnabled(true);
        testCreditDto.setIsSalaryClient(false);

        List<PaymentScheduleElementDto> schedule = new ArrayList<>();
        schedule.add(new PaymentScheduleElementDto(1, LocalDate.now(),
                BigDecimal.valueOf(1000), BigDecimal.valueOf(100),
                BigDecimal.valueOf(900), BigDecimal.valueOf(99000)));

        testCreditDto.setPaymentSchedule(schedule);
    }

    @Test
    void finishCalculateCredit_WhenStatementNotFound_ThrowsEntityNotFoundException() {
        when(statementRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            creditService.finishCalculateCredit(requestDto, statementId);
        });

        verify(statementRepository, never()).save(any(Statement.class));
        verify(creditRepository, never()).save(any(Credit.class));
    }

    @Test
    void finishCalculateCredit_WhenScoringServiceFails_ThrowsServiceUnavailableException() {
        when(statementRepository.findById(any(UUID.class))).thenReturn(Optional.of(testStatement));
        when(restTemplate.postForEntity(anyString(), any(ScoringDataDto.class), any(Class.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        assertThrows(ServiceUnavailableException.class, () -> {
            creditService.finishCalculateCredit(requestDto, statementId);
        });

        verify(creditRepository, never()).save(any(Credit.class));
    }

    @Test
    void buildScoringData_WithValidRequest_ReturnsCorrectDto() {
        ScoringDataDto result = creditService.buildScoringData(requestDto, testStatement);

        assertNotNull(result);
        assertEquals(testStatement.getAppliedOffer().getRequestedAmount(), result.getAmount());
        assertEquals(testStatement.getAppliedOffer().getTerm(), result.getTerm());
        assertEquals(testStatement.getClientId().getFirstName(), result.getFirstName());
        assertEquals(testStatement.getClientId().getLastName(), result.getLastName());
        assertEquals(testStatement.getClientId().getMiddleName(), result.getMiddleName());
        assertEquals(requestDto.getGender(), result.getGender());
        assertEquals(testStatement.getClientId().getBirthDate(), result.getBirthdate());
        assertEquals(requestDto.getPassportSeries(), result.getPassportSeries());
        assertEquals(requestDto.getPassportNumber(), result.getPassportNumber());
        assertEquals(requestDto.getPassportIssueDate(), result.getPassportIssueDate());
        assertEquals(requestDto.getPassportIssueBranch(), result.getPassportIssueBranch());
        assertEquals(requestDto.getMaritalStatus(), result.getMaritalStatus());
        assertEquals(requestDto.getDependentAmount(), result.getDependentAmount());
        assertEquals(requestDto.getEmployment(), result.getEmployment());
        assertEquals(requestDto.getAccountNumber(), result.getAccountNumber());
        assertEquals(requestDto.getIsInsuranceEnabled(), result.getIsInsuranceEnabled());
        assertEquals(requestDto.getIsSalaryClient(), result.getIsSalaryClient());
    }

    @Test
    void createAndSaveCredit_WithValidDto_CreatesCorrectCredit() {
        when(creditRepository.save(any(Credit.class))).thenReturn(new Credit());

        creditService.createAndSaveCredit(testCreditDto, testStatement);

        verify(creditRepository, times(1)).save(any(Credit.class));
    }

    @Test
    void updateStatementStatus_UpdatesStatusCorrectly() {
        Statement statement = Statement.builder()
                .statusHistory(new ArrayList<>())
                .build();

        creditService.updateStatementStatus(statement);

        assertEquals(ApplicationStatus.CREDIT_ISSUED, statement.getStatus());
        assertNotNull(statement.getStatusHistory());
        assertEquals(1, statement.getStatusHistory().size());

        StatusHistory historyEntry = statement.getStatusHistory().get(0);
        assertEquals(ApplicationStatus.CREDIT_ISSUED, historyEntry.getStatus());
        assertEquals(ChangeType.AUTOMATIC, historyEntry.getChangeType());
        assertNotNull(historyEntry.getTime());
    }
}