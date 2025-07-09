package org.example.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.model.enumerated.Gender;
import org.example.model.enumerated.MaritalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"passportSeries","passportNumber","passportIssueDate"})
public class ScoringDataDto {
     @Schema(description = "Сумма кредита", defaultValue = "200000")
     @NotNull(message = "Сумма обязательна для заполнения")
     @DecimalMin(value = "20000", inclusive = true, message = "сумма должна быть не менее 20000")
     private BigDecimal amount;
     @Schema(description = "Срок кредита (в месяцах)", defaultValue = "12")
     @NotNull(message = "Срок обязателен для заполнения")
     @Min(value = 6, message = "Срок должен не менее 6 месяцев")
     private Integer term;
     @Schema(description = "Имя", defaultValue = "Ivan")
     @Size(min = 2, max = 30, message = "Имя должно быть от 2 до 30 латинских букв")
     @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Имя должно содержать только латинские буквы")
     private String firstName;
     @Schema(description = "Фамилия", defaultValue = "Ivanov")
     @Size(min = 2, max = 30, message = "Фамилия должна быть от 2 до 30 латинских букв")
     @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Фамилия должна содержать только латинские буквы")
     private String lastName;
     @Schema(description = "Отчество (при наличии)", defaultValue = "Ivanovich")
     @Size(min = 2, max = 30, message = "Отчество должно быть от 2 до 30 латинских букв")
     @Pattern(regexp = "^[A-Za-z]{2,30}$", message = "Отчество должно содержать только латинские буквы")
     private String middleName;
     @Schema(description = "Пол", defaultValue = "MALE")
     private Gender gender;
     @Schema(description = "Дата рождения (гггг-мм-дд)")
     private LocalDate birthdate;
     @Schema(description="Серия паспорта - 4 цифры", defaultValue = "4444")
     @Pattern(regexp="^\\d{4}$", message="Серия паспорта должна состоять из 4 цифр")
     private String passportSeries;
     @Schema(description="Номер паспорта - 6 цифр", defaultValue = "666666")
     @Pattern(regexp="^\\d{6}$", message="Номер паспорта должен состоять из 6 цифр")
     private String passportNumber;
     @Schema(description = "Дата выдачи (гггг-мм-дд)")
     private LocalDate passportIssueDate;
     @Schema(description = "Кем выдан", defaultValue = "Issuing department")
     private String passportIssueBranch;
     @Schema(description = "Семейное положение", defaultValue = "SINGLE")
     private MaritalStatus maritalStatus;
     private Integer dependentAmount;
     private EmploymentDto employment;
     private String accountNumber;
     private Boolean isInsuranceEnabled;
     private Boolean isSalaryClient;

}
