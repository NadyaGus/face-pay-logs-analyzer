package com.facepay.app.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для модели {@link PaymentTransaction}.
 * <p>
 * Проверяют конструкторы, Lombok-генерацию equals/hashCode/toString,
 * а также геттеры/сеттеры для всех полей.
 * </p>
 */
@DisplayName("PaymentTransaction model")
class PaymentTransactionTest {

    private static final Instant NOW = Instant.parse("2026-08-31T10:00:00Z");

    private PaymentTransaction createSampleTransaction() {
        return new PaymentTransaction(
                "txn-sample",
                NOW,
                "ACC-11111111",
                new BigDecimal("2500.00"),
                "RUB",
                com.facepay.app.enums.PaymentStatus.SUCCESS,
                "MERCH-001",
                null,
                null,
                null
        );
    }

    // ==================== All-args Constructor ====================

    @Nested
    @DisplayName("All-args constructor - конструктор со всеми параметрами")
    class AllArgsConstructor {

        @Test
        @DisplayName("Создание транзакции со всеми полями")
        void constructor_withAllFields_setsAllFields() {
            // when
            PaymentTransaction transaction = createSampleTransaction();

            // then
            assertThat(transaction.getTransactionId()).isEqualTo("txn-sample");
            assertThat(transaction.getTimestamp()).isEqualTo(NOW);
            assertThat(transaction.getAccountId()).isEqualTo("ACC-11111111");
            assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("2500.00"));
            assertThat(transaction.getCurrency()).isEqualTo("RUB");
            assertThat(transaction.getStatus()).isEqualTo(com.facepay.app.enums.PaymentStatus.SUCCESS);
            assertThat(transaction.getMerchantId()).isEqualTo("MERCH-001");
            assertThat(transaction.getErrorCode()).isNull();
            assertThat(transaction.getErrorMessage()).isNull();
            assertThat(transaction.getMetadata()).isNull();
        }

        @Test
        @DisplayName("Создание FAILED-транзакции с кодом ошибки")
        void constructor_withErrorCode_setsFields() {
            // when
            PaymentTransaction transaction = new PaymentTransaction(
                    "txn-failed",
                    NOW,
                    "ACC-22222222",
                    new BigDecimal("100.50"),
                    "USD",
                    com.facepay.app.enums.PaymentStatus.FAILED,
                    "MERCH-002",
                    com.facepay.app.enums.FaceIDError.INSUFFICIENT_FUNDS,
                    "Недостаточно средств",
                    "{\"retry\":true}"
            );

            // then
            assertThat(transaction.getTransactionId()).isEqualTo("txn-failed");
            assertThat(transaction.getAccountId()).isEqualTo("ACC-22222222");
            assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("100.50"));
            assertThat(transaction.getCurrency()).isEqualTo("USD");
            assertThat(transaction.getStatus()).isEqualTo(com.facepay.app.enums.PaymentStatus.FAILED);
            assertThat(transaction.getErrorCode()).isEqualTo(com.facepay.app.enums.FaceIDError.INSUFFICIENT_FUNDS);
            assertThat(transaction.getErrorMessage()).isEqualTo("Недостаточно средств");
            assertThat(transaction.getMetadata()).isEqualTo("{\"retry\":true}");
        }
    }

    // ==================== No-args Constructor ====================

    @Nested
    @DisplayName("No-args constructor - конструктор по умолчанию")
    class NoArgsConstructor {

        @Test
        @DisplayName("Создание пустой транзакции")
        void noArgsConstructor_createsEmptyInstance() {
            // when
            PaymentTransaction transaction = new PaymentTransaction();

            // then
            assertThat(transaction).isNotNull();
            assertThat(transaction.getTransactionId()).isNull();
            assertThat(transaction.getTimestamp()).isNull();
            assertThat(transaction.getAccountId()).isNull();
            assertThat(transaction.getAmount()).isNull();
            assertThat(transaction.getCurrency()).isNull();
            assertThat(transaction.getStatus()).isNull();
            assertThat(transaction.getMerchantId()).isNull();
            assertThat(transaction.getErrorCode()).isNull();
            assertThat(transaction.getErrorMessage()).isNull();
            assertThat(transaction.getMetadata()).isNull();
        }
    }

    // ==================== Equals and HashCode ====================

    @Nested
    @DisplayName("equals() и hashCode() - Lombok генерация")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Два объекта с одинаковыми данными равны")
        void equalData_returnsTrue() {
            // given
            PaymentTransaction t1 = new PaymentTransaction(
                    "txn-equal", NOW, "ACC-EQ", BigDecimal.ONE, "RUB",
                    com.facepay.app.enums.PaymentStatus.SUCCESS, "MERCH-1", null, null, null
            );
            PaymentTransaction t2 = new PaymentTransaction(
                    "txn-equal", NOW, "ACC-EQ", BigDecimal.ONE, "RUB",
                    com.facepay.app.enums.PaymentStatus.SUCCESS, "MERCH-1", null, null, null
            );

            // then
            assertThat(t1).isEqualTo(t2);
            assertThat(t2).isEqualTo(t1);
        }

        @Test
        @DisplayName("Объект равен самому себе")
        void sameObject_returnsTrue() {
            // given
            PaymentTransaction transaction = createSampleTransaction();

            // then
            assertThat(transaction).isEqualTo(transaction);
        }

        @Test
        @DisplayName("Объект не равен null")
        void notEqualToNull_returnsFalse() {
            // given
            PaymentTransaction transaction = createSampleTransaction();

            // then
            assertThat(transaction).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Объект не равен объекту другого типа")
        void notEqualToDifferentType_returnsFalse() {
            // given
            PaymentTransaction transaction = createSampleTransaction();

            // then
            assertThat(transaction).isNotEqualTo("string");
        }

        @Test
        @DisplayName("Объекты с разными transactionId не равны")
        void differentTransactionId_returnsFalse() {
            // given
            PaymentTransaction t1 = new PaymentTransaction(
                    "txn-1", NOW, "ACC-1", BigDecimal.ONE, "RUB",
                    com.facepay.app.enums.PaymentStatus.SUCCESS, "MERCH-1", null, null, null
            );
            PaymentTransaction t2 = new PaymentTransaction(
                    "txn-2", NOW, "ACC-1", BigDecimal.ONE, "RUB",
                    com.facepay.app.enums.PaymentStatus.SUCCESS, "MERCH-1", null, null, null
            );

            // then
            assertThat(t1).isNotEqualTo(t2);
        }

        @Test
        @DisplayName("hashCode одинаков для равных объектов")
        void hashCode_sameForEqualObjects() {
            // given
            PaymentTransaction t1 = new PaymentTransaction(
                    "txn-hash", NOW, "ACC-H", BigDecimal.TEN, "USD",
                    com.facepay.app.enums.PaymentStatus.FAILED, "MERCH-2", null, null, null
            );
            PaymentTransaction t2 = new PaymentTransaction(
                    "txn-hash", NOW, "ACC-H", BigDecimal.TEN, "USD",
                    com.facepay.app.enums.PaymentStatus.FAILED, "MERCH-2", null, null, null
            );

            // then
            assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
        }
    }

    // ==================== ToString ====================

    @Nested
    @DisplayName("toString() - строковое представление")
    class ToString {

        @Test
        @DisplayName("Содержит ключевые поля транзакции")
        void toString_containsKeyFields() {
            // when
            String result = createSampleTransaction().toString();

            // then
            assertThat(result).contains("PaymentTransaction");
            assertThat(result).contains("txn-sample");
            assertThat(result).contains("ACC-11111111");
            assertThat(result).contains("2500.00");
            assertThat(result).contains("RUB");
            assertThat(result).contains("Успешно завершена");
        }
    }

    // ==================== Setters ====================

    @Nested
    @DisplayName("Setters - изменение полей")
    class Setters {

        @Test
        @DisplayName("Изменение transactionId")
        void setTransactionId_works() {
            // given
            PaymentTransaction transaction = new PaymentTransaction();

            // when
            transaction.setTransactionId("txn-updated");

            // then
            assertThat(transaction.getTransactionId()).isEqualTo("txn-updated");
        }

        @Test
        @DisplayName("Изменение amount")
        void setAmount_works() {
            // given
            PaymentTransaction transaction = new PaymentTransaction();

            // when
            transaction.setAmount(new BigDecimal("9999.99"));

            // then
            assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("9999.99"));
        }

        @Test
        @DisplayName("Изменение status")
        void setStatus_works() {
            // given
            PaymentTransaction transaction = new PaymentTransaction();

            // when
            transaction.setStatus(com.facepay.app.enums.PaymentStatus.FAILED);

            // then
            assertThat(transaction.getStatus()).isEqualTo(com.facepay.app.enums.PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("Изменение errorCode")
        void setErrorCode_works() {
            // given
            PaymentTransaction transaction = new PaymentTransaction();

            // when
            transaction.setErrorCode(com.facepay.app.enums.FaceIDError.SPOOFING_ATTACK_DETECTED);

            // then
            assertThat(transaction.getErrorCode()).isEqualTo(com.facepay.app.enums.FaceIDError.SPOOFING_ATTACK_DETECTED);
        }

        @Test
        @DisplayName("Цепочечное изменение полей")
        void multipleSetters_works() {
            // given
            PaymentTransaction transaction = new PaymentTransaction();

            // when
            transaction.setTransactionId("txn-chain");
            transaction.setAccountId("ACC-CHAIN");
            transaction.setAmount(BigDecimal.valueOf(500));
            transaction.setStatus(com.facepay.app.enums.PaymentStatus.SUCCESS);

            // then
            assertThat(transaction.getTransactionId()).isEqualTo("txn-chain");
            assertThat(transaction.getAccountId()).isEqualTo("ACC-CHAIN");
            assertThat(transaction.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500));
            assertThat(transaction.getStatus()).isEqualTo(com.facepay.app.enums.PaymentStatus.SUCCESS);
        }
    }

    // ==================== Field Coverage ====================

    @Nested
    @DisplayName("Покрытие всех полей")
    class FieldCoverage {

        @Test
        @DisplayName("Все 10 полей доступны для чтения и записи")
        void allFieldsAccessible() {
            // given
            PaymentTransaction transaction = new PaymentTransaction();

            // when - устанавливаем все поля
            transaction.setTransactionId("id");
            transaction.setTimestamp(NOW);
            transaction.setAccountId("acc");
            transaction.setAmount(BigDecimal.ONE);
            transaction.setCurrency("RUB");
            transaction.setStatus(com.facepay.app.enums.PaymentStatus.SUCCESS);
            transaction.setMerchantId("merch");
            transaction.setErrorCode(com.facepay.app.enums.FaceIDError.FACE_NOT_RECOGNIZED);
            transaction.setErrorMessage("error");
            transaction.setMetadata("meta");

            // then - проверяем все геттеры
            assertThat(transaction.getTransactionId()).isEqualTo("id");
            assertThat(transaction.getTimestamp()).isEqualTo(NOW);
            assertThat(transaction.getAccountId()).isEqualTo("acc");
            assertThat(transaction.getAmount()).isEqualByComparingTo(BigDecimal.ONE);
            assertThat(transaction.getCurrency()).isEqualTo("RUB");
            assertThat(transaction.getStatus()).isEqualTo(com.facepay.app.enums.PaymentStatus.SUCCESS);
            assertThat(transaction.getMerchantId()).isEqualTo("merch");
            assertThat(transaction.getErrorCode()).isEqualTo(com.facepay.app.enums.FaceIDError.FACE_NOT_RECOGNIZED);
            assertThat(transaction.getErrorMessage()).isEqualTo("error");
            assertThat(transaction.getMetadata()).isEqualTo("meta");
        }
    }
}
