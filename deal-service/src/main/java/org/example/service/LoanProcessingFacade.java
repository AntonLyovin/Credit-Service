package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.model.dto.FinishRegistrationRequestDto;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.example.model.dto.ScoringDataDto;
import org.example.model.entity.Client;
import org.example.model.entity.Credit;
import org.example.model.entity.Statement;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.ServiceUnavailableException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class LoanProcessingFacade {
    private final ClientService clientService;
    private final StatementService statementService;
    private final OfferCalculationService offerService;
    private final CreditService creditService;

    public LoanProcessingFacade(ClientService clientService,
                                StatementService statementService,
                                OfferCalculationService offerService,
                                CreditService creditService) {
        this.clientService = clientService;
        this.statementService = statementService;
        this.offerService = offerService;
        this.creditService = creditService;
    }

    @Transactional
    public List<LoanOfferDto> processLoanApplication(LoanStatementRequestDto requestDto) throws ServiceUnavailableException {
        Client client = clientService.createClient(requestDto);
        Statement statement = statementService.createStatement(client);

        List<LoanOfferDto> offers = offerService.calculateOffers(requestDto);
        offers.forEach(offer -> offer.setStatementId(statement.getStatementId()));
        offers.sort(Comparator.comparing(LoanOfferDto::getRate));

        return offers;
    }

    @Transactional
    public void processOfferSelection(LoanOfferDto offerDto) {
        statementService.applyOfferToStatement(offerDto.getStatementId(), offerDto);
    }

    @Transactional
    public void processCreditCalculation(FinishRegistrationRequestDto requestDto, UUID statementId) throws ServiceUnavailableException {
        Statement statement = statementService.getStatementById(statementId);
        ScoringDataDto scoringData = buildScoringData(requestDto, statement);
        Credit credit = creditService.createCredit(scoringData, statement);
        statementService.updateStatementWithCredit(statement, credit);
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
}