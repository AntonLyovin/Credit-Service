package calculatorApp.calculator.service;

import calculatorApp.calculator.model.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static calculatorApp.calculator.util.CreditScoring.*;

@Service
@ComponentScan
@Slf4j
public class ScoringServiceImpl implements ScoringService {

    @Override
    public CreditDto calculateCredit(ScoringDataDto data) {

        CreditDto result = new CreditDto();
        ScoringResultDto scoringResult = performScoring(data);

        if (!scoringResult.isApproved()) {
            throw new RuntimeException("Заявка отклонена: " + scoringResult.getRejectionReason());
        }
        log.info("Заявка одобрена");
        BigDecimal rate = scoringResult.getRate();
        BigDecimal psk = calculateTotalCost(data.getAmount(), rate);
        BigDecimal monthlyPayment = calculateMonthlyPayment(psk, data.getTerm(), rate);
        List<PaymentScheduleElementDto> schedule =
                generatePaymentSchedule(psk, data.getTerm(), performScoring(data).getRate(), monthlyPayment);


        result.setAmount(data.getAmount());
        result.setTerm(data.getTerm());
        result.setMonthlyPayment(monthlyPayment.setScale(2, RoundingMode.HALF_UP));
        result.setRate(performScoring(data).getRate());
        result.setPsk(calculateTotalCost(data.getAmount(), performScoring(data).getRate()));
        result.setIsSalaryClient(data.getIsSalaryClient());
        result.setIsInsuranceEnabled(data.getIsInsuranceEnabled());
        result.setPaymentSchedule(schedule);

        log.info("тут можно логировать что-то еще, к примеру, результат расчета");
        return result;
    }

    private List<PaymentScheduleElementDto> generatePaymentSchedule(BigDecimal psk, int termMonths,
                                                                    BigDecimal annualRatePercent, BigDecimal monthlyPayment) {
        log.info("Генерация графика платежей");

        List<PaymentScheduleElementDto> schedule = new ArrayList<>();
        BigDecimal remainingDebt = psk;

        for (int month = 1; month <= termMonths; month++) {
            BigDecimal interestPart = remainingDebt.multiply(annualRatePercent.divide(BigDecimal.valueOf(12 * 100), MathContext.DECIMAL128));
            BigDecimal debtPart = monthlyPayment.subtract(interestPart);

            if (remainingDebt.compareTo(debtPart) < 0) {
                debtPart = remainingDebt;
                monthlyPayment = interestPart.add(debtPart);
            }

            remainingDebt = remainingDebt.subtract(debtPart);

            schedule.add(new PaymentScheduleElementDto(
                    month,
                    LocalDate.now().plusMonths(month),
                    monthlyPayment.setScale(2, RoundingMode.HALF_UP),
                    interestPart.setScale(2, RoundingMode.HALF_UP),
                    debtPart.setScale(2, RoundingMode.HALF_UP),
                    remainingDebt.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
            ));
        }
        return schedule;
    }

}
