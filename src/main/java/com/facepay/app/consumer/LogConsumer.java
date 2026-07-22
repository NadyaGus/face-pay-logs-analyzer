package com.facepay.app.consumer;

import com.facepay.app.enums.PaymentStatus;
import com.facepay.app.models.PaymentTransaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.SparkConf;

import java.io.Serializable;

/**
 * Spark Streaming Consumer для обработки платежных транзакций из Kafka
 * <p>
 * Читает сообщения из Kafka topic "face-pay-logs", фильтрует критичные логи
 * и сохраняет их в PostgreSQL для дальнейшего анализа.
 * </p>
 */
public class LogConsumer implements Serializable {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC = "face-pay-logs";
    private static final String PG_TABLE = "critical_logs";;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private final SparkSession spark;

    public LogConsumer() {
        this.spark = createSparkSession();
    }

    /**
     * Точка входа в приложение
     */
    public static void main(String[] args) {
        try {
            LogConsumer consumer = new LogConsumer();
            consumer.start();

            // Запускаем приложение на 1 минуту
            System.out.println("Spark streaming will run for 60 seconds...");
            Thread.sleep(60000);
            
            System.out.println("Stopping Spark streaming...");
            consumer.spark.stop();
            System.out.println("Spark streaming stopped.");
        } catch (Exception e) {
            System.err.println("Error in main: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Создает SparkSession с настройками для разработки
     */
    private SparkSession createSparkSession() {
        SparkConf conf = new SparkConf()
                .setAppName("FacePay-Log-Consumer")
                .setMaster("local[*]")
                .set("spark.sql.shuffle.partitions", "1")
                // Отключаем Spark UI чтобы избежать проблем с сервлетами
                .set("spark.ui.enabled", "false");
        
        return SparkSession.builder()
                .config(conf)
                .getOrCreate();
    }

    /**
     * Читает сообщения из Kafka и возвращает DataFrame
     */
    private Dataset<Row> readFromKafka() {
        return spark.readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", BOOTSTRAP_SERVERS)
                .option("subscribe", TOPIC)
                .load();
    }

    /**
     * Парсит JSON из Kafka сообщения в структурированные данные
     */
    private Dataset<PaymentTransaction> parseTransactions(Dataset<Row> kafkaData) {
        // Конвертируем бинарные данные в строку
        Dataset<String> jsonStrings = kafkaData
                .selectExpr("CAST(value AS STRING)")
                .as(Encoders.STRING());

        // Парсим JSON и конвертируем в PaymentTransaction
        return jsonStrings.map(
                (MapFunction<String, PaymentTransaction>) json -> objectMapper.readValue(json, PaymentTransaction.class),
                Encoders.bean(PaymentTransaction.class)
        );
    }

    /**
     * Фильтрует критичные логи (неуспешные транзакции с ошибками)
     */
    private Dataset<PaymentTransaction> filterCriticalLogs(Dataset<PaymentTransaction> allTransactions) {
        System.out.println("[filterCriticalLogs] Filtering logs...");
        return allTransactions.filter(
                (FilterFunction<PaymentTransaction>) transaction -> {
                    // Jackson десериализует JSON "FAILED" в enum PaymentStatus.FAILED
                    boolean isFailed = transaction.getStatus() != null && transaction.getStatus() == PaymentStatus.FAILED;
                    boolean hasErrorCode = transaction.getErrorCode() != null;
                    return isFailed && hasErrorCode;
                }
        );
    }

    /**
     * Сохраняет критичные логи в PostgreSQL
     */
    private void saveToPostgreSQL(Dataset<PaymentTransaction> criticalLogs) {
        try {
            System.out.println("[saveToPostgreSQL] Converting to DataFrame and renaming columns...");
            Dataset<Row> criticalLogsAsRow = criticalLogs.toDF();
            
            // Переименовываем колонки из camelCase в snake_case для PostgreSQL
            Dataset<Row> renamedColumns = criticalLogsAsRow
                    .withColumnRenamed("transactionId", "transaction_id")
                    .withColumnRenamed("timestamp", "timestamp")
                    .withColumnRenamed("accountId", "account_id")
                    .withColumnRenamed("merchantId", "merchant_id")
                    .withColumnRenamed("errorCode", "error_code")
                    .withColumnRenamed("errorMessage", "error_message")
                    .withColumnRenamed("created_at", "created_at");
            
            System.out.println("[saveToPostgreSQL] Starting stream with foreachBatch...");
            renamedColumns.writeStream()
                    .foreachBatch((batchDF, batchId) -> {
                        System.out.println("[foreachBatch] Processing batch ID: " + batchId);
                        batchDF.write()
                                .mode("append")
                                .format("jdbc")
                                .option("url", "jdbc:postgresql://localhost:5432/facepay_stream")
                                .option("dbtable", "critical_logs")
                                .option("user", "admin")
                                .option("password", "admin")
                                .option("driver", "org.postgresql.Driver")
                                .save();
                        System.out.println("[foreachBatch] Batch " + batchId + " written successfully");
                    })
                    .outputMode("append")
                    .option("checkpointLocation", "/tmp/checkpoint")
                    // Чтобы не было ошибок при перезапуске приложения уберем ошибку при потере данных
                    .option("failOnDataLoss", "false")
                    .start();
            
            System.out.println("[saveToPostgreSQL] Stream started successfully");
        } catch (Exception e) {
            System.out.println("[saveToPostgreSQL] Exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Основной метод запуска consumer
     */
    public void start() {
        System.out.println("Starting Spark Consumer...");
        System.out.println("Reading from Kafka topic: " + TOPIC);
        System.out.println("Critical logs will be saved to PostgreSQL: " + PG_TABLE);
        System.out.println();

        try {
            // Читаем из Kafka
            Dataset<Row> kafkaData = readFromKafka();

            // Парсим JSON в объекты PaymentTransaction
            Dataset<PaymentTransaction> allTransactions = parseTransactions(kafkaData);

            // Фильтруем критичные логи
            Dataset<PaymentTransaction> criticalLogs = filterCriticalLogs(allTransactions);

            // Сохраняем в PostgreSQL
            saveToPostgreSQL(criticalLogs);

            System.out.println("Spark streaming started. Running for 60 seconds...");
        } catch (Exception e) {
            System.err.println("Error in Spark Consumer: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
