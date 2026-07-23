package com.facepay.app.producer;

import com.facepay.app.models.PaymentTransaction;

import java.time.Duration;

/**
 * Главный класс приложения генератора платежных транзакций
 * <p>
 * Запускает генерацию транзакций и отправляет их в Kafka.
 * </p>
 */
public class PaymentProducerApp {

    private static final String BOOTSTRAP_SERVERS = System.getenv("BOOTSTRAP_SERVERS");
    private static final String TOPIC = System.getenv("KAFKA_TOPIC");

    public static void main(String[] args) {
        System.out.println("[Generator] Starting Payment Producer App...");
        System.out.println("[Generator] Bootstrap servers: " + BOOTSTRAP_SERVERS);
        System.out.println("[Generator] Topic: " + TOPIC);

        if (BOOTSTRAP_SERVERS == null || BOOTSTRAP_SERVERS.isEmpty()) {
            System.err.println("[Generator] Error: BOOTSTRAP_SERVERS environment variable is not set");
            System.exit(1);
        }

        try (PaymentProducer producer = new PaymentProducer(
                BOOTSTRAP_SERVERS,
                TOPIC != null ? TOPIC : "face-pay-logs"
        )) {
            System.out.println("[Generator] Producer initialized. Starting to send transactions...");

            // Генерируем и отправляем транзакции каждые 2 секунды
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    PaymentTransaction transaction = PaymentGenerator.generateRandomTransaction();
                    producer.send(transaction);
                    
                    // Спим перед следующей транзакцией
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    System.out.println("[Generator] Interrupted. Shutting down...");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("[Generator] Error sending transaction: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("[Generator] Error initializing producer: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("[Generator] App stopped.");
    }
}
