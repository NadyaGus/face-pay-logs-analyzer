package com.facepay.app.enums;

import lombok.Getter;

/**
 * Статус платежной транзакции
 * <p>
 * Описывает текущее состояние транзакции:
 * - SUCCESS: Успешно завершена
 * - PENDING: Ожидает подтверждения
 * - FAILED: Не удалась (может быть конкретная причина в errorCode)
 * </p>
 */
@Getter
public enum PaymentStatus {
    /**
     * Успешно завершена
     */
    SUCCESS("Успешно завершена"),
    
    /**
     * Не удалась (может быть конкретная причина в errorCode)
     */
    FAILED("Не удалась");
    
    private final String description;
    
    PaymentStatus(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
