package calculatorApp.calculator.util;

import calculatorApp.calculator.model.dto.*;
import calculatorApp.calculator.model.enumerated.EmploymentStatusEnum;
import calculatorApp.calculator.model.enumerated.Gender;
import calculatorApp.calculator.model.enumerated.MaritalStatus;
import calculatorApp.calculator.model.enumerated.Position;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

@Slf4j
public class CreditScoring {
    private CreditScoring() {
    }

    public static LoanOfferDto calculatePreScoring(CalcDto calcDto) {
        log.info("Добавление резульатов расчета прескоринга");
        LoanOfferDto result = new LoanOfferDto();

        BigDecimal annualRate = BigDecimal.valueOf(20);
        if (calcDto.getIsInsuranceEnabled()) {
            annualRate = annualRate.subtract(BigDecimal.valueOf(1));
            calcDto.setAmount(calcDto.getAmount().add(BigDecimal.valueOf(50000)));
        }

        if (calcDto.getIsSalaryClient()) {
            annualRate = annualRate.subtract(BigDecimal.valueOf(1));
        }


        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), MathContext.DECIMAL128).divide(BigDecimal.valueOf(100), MathContext.DECIMAL128).setScale(2, RoundingMode.HALF_UP);

        Integer termInYears = calcDto.getTerm();
        BigDecimal termInYearsBigDecimal = BigDecimal.valueOf(termInYears);

        BigDecimal numberOfPayments = termInYearsBigDecimal.multiply(BigDecimal.valueOf(12)
        );

        BigDecimal one = BigDecimal.ONE;
        BigDecimal pow = (one.add(monthlyRate)).pow(numberOfPayments.intValue());
        BigDecimal numerator = monthlyRate.multiply(pow);
        BigDecimal denominator = pow.subtract(one);
        BigDecimal monthlyPayment = calcDto.getAmount().multiply(numerator).divide(denominator, MathContext.DECIMAL128).setScale(2, RoundingMode.HALF_UP);

        result.setStatementId(UUID.randomUUID());
        result.setRequestedAmount(calcDto.getAmount());
        result.setTotalAmount(calcDto.getAmount());
        result.setIsSalaryClient(calcDto.getIsSalaryClient());
        result.setIsInsuranceEnabled(calcDto.getIsInsuranceEnabled());
        result.setTerm(calcDto.getTerm());
        result.setRate(annualRate);
        result.setMonthlyPayment(monthlyPayment);
        return result;
    }
    public static ScoringResultDto performScoring(ScoringDataDto data) {
        log.info("Начало скоринга");
        ScoringResultDto result = new ScoringResultDto();
        result.setApproved(true);

        BigDecimal baseRate = new BigDecimal("20");
        BigDecimal rate = baseRate;

        int age = calculateAge(data.getBirthdate());

        if (age < 20 || age > 65) {
            result.setApproved(false);
            result.setRejectionReason("Возраст вне допустимых границ");
            return result;
        }

        if (data.getMaritalStatus() == MaritalStatus.MARRIED) {
            rate = rate.subtract(new BigDecimal("3"));
        } else if (data.getMaritalStatus() == MaritalStatus.DIVORCED) {
            rate = rate.add(new BigDecimal("1"));
        }

        Gender gender = data.getGender();
        if (gender == Gender.FEMALE && age >= 32 && age <= 60) {
            rate = rate.subtract(new BigDecimal("3"));
        } else if (gender == Gender.MALE && age >= 30 && age <= 55) {
            rate = rate.subtract(new BigDecimal("3"));
        } else if (gender == Gender.NOT_BINARY) {
            rate = rate.add(new BigDecimal("7"));
        }

        EmploymentDto employment = data.getEmployment();
        if (employment != null) {
            int totalExperienceMonths = employment.getWorkExperienceTotal() != null ? employment.getWorkExperienceTotal() : 0;
            int currentExperienceMonths = employment.getGetWorkExperienceCurrent() != null ? employment.getGetWorkExperienceCurrent() : 0;

            if (totalExperienceMonths < 18 || currentExperienceMonths < 3) {
                result.setApproved(false);
                result.setRejectionReason("Недостаточный стаж работы");
                return result;
            }

            BigDecimal salary = employment.getSalary() != null ? employment.getSalary() : BigDecimal.ZERO;
            if (salary.compareTo(BigDecimal.ZERO) > 0 && data.getAmount().compareTo(salary.multiply(BigDecimal.valueOf(24))) > 0) {
                result.setApproved(false);
                result.setRejectionReason("Сумма займа превышает 24 зарплаты");
                return result;
            }

            int totalWorkExpMonths = employment.getWorkExperienceTotal() != null ? employment.getWorkExperienceTotal() : Integer.MAX_VALUE;
            if (totalWorkExpMonths < 18) {
                result.setApproved(false);
                result.setRejectionReason("Общий стаж менее 18 месяцев");
                return result;
            }


            EmploymentStatusEnum status = employment.getEmploymentStatus();
            if (status == EmploymentStatusEnum.UNEMPLOYED) {
                result.setApproved(false);
                result.setRejectionReason("Статус безработный");
                return result;
            } else if (status == EmploymentStatusEnum.SELF_EMPLOYED) {
                rate = rate.add(new BigDecimal("2"));
            } else if (status == EmploymentStatusEnum.BUSINESS_OWNER) {
                rate = rate.add(new BigDecimal("1"));
            }

            Position position = employment.getPosition();
            if (position == Position.MIDDLE_MANAGER) {
                rate = rate.subtract(new BigDecimal("2"));
            } else if (position == Position.TOP_MANAGER) {
                rate = rate.subtract(new BigDecimal("3"));
            }

        }

        result.setRate(rate.max(BigDecimal.ZERO));

        return result;
    }

    public static int calculateAge(LocalDate birthdate) {
        log.info("Проверка возраста");
        return Period.between(birthdate, LocalDate.now()).getYears();
    }

    public static BigDecimal calculateTotalCost(BigDecimal amount, BigDecimal ratePercent) {
        log.info("Расчет полной суммы кредита");
        return amount.multiply(BigDecimal.ONE.add(ratePercent.divide(BigDecimal.valueOf(100))));

    }

    public static BigDecimal calculateMonthlyPayment(BigDecimal psk, int termMonths, BigDecimal annualRatePercent) {
        log.info("Расчет ежемесячного платежа");
        BigDecimal monthlyRate = annualRatePercent.divide(BigDecimal.valueOf(12 * 100), MathContext.DECIMAL128);

        BigDecimal numerator = psk.multiply(monthlyRate);
        BigDecimal denominatorFactor = BigDecimal.ONE.add(monthlyRate).pow(termMonths);
        denominatorFactor = BigDecimal.ONE.divide(denominatorFactor, MathContext.DECIMAL128);
        ;
        BigDecimal denominator = BigDecimal.ONE.subtract(denominatorFactor);
        return numerator.divide(denominator, MathContext.DECIMAL128);
    }
}