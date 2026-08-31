package com.facepay.app.models;

import jakarta.persistence.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для JPA-сущности {@link ErrorLog}.
 * <p>
 * Проверяют паттерн Builder, Lombok-генерацию equals/hashCode/toString,
 * а также JPA-аннотации.
 * </p>
 */
@DisplayName("ErrorLog entity")
class ErrorLogTest {

    private static final Instant NOW = Instant.parse("2026-08-31T10:00:00Z");

    private ErrorLog createSampleErrorLog() {
        return ErrorLog.builder()
                .transactionId("txn-123")
                .timestamp(NOW)
                .accountId("ACC-12345678")
                .amount(new BigDecimal("1500.50"))
                .currency("RUB")
                .status("FAILED")
                .merchantId("MERCH-001")
                .errorCode("INSUFFICIENT_FUNDS")
                .errorMessage("Недостаточно средств")
                .metadata("{\"device\":\"terminal-01\"}")
                .createdAt(NOW)
                .build();
    }

    // ==================== Builder ====================

    @Nested
    @DisplayName("Builder - создание сущности")
    class Builder {

        @Test
        @DisplayName("Создание со всеми полями успешно")
        void builder_withAllFields_buildsSuccessfully() {
            // when
            ErrorLog errorLog = createSampleErrorLog();

            // then
            assertThat(errorLog).isNotNull();
            assertThat(errorLog.getTransactionId()).isEqualTo("txn-123");
            assertThat(errorLog.getAccountId()).isEqualTo("ACC-12345678");
            assertThat(errorLog.getAmount()).isEqualByComparingTo(new BigDecimal("1500.50"));
            assertThat(errorLog.getCurrency()).isEqualTo("RUB");
            assertThat(errorLog.getStatus()).isEqualTo("FAILED");
            assertThat(errorLog.getMerchantId()).isEqualTo("MERCH-001");
            assertThat(errorLog.getErrorCode()).isEqualTo("INSUFFICIENT_FUNDS");
            assertThat(errorLog.getErrorMessage()).isEqualTo("Недостаточно средств");
            assertThat(errorLog.getMetadata()).isEqualTo("{\"device\":\"terminal-01\"}");
            assertThat(errorLog.getCreatedAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("Создание с минимальным набором полей")
        void builder_withMinimalFields_buildsSuccessfully() {
            // when
            ErrorLog errorLog = ErrorLog.builder()
                    .transactionId("txn-456")
                    .timestamp(NOW)
                    .accountId("ACC-87654321")
                    .amount(BigDecimal.ONE)
                    .currency("USD")
                    .status("FAILED")
                    .createdAt(NOW)
                    .build();

            // then
            assertThat(errorLog).isNotNull();
            assertThat(errorLog.getTransactionId()).isEqualTo("txn-456");
            assertThat(errorLog.getMerchantId()).isNull();
            assertThat(errorLog.getErrorCode()).isNull();
            assertThat(errorLog.getErrorMessage()).isNull();
            assertThat(errorLog.getMetadata()).isNull();
        }

        @Test
        @DisplayName("Builder возвращает новый экземпляр (иммутабельность)")
        void builder_returnsNewInstance() {
            // when
            ErrorLog log1 = createSampleErrorLog();
            ErrorLog log2 = ErrorLog.builder()
                    .transactionId("txn-999")
                    .timestamp(NOW)
                    .accountId("ACC-00000000")
                    .amount(BigDecimal.TEN)
                    .currency("EUR")
                    .status("FAILED")
                    .createdAt(NOW)
                    .build();

            // then
            assertThat(log1.getTransactionId()).isEqualTo("txn-123");
            assertThat(log2.getTransactionId()).isEqualTo("txn-999");
        }
    }

    // ==================== equals() and hashCode() ====================

    @Nested
    @DisplayName("equals() и hashCode() - Lombok генерация")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Два объекта с одинаковыми данными равны")
        void equalData_returnsTrue() {
            // given
            ErrorLog log1 = ErrorLog.builder()
                    .transactionId("txn-equal")
                    .timestamp(NOW)
                    .accountId("ACC-EQUAL")
                    .amount(new BigDecimal("100"))
                    .currency("RUB")
                    .status("FAILED")
                    .createdAt(NOW)
                    .build();

            ErrorLog log2 = ErrorLog.builder()
                    .transactionId("txn-equal")
                    .timestamp(NOW)
                    .accountId("ACC-EQUAL")
                    .amount(new BigDecimal("100"))
                    .currency("RUB")
                    .status("FAILED")
                    .createdAt(NOW)
                    .build();

            // then
            assertThat(log1).isEqualTo(log2);
            assertThat(log2).isEqualTo(log1);
        }

        @Test
        @DisplayName("Объект равен самому себе")
        void sameObject_returnsTrue() {
            // given
            ErrorLog log = createSampleErrorLog();

            // then
            assertThat(log).isEqualTo(log);
        }

        @Test
        @DisplayName("Объект не равен null")
        void notEqualToNull_returnsFalse() {
            // given
            ErrorLog log = createSampleErrorLog();

            // then
            assertThat(log).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Объект не равен объекту другого типа")
        void notEqualToDifferentType_returnsFalse() {
            // given
            ErrorLog log = createSampleErrorLog();

            // then
            assertThat(log).isNotEqualTo("string");
        }

        @Test
        @DisplayName("Объекты с разными данными не равны")
        void differentData_returnsFalse() {
            // given
            ErrorLog log1 = ErrorLog.builder()
                    .transactionId("txn-1")
                    .timestamp(NOW)
                    .accountId("ACC-1")
                    .amount(BigDecimal.ONE)
                    .currency("RUB")
                    .status("FAILED")
                    .createdAt(NOW)
                    .build();

            ErrorLog log2 = ErrorLog.builder()
                    .transactionId("txn-2")
                    .timestamp(NOW)
                    .accountId("ACC-1")
                    .amount(BigDecimal.ONE)
                    .currency("RUB")
                    .status("FAILED")
                    .createdAt(NOW)
                    .build();

            // then
            assertThat(log1).isNotEqualTo(log2);
        }

        @Test
        @DisplayName("hashCode одинаков для равных объектов")
        void hashCode_sameForEqualObjects() {
            // given
            ErrorLog log1 = ErrorLog.builder()
                    .transactionId("txn-hash")
                    .timestamp(NOW)
                    .accountId("ACC-HASH")
                    .amount(BigDecimal.valueOf(50))
                    .currency("RUB")
                    .status("FAILED")
                    .createdAt(NOW)
                    .build();

            ErrorLog log2 = ErrorLog.builder()
                    .transactionId("txn-hash")
                    .timestamp(NOW)
                    .accountId("ACC-HASH")
                    .amount(BigDecimal.valueOf(50))
                    .currency("RUB")
                    .status("FAILED")
                    .createdAt(NOW)
                    .build();

            // then
            assertThat(log1.hashCode()).isEqualTo(log2.hashCode());
        }
    }

    // ==================== toString() ====================

    @Nested
    @DisplayName("toString() - строковое представление")
    class ToString {

        @Test
        @DisplayName("Содержит ключевые поля")
        void toString_containsKeyFields() {
            // when
            String result = createSampleErrorLog().toString();

            // then
            assertThat(result).contains("ErrorLog");
            assertThat(result).contains("txn-123");
            assertThat(result).contains("ACC-12345678");
            assertThat(result).contains("1500.50");
            assertThat(result).contains("INSUFFICIENT_FUNDS");
        }
    }

    // ==================== JPA Annotations ====================

    @Nested
    @DisplayName("JPA-аннотации")
    class JpaAnnotations {

        @Test
        @DisplayName("Аннотация @Entity присутствует")
        void entityAnnotationPresent() {
            assertThat(ErrorLog.class.isAnnotationPresent(jakarta.persistence.Entity.class))
                    .isTrue();
        }

        @Test
        @DisplayName("Аннотация @Table имеет правильное имя")
        void tableAnnotationHasCorrectName() {
            // then
            Table tableAnnotation = ErrorLog.class.getAnnotation(Table.class);
            assertThat(tableAnnotation).isNotNull();
            assertThat(tableAnnotation.name()).isEqualTo("critical_logs");
        }

        @Test
        @DisplayName("Все поля инициализируются через Builder")
        void allFieldsSettableViaBuilder() {
            // when
            ErrorLog errorLog = ErrorLog.builder()
                    .id(1L)
                    .transactionId("txn-all-fields")
                    .timestamp(NOW)
                    .accountId("ACC-ALL")
                    .amount(BigDecimal.valueOf(999))
                    .currency("GBP")
                    .status("FAILED")
                    .merchantId("MERCH-ALL")
                    .errorCode("TEST")
                    .errorMessage("Test error")
                    .metadata("{}")
                    .createdAt(NOW)
                    .build();

            // then
            assertThat(errorLog.getId()).isEqualTo(1L);
            assertThat(errorLog.getTransactionId()).isEqualTo("txn-all-fields");
            assertThat(errorLog.getTimestamp()).isEqualTo(NOW);
            assertThat(errorLog.getAccountId()).isEqualTo("ACC-ALL");
            assertThat(errorLog.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(999));
            assertThat(errorLog.getCurrency()).isEqualTo("GBP");
            assertThat(errorLog.getStatus()).isEqualTo("FAILED");
            assertThat(errorLog.getMerchantId()).isEqualTo("MERCH-ALL");
            assertThat(errorLog.getErrorCode()).isEqualTo("TEST");
            assertThat(errorLog.getErrorMessage()).isEqualTo("Test error");
            assertThat(errorLog.getMetadata()).isEqualTo("{}");
            assertThat(errorLog.getCreatedAt()).isEqualTo(NOW);
        }
    }

    // ==================== No-args Constructor ====================

    @Nested
    @DisplayName("No-args constructor - конструктор по умолчанию")
    class NoArgsConstructor {

        @Test
        @DisplayName("Создание через конструктор по умолчанию")
        void noArgsConstructor_createsEmptyInstance() {
            // when
            ErrorLog errorLog = new ErrorLog();

            // then
            assertThat(errorLog).isNotNull();
            assertThat(errorLog.getId()).isNull();
            assertThat(errorLog.getTransactionId()).isNull();
            assertThat(errorLog.getTimestamp()).isNull();
            assertThat(errorLog.getAccountId()).isNull();
            assertThat(errorLog.getAmount()).isNull();
            assertThat(errorLog.getCurrency()).isNull();
            assertThat(errorLog.getStatus()).isNull();
            assertThat(errorLog.getMerchantId()).isNull();
            assertThat(errorLog.getErrorCode()).isNull();
            assertThat(errorLog.getErrorMessage()).isNull();
            assertThat(errorLog.getMetadata()).isNull();
            assertThat(errorLog.getCreatedAt()).isNull();
        }
    }
}
