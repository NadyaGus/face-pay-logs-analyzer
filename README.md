# FacePay Stream Analyzer

Мультимодульное приложение для анализа логов FaceID в реальном времени.

---

## 📋 Функционал

- **Генерация транзакций** (generator):
  - Симуляция платежей через FaceID с различными статусами
  - 80% успешных транзакций
  - 8% бизнес-ошибки (недостаточно средств, лимиты, отключенная услуга)
  - 6% ошибки биометрии (не распознано, мошенничество, плохое качество)
  - 6% технические ошибки (сбой камеры, таймаут, потеря связи)

- **Потоковая обработка** (spark-consumer):
  - Spark Streaming читает Kafka topic `face-pay-logs`
  - Фильтрация критичных логов (неуспешные транзакции с кодами ошибок)
  - Сохранение в PostgreSQL таблицу `critical_logs`

### Сервисы

| Сервис | Описание | Порт |
|--------|----------|------|
| `kafka` | Message broker (KRaft режим) | `9092` |
| `postgres` | База данных для хранения логов | `5432` |
| `generator` | Генератор тестовых транзакций | - |
| `spark-consumer` | Spark Streaming consumer | - |

---

## 🚀 Запуск

### Предварительные требования

- Docker и Docker Compose установлены

### Структура проекта

```
fintech-stream-analyzer/
├── core/              # Модели данных и enum (общая библиотека)
├── generator/         # Генератор транзакций (Kafka Producer)
├── spark-consumer/    # Spark Streaming Consumer (PostgreSQL Writer)
├── pom.xml            # Parent POM (multi-module)
├── docker-compose.yml # Оркестрация контейнеров
└── Dockerfile         # Сборка приложения
```

### Сборка и запуск

```bash
# Собрать все модули и создать Docker образы
./mvnw clean install

# Запустить все сервисы (Kafka + PostgreSQL + generator + spark-consumer)
docker compose up -d
```

## 📊 Проверка работы

### 1. Проверить Kafka topic

```bash
docker exec facepay-kafka kafka-topics --bootstrap-server localhost:9092 --list
```

Должен появиться топик `face-pay-logs`.

### 2. Посмотреть логи генератора

```bash
docker compose logs -f generator | grep "Sent transaction"
```

Должны появиться строки вида:
```
facepay-generator  | Sent transaction abc-123... to partition 0 with offset X
```

### 3. Посмотреть логи spark-consumer

```bash
docker compose logs -f spark-consumer | grep -E "(CRITICAL|Starting)"
```

Должны появиться строки вида:
```
facepay-spark-consumer  | [Consumer] Started. Press Ctrl+C to stop.
```

### 4. Проверить таблицу в PostgreSQL

Подключитесь к базе данных:

```bash
docker exec -it facepay-postgres psql -U admin -d facepay_stream
```

Посмотрите таблицу `critical_logs`:

```sql
-- Просмотр всех записей
SELECT * FROM critical_logs ORDER BY created_at DESC LIMIT 10;

-- Проверка структуры таблицы
\d critical_logs

-- Подсчет записей
SELECT COUNT(*) FROM critical_logs;

-- Группировка по типу ошибок
SELECT error_code, COUNT(*) as count 
FROM critical_logs 
GROUP BY error_code 
ORDER BY count DESC;
```

Выход из psql: `\q`

---

## 🔧 Технологии

- **Java 21** — язык программирования
- **Maven** — сборка проекта
- **Kafka 8.3.0** — message broker
- **Spark SQL 3.5.1** — потоковая обработка
- **Spring Boot 4.1.0** — управление зависимостями
- **PostgreSQL 17** — хранение данных
- **Docker Compose** — оркестрация

---

## 📝 Остановка

```bash
# Остановить контейнеры (данные сохраняются в volumes)
docker compose down

# Остановить и удалить volumes (данные будут удалены!)
docker compose down -v

# Очистка кэша Maven
./mvnw clean
```

---

## 🛠️ Разработка

### Сборка
```bash
./mvnw clean install
```

### Управление сервисами
```bash
# Запустить отдельные сервисы
docker compose up -d generator spark-consumer

# Перезапустить сервис
docker compose restart generator

# Пересобрать и запустить
docker compose build spark-consumer && docker compose up -d spark-consumer

# Остановить все
docker compose down
```
