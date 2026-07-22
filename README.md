# FacePay Stream Analyzer

Анализатор логов FaceID для обнаружения мошенничества в платежных транзакциях.

Приложение генерирует тестовые платежные транзакции с биометрической аутентификацией, отправляет их в Kafka,
а затем Spark Streaming consumer анализирует и сохраняет критичные логи (неуспешные транзакции с ошибками) в PostgreSQL.

---

## 📋 Функционал

- **Генерация транзакций**: Симуляция платежей через FaceID с различными статусами
  - 80% успешных транзакций
  - 8% бизнес-ошибки (недостаточно средств, лимиты, отключенная услуга)
  - 6% ошибки биометрии (не распознано, мошенничество, плохое качество)
  - 6% технические ошибки (сбой камеры, таймаут, потеря связи)

- **Потоковая обработка**: Spark Streaming читает Kafka topic и фильтрует критичные логи
- **Хранение**: Данные сохраняются в PostgreSQL таблицу `critical_logs`

---

## 🚀 Запуск

### Предварительные требования

- Docker и Docker Compose установлены

### Запуск контейнеров

```bash
# Запустить все сервисы (Kafka + PostgreSQL + приложение)
docker compose up -d

```

### Проверка статуса

```bash
docker compose ps
```

Ожидаемый вывод:
```
NAME               STATUS                    PORTS
facepay-kafka      Up (healthy)              0.0.0.0:9092->9092/tcp
facepay-postgres   Up (healthy)              0.0.0.0:5432->5432/tcp
facepay-app        Up                        8080/tcp
```

### Просмотр логов

```bash
# Логи всего приложения
docker compose logs -f app

# Логи только Kafka
docker compose logs -f kafka

# Логи только PostgreSQL
docker compose logs -f postgres
```

---

## 📊 Проверка работы

### 1. Проверить Kafka topic

```bash
docker exec facepay-kafka kafka-topics --bootstrap-server localhost:9092 --list
```

Должен появиться топик `face-pay-logs`.

### 2. Проверить таблицу в PostgreSQL

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
```

Выход из psql: `\q`

### 3. Проверить в логах приложения

```bash
docker compose logs app | grep -E "(CRITICAL|Sent transaction)"
```

Должны появиться строки вида:
```
[Generator] Sent transaction #1: abc-123... (Status: Успешно завершена)
[CRITICAL] Transaction: xyz-456... | Status: Не удалась | Error: Лицо закрыто
```

---

## 🛑 Остановка

```bash
# Остановить контейнеры (данные сохраняются)
docker compose down

# Остановить и удалить volumes (данные будут удалены!)
docker compose down -v
```

---

## 📚 Технологии

- **Kafka** - message broker
- **Spark Streaming** - потоковая обработка данных
- **PostgreSQL** - хранение данных
- **Spring Boot** - конфигурация и управление зависимостями
- **Docker Compose** - оркестрация контейнеров
