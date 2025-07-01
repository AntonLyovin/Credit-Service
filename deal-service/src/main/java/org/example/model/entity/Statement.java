package org.example.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.model.AppliedOffer;
import org.example.model.StatusHistory;
import org.example.model.enumerated.ApplicationStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "statement")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Statement {

    @Id
    @Column(name = "statement_id")
    UUID statementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", referencedColumnName = "client_id")
    Client clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_id", referencedColumnName = "credit_id")
    Credit creditId;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status")
    ApplicationStatus status;

    @Column(name = "creation_date")
    LocalDate creationDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applied_offer")
    AppliedOffer appliedOffer;

    @Column(name = "sign_date")
    LocalDate signDate;

    @Column(name = "ses_code")
    String sesCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "status_history", columnDefinition = "jsonb")
    private List<StatusHistory> statusHistory = new ArrayList<>();


    public void setClient(Client client) {
    }
}
