package com.facepay.app.models;

import com.facepay.app.enums.FaceIDError;
import com.facepay.app.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Модель платежной транзакции для FaceID оплаты
 * <p>
 * Учитывает специфику биометрической аутентификации:
 * - Ошибки биометрии (не распознано, мошенничество, плохое качество)
 * - Бизнес-ошибки (недостаточно средств, отключена услуга, лимиты)
 * - Технические ошибки (сбой камеры, таймаут, потеря связи)
 * </p>
 *
 * @param transactionId    Уникальный идентификатор транзакции (UUID)
 * @param timestamp        Время события (UTC)
 * @param accountId        ID счета плательщика
 * @param amount           Сумма транзакции (BigDecimal для точных финансовых расчетов)
 * @param currency         Валюта (RUB, USD, EUR)
 * @param status           Статус транзакции (SUCCESS, FAILED, PENDING)
 * @param merchantId       ID получателя (магазина/терминала)
 * @param errorCode        Код ошибки (если есть) - см. enum FaceIDError
 * @param errorMessage     Описание ошибки
 * @param metadata         Дополнительные данные в JSON формате
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {
    private String transactionId;
    private Instant timestamp;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String merchantId;
    private FaceIDError errorCode;
    private String errorMessage;
    private String metadata;
}
