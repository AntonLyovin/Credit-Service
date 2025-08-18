package org.example.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.model.enumerated.EmploymentStatusEnum;
import org.example.model.enumerated.Position;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class EmploymentDto {
    @Schema(description = "Статус работы", defaultValue = "SELF_EMPLOYED")
     EmploymentStatusEnum employmentStatus;
    @Schema(description = "ИНН", defaultValue = "6666666")
     String employerINN;
    @Schema(description = "Зарплата", defaultValue = "100000")
     BigDecimal salary;
    @Schema(description = "Должность", defaultValue = "WORKER")
     Position position;
    @Schema(description = "Общий стаж работы в месяцах", defaultValue = "20")
     Integer workExperienceTotal;
    @Schema(description = "Текущий стаж работы в месяцах", defaultValue = "18")
     Integer getWorkExperienceCurrent;
}
