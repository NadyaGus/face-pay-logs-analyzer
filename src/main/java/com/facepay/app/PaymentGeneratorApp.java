package com.facepay.app;

import com.facepay.app.producer.PaymentGenerator;
import com.facepay.app.producer.PaymentProducer;
import com.facepay.app.models.PaymentTransaction;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PaymentGeneratorApp {
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC = "face-pay-logs";
    private static final long DELAY_MS = 500;
    private static final long RUN_DURATION_MINUTES = 1;
    
    public static void main(String[] args) {
        System.out.println("=== Payment Generator App ===");
        System.out.println("Bootstrap Servers: " + BOOTSTRAP_SERVERS);
        System.out.println("Topic: " + TOPIC);
        System.out.println("Delay between sends: " + DELAY_MS + "ms");
        System.out.println("Run duration: " + RUN_DURATION_MINUTES + " minute(s)");
        System.out.println();
        
        AtomicInteger counter = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        long runDurationMillis = RUN_DURATION_MINUTES * 60 * 1000;
        
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        
        try (PaymentProducer producer = new PaymentProducer(BOOTSTRAP_SERVERS, TOPIC)) {
            System.out.println("Starting payment generator...");
            System.out.println();
            
            scheduler.scheduleAtFixedRate(() -> {
                PaymentTransaction transaction = PaymentGenerator.generateRandomTransaction();
                producer.send(transaction);
                System.out.println("Sent transaction #" + counter.incrementAndGet() + 
                                 ": " + transaction.getTransactionId());
                
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= runDurationMillis) {
                    System.out.println("\nDuration limit reached. Shutting down...");
                    scheduler.shutdown();
                }
            }, 0, DELAY_MS, TimeUnit.MILLISECONDS);
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down...");
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                }
            }));
            
            while (!scheduler.isTerminated()) {
                Thread.sleep(100);
            }
            
            System.out.println("=== Generation completed! ===");
            System.out.println("Sent " + counter.get() + " transactions to Kafka");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
