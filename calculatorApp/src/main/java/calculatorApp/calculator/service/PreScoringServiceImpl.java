package calculatorApp.calculator.service;

import calculatorApp.calculator.model.dto.CalcDto;
import calculatorApp.calculator.model.dto.LoanOfferDto;
import calculatorApp.calculator.model.dto.LoanStatementRequestDto;
import calculatorApp.calculator.util.CreditScoring;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class PreScoringServiceImpl implements PreScoringService {
    @Override
    public List<LoanOfferDto> calculatePreOffer(LoanStatementRequestDto requestDto) {
        List<CalcDto> calcDTOList = prepareDtoList(requestDto.getAmount(), requestDto.getTerm());
        log.info("Предоставление кредитных предложений");
        return calcDTOList.stream()
                .map(CreditScoring::calculatePreScoring)
                .sorted(Comparator.comparing(LoanOfferDto::getRate))
                .collect(Collectors.toList());
    }


    private List<CalcDto> prepareDtoList(BigDecimal amount, Integer term) {
        log.info("Подготовка кредитных предложений");
        List<CalcDto> result = new ArrayList<>();
        result.add(CalcDto.builder().amount(amount).term(term).isInsuranceEnabled(false).isSalaryClient(false).build());
        result.add(CalcDto.builder().amount(amount).term(term).isInsuranceEnabled(false).isSalaryClient(true).build());
        result.add(CalcDto.builder().amount(amount).term(term).isInsuranceEnabled(true).isSalaryClient(true).build());
        result.add(CalcDto.builder().amount(amount).term(term).isInsuranceEnabled(true).isSalaryClient(false).build());
        return result;
    }


}
