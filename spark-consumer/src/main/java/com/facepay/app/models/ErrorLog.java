package com.facepay.app.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Модель ошибки транзакции для хранения в PostgreSQL
 */
@Entity
@Table(name = "critical_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;

    private Instant timestamp;

    private String accountId;

    private BigDecimal amount;

    private String currency;

    private String status;

    private String merchantId;

    private String errorCode;

    private String errorMessage;

    private String metadata;

    @Column(name = "created_at")
    private Instant createdAt;

}
