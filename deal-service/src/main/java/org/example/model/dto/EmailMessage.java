package org.example.model.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.model.AppliedOffer;
import org.example.model.Passport;
import org.example.model.enumerated.Theme;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailMessage {
    String lastName;
    String firstName;
    String middleName;
    LocalDate birthDate;
    Passport passport;
    String address;
    Theme theme;
    String statementId;
    AppliedOffer appliedOffer;
    CreditDto credit;
    String sesCode;
    String text;
}
