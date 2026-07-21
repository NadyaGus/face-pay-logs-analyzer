package com.facepay.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Модель платежной транзакции для передачи через Kafka
 * <p>
 * Используется для обработки платежей через FaceID.
 * </p>
 *
 * @param transactionId    Уникальный идентификатор транзакции (UUID)
 * @param timestamp        Время события (UTC)
 * @param paymentMethod    Метод оплаты (FACEID, NFC, CARD, MOBILE, CASH)
 * @param accountId        ID счета плательщика
 * @param amount           Сумма транзакции
 * @param currency         Валюта (RUB, USD, EUR)
 * @param status           Статус транзакции (SUCCESS, FAILED, PENDING, CANCELLED)
 * @param merchantId       ID получателя (магазина/терминала)
 * @param description      Описание транзакции
 * @param metadata         Дополнительные данные в JSON формате (опционально)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {
    private String transactionId;
    private Instant timestamp;
    private String paymentMethod;
    private String accountId;
    private Double amount;
    private String currency;
    private String status;
    private String merchantId;
    private String description;
    private String metadata;
}
