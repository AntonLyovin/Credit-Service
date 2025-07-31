package org.example.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.AppliedOffer;
import org.example.model.StatusHistory;
import org.example.model.dto.EmailMessage;
import org.example.model.dto.LoanOfferDto;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public EmailMessage fillEmailMessageForOfferSelect(LoanOfferDto loanOfferDto){
        Optional<Client> clientFind = statementRepository.findById(loanOfferDto.getStatementId())
                .map(i -> i.getClientId());
        if (clientFind.isEmpty()){
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
                .text("Перейдите к следующему шагу")
                .build();
        return message;
    }

//    @Transactional
//    public EmailMessage fillEmailMessageForFinishRegistration(FinishRegistrationRequestDto requestDto, UUID statementId ){
//        String address = statementRepository.findById(statementId)
//                .map(i -> i.getClientId())
//                .map(i -> i.getEmail())
//                .orElse(null);
//        EmailMessage message = EmailMessage.builder()
//                .address(address)
//                .theme(Theme.FINISH_REGISTRATION)
//                .statementId(statementId.toString())
//                .text("Начался финальный процесс регистрации")
//                .build();
//        return message;
//    }

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


}
