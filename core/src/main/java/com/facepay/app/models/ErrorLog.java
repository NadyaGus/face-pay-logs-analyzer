package com.facepay.app.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Модель критической ошибки транзакции для хранения в PostgreSQL
 * <p>
 * Содержит полную информацию оFailed-транзакциях с кодами ошибок,
 * которые фильтруются Spark Streaming и сохраняются для анализа.
 * </p>
 */
@Entity
@Table(name = "critical_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String status;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
