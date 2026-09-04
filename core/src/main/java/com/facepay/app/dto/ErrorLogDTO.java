package com.facepay.app.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO для создания логов ошибок транзакций
 * <p>
 * Используется для входящих запросов от Spark Consumer.
 * Отделён от ErrorLog Entity для защиты API-контракта.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorLogDTO {

    @NotBlank(message = "transactionId is required")
    private String transactionId;

    @NotNull(message = "timestamp is required")
    private Instant timestamp;

    @NotBlank(message = "accountId is required")
    private String accountId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    private String currency;

    @NotBlank(message = "status is required")
    private String status;

    @NotBlank(message = "merchantId is required")
    private String merchantId;

    private String errorCode;

    private String errorMessage;

    private String metadata;
}
