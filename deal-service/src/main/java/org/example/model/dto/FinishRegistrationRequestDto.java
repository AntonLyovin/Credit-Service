package org.example.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.model.enumerated.Gender;
import org.example.model.enumerated.MaritalStatus;

import java.time.LocalDate;
@Data
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinishRegistrationRequestDto {
     Gender gender;
     MaritalStatus maritalStatus;
     Integer dependentAmount;
     EmploymentDto employment;
     @Schema(description = "Серия паспорта", defaultValue = "4444")
     String passportSeries;
     @Schema(description = "Номер паспорта", defaultValue = "666666")
     String passportNumber;
     LocalDate passportIssueDate;
     @Schema(description = "Место выдачи паспорта", defaultValue = "UFMS")
     String passportIssueBranch;
     String accountNumber;
     Boolean isInsuranceEnabled;
     Boolean isSalaryClient;
}
