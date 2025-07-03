package org.example.service;

import calculatorApp.calculator.model.dto.CreditDto;
import calculatorApp.calculator.model.dto.ScoringDataDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.example.model.PaymentSchedule;
import org.example.model.StatusHistory;
import org.example.model.dto.FinishRegistrationRequestDto;
import org.example.model.entity.Credit;
import org.example.model.entity.Statement;
import org.example.model.enumerated.ApplicationStatus;
import org.example.model.enumerated.ChangeType;
import org.example.model.enumerated.CreditStatus;
import org.example.repository.CreditRepository;
import org.example.repository.StatementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.naming.ServiceUnavailableException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CreditServiceImp implements CreditService {
    StatementRepository statementRepository;
    CreditRepository creditRepository;
    RestTemplate restTemplate;

    @Autowired
    public void setCreditRepository(CreditRepository creditRepository) {
        this.creditRepository = creditRepository;
    }

    @Autowired
    public void setStatementRepository(StatementRepository statementRepository) {
        this.statementRepository = statementRepository;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private static final String SCORING_SERVICE_URL = "http://localhost:8080/calculator/calc";

    public ResponseEntity<Void> finishCalculateCredit(FinishRegistrationRequestDto requestDto, String statementId) throws ServiceUnavailableException {

        Statement statement = statementRepository.findById(UUID.fromString(statementId))
                .orElseThrow(() -> new EntityNotFoundException("Заявка не найдена"));

        ScoringDataDto scoringData = buildScoringData(requestDto, statement);

        CreditDto creditDto;
        try {
            creditDto = callScoringService(scoringData);
        } catch (ServiceUnavailableException e) {
            throw new ServiceUnavailableException("Ошибка при расчете кредитного предложения: " + e.getMessage());
        }

        createAndSaveCredit(creditDto, statement);

        updateStatementStatus(statement);

        return ResponseEntity.ok().build();
    }

    ScoringDataDto buildScoringData(FinishRegistrationRequestDto requestDto, Statement statement) {
        return ScoringDataDto.builder()
                .amount(statement.getAppliedOffer().getRequestedAmount())
                .term(statement.getAppliedOffer().getTerm())
                .firstName(statement.getClientId().getFirstName())
                .lastName(statement.getClientId().getLastName())
                .middleName(statement.getClientId().getMiddleName())
                .gender(requestDto.getGender())
                .birthdate(statement.getClientId().getBirthDate())
                .passportSeries(requestDto.getPassportSeries())
                .passportNumber(requestDto.getPassportNumber())
                .passportIssueDate(requestDto.getPassportIssueDate())
                .passportIssueBranch(requestDto.getPassportIssueBranch())
                .maritalStatus(requestDto.getMaritalStatus())
                .dependentAmount(requestDto.getDependentAmount())
                .employment(requestDto.getEmployment())
                .accountNumber(requestDto.getAccountNumber())
                .isInsuranceEnabled(requestDto.getIsInsuranceEnabled())
                .isSalaryClient(requestDto.getIsSalaryClient())
                .build();
    }

    private CreditDto callScoringService(ScoringDataDto scoringData) throws ServiceUnavailableException {
        try {
            ResponseEntity<CreditDto> response = restTemplate.postForEntity(
                    SCORING_SERVICE_URL,
                    scoringData,
                    CreditDto.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new ServiceUnavailableException("Сервис скоринга вернул некорректный ответ. Статус: " +
                        response.getStatusCode());
            }
        } catch (Exception e) {
            throw new ServiceUnavailableException("Ошибка при обращении к сервису скоринга: ");
        }
    }

    void createAndSaveCredit(CreditDto creditDto, Statement statement) {
        log.info("Конвертирования списка платежей");
        List<PaymentSchedule> paymentSchedules = creditDto.getPaymentSchedule().stream()
                .map(dto -> PaymentSchedule.builder()
                        .number(dto.getNumber())
                        .date(dto.getDate())
                        .totalPayment(dto.getTotalPayment())
                        .interestPayment(dto.getInterestPayment())
                        .debtPayment(dto.getDebtPayment())
                        .remainingDebt(dto.getRemainingDebt())
                        .build())
                .collect(Collectors.toList());

        log.info("Заполнение параметров кредита");
        Credit credit = Credit.builder()
                .creditId(UUID.randomUUID())
                .amount(creditDto.getAmount())
                .term(creditDto.getTerm())
                .monthlyPayment(creditDto.getMonthlyPayment())
                .rate(creditDto.getRate())
                .psk(creditDto.getPsk())
                .paymentSchedule(paymentSchedules)  // Теперь передаем список
                .insuranceEnabled(creditDto.getIsInsuranceEnabled())
                .salaryClient(creditDto.getIsSalaryClient())
                .creditStatus(CreditStatus.CALCULATED)
                .build();
        creditRepository.save(credit);
        log.info("Сохранение кредита с Id: {}", credit.getCreditId());

    }

    void updateStatementStatus(Statement statement) {
        log.info("Обновление статуса");
        statement.setStatus(ApplicationStatus.CREDIT_ISSUED);

        StatusHistory statusHistory = StatusHistory.builder()
                .status(ApplicationStatus.CREDIT_ISSUED)
                .time(LocalDate.now())
                .changeType(ChangeType.AUTOMATIC)
                .build();

        statement.getStatusHistory().add(statusHistory);
        statementRepository.save(statement);
    }


}
