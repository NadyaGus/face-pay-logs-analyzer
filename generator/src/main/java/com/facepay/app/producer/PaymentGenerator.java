package com.facepay.app.producer;

import com.facepay.app.enums.FaceIDError;
import com.facepay.app.enums.PaymentStatus;
import com.facepay.app.models.PaymentTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Random;

/**
 * Генератор случайных платежных транзакций для FaceID
 * <p>
 * Учитывает реальные сценарии биометрической аутентификации:
 * - Успешные платежи
 * - Ошибки биометрии (не распознано, мошенничество, плохое качество)
 * - Бизнес-ошибки (недостаточно средств, отключена услуга, лимиты)
 * - Технические ошибки (сбой камеры, таймаут, потеря связи)
 * </p>
 */
public class PaymentGenerator {
    
    private static final Random RANDOM = new Random();
    
    /**
     * Генерирует случайную платежную транзакцию FaceID
     * 
     * @return PaymentTransaction со случайными данными
     */
    public static PaymentTransaction generateRandomTransaction() {
        String transactionId = java.util.UUID.randomUUID().toString();
        String accountId = "ACC-" + String.format("%08d", RANDOM.nextInt(100000000));
        String merchantId = "MERCHANT-" + String.format("%06d", RANDOM.nextInt(1000000));
        
        // Генерация случайной суммы (10-50000 RUB)
        double amountDouble = randomAmount();
        BigDecimal amount = BigDecimal.valueOf(amountDouble).setScale(2, RoundingMode.HALF_UP);
        
        // Определение статуса и типа ошибки (если есть)
        double statusRandom = RANDOM.nextDouble();
        
        if (statusRandom < 0.80) {
            // 80% успешных транзакций
            return new PaymentTransaction(
                transactionId,
                Instant.now(),
                accountId,
                amount,
                "RUB",
                PaymentStatus.SUCCESS,
                merchantId,
                null,
                null,
                "Успешная оплата через FaceID"
            );
        } else if (statusRandom < 0.88) {
            // 8% бизнес-ошибки
            return generateBusinessError(transactionId, accountId, merchantId, amount);
        } else if (statusRandom < 0.94) {
            // 6% ошибки биометрии
            return generateBiometricError(transactionId, accountId, merchantId, amount);
        } else {
            // 6% технические ошибки
            return generateTechError(transactionId, accountId, merchantId, amount);
        }
    }
    
    /**
     * Генерирует бизнес-ошибку
     */
    private static PaymentTransaction generateBusinessError(String txnId, String accountId, 
                                                            String merchantId, BigDecimal amount) {
        int errorType = RANDOM.nextInt(3);
        FaceIDError errorCode;
        String description = switch (errorType) {
            case 0 -> {
                errorCode = FaceIDError.INSUFFICIENT_FUNDS;
                yield errorCode.getDetailedDescription();
            }
            case 1 -> {
                errorCode = FaceIDError.BIOMETRIC_SERVICE_DISABLED;
                yield errorCode.getDetailedDescription();
            }
            default -> {
                errorCode = FaceIDError.LIMIT_EXCEEDED;
                yield errorCode.getDetailedDescription();
            }
        };

        return new PaymentTransaction(
            txnId, Instant.now(), accountId, amount, "RUB", PaymentStatus.FAILED,
            merchantId, errorCode, description, null
        );
    }
    
    /**
     * Генерирует ошибку биометрии
     */
    private static PaymentTransaction generateBiometricError(String txnId, String accountId,
                                                             String merchantId, BigDecimal amount) {
        int errorType = RANDOM.nextInt(4);
        FaceIDError errorCode;
        String description = switch (errorType) {
            case 0 -> {
                errorCode = FaceIDError.FACE_NOT_RECOGNIZED;
                yield errorCode.getDetailedDescription();
            }
            case 1 -> {
                errorCode = FaceIDError.SPOOFING_ATTACK_DETECTED;
                yield errorCode.getDetailedDescription();
            }
            case 2 -> {
                errorCode = FaceIDError.POOR_LIGHTING_OR_QUALITY;
                yield errorCode.getDetailedDescription();
            }
            default -> {
                errorCode = FaceIDError.FACE_OCCLUSION;
                yield errorCode.getDetailedDescription();
            }
        };

        return new PaymentTransaction(
            txnId, Instant.now(), accountId, amount, "RUB", PaymentStatus.FAILED,
            merchantId, errorCode, description, null
        );
    }
    
    /**
     * Генерирует техническую ошибку
     */
    private static PaymentTransaction generateTechError(String txnId, String accountId,
                                                        String merchantId, BigDecimal amount) {
        int errorType = RANDOM.nextInt(3);
        FaceIDError errorCode;
        String description = switch (errorType) {
            case 0 -> {
                errorCode = FaceIDError.TERMINAL_CAMERA_HARDWARE_FAULT;
                yield errorCode.getDetailedDescription();
            }
            case 1 -> {
                errorCode = FaceIDError.BIOMETRIC_ENGINE_TIMEOUT;
                yield errorCode.getDetailedDescription();
            }
            default -> {
                errorCode = FaceIDError.NETWORK_DISCONNECTION;
                yield errorCode.getDetailedDescription();
            }
        };

        return new PaymentTransaction(
            txnId, Instant.now(), accountId, amount, "RUB", PaymentStatus.FAILED,
            merchantId, errorCode, description, null
        );
    }
    
    /**
     * Генерирует случайное число в диапазоне
     */
    private static double randomAmount() {
        return (double) 10 + ((double) 50000 - (double) 10) * RANDOM.nextDouble();
    }
    
    /**
     * Генерирует несколько транзакций
     * 
     * @param count количество транзакций
     * @return массив транзакций
     */
    public static PaymentTransaction[] generateTransactions(int count) {
        PaymentTransaction[] transactions = new PaymentTransaction[count];
        for (int i = 0; i < count; i++) {
            transactions[i] = generateRandomTransaction();
        }
        return transactions;
    }
}
