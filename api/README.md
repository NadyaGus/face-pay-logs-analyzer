# FacePay API — Error Logs

REST API для работы с ошибками транзакций FaceID.

**Base URL:** `http://localhost:8080`  
**Prefix:** `/api/errors`

---

## Endpoints

### 1. Получить список ошибок (пагинация)

```
GET /api/errors?page=0&size=20
```

**Пример:**
```bash
curl "http://localhost:8080/api/errors?page=0&size=10"
```

**Response:** `Page<ErrorLog>` — пагинированный список с полями `content`, `totalElements`, `totalPages`, `number`.

---

### 2. Получить ошибку по ID

```
GET /api/errors/{id}
```

**Пример:**
```bash
curl http://localhost:8080/api/errors/1
```

**Response:** `ErrorLog` или `404 Not Found`.

---

### 3. Получить статистику по ошибкам

```
GET /api/errors/stats
```

**Пример:**
```bash
curl http://localhost:8080/api/errors/stats
```

**Response:** массив объектов с агрегацией по `errorCode`:

```json
[
  { "errorCode": "INSUFFICIENT_FUNDS", "count": 21 },
  { "errorCode": "NETWORK_DISCONNECTION", "count": 19 },
  ...
]
```

---

### 4. Поиск ошибок по accountId

```
GET /api/errors/search?accountId={accountId}
```

**Пример:**
```bash
curl "http://localhost:8080/api/errors/search?accountId=ACC-94889680"
```

**Response:** `List<ErrorLog>` — ошибки, где `accountId` содержит указанное значение.

---

## Структура ErrorLog

| Поле | Тип | Описание |
|------|-----|----------|
| `id` | Long | Уникальный идентификатор |
| `transactionId` | String | UUID транзакции |
| `timestamp` | Timestamp | Временная метка транзакции |
| `accountId` | String | ID аккаунта (ACC-xxxxxxxx) |
| `amount` | BigDecimal | Сумма |
| `currency` | String | Валюта (RUB) |
| `status` | String | Статус (FAILED) |
| `merchantId` | String | ID мерчанта |
| `errorCode` | String | Код ошибки |
| `errorMessage` | String | Описание ошибки |
| `metadata` | String | Дополнительные данные |
| `createdAt` | Timestamp | Время создания записи |

---

## Запуск

```bash
docker compose up -d
```

Swagger UI доступен на: `http://localhost:8080/swagger-ui.html`

## Roadmap
[+] Запросы GET
[] Фильтрация и сортировка GET запросов (`errorCode`, `merchantId`, `from`, `to`, `sort`)
[] Запросы POST (`POST /api/errors` — создание, `PATCH /api/errors/{id}` — обновление)
[] Валидация запросов (@NotNull, @Size, кастомный @ErrorCode)
[] Расширенная статистика (totalErrors, avgPerDay, top N, группировка по времени)
[] DELETE / Архивация (`DELETE /api/errors?before=...` — удаление старых записей)
[] Health checks (`/api/health`, `/api/db-health` — проверка)


