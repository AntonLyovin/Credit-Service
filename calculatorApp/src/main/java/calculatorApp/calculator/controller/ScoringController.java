package calculatorApp.calculator.controller;

import calculatorApp.calculator.model.dto.CreditDto;
import calculatorApp.calculator.model.dto.LoanOfferDto;
import calculatorApp.calculator.model.dto.LoanStatementRequestDto;
import calculatorApp.calculator.model.dto.ScoringDataDto;
import calculatorApp.calculator.service.PreScoringService;
import calculatorApp.calculator.service.ScoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/calculator")
@Slf4j
public class ScoringController {

    private final ScoringService scoringService;
    private final PreScoringService preScoringService;

    @PostMapping("/calc")
    @Operation(
            summary = "Расчет кредита",
            description = "Принимает данные для скоринга и возвращает параметры кредита"
    )
    public CreditDto calculateCredit(@RequestBody  @Parameter(description = "Данные для скоринга") @Valid ScoringDataDto data) {
        log.info("Начало расчета кредита. Тело запроса: {}", data);
        return scoringService.calculateCredit(data);
    }

    @PostMapping("/offers")
    @Operation(
            summary = "Расчет кредита",
            description = "Позволяет рассчитать предварительные условия кредита"
    )
    public List<LoanOfferDto> calculatePreOffer(@RequestBody @Parameter(description = "Данные для прескориинга") @Valid LoanStatementRequestDto requestDto) {
        log.info("Начало обработки условий займа. Тело запроса: {}", requestDto);
        return preScoringService.calculatePreOffer(requestDto);
    }


}
