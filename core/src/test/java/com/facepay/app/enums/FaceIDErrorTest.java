package com.facepay.app.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для enum {@link FaceIDError}.
 * <p>
 * Проверяют фабричный метод fromCode(), описания ошибок и поведение при невалидных входных данных.
 * </p>
 */
@DisplayName("FaceIDError enum")
class FaceIDErrorTest {

    // ==================== fromCode() ====================

    @Nested
    @DisplayName("fromCode() - валидные коды")
    class FromCodeValid {

        @Test
        @DisplayName("Возвращает константу по полному коду ошибки")
        void fromCode_withValidCode_returnsEnumConstant() {
            // when
            FaceIDError result = FaceIDError.fromCode("FACE_NOT_RECOGNIZED");

            // then
            assertThat(result).isEqualTo(FaceIDError.FACE_NOT_RECOGNIZED);
        }

        @Test
        @DisplayName("Возвращает все 10 констант по их кодам")
        void fromCode_allCodes_returnsAllConstants() {
            // then
            assertThat(FaceIDError.fromCode("FACE_NOT_RECOGNIZED")).isEqualTo(FaceIDError.FACE_NOT_RECOGNIZED);
            assertThat(FaceIDError.fromCode("SPOOFING_ATTACK_DETECTED")).isEqualTo(FaceIDError.SPOOFING_ATTACK_DETECTED);
            assertThat(FaceIDError.fromCode("POOR_LIGHTING_OR_QUALITY")).isEqualTo(FaceIDError.POOR_LIGHTING_OR_QUALITY);
            assertThat(FaceIDError.fromCode("FACE_OCCLUSION")).isEqualTo(FaceIDError.FACE_OCCLUSION);
            assertThat(FaceIDError.fromCode("INSUFFICIENT_FUNDS")).isEqualTo(FaceIDError.INSUFFICIENT_FUNDS);
            assertThat(FaceIDError.fromCode("BIOMETRIC_SERVICE_DISABLED")).isEqualTo(FaceIDError.BIOMETRIC_SERVICE_DISABLED);
            assertThat(FaceIDError.fromCode("LIMIT_EXCEEDED")).isEqualTo(FaceIDError.LIMIT_EXCEEDED);
            assertThat(FaceIDError.fromCode("TERMINAL_CAMERA_HARDWARE_FAULT")).isEqualTo(FaceIDError.TERMINAL_CAMERA_HARDWARE_FAULT);
            assertThat(FaceIDError.fromCode("BIOMETRIC_ENGINE_TIMEOUT")).isEqualTo(FaceIDError.BIOMETRIC_ENGINE_TIMEOUT);
            assertThat(FaceIDError.fromCode("NETWORK_DISCONNECTION")).isEqualTo(FaceIDError.NETWORK_DISCONNECTION);
        }

        @Test
        @DisplayName("Коды ошибок регистрозависимы")
        void fromCode_caseSensitive_returnsNullForWrongCase() {
            // when/then
            assertThat(FaceIDError.fromCode("face_not_recognized")).isNull();
            assertThat(FaceIDError.fromCode("FACE_NOT_RECOGNIZED")).isNotNull();
        }
    }

    @Nested
    @DisplayName("fromCode() - невалидные коды")
    class FromCodeInvalid {

        @Test
        @DisplayName("При null возвращает null, а не NullPointerException")
        void fromCode_withNull_returnsNull() {
            // when
            FaceIDError result = FaceIDError.fromCode(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("При пустой строке возвращает null")
        void fromCode_withEmptyString_returnsNull() {
            // when
            FaceIDError result = FaceIDError.fromCode("");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("При несуществующем коде возвращает null")
        void fromCode_withUnknownCode_returnsNull() {
            // when
            FaceIDError result = FaceIDError.fromCode("UNKNOWN_ERROR_CODE");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("При коде с пробелами возвращает null")
        void fromCode_withSpaces_returnsNull() {
            // when
            FaceIDError result = FaceIDError.fromCode(" INSUFFICIENT_FUNDS ");

            // then
            assertThat(result).isNull();
        }
    }

    // ==================== toString() ====================

    @Nested
    @DisplayName("toString() - возвращает shortDescription")
    class ToString {

        @Test
        @DisplayName("Возвращает краткое описание для биометрических ошибок")
        void toString_biometricErrors_returnsShortDescription() {
            assertThat(FaceIDError.FACE_NOT_RECOGNIZED.toString()).isEqualTo("Пользователь не найден");
            assertThat(FaceIDError.SPOOFING_ATTACK_DETECTED.toString()).isEqualTo("Попытка мошенничества");
            assertThat(FaceIDError.POOR_LIGHTING_OR_QUALITY.toString()).isEqualTo("Плохое качество кадра");
            assertThat(FaceIDError.FACE_OCCLUSION.toString()).isEqualTo("Лицо закрыто");
        }

        @Test
        @DisplayName("Возвращает краткое описание для бизнес-ошибок")
        void toString_businessErrors_returnsShortDescription() {
            assertThat(FaceIDError.INSUFFICIENT_FUNDS.toString()).isEqualTo("Недостаточно средств");
            assertThat(FaceIDError.BIOMETRIC_SERVICE_DISABLED.toString()).isEqualTo("Услуга отключена");
            assertThat(FaceIDError.LIMIT_EXCEEDED.toString()).isEqualTo("Лимит превышен");
        }

        @Test
        @DisplayName("Возвращает краткое описание для технических ошибок")
        void toString_techErrors_returnsShortDescription() {
            assertThat(FaceIDError.TERMINAL_CAMERA_HARDWARE_FAULT.toString()).isEqualTo("Сбой оборудования камеры");
            assertThat(FaceIDError.BIOMETRIC_ENGINE_TIMEOUT.toString()).isEqualTo("Таймаут сервера биометрии");
            assertThat(FaceIDError.NETWORK_DISCONNECTION.toString()).isEqualTo("Проблемы со связью");
        }
    }

    // ==================== getDescription() ====================

    @Nested
    @DisplayName("getDescription() - геттеры описаний")
    class GetDescription {

        @Test
        @DisplayName("getShortDescription() возвращает корректные значения")
        void getShortDescription_returnsCorrectValues() {
            // then
            assertThat(FaceIDError.INSUFFICIENT_FUNDS.getShortDescription()).isEqualTo("Недостаточно средств");
            assertThat(FaceIDError.FACE_NOT_RECOGNIZED.getShortDescription()).isEqualTo("Пользователь не найден");
            assertThat(FaceIDError.NETWORK_DISCONNECTION.getShortDescription()).isEqualTo("Проблемы со связью");
        }

        @Test
        @DisplayName("getDetailedDescription() возвращает корректные значения")
        void getDetailedDescription_returnsCorrectValues() {
            // then
            assertThat(FaceIDError.INSUFFICIENT_FUNDS.getDetailedDescription())
                    .isEqualTo("Лицо распознано, но на счете не хватает денег для списания");
            assertThat(FaceIDError.SPOOFING_ATTACK_DETECTED.getDetailedDescription())
                    .isEqualTo("Алгоритмы Liveness определили фальшивое изображение");
            assertThat(FaceIDError.NETWORK_DISCONNECTION.getDetailedDescription())
                    .isEqualTo("Терминал потерял интернет (Wi-Fi/GSM)");
        }

        @Test
        @DisplayName("Краткое и подробное описания не совпадают")
        void descriptions_areDifferent() {
            // for all enum values, short and detailed descriptions should be different
            for (FaceIDError error : FaceIDError.values()) {
                assertThat(error.getShortDescription())
                        .isNotEqualTo(error.getDetailedDescription())
                        .isNotBlank();
                assertThat(error.getDetailedDescription())
                        .isNotBlank();
            }
        }
    }

    // ==================== values() ====================

    @Nested
    @DisplayName("values() - константы enum")
    class Values {

        @Test
        @DisplayName("Возвращает ровно 10 констант")
        void values_returnsExactlyTenConstants() {
            assertThat(FaceIDError.values()).hasSize(10);
        }

        @Test
        @DisplayName("Содержит все 3 категории ошибок")
        void values_containsAllCategories() {
            // Биометрические (4)
            assertThat(FaceIDError.values()).contains(
                    FaceIDError.FACE_NOT_RECOGNIZED,
                    FaceIDError.SPOOFING_ATTACK_DETECTED,
                    FaceIDError.POOR_LIGHTING_OR_QUALITY,
                    FaceIDError.FACE_OCCLUSION
            );

            // Бизнес-ошибки (3)
            assertThat(FaceIDError.values()).contains(
                    FaceIDError.INSUFFICIENT_FUNDS,
                    FaceIDError.BIOMETRIC_SERVICE_DISABLED,
                    FaceIDError.LIMIT_EXCEEDED
            );

            // Технические (3)
            assertThat(FaceIDError.values()).contains(
                    FaceIDError.TERMINAL_CAMERA_HARDWARE_FAULT,
                    FaceIDError.BIOMETRIC_ENGINE_TIMEOUT,
                    FaceIDError.NETWORK_DISCONNECTION
            );
        }
    }
}
