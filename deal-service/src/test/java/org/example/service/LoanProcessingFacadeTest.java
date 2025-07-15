package org.example.service;

import org.example.model.AppliedOffer;
import org.example.model.dto.*;
import org.example.model.entity.Client;
import org.example.model.entity.Credit;
import org.example.model.entity.Statement;
import org.example.model.enumerated.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.naming.ServiceUnavailableException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanProcessingFacadeTest {

    @Mock
    private ClientService clientService;

    @Mock
    private StatementService statementService;

    @Mock
    private OfferCalculationService offerService;

    @Mock
    private CreditService creditService;

    @InjectMocks
    private LoanProcessingFacade loanProcessingFacade;


    @Test
    void processLoanApplication_ServiceUnavailable() throws ServiceUnavailableException {
        LoanStatementRequestDto requestDto = createTestLoanRequest();
        Client testClient = createTestClient();
        Statement testStatement = createTestStatement();

        when(clientService.createClient(any(LoanStatementRequestDto.class))).thenReturn(testClient);
        when(statementService.createStatement(any(Client.class))).thenReturn(testStatement);
        when(offerService.calculateOffers(any(LoanStatementRequestDto.class)))
                .thenThrow(new ServiceUnavailableException("Service unavailable"));

        assertThrows(ServiceUnavailableException.class, () ->
                loanProcessingFacade.processLoanApplication(requestDto));
    }

    @Test
    void processOfferSelection_Success() {
        LoanOfferDto offerDto = createTestOffer(BigDecimal.valueOf(8.5));
        offerDto.setStatementId(UUID.randomUUID());

        loanProcessingFacade.processOfferSelection(offerDto);

        verify(statementService).applyOfferToStatement(offerDto.getStatementId(), offerDto);
    }

    @Test
    void processCreditCalculation_Success() throws ServiceUnavailableException {
        UUID statementId = UUID.randomUUID();
        FinishRegistrationRequestDto requestDto = createFinishRegistrationRequest();
        Statement testStatement = createTestStatementWithOffer();
        Credit testCredit = createTestCredit();

        when(statementService.getStatementById(statementId)).thenReturn(testStatement);
        when(creditService.createCredit(any(ScoringDataDto.class), any(Statement.class)))
                .thenReturn(testCredit);

        loanProcessingFacade.processCreditCalculation(requestDto, statementId);

        verify(statementService).getStatementById(statementId);
        verify(creditService).createCredit(any(ScoringDataDto.class), eq(testStatement));
        verify(statementService).updateStatementWithCredit(testStatement, testCredit);
    }

    @Test
    void buildScoringData_Complete() {
        FinishRegistrationRequestDto requestDto = createFinishRegistrationRequest();
        Statement statement = createTestStatementWithOffer();

        ScoringDataDto result = loanProcessingFacade.buildScoringData(requestDto, statement);

        assertNotNull(result);
        assertEquals(statement.getAppliedOffer().getRequestedAmount(), result.getAmount());
        assertEquals(statement.getAppliedOffer().getTerm(), result.getTerm());
        assertEquals(statement.getClientId().getFirstName(), result.getFirstName());
        assertEquals(statement.getClientId().getLastName(), result.getLastName());
        assertEquals(statement.getClientId().getMiddleName(), result.getMiddleName());
        assertEquals(requestDto.getGender(), result.getGender());
        assertEquals(statement.getClientId().getBirthDate(), result.getBirthdate());
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

    private LoanStatementRequestDto createTestLoanRequest() {
        return LoanStatementRequestDto.builder()
                .lastName("Ivanov")
                .firstName("Ivan")
                .middleName("Ivanovich")
                .birthdate(LocalDate.of(1990, 5, 15))
                .email("ivanov@example.com")
                .build();
    }

    private Client createTestClient() {
        return Client.builder()
                .clientId(UUID.randomUUID())
                .lastName("Ivanov")
                .firstName("Ivan")
                .middleName("Ivanovich")
                .birthDate(LocalDate.of(1990, 5, 15))
                .email("ivanov@example.com")
                .build();
    }

    private Statement createTestStatement() {
        return Statement.builder()
                .statementId(UUID.randomUUID())
                .clientId(createTestClient())
                .build();
    }

    private Statement createTestStatementWithOffer() {
        Statement statement = createTestStatement();

        AppliedOffer appliedOffer = AppliedOffer.builder()
                .statementId(statement.getStatementId())
                .requestedAmount(BigDecimal.valueOf(100000))
                .totalAmount(BigDecimal.valueOf(108000))
                .term(24)
                .monthlyPayment(BigDecimal.valueOf(4500))
                .rate(BigDecimal.valueOf(8.5))
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .build();

        statement.setAppliedOffer(appliedOffer);
        return statement;
    }

    private LoanOfferDto createTestOffer(BigDecimal rate) {
        return LoanOfferDto.builder()
                .requestedAmount(BigDecimal.valueOf(100000))
                .term(24)
                .monthlyPayment(BigDecimal.valueOf(4500))
                .rate((rate))
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .build();
    }

    private FinishRegistrationRequestDto createFinishRegistrationRequest() {
        return FinishRegistrationRequestDto.builder()
                .gender(Gender.MALE)
                .passportSeries("1234")
                .passportNumber("567890")
                .passportIssueDate(LocalDate.of(2010, 6, 20))
                .passportIssueBranch("UFMS-1")
                .maritalStatus(MaritalStatus.MARRIED)
                .dependentAmount(0)
                .employment(EmploymentDto.builder()
                        .employmentStatus(EmploymentStatusEnum.SELF_EMPLOYED)
                        .salary(BigDecimal.valueOf(50000))
                        .position(Position.WORKER)
                        .workExperienceTotal(60)
                        .getWorkExperienceCurrent(24)
                        .build())
                .accountNumber("40817810099910004321")
                .isInsuranceEnabled(true)
                .isSalaryClient(false)
                .build();
    }

    private Credit createTestCredit() {
        return Credit.builder()
                .creditId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(100000))
                .term(24)
                .monthlyPayment(BigDecimal.valueOf(4500))
                .rate(BigDecimal.valueOf(8.5))
                .psk(BigDecimal.valueOf(108000))
                .insuranceEnabled(true)
                .salaryClient(false)
                .creditStatus(CreditStatus.CALCULATED)
                .build();
    }
}