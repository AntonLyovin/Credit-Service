package org.example.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.model.enumerated.CreditStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Credit {
    UUID creditId;
    BigDecimal amount;
    Integer term;
    BigDecimal monthlyPayment;
    BigDecimal rate;
    BigDecimal psk;
    List<PaymentSchedule> paymentSchedule;
    Boolean insuranceEnabled;
    Boolean salaryClient;
    CreditStatus creditStatus;

}
