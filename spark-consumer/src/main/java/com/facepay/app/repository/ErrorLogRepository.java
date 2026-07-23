package com.facepay.app.repository;

import com.facepay.app.models.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Репозиторий для работы с ошибками транзакций
 */
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

    /**
     * Найти последние ошибки за последний час
     */
    List<ErrorLog> findByTimestampAfter(Instant timestamp);

    /**
     * Найти ошибки по коду ошибки
     */
    List<ErrorLog> findByErrorCode(String errorCode);

    /**
     * Найти ошибки по ID аккаунта
     */
    List<ErrorLog> findByAccountId(String accountId);
}
