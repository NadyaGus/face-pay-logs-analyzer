# FacePay-logs-analyzer


Мультимодульное приложение для анализа логов оплаты по биометрии в реальном времени.

---

## 📋 Функционал

- **Генерация транзакций** (generator):
  - Симуляция платежей через биометрию с различными статусами:
    - 80% успешных транзакций
    - 8% бизнес-ошибки (недостаточно средств, лимиты, отключенная услуга)
    - 6% ошибки биометрии (не распознано, мошенничество, плохое качество)
    - 6% технические ошибки (сбой камеры, таймаут, потеря связи)

- **Потоковая обработка** (spark-consumer):
  - Spark Streaming читает Kafka topic `face-pay-logs`
  - Фильтрация критичных логов (неуспешные транзакции с кодами ошибок)
  - Сохранение в PostgreSQL таблицу `critical_logs`

- **REST API** (api):
  - Пагинированный список ошибок
  - Поиск по accountId
  - Статистика по errorCode
  - Swagger UI документация

### Сервисы

| Сервис | Описание | Порт |
|--------|----------|------|
| `kafka` | Message broker (KRaft режим) | `9092` |
| `postgres` | База данных для хранения логов | `5432` |
| `generator` | Генератор тестовых транзакций | - |
| `spark-consumer` | Spark Streaming consumer | - |
| `api` | REST API + Swagger UI | `8080` |

---

## 🚀 Запуск

### Предварительные требования

- Docker и Docker Compose установлены
- Java 21+ (для локальной разработки)

### Конфигурация

Проект использует `.env` файл для управления настройками (креды БД, адреса Kafka и т.д.).

```bash
# Создайте .env файл из шаблона
cp .env.example .env

# Отредактируйте .env под ваши нужды
# POSTGRES_PASSWORD=your_secure_password
# POSTGRES_DB=your_database_name
# и т.д.
```

> ⚠️ **Важно:** Никогда не коммитьте `.env` в Git. Файл уже добавлен в `.gitignore`.

### Структура проекта

```
fintech-stream-analyzer/
├── core/              # Модели данных, enum, JPA-сущности
├── generator/         # Генератор транзакций (Kafka Producer)
├── spark-consumer/    # Spark Streaming Consumer (PostgreSQL Writer)
├── api/               # REST API (Spring Boot)
├── pom.xml            # Parent POM (multi-module)
├── docker-compose.yml # Оркестрация контейнеров
└── Dockerfile         # Сборка приложения
```

### Сборка и запуск

```bash
# 1. Создайте .env файл (если ещё не создан)
cp .env.example .env

# 2. Собрать все модули и создать Docker образы
./mvnw clean install

# 3. Запустить все сервисы (Kafka + PostgreSQL + generator + spark-consumer + api)
docker compose up -d
```

### Локальная разработка

```bash
# Собрать конкретный модуль
./mvnw clean install -pl core -DskipTests

# Запустить API локально (после запуска postgres и kafka в docker)
cd api && java -jar target/api-0.0.1-SNAPSHOT.jar
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

## 🧪 Тесты

```bash
# Запустить тесты для core модуля
./mvnw test -pl core

# Запустить все тесты
./mvnw test
```

### Текущее покрытие

| Модуль | Статус | Кол-во тестов |
|--------|--------|---------------|
| `core` | ✅ 51 тест | 51 |
| `api` | ⏳ В процессе | - |
| `generator` | ⏳ В процессе | - |
| `spark-consumer` | ⏳ В процессе | - |

---

## 🔮 Roadmap

- [x] Генератор транзакций (Kafka Producer)
- [x] Spark Streaming consumer (Kafka → PostgreSQL)
- [x] Docker Compose оркестрация (Kafka + PostgreSQL + app)
- [x] REST API модуль (Spring Boot)
- [x] Тесты для core модуля (51 тест)
- [ ] Тесты для api, generator, spark-consumer
- [ ] Фильтрация и сортировка API
- [ ] Валидация входных данных
- [ ] Аутентификация (Spring Security)
- [ ] Метрики (Actuator + Prometheus)
- [ ] CI/CD, алертинг, визуализация

---

## 🛠️ Разработка

### Сборка
```bash
# Собрать все модули
./mvnw clean install

# Собрать без тестов
./mvnw clean install -DskipTests

# Собрать конкретный модуль
./mvnw clean install -pl core
```

### Управление сервисами
```bash
# Запустить отдельные сервисы
docker compose up -d generator spark-consumer

# Перезапустить сервис
docker compose restart generator

# Пересобрать и запустить
docker compose build spark-consumer && docker compose up -d spark-consumer

# Пересобрать API
docker compose build api && docker compose up -d api

# Остановить все
docker compose down
```

