package com.facepay.app;

import com.facepay.app.consumer.LogConsumer;
import com.facepay.app.models.PaymentTransaction;
import com.facepay.app.producer.PaymentGenerator;
import com.facepay.app.producer.PaymentProducer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Унифицированное приложение потоковой обработки платежей
 * <p>
 * Точка входа, которая объединяет:
 * - Генерацию транзакций (PaymentGenerator + PaymentProducer)
 * - Spark streaming consumer (LogConsumer)
 * </p>
 */
public class PaymentStreamApp {

    private static final String BOOTSTRAP_SERVERS = System.getenv("BOOTSTRAP_SERVERS") != null 
            ? System.getenv("BOOTSTRAP_SERVERS") 
            : "localhost:9092";
    private static final String TOPIC = "face-pay-logs";
    private static final long GENERATOR_DELAY_MS = 1000;
    private static final long RUN_DURATION_MINUTES = 2;

    public static void main(String[] args) {
        System.out.println("=== Unified Payment Stream App ===");
        System.out.println("Bootstrap Servers: " + BOOTSTRAP_SERVERS);
        System.out.println("Topic: " + TOPIC);
        System.out.println("Generator delay: " + GENERATOR_DELAY_MS + "ms");
        System.out.println("Run duration: " + RUN_DURATION_MINUTES + " minute(s)");
        System.out.println();

        AtomicInteger counter = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        long runDurationMillis = RUN_DURATION_MINUTES * 60 * 1000;

        // Запускаем Spark consumer в отдельном потоке
        LogConsumer consumer = new LogConsumer();
        consumer.start();
        System.out.println("[Consumer] Started successfully");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        try (PaymentProducer producer = new PaymentProducer(BOOTSTRAP_SERVERS, TOPIC)) {
            System.out.println("[Generator] Starting payment generator...");
            System.out.println();

            scheduler.scheduleAtFixedRate(() -> {
                PaymentTransaction transaction = PaymentGenerator.generateRandomTransaction();
                producer.send(transaction);
                System.out.println("[Generator] Sent transaction #" + counter.incrementAndGet() + 
                                 ": " + transaction.getTransactionId() +
                                 " (Status: " + transaction.getStatus() + ")");

                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= runDurationMillis) {
                    System.out.println("\n[Generator] Duration limit reached. Shutting down...");
                    scheduler.shutdown();
                }
            }, 0, GENERATOR_DELAY_MS, TimeUnit.MILLISECONDS);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[Shutdown] Graceful shutdown started...");
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                }
                consumer.stop();
                System.out.println("[Shutdown] Completed");
            }));

            while (!scheduler.isTerminated()) {
                Thread.sleep(100);
            }

            System.out.println("\n=== Generation completed! ===");
            System.out.println("Sent " + counter.get() + " transactions to Kafka");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
