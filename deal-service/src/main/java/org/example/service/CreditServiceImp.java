package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.config.ScoringServiceProperties;
import org.example.exception.custom.*;
import org.example.model.PaymentSchedule;
import org.example.model.StatusHistory;
import org.example.model.dto.CreditDto;
import org.example.model.dto.FinishRegistrationRequestDto;
import org.example.model.dto.ScoringDataDto;
import org.example.model.entity.Credit;
import org.example.model.entity.Statement;
import org.example.model.enumerated.ApplicationStatus;
import org.example.model.enumerated.ChangeType;
import org.example.model.enumerated.CreditStatus;
import org.example.repository.CreditRepository;
import org.example.repository.StatementRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CreditServiceImp implements CreditService {
    private final StatementRepository statementRepository;
    private final CreditRepository creditRepository;
    private final RestTemplate restTemplate;
    private final ScoringServiceProperties scoringServiceProperties;

    public CreditServiceImp(StatementRepository statementRepository, CreditRepository creditRepository, RestTemplate restTemplate, ScoringServiceProperties scoringServiceProperties) {
        this.statementRepository = statementRepository;
        this.creditRepository = creditRepository;
        this.restTemplate = restTemplate;
        this.scoringServiceProperties = scoringServiceProperties;
    }

    @Transactional
    public ResponseEntity<Void> finishCalculateCredit(FinishRegistrationRequestDto requestDto, String statementId) {
        try {
            validateInput(requestDto, statementId);
            Statement statement = getValidatedStatement(statementId);
            ScoringDataDto scoringData = buildScoringData(requestDto, statement);
            CreditDto creditDto = callScoringService(scoringData);
            createAndSaveCredit(creditDto, statement);
            updateStatementStatus(statement);

            log.info("Кредит успешно рассчитан для заявки ID: {}", statementId);

            return ResponseEntity.ok().build();

        } catch (InvalidInputException ex) {
            log.warn("Ошибка валидации входных данных: {}", ex.getMessage());
            throw ex;
        } catch (StatementNotFoundException ex) {
            log.error("Заявка не найдена: {}", statementId);
            throw ex;
        } catch (ScoringServiceException ex) {
            log.error("Ошибка сервиса скоринга: {}", ex.getMessage());
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Ошибка доступа к данным: {}", ex.getMessage());
            throw new DatabaseException("Ошибка при работе с базой данных");
        } catch (Exception ex) {
            log.error("Неожиданная ошибка при расчете кредита: {}", ex.getMessage(), ex);
            throw new CreditProcessingException("Ошибка при обработке кредитной заявки");
        }
    }

    private void validateInput(FinishRegistrationRequestDto requestDto, String statementId) {
        if (requestDto == null) {
            throw new InvalidInputException("Запрос не может быть null");
        }
        try {
            UUID.fromString(statementId);
        } catch (IllegalArgumentException ex) {
            throw new InvalidInputException("Неверный формат ID заявки: " + statementId);
        }
    }

    private Statement getValidatedStatement(String statementId) {
        return statementRepository.findById(UUID.fromString(statementId))
                .orElseThrow(() -> new StatementNotFoundException(statementId));
    }


    private CreditDto callScoringService(ScoringDataDto scoringData) {
        try {
            ResponseEntity<CreditDto> response = restTemplate.postForEntity(
                    scoringServiceProperties.getUrl(),
                    scoringData,
                    CreditDto.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ScoringServiceException("Сервис скоринга вернул некорректный ответ");
            }
            return response.getBody();
        } catch (ResourceAccessException e) {
            throw new ScoringServiceUnavailableException("Сервис скоринга недоступен: " + e.getMessage());
        }
    }


    public ScoringDataDto buildScoringData(FinishRegistrationRequestDto requestDto, Statement statement) {
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


    public void createAndSaveCredit(CreditDto creditDto, Statement statement) {
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

    public void updateStatementStatus(Statement statement) {
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
