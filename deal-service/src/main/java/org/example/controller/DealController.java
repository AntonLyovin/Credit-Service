package org.example.controller;

import calculatorApp.calculator.model.dto.LoanOfferDto;
import calculatorApp.calculator.model.dto.LoanStatementRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.StatementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/deal")
@Slf4j
@RequiredArgsConstructor

public class DealController {
    private final StatementService statementService;

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
}
