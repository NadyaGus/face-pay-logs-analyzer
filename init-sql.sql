-- Создание таблицы для критичных логов
CREATE TABLE IF NOT EXISTS critical_logs (
    id SERIAL PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    account_id VARCHAR(255) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL,
    merchant_id VARCHAR(255) NOT NULL,
    error_code VARCHAR(100),
    error_message TEXT,
    metadata TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Индекс для быстрого поиска по transaction_id
CREATE INDEX IF NOT EXISTS idx_critical_logs_transaction_id ON critical_logs(transaction_id);

-- Индекс для быстрого поиска по timestamp
CREATE INDEX IF NOT EXISTS idx_critical_logs_timestamp ON critical_logs(timestamp);

-- Индекс для поиска по статусу
CREATE INDEX IF NOT EXISTS idx_critical_logs_status ON critical_logs(status);
