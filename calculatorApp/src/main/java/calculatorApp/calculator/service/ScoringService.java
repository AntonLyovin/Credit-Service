package calculatorApp.calculator.service;

import calculatorApp.calculator.model.dto.CreditDto;
import calculatorApp.calculator.model.dto.ScoringDataDto;

public interface ScoringService {
    CreditDto calculateCredit(ScoringDataDto data);
}
