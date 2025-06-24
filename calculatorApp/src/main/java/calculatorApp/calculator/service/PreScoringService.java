package calculatorApp.calculator.service;

import calculatorApp.calculator.model.dto.LoanOfferDto;
import calculatorApp.calculator.model.dto.LoanStatementRequestDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PreScoringService {
    List<LoanOfferDto> calculatePreOffer(LoanStatementRequestDto requestDto);

}
