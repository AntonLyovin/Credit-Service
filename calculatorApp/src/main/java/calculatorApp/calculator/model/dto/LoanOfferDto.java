package calculatorApp.calculator.model.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoanOfferDto {
     UUID statementId;
     BigDecimal requestedAmount;
     BigDecimal totalAmount;
     Integer term;
     BigDecimal monthlyPayment;
     BigDecimal rate;
     Boolean isInsuranceEnabled;
     Boolean isSalaryClient;

}
