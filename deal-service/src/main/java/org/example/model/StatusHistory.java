package org.example.model;


import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.model.enumerated.ApplicationStatus;
import org.example.model.enumerated.ChangeType;

import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class StatusHistory implements Serializable {

    @Enumerated(EnumType.STRING)
    ApplicationStatus status;
    LocalDate time;
    @Enumerated(EnumType.STRING)
    ChangeType changeType;

}
