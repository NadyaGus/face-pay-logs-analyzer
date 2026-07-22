package com.facepay.app.consumer;

import com.facepay.app.enums.PaymentStatus;
import com.facepay.app.models.PaymentTransaction;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final String BOOTSTRAP_SERVERS = System.getenv("BOOTSTRAP_SERVERS");
    private static final String TOPIC = "face-pay-logs";
    private static final String DB_TABLE = "critical_logs";

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private final SparkSession spark;

    public LogConsumer() {
        this.spark = createSparkSession();
    }

    /**
     * Остановка Spark сессии
     */
    public void stop() {
        spark.stop();
    }

    /**
     * Точка входа в приложение
     */
    public static void main(String[] args) {
        try {
            LogConsumer consumer = new LogConsumer();
            consumer.start();
            
            System.out.println("[Consumer] Started. Press Ctrl+C to stop.");
            
            // Keep running until interrupted
            Thread.sleep(Long.MAX_VALUE);
            
            consumer.stop();
        } catch (InterruptedException e) {
            System.out.println("[Consumer] Interrupted. Shutting down...");
            System.exit(0);
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
     * Основной метод запуска consumer
     */
    public void start() {
        try {
            Dataset<Row> kafkaData = readFromKafka();
            Dataset<PaymentTransaction> allTransactions = parseTransactions(kafkaData);
            Dataset<PaymentTransaction> criticalLogs = filterCriticalLogs(allTransactions);
            saveToPostgreSQL(criticalLogs);
        } catch (Exception e) {
            System.err.println("Error in consumer start: " + e.getMessage());
            e.printStackTrace();
        }
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
        Dataset<String> jsonStrings = kafkaData
                .selectExpr("CAST(value AS STRING)")
                .as(Encoders.STRING());

        return jsonStrings.map(
                (MapFunction<String, PaymentTransaction>) json -> objectMapper.readValue(json, PaymentTransaction.class),
                Encoders.bean(PaymentTransaction.class)
        );
    }

    /**
     * Фильтрует критичные логи (неуспешные транзакции с ошибками)
     */
    private Dataset<PaymentTransaction> filterCriticalLogs(Dataset<PaymentTransaction> allTransactions) {
        return allTransactions.filter(this::isCriticalTransaction);
    }

    /**
     * Проверяет, является ли транзакция критичной
     */
    private boolean isCriticalTransaction(PaymentTransaction transaction) {
        boolean isFailed = transaction.getStatus() != null && transaction.getStatus() == PaymentStatus.FAILED;
        boolean hasErrorCode = transaction.getErrorCode() != null;
        
        if (isFailed && hasErrorCode) {
            logCriticalTransaction(transaction);
        }
        
        return isFailed && hasErrorCode;
    }

    /**
     * Логирует критичную транзакцию
     */
    private void logCriticalTransaction(PaymentTransaction transaction) {
        System.out.println("[CRITICAL] Transaction: " + transaction.getTransactionId() + 
                         " | Status: " + transaction.getStatus() + 
                         " | Error: " + transaction.getErrorCode());
    }

    /**
     * Создает JDBC URL для PostgreSQL
     */
    private String getPostgresUrl() {
        return System.getenv("POSTGRES_URL");
    }

    /**
     * Получает имя пользователя PostgreSQL
     */
    private String getPostgresUser() {
        return System.getenv("POSTGRES_USER");
    }

    /**
     * Получает пароль PostgreSQL
     */
    private String getPostgresPassword() {
        return System.getenv("POSTGRES_PASSWORD");
    }

    /**
     * Сохраняет критичные логи в PostgreSQL
     */
    private void saveToPostgreSQL(Dataset<PaymentTransaction> criticalLogs) {
        Dataset<Row> rowData = prepareRowData(criticalLogs);
        startStreamWriter(rowData);
    }

    /**
     * Подготавливает данные для сохранения: конвертирует и переименовывает колонки
     */
    private Dataset<Row> prepareRowData(Dataset<PaymentTransaction> criticalLogs) {
        Dataset<Row> criticalLogsAsRow = criticalLogs.toDF();
        
        return criticalLogsAsRow
                .withColumnRenamed("transactionId", "transaction_id")
                .withColumnRenamed("timestamp", "timestamp")
                .withColumnRenamed("accountId", "account_id")
                .withColumnRenamed("merchantId", "merchant_id")
                .withColumnRenamed("errorCode", "error_code")
                .withColumnRenamed("errorMessage", "error_message")
                .withColumnRenamed("created_at", "created_at");
    }

    /**
     * Запускает потоковую запись в PostgreSQL
     */
    private void startStreamWriter(Dataset<Row> rowData) {
        try {
            String postgresUrl = getPostgresUrl();
            String postgresUser = getPostgresUser();
            String postgresPassword = getPostgresPassword();
            
            rowData.writeStream()
                    .foreachBatch(new org.apache.spark.api.java.function.VoidFunction2<org.apache.spark.sql.Dataset<org.apache.spark.sql.Row>, java.lang.Long>() {
                        @Override
                        public void call(org.apache.spark.sql.Dataset<org.apache.spark.sql.Row> batchDF, java.lang.Long batchId) {
                            processBatch(batchDF, batchId, postgresUrl, postgresUser, postgresPassword);
                        }
                    })
                    .outputMode("append")
                    .option("checkpointLocation", "/tmp/checkpoint")
                    .option("failOnDataLoss", "false")
                    .start();
        } catch (java.util.concurrent.TimeoutException e) {
            System.err.println("Timeout while starting stream: " + e.getMessage());
            throw new RuntimeException("Failed to start stream due to timeout", e);
        } catch (Exception e) {
            System.err.println("Error starting stream: " + e.getMessage());
            throw new RuntimeException("Failed to start stream", e);
        }
    }

    /**
     * Обрабатывает один батч данных
     */
    private void processBatch(Dataset<Row> batchDF, long batchId, String postgresUrl, String postgresUser, String postgresPassword) {
        if (batchDF.count() > 0) {
            saveBatchToDatabase(batchDF, postgresUrl, postgresUser, postgresPassword);
        }
    }

    /**
     * Сохраняет батч в PostgreSQL
     */
    private void saveBatchToDatabase(Dataset<Row> batchDF, String postgresUrl, String postgresUser, String postgresPassword) {
        batchDF.write()
                .mode("append")
                .format("jdbc")
                .option("url", postgresUrl)
                .option("dbtable", DB_TABLE)
                .option("user", postgresUser)
                .option("password", postgresPassword)
                .option("driver", "org.postgresql.Driver")
                .save();
    }
}
