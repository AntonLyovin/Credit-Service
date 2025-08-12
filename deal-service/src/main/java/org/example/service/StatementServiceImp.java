package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.AppliedOffer;
import org.example.model.StatusHistory;
import org.example.model.dto.*;
import org.example.model.entity.Client;
import org.example.model.entity.Credit;
import org.example.model.entity.Statement;
import org.example.model.enumerated.ApplicationStatus;
import org.example.model.enumerated.ChangeType;
import org.example.model.enumerated.Theme;
import org.example.repository.StatementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatementServiceImp implements StatementService {
    private final StatementRepository statementRepository;

    @Override
    @Transactional
    public Statement createStatement(Client client) {
        Statement statement = Statement.builder()
                .clientId(client)
                .creationDate(LocalDate.now())
                .signDate(LocalDate.now())
                .statusHistory(createInitialStatusHistory())
                .build();

        return statementRepository.save(statement);
    }

    @Transactional
    public EmailMessage fillEmailMessageForOfferSelect(LoanOfferDto loanOfferDto) {
        Optional<Client> clientFind = statementRepository.findById(loanOfferDto.getStatementId())
                .map(i -> i.getClientId());
        if (clientFind.isEmpty()) {
            throw new RuntimeException("Клиент не найден по statementId");
        }
        Client client = clientFind.get();
        Optional<Statement> statementFind = statementRepository.findById(loanOfferDto.getStatementId());
        Statement statement = statementFind.get();
        EmailMessage message = EmailMessage.builder()
                .firstName(client.getFirstName())
                .middleName(client.getMiddleName())
                .lastName(client.getLastName())
                .address(client.getEmail())
                .theme(Theme.FINISH_REGISTRATION)
                .statementId(loanOfferDto.getStatementId().toString())
                .appliedOffer(statement.getAppliedOffer())
                .text("Выбрано одно из предложений")
                .build();
        return message;
    }

    @Transactional
    public EmailMessage fillEmailMessageForFinishRegistration(FinishRegistrationRequestDto requestDto, UUID statementId) {
        Optional<Client> clientFind = statementRepository.findById(statementId)
                .map(i -> i.getClientId());
        if (clientFind.isEmpty()) {
            throw new RuntimeException("Клиент не найден по statementId");
        }
        Client client = clientFind.get();
        String address = statementRepository.findById(statementId)
                .map(i -> i.getClientId())
                .map(i -> i.getEmail())
                .orElse(null);
        EmailMessage message = EmailMessage.builder()
                .firstName(client.getFirstName())
                .middleName(client.getMiddleName())
                .lastName(client.getLastName())
                .address(address)
                .theme(Theme.CREATE_DOCUMENTS)
                .statementId(statementId.toString())
                .text("На основе выбранного предложения сформирован кредит.\n\nПерейдите к следующему шагу чтобы получить документы.")
                .build();
        return message;
    }

    @Transactional
    public EmailMessage fillEmailMessageForPrepareDocuments(UUID statementId) {
        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена по statementId: " + statementId));

        Client client = statement.getClientId();
        if (client == null) {
            throw new RuntimeException("Клиент не найден для заявки: " + statementId);
        }

        String address = Optional.ofNullable(client.getEmail())
                .orElseThrow(() -> new RuntimeException("Email клиента не найден"));

        Credit credit = statement.getCreditId();
        if (credit == null) {
            throw new RuntimeException("Кредит не найден для заявки: " + statementId);
        }

        List<PaymentScheduleElementDto> scheduleDtos = credit.getPaymentSchedule() != null
                ? credit.getPaymentSchedule().stream()
                .map(ps -> PaymentScheduleElementDto.builder()
                        .number(ps.getNumber())
                        .date(ps.getDate())
                        .totalPayment(ps.getTotalPayment())
                        .interestPayment(ps.getInterestPayment())
                        .debtPayment(ps.getDebtPayment())
                        .remainingDebt(ps.getRemainingDebt())
                        .build()
                ).collect(Collectors.toList())
                : Collections.emptyList();

        CreditDto creditDto = CreditDto.builder()
                .amount(credit.getAmount())
                .term(credit.getTerm())
                .monthlyPayment(credit.getMonthlyPayment())
                .rate(credit.getRate())
                .psk(credit.getPsk())
                .paymentSchedule(scheduleDtos)  // используем подготовленный список
                .build();
        EmailMessage message = EmailMessage.builder()
                .firstName(client.getFirstName())
                .middleName(client.getMiddleName())
                .lastName(client.getLastName())
                .address(address)
                .theme(Theme.SEND_DOCUMENTS)
                .statementId(statementId.toString())
                .credit(creditDto)
                .text("Выбранные условия кредита")
                .build();
        return message;
    }

    @Transactional
    public EmailMessage fillEmailMessageForSignDocuments(UUID statementId) {
        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена по statementId: " + statementId));
        Client client = statement.getClientId();
        if (client == null) {
            throw new RuntimeException("Клиент не найден для заявки: " + statementId);
        }

        String address = Optional.ofNullable(client.getEmail())
                .orElseThrow(() -> new RuntimeException("Email клиента не найден"));

        EmailMessage message = EmailMessage.builder()
                .firstName(client.getFirstName())
                .middleName(client.getMiddleName())
                .lastName(client.getLastName())
                .address(address)
                .theme(Theme.SEND_SES)
                .statementId(statementId.toString())
                .text("Ваш код для подписания документов:")
                .sesCode(statement.getSesCode())
                .build();
        return message;
    }

    @Transactional
    public EmailMessage fillEmailMessageForVerifySesCode(UUID statementId, String sesCode) {
        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена по statementId: " + statementId));
        Client client = statement.getClientId();
        if (client == null) {
            throw new RuntimeException("Клиент не найден для заявки: " + statementId);
        }
        String address = Optional.ofNullable(client.getEmail())
                .orElseThrow(() -> new RuntimeException("Email клиента не найден"));

        String systemCode = statement.getSesCode();

        if (!sesCode.equals(systemCode)) {
            throw new RuntimeException("Неверный SES-код");
        }

        return EmailMessage.builder()
                .firstName(client.getFirstName())
                .middleName(client.getMiddleName())
                .lastName(client.getLastName())
                .address(address)
                .theme(Theme.CREDIT_ISSUED)
                .statementId(statementId.toString())
                .text("Ваш код принят")
                .build();
    }

    @Override
    @Transactional
    public void applyOfferToStatement(UUID statementId, LoanOfferDto offerDto) {
        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка не найдена"));

        statement.setStatus(ApplicationStatus.APPROVED);
        statement.getStatusHistory().add(createApprovedStatusHistory());

        AppliedOffer appliedOffer = convertToAppliedOffer(offerDto);
        statement.setAppliedOffer(appliedOffer);

        statementRepository.save(statement);
    }

    @Override
    @Transactional
    public Statement getStatementById(UUID statementId) {
        return statementRepository.findById(statementId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка не найдена"));
    }

    @Override
    @Transactional
    public void updateStatementWithCredit(Statement statement, Credit credit) {
        statement.setCreditId(credit);
        statement.setStatus(ApplicationStatus.CC_APPROVED);
        statement.getStatusHistory().add(createCreditIssuedStatusHistory());
        statementRepository.save(statement);
    }

    public List<StatusHistory> createInitialStatusHistory() {
        return List.of(
                StatusHistory.builder()
                        .time(LocalDate.now())
                        .status(ApplicationStatus.PREAPPROVAL)
                        .changeType(ChangeType.AUTOMATIC)
                        .build()
        );
    }

    private StatusHistory createApprovedStatusHistory() {
        return StatusHistory.builder()
                .status(ApplicationStatus.APPROVED)
                .time(LocalDate.now())
                .changeType(ChangeType.MANUAL)
                .build();
    }

    private StatusHistory createCreditIssuedStatusHistory() {
        return StatusHistory.builder()
                .status(ApplicationStatus.CREDIT_ISSUED)
                .time(LocalDate.now())
                .changeType(ChangeType.AUTOMATIC)
                .build();
    }

    private AppliedOffer convertToAppliedOffer(LoanOfferDto dto) {
        return AppliedOffer.builder()
                .requestedAmount(dto.getRequestedAmount())
                .totalAmount(dto.getTotalAmount())
                .term(dto.getTerm())
                .monthlyPayment(dto.getMonthlyPayment())
                .rate(dto.getRate())
                .isInsuranceEnabled(dto.getIsInsuranceEnabled())
                .isSalaryClient(dto.getIsSalaryClient())
                .build();
    }

    @Transactional
    public void updateStatementWithDocuments(Statement statement) {
        statement.setStatus(ApplicationStatus.PREPARE_DOCUMENTS);
        statementRepository.save(statement);
    }

    @Transactional
    public void updateStatementSignDocuments(Statement statement) {
        statement.setSesCode(UUID.randomUUID().toString());
        statementRepository.save(statement);
    }

    @Transactional
    public void updateStatementWithSesCode(Statement statement) {
        statement.setStatus(ApplicationStatus.DOCUMENT_SIGNED);
        statementRepository.save(statement);
    }

    @Transactional
    public List<StatementDto> findAllStatement(){
        List<Statement> statements = statementRepository.findAll();
        return statements.stream()
                .map(statement -> StatementDto.builder()
                        .statementId(statement.getStatementId())
                        .status(statement.getStatus())
                        .creationDate(statement.getCreationDate())
                        .appliedOffer(statement.getAppliedOffer())
                        .signDate(statement.getSignDate())
                        .sesCode(statement.getSesCode())
                        .statusHistory(statement.getStatusHistory())
                        .build()
                )
                .collect(Collectors.toList());
    }


}
