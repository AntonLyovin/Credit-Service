package org.example.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.model.enumerated.EmploymentStatusEnum;
import org.example.model.enumerated.Position;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class EmploymentDto {
    @Schema(description = "Статус работы", defaultValue = "SELF_EMPLOYED")
    private EmploymentStatusEnum employmentStatus;
    @Schema(description = "ИНН", defaultValue = "6666666")
    private String employerINN;
    @Schema(description = "Зарплата", defaultValue = "100000")
    private BigDecimal salary;
    @Schema(description = "Должность", defaultValue = "WORKER")
    private Position position;
    @Schema(description = "Общий стаж работы в месяцах", defaultValue = "20")
    private Integer workExperienceTotal;
    @Schema(description = "Текущий стаж работы в месяцах", defaultValue = "18")
    private Integer getWorkExperienceCurrent;
}
