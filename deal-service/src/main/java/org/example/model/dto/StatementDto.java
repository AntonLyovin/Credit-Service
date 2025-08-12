package org.example.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.model.AppliedOffer;
import org.example.model.StatusHistory;
import org.example.model.enumerated.ApplicationStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatementDto {
    UUID statementId;
    ApplicationStatus status;
    LocalDate creationDate;
    AppliedOffer appliedOffer;
    LocalDate signDate;
    String sesCode;
    List<StatusHistory> statusHistory = new ArrayList<>();
}
