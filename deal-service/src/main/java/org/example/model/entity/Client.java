package org.example.model.entity;

import calculatorApp.calculator.model.enumerated.Gender;
import calculatorApp.calculator.model.enumerated.MartialStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.model.Employment;
import org.example.model.Passport;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Table(name = "clients")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    @Id
    @Column(name = "client_id")
    UUID clientId;

    @Column(name = "last_name")
    String lastName;

    @Column(name = "first_name")
    String firstName;

    @Column(name = "middle_name")
    String middleName;

    @Column(name = "birth_date")
    LocalDate birthDate;

    @Column(name = "email")
    String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "martial_status")
    MartialStatus martialStatus;

    @Column(name = "dependent_amount")
    Integer dependentAmount;

    @JdbcTypeCode(SqlTypes.JSON)
    Passport passport;

    @JdbcTypeCode(SqlTypes.JSON)
    Employment employment;

    @Column(name = "account_number")
    String accountNumber;

}
