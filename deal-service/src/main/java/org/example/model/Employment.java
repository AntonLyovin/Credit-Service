package org.example.model;

import calculatorApp.calculator.model.enumerated.EmploymentStatusEnum;
import calculatorApp.calculator.model.enumerated.Position;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Employment implements Serializable {

    UUID employment;

    @Enumerated(EnumType.STRING)
    EmploymentStatusEnum status;

    String employerINN;

    BigDecimal salary;

    @Enumerated(EnumType.STRING)
    Position position;

    Integer workExperienceTotal;

    Integer getWorkExperienceCurrent;

}
