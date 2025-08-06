package org.example.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PaymentSchedule implements Serializable {
    Integer number;
    LocalDate date;
    BigDecimal totalPayment;
    BigDecimal interestPayment;
    BigDecimal debtPayment;
    BigDecimal remainingDebt;

    @Override
    public String toString() {
        return String.format(
                "Платеж #%d:\n" +
                        "  Дата: %s\n" +
                        "  Общая сумма: %.2f\n" +
                        "  Процентный платеж: %.2f\n" +
                        "  Основной долг: %.2f\n" +
                        "  Остаток долга: %.2f\n",
                number,
                date.toString(),
                totalPayment,
                interestPayment,
                debtPayment,
                remainingDebt
        );
    }

}
