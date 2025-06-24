package calculatorApp.calculator.model.dto;

import calculatorApp.calculator.model.enumerated.EmploymentStatusEnum;
import calculatorApp.calculator.model.enumerated.Position;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmploymentDto {
    @Schema(description = "Статус работы", defaultValue = "SELF_EMPLOYED")
    private EmploymentStatusEnum employmentStatus;
    @Schema(description = "ИНН", defaultValue = "6666666")
    private String employerINN;
    @Schema(description = "Зарплата", defaultValue = "100000")
    private BigDecimal salary;
    @Schema(description = "Должность", defaultValue = "DIRECTOR")
    private Position position;
    @Schema(description = "Общий стаж работы в месяцах", defaultValue = "20")
    private Integer workExperienceTotal;
    @Schema(description = "Текущий стаж работы в месяцах", defaultValue = "18")
    private Integer getWorkExperienceCurrent;
}
