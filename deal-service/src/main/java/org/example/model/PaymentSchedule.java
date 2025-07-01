package org.example.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentSchedule implements Serializable {
     Integer number;
     LocalDate date;
     BigDecimal totalPayment;
     BigDecimal interestPayment;
     BigDecimal debtPayment;
     BigDecimal remainingDebt;
}
