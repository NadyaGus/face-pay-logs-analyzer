package com.facepay.app.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.facepay.app.models.PaymentTransaction;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Продюсер для отправки платежных транзакций в Kafka
 * <p>
 * Отправляет JSON сериализованные транзакции в topic "face-pay-logs".
 * </p>
 */
public class PaymentProducer implements AutoCloseable {
    
    private final Producer<String, String> producer;
    private final String topic;
    private final ObjectMapper objectMapper;
    
    /**
     * Создает продюсера с указанным topic
     * 
     * @param bootstrapServers адреса брокеров Kafka
     * @param topic название топика для отправки
     */
    public PaymentProducer(String bootstrapServers, String topic) {
        this.topic = topic;
        
        // Настраиваем ObjectMapper для работы с Java 8 Date/Time
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        props.put("acks", "all");
        props.put("retries", 3);
        props.put("batch.size", 32768);
        props.put("linger.ms", 5);
        props.put("buffer.memory", 33554432);
        
        this.producer = new KafkaProducer<>(props);
    }
    
    /**
     * Отправляет одну транзакцию в Kafka (асинхронно)
     * 
     * @param transaction транзакция для отправки
     * @return Future с метаданными сообщения
     */
    public Future<RecordMetadata> send(PaymentTransaction transaction) {
        try {
            String jsonString = objectMapper.writeValueAsString(transaction);
            ProducerRecord<String, String> record = 
                new ProducerRecord<>(topic, transaction.getTransactionId(), jsonString);
            
            return producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    System.out.printf("Sent transaction %s to partition %d with offset %d%n",
                        transaction.getTransactionId(),
                        metadata.partition(),
                        metadata.offset());
                } else {
                    System.err.printf("Failed to send transaction %s: %s%n",
                        transaction.getTransactionId(),
                        exception.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("Error serializing transaction: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Отправляет одну транзакцию с ключом (для партиционирования)
     * 
     * @param transaction транзакция
     * @param key ключ для партиционирования (например, accountId)
     * @return Future с метаданными
     */
    public Future<RecordMetadata> send(PaymentTransaction transaction, String key) {
        try {
            String jsonString = objectMapper.writeValueAsString(transaction);
            ProducerRecord<String, String> record = 
                new ProducerRecord<>(topic, key, jsonString);
            
            return producer.send(record);
        } catch (Exception e) {
            System.err.println("Error serializing transaction: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void close() {
        producer.flush();
        producer.close();
    }
}
