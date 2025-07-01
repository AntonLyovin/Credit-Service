package org.example.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.model.PaymentSchedule;
import org.example.model.enumerated.CreditStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "credit")
@FieldDefaults(level = AccessLevel.PRIVATE)

public class Credit {
    @Id
    @Column(name = "credit_id")
    UUID creditId;

    @Column(name = "amount")
    BigDecimal amount;

    @Column(name = "term")
    Integer term;

    @Column(name = "monthly_payment")
    BigDecimal monthlyPayment;

    @Column(name = "rate")
    BigDecimal rate;

    @Column(name = "psk")
    BigDecimal psk;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_schedule")
    PaymentSchedule paymentSchedule;

    @Column(name = "insurance_enabled")
    Boolean insuranceEnabled;

    @Column(name = "salary_client")
    Boolean salaryClient;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_status")
    CreditStatus creditStatus;

}
