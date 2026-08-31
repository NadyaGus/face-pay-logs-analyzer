package com.facepay.app.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для enum {@link PaymentStatus}.
 * <p>
 * Проверяют описания статусов и количество констант.
 * </p>
 */
@DisplayName("PaymentStatus enum")
class PaymentStatusTest {

    // ==================== values() ====================

    @Nested
    @DisplayName("values() - константы enum")
    class Values {

        @Test
        @DisplayName("Возвращает ровно 2 константы")
        void values_returnsExactlyTwoConstants() {
            assertThat(PaymentStatus.values()).hasSize(2);
        }

        @Test
        @DisplayName("Содержит SUCCESS и FAILED")
        void values_containsExpectedConstants() {
            assertThat(PaymentStatus.values()).contains(
                    PaymentStatus.SUCCESS,
                    PaymentStatus.FAILED
            );
        }
    }

    // ==================== toString() ====================

    @Nested
    @DisplayName("toString() - строковые представления")
    class ToString {

        @Test
        @DisplayName("SUCCESS возвращает 'Успешно завершена'")
        void toString_success_returnsDescription() {
            assertThat(PaymentStatus.SUCCESS.toString()).isEqualTo("Успешно завершена");
        }

        @Test
        @DisplayName("FAILED возвращает 'Не удалась'")
        void toString_failed_returnsDescription() {
            assertThat(PaymentStatus.FAILED.toString()).isEqualTo("Не удалась");
        }
    }

    // ==================== getDescription() ====================

    @Nested
    @DisplayName("getDescription() - геттеры описаний")
    class GetDescription {

        @Test
        @DisplayName("getDescription() возвращает корректные значения")
        void getDescription_returnsCorrectValues() {
            assertThat(PaymentStatus.SUCCESS.getDescription()).isEqualTo("Успешно завершена");
            assertThat(PaymentStatus.FAILED.getDescription()).isEqualTo("Не удалась");
        }

        @Test
        @DisplayName("Описания не пустые и не null")
        void descriptions_areNotBlank() {
            for (PaymentStatus status : PaymentStatus.values()) {
                assertThat(status.getDescription())
                        .isNotNull()
                        .isNotBlank();
            }
        }
    }
}
