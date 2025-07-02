package org.example.model.dto;

import calculatorApp.calculator.model.dto.EmploymentDto;
import calculatorApp.calculator.model.enumerated.Gender;
import calculatorApp.calculator.model.enumerated.MartialStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
@Data
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class FinishRegistrationRequestDto {
     Gender gender;
     MartialStatus maritalStatus;
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
