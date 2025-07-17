package org.example.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
@Builder
public class LoanOfferDto {
     UUID statementId;
     @NotNull(message = "Сумма обязательна для заполнения")
     @DecimalMin(value = "20000", inclusive = true, message = "сумма должна быть не менее 20000")
     BigDecimal requestedAmount;
     @NotNull(message = "Сумма обязательна для заполнения")
     @DecimalMin(value = "20000", inclusive = true, message = "сумма должна быть не менее 20000")
     BigDecimal totalAmount;
     @Schema(description = "Срок кредита (в месяцах)", defaultValue = "12")
     @NotNull(message = "Срок обязателен для заполнения")
     @Min(value = 6, message = "Срок должен не менее 6 месяцев")
     Integer term;
     @NotNull(message = "Ежемесячный платеж обязателен для заполнения")
     BigDecimal monthlyPayment;
     BigDecimal rate;
     Boolean isInsuranceEnabled;
     Boolean isSalaryClient;

}
