package org.example.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.model.enumerated.EmploymentStatusEnum;
import org.example.model.enumerated.Position;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Data
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
