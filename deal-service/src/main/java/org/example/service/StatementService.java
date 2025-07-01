package org.example.service;

import calculatorApp.calculator.model.dto.LoanOfferDto;
import calculatorApp.calculator.model.dto.LoanStatementRequestDto;

import java.util.List;

public interface StatementService {
    List<LoanOfferDto> createStatement(LoanStatementRequestDto requestDto);
    void selectOffer(LoanOfferDto loanOfferDto);
}
