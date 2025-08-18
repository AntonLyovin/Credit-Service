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
import org.example.service.GatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.ServiceUnavailableException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@Slf4j
@RequiredArgsConstructor
public class GatewayController {
    private final GatewayService gatewayService;

    @PostMapping("/statement")
    @Operation(
            summary = "Микросервис заявка",
            description = "Проводит прескоринг и отправляет запрос на расчет возможных условий кредита"
    )
    public List<LoanOfferDto> createStatement(
            @RequestBody @Parameter(description = "Данные для прескоринга")
            @Valid LoanStatementRequestDto requestDto) throws ServiceUnavailableException {

        log.info("Начало расчета кредита. Тело запроса: {}", requestDto);
        return gatewayService.calculateOffers(requestDto);
    }

    @PostMapping("/statement/select")
    @Operation(
            summary = "Выбор предложения",
            description = "Принимает выбранное кредитное предложение"
    )
    public ResponseEntity<Void> selectOffer(
            @RequestBody @Valid LoanOfferDto loanOfferDto) throws ServiceUnavailableException {

        log.info("Начало выбора предложения. Тело запроса: {}", loanOfferDto);
        gatewayService.selectOffer(loanOfferDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/statement/registration/{statementId}")
    @Operation(
            summary = "Финальный расчет кредита",
            description = "Принимает данные для финального расчета и создает кредит"
    )
    public ResponseEntity<Void> finishCalculateCredit(
            @PathVariable UUID statementId,
            @RequestBody @Valid FinishRegistrationRequestDto requestDto) throws ServiceUnavailableException {
        log.info("Начало финального расчета. statementId: {} Тело запроса: {}",
                statementId, requestDto);
        gatewayService.processCreditCalculation(requestDto, statementId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/document/{statementId}")
    @Operation(
            summary = "запрос на отправку документов",
            description = "Отправляет документы для ранее принятого предложения"
    )
    public ResponseEntity<Void> sendDocument(
            @PathVariable UUID statementId) throws ServiceUnavailableException {
        log.info("Начало отправки документов. statementId: {} ",
                statementId);
        gatewayService.processSendDocuments(statementId);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/document/{statementId}/sign")
    @Operation(
            summary = "Запрос на подписание документов",
            description = "Генерирует SES code для выбранных документов"
    )
    public ResponseEntity<Void> signDocument(
            @PathVariable UUID statementId) throws ServiceUnavailableException {
        log.info("Начало подписания документов. statementId: {} ",
                statementId);
        gatewayService.processSignDocuments(statementId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/document/{statementId}/code")
    @Operation(
            summary = "Отправка кода на подписание документов",
            description = "Отправляет сгенерированный SES code для выбранных документов"
    )
    public ResponseEntity<Void> verifySesCode(
            @PathVariable UUID statementId,
            @RequestParam @NotBlank String sesCode) throws ServiceUnavailableException {
        log.info("Начало верификации Ses code. statementId: {} ",
                statementId);
        gatewayService.processVerifySesCode(statementId, sesCode);
        return ResponseEntity.ok().build();
    }
}
