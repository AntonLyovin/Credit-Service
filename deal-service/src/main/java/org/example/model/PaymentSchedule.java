package org.example.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PaymentSchedule implements Serializable {
     Integer number;
     LocalDate date;
     BigDecimal totalPayment;
     BigDecimal interestPayment;
     BigDecimal debtPayment;
     BigDecimal remainingDebt;
}
