package calculatorApp.calculator.model.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CalcDto {
    UUID statementId;
    BigDecimal amount;
    Integer term;
    Boolean isInsuranceEnabled;
    Boolean isSalaryClient;
}