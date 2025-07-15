package org.example.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
public class AppliedOffer implements Serializable {
    UUID statementId;
    BigDecimal requestedAmount;
    BigDecimal totalAmount;
    Integer term;
    BigDecimal monthlyPayment;
    BigDecimal rate;
    Boolean isInsuranceEnabled;
    Boolean isSalaryClient;


}
