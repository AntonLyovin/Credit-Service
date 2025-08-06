package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.dto.FinishRegistrationRequestDto;
import org.example.model.dto.LoanOfferDto;
import org.example.model.dto.LoanStatementRequestDto;
import org.example.service.LoanProcessingFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.ServiceUnavailableException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/deal")
@Slf4j
@RequiredArgsConstructor

public class DealController {
    private final LoanProcessingFacade loanProcessingFacade;

    @PostMapping("/statement")
    @Operation(
            summary = "Расчет предложений",
            description = "Принимает данные для прескоринга и возвращает список кредитных предложений"
    )
    public List<LoanOfferDto> createStatement(
            @RequestBody @Parameter(description = "Данные для прескоринга")
            @Valid LoanStatementRequestDto requestDto) throws ServiceUnavailableException {

        log.info("Начало расчета кредита. Тело запроса: {}", requestDto);
        return loanProcessingFacade.processLoanApplication(requestDto);
    }

    @PostMapping("/offer/select")
    @Operation(
            summary = "Выбор предложения",
            description = "Принимает выбранное кредитное предложение и обновляет заявку"
    )
    public ResponseEntity<Void> selectOffer(
            @RequestBody @Valid LoanOfferDto loanOfferDto) {

        log.info("Начало выбора предложения. Тело запроса: {}", loanOfferDto);
        loanProcessingFacade.processOfferSelection(loanOfferDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/calculate/{statementId}")
    @Operation(
            summary = "Финальный расчет кредита",
            description = "Принимает данные для финального расчета и создает кредит"
    )
    public ResponseEntity<Void> finishCalculateCredit(
            @PathVariable UUID statementId,
            @RequestBody @Valid FinishRegistrationRequestDto requestDto) throws ServiceUnavailableException {
        log.info("Начало финального расчета. statementId: {} Тело запроса: {}",
                statementId, requestDto);
        loanProcessingFacade.processCreditCalculation(requestDto, statementId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/document/{statementId}/send")
    @Operation(
            summary = "запрос на отправку документов",
            description = "Отправляет документы для ранее принятого предложения"
    )
    public ResponseEntity<Void> sendDocument(
            @PathVariable UUID statementId) {
        log.info("Начало отправки документов. statementId: {} ",
                statementId);
        loanProcessingFacade.processSendDocuments(statementId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/document/{statementId}/sign")
    @Operation(
            summary = "Запрос на подписание документов",
            description = "Генерирует SES code для выбранных документов"
    )
    public ResponseEntity<Void> signDocument(
            @PathVariable UUID statementId) {
        log.info("Начало подписания документов. statementId: {} ",
                statementId);
        loanProcessingFacade.processSignDocuments(statementId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/document/{statementId}/code")
    @Operation(
            summary = "Отправка кода на подписание документов",
            description = "Отправляет сгенерированный SES code для выбранных документов"
    )
    public ResponseEntity<Void> verifySesCode(
            @PathVariable UUID statementId,
            @RequestParam @NotBlank String sesCode) {
        log.info("Начало верификации Ses code. statementId: {} ",
                statementId);
        loanProcessingFacade.processVerifySesCode(statementId, sesCode);
        return ResponseEntity.ok().build();
    }


}
