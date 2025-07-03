package org.example.controller;

import calculatorApp.calculator.model.dto.LoanOfferDto;
import calculatorApp.calculator.model.dto.LoanStatementRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.dto.FinishRegistrationRequestDto;
import org.example.service.CreditService;
import org.example.service.StatementService;
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
    private final StatementService statementService;
    private final CreditService creditService;

    @PostMapping("/statement")
    @Operation(
            summary = "Расчет предложений",
            description = "Принимает данные для прескоринга и возвращает список кредитных предложений"
    )
    public List<LoanOfferDto> createStatement(@RequestBody  @Parameter(description = "Данные для прескоринга") @Valid LoanStatementRequestDto requestDto) {
        log.info("Начало расчета кредита. Тело запроса: {}", requestDto);
        return statementService.createStatement(requestDto);
    }
    @PostMapping("/offer/select")
    public ResponseEntity<Void> selectOffer(@RequestBody LoanOfferDto loanOfferDto) {
        statementService.selectOffer(loanOfferDto);
        log.info("Начало выбора предложения. Тело запроса: {}", loanOfferDto);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/calculate/{statementId}")
    public ResponseEntity<Void> finishCalculateCredit(
            String statementId,
            @RequestBody FinishRegistrationRequestDto requestDto) {
        try {
            UUID.fromString(statementId); // Дополнительная проверка
            log.info("Начало финального расчета. statementId: {} Тело запроса: {}", statementId, requestDto);
            return creditService.finishCalculateCredit(requestDto, statementId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ServiceUnavailableException e) {
            return ResponseEntity.status(503).build();
        }
    }
}
