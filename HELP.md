# FacePay Stream Analyzer - Шпаргалка

## 🚀 Быстрый старт

### Запуск генератора транзакций
```bash
./mvnw exec:java -Dexec.mainClass="com.facepay.app.PaymentGeneratorApp"
```
---

## 🐳 Docker Compose

### Запуск контейнеров (Kafka + PostgreSQL)
```bash
docker compose up -d
```

### Остановка контейнеров
```bash
docker compose down
```

### Просмотр логов
```bash
docker compose logs -f
```

### Перезапуск контейнеров
```bash
docker compose restart
```

### Удаление volumes (данные будут потеряны!)
```bash
docker compose down -v
```

---

## 🔥 Kafka CLI команды

### Создать топик
```bash
docker exec facepay-kafka kafka-topics --bootstrap-server localhost:9092 \
  --create --topic my-topic --partitions 3 --replication-factor 1
```

### Посмотреть список топиков
```bash
docker exec facepay-kafka kafka-topics --bootstrap-server localhost:9092 --list
```

### Посмотреть детали топика
```bash
docker exec facepay-kafka kafka-topics --bootstrap-server localhost:9092 \
  --describe --topic face-pay-logs
```

### Читать сообщения (последние 10) в момент отправки
```bash
docker exec facepay-kafka kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic face-pay-logs --max-messages 10
```

### Читать все сообщения с начала
```bash
docker exec facepay-kafka kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic face-pay-logs --from-beginning
```

### Читать сообщения в формате JSON
```bash
docker exec facepay-kafka kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic face-pay-logs --from-beginning --property print.value=true \
  --property print.key=true --property print.offset=true
```

### Посмотреть смещения (offsets)
```bash
docker exec facepay-kafka kafka-consumer-groups --bootstrap-server localhost:9092 \
  --group my-group --describe
```

---

## 🐘 PostgreSQL CLI

### Подключиться к базе
```bash
docker exec -it facepay-postgres psql -U admin -d facepay_stream
```

### Просмотр таблиц
```sql
\dt
```

### Выход из psql
```sql
\q
```

---

## 🛠️ Отладка

### Проверить статус контейнеров
```bash
docker ps
```

### Посмотреть логи Kafka
```bash
docker logs facepay-kafka
```

### Посмотреть логи PostgreSQL
```bash
docker logs facepay-postgres
```

### Пересобрать Docker образы
```bash
docker compose build --no-cache
```

---

## 📝 Код

### Генерация транзакций

| Метод | Описание |
|-------|----------|
| `PaymentGenerator.generateRandomTransaction()` | Одна случайная транзакция |
| `PaymentGenerator.generateTransactions(20)` | Массив из 20 транзакций |

### Типы ошибок в транзакциях
- **Бизнес-ошибки** (8%): `INSUFFICIENT_FUNDS`, `BIOMETRIC_SERVICE_DISABLED`, `LIMIT_EXCEEDED`
- **Ошибки биометрии** (6%): `FACE_NOT_RECOGNIZED`, `SPOOFING_ATTACK_DETECTED`, `POOR_LIGHTING_OR_QUALITY`, `FACE_OCCLUSION`
- **Технические ошибки** (6%): `TERMINAL_CAMERA_HARDWARE_FAULT`, `BIOMETRIC_ENGINE_TIMEOUT`, `NETWORK_DISCONNECTION`

---

## ⚙️ Конфигурация

### Параметры PaymentGeneratorApp
```java
private static final String BOOTSTRAP_SERVERS = "localhost:9092";
private static final String TOPIC = "face-pay-logs";
private static final long DELAY_MS = 500;           // Задержка между отправками
private static final long RUN_DURATION_MINUTES = 1; // Время работы
```

---

## 🐛 Тroubleshooting

### Ошибка: "Connection refused"
```bash
# Убедитесь, что Kafka запущен
docker compose up -d kafka
```

### Ошибка: "Topic not found"
```bash
# Создайте топик
docker exec facepay-kafka kafka-topics --bootstrap-server localhost:9092 \
  --create --topic face-pay-logs --partitions 3 --replication-factor 1
```

### Ошибка: "Instant not supported"
```xml
<!-- Добавьте в pom.xml -->
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

### Kafka не принимает сообщения
```bash
# Проверьте логи Kafka
docker logs facepay-kafka

# Убедитесь, что порт 9092 доступен
docker exec facepay-kafka kafka-topics --bootstrap-server localhost:9092 --list
```

---

## 📚 Полезные ссылки

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Docker Compose Reference](https://docs.docker.com/compose/reference/)
