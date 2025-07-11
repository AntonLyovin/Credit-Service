package org.example.model.dto;


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
     String passportSeries;
     String passportNumber;
     LocalDate passportIssueDate;
     String passportIssueBranch;
     String accountNumber;
     Boolean isInsuranceEnabled;
     Boolean isSalaryClient;
}
