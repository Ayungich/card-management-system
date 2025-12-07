-- ============================================================================
-- Card Management System - DDL Script
-- Создание схемы базы данных PostgreSQL 16
-- Версия: 1.0
-- Дата: 2025-12-07
-- ============================================================================

-- Удаление существующих таблиц (если есть)
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS cards CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================================================
-- 1. ТАБЛИЦА: USERS (Пользователи)
-- ============================================================================
-- Назначение: Хранение информации о пользователях системы
-- Связи: One-to-Many с cards, user_roles, audit_logs
-- ============================================================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Ограничения
    CONSTRAINT chk_username_length CHECK (LENGTH(username) >= 3),
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$')
);

-- Комментарии
COMMENT ON TABLE users IS 'Пользователи системы (клиенты и администраторы)';
COMMENT ON COLUMN users.id IS 'Уникальный идентификатор пользователя';
COMMENT ON COLUMN users.username IS 'Имя пользователя для входа (3-50 символов)';
COMMENT ON COLUMN users.email IS 'Email пользователя (уникальный)';
COMMENT ON COLUMN users.password_hash IS 'Хеш пароля (BCrypt, cost=10)';
COMMENT ON COLUMN users.created_at IS 'Дата регистрации';
COMMENT ON COLUMN users.updated_at IS 'Дата последнего обновления';

-- ============================================================================
-- 2. ТАБЛИЦА: ROLES (Роли)
-- ============================================================================
-- Назначение: Справочник ролей пользователей
-- Связи: Many-to-Many с users через user_roles
-- ============================================================================

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(20) NOT NULL UNIQUE,
    
    -- Ограничения
    CONSTRAINT chk_role_name CHECK (name IN ('USER', 'ADMIN'))
);

-- Комментарии
COMMENT ON TABLE roles IS 'Справочник ролей пользователей';
COMMENT ON COLUMN roles.id IS 'Уникальный идентификатор роли';
COMMENT ON COLUMN roles.name IS 'Название роли (USER, ADMIN)';

-- Предзаполнение ролей
INSERT INTO roles (name) VALUES ('USER'), ('ADMIN');

-- ============================================================================
-- 3. ТАБЛИЦА: USER_ROLES (Связь пользователей и ролей)
-- ============================================================================
-- Назначение: Many-to-Many связь между users и roles
-- Связи: Many-to-One с users и roles
-- ============================================================================

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    
    -- Первичный ключ
    PRIMARY KEY (user_id, role_id),
    
    -- Внешние ключи
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) 
        REFERENCES roles(id) ON DELETE CASCADE
);

-- Комментарии
COMMENT ON TABLE user_roles IS 'Связь пользователей и ролей (Many-to-Many)';
COMMENT ON COLUMN user_roles.user_id IS 'Ссылка на пользователя';
COMMENT ON COLUMN user_roles.role_id IS 'Ссылка на роль';

-- Индексы
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- ============================================================================
-- 4. ТАБЛИЦА: CARDS (Банковские карты)
-- ============================================================================
-- Назначение: Хранение информации о банковских картах
-- Связи: Many-to-One с users, One-to-Many с transactions
-- ============================================================================

CREATE TABLE cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    card_number_encrypted VARCHAR(255) NOT NULL UNIQUE,
    owner_id UUID NOT NULL,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    expiration_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Ограничения
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT chk_card_status CHECK (status IN ('ACTIVE', 'BLOCKED', 'EXPIRED')),
    
    -- Внешние ключи
    CONSTRAINT fk_cards_owner FOREIGN KEY (owner_id) 
        REFERENCES users(id) ON DELETE CASCADE
);

-- Комментарии
COMMENT ON TABLE cards IS 'Банковские карты пользователей';
COMMENT ON COLUMN cards.id IS 'Уникальный идентификатор карты';
COMMENT ON COLUMN cards.card_number_encrypted IS 'Зашифрованный номер карты (AES-256)';
COMMENT ON COLUMN cards.owner_id IS 'Владелец карты (ссылка на users)';
COMMENT ON COLUMN cards.balance IS 'Текущий баланс в рублях (не может быть отрицательным)';
COMMENT ON COLUMN cards.expiration_date IS 'Срок действия карты';
COMMENT ON COLUMN cards.status IS 'Статус карты (ACTIVE, BLOCKED, EXPIRED)';
COMMENT ON COLUMN cards.created_at IS 'Дата выпуска карты';
COMMENT ON COLUMN cards.updated_at IS 'Дата последнего изменения';

-- Индексы
CREATE INDEX idx_cards_owner_id ON cards(owner_id);
CREATE INDEX idx_cards_status ON cards(status);
CREATE INDEX idx_cards_expiration_date ON cards(expiration_date);

-- ============================================================================
-- 5. ТАБЛИЦА: TRANSACTIONS (Транзакции)
-- ============================================================================
-- Назначение: История переводов между картами
-- Связи: Many-to-One с cards (дважды: from и to)
-- ============================================================================

CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_card_id UUID NOT NULL,
    to_card_id UUID NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(255),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Ограничения
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_transaction_status CHECK (status IN ('SUCCESS', 'FAILED')),
    CONSTRAINT chk_different_cards CHECK (from_card_id != to_card_id),
    
    -- Внешние ключи
    CONSTRAINT fk_transactions_from_card FOREIGN KEY (from_card_id) 
        REFERENCES cards(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_to_card FOREIGN KEY (to_card_id) 
        REFERENCES cards(id) ON DELETE CASCADE
);

-- Комментарии
COMMENT ON TABLE transactions IS 'История переводов между картами';
COMMENT ON COLUMN transactions.id IS 'Уникальный идентификатор транзакции';
COMMENT ON COLUMN transactions.from_card_id IS 'Карта-отправитель';
COMMENT ON COLUMN transactions.to_card_id IS 'Карта-получатель';
COMMENT ON COLUMN transactions.amount IS 'Сумма перевода (должна быть положительной)';
COMMENT ON COLUMN transactions.status IS 'Статус транзакции (SUCCESS, FAILED)';
COMMENT ON COLUMN transactions.failure_reason IS 'Причина отказа (если status=FAILED)';
COMMENT ON COLUMN transactions.timestamp IS 'Дата и время транзакции';

-- Индексы
CREATE INDEX idx_transactions_from_card ON transactions(from_card_id);
CREATE INDEX idx_transactions_to_card ON transactions(to_card_id);
CREATE INDEX idx_transactions_timestamp ON transactions(timestamp DESC);
CREATE INDEX idx_transactions_status ON transactions(status);

-- ============================================================================
-- 6. ТАБЛИЦА: AUDIT_LOGS (Логи аудита)
-- ============================================================================
-- Назначение: Системный аудит всех критичных действий
-- Связи: Many-to-One с users
-- ============================================================================

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    details TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Ограничения
    CONSTRAINT chk_audit_action CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'BLOCK', 'ACTIVATE', 'TRANSFER')),
    CONSTRAINT chk_audit_entity_type CHECK (entity_type IN ('Card', 'User', 'Transaction', 'Role')),
    
    -- Внешние ключи
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE SET NULL
);

-- Комментарии
COMMENT ON TABLE audit_logs IS 'Логи аудита всех критичных действий (append-only)';
COMMENT ON COLUMN audit_logs.id IS 'Уникальный идентификатор записи';
COMMENT ON COLUMN audit_logs.user_id IS 'Кто выполнил действие (NULL для системных действий)';
COMMENT ON COLUMN audit_logs.action IS 'Тип действия (CREATE, UPDATE, DELETE, BLOCK, ACTIVATE, TRANSFER)';
COMMENT ON COLUMN audit_logs.entity_type IS 'Тип сущности (Card, User, Transaction, Role)';
COMMENT ON COLUMN audit_logs.entity_id IS 'ID сущности, с которой выполнено действие';
COMMENT ON COLUMN audit_logs.details IS 'Дополнительные детали (JSON или текст)';
COMMENT ON COLUMN audit_logs.timestamp IS 'Дата и время действия';

-- Индексы
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp DESC);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);

-- ============================================================================
-- 7. ТРИГГЕРЫ
-- ============================================================================

-- Триггер для автоматического обновления updated_at в таблице users
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Триггер для автоматического обновления updated_at в таблице cards
CREATE TRIGGER trigger_cards_updated_at
    BEFORE UPDATE ON cards
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Триггер для автоматической проверки истекших карт
CREATE OR REPLACE FUNCTION check_card_expiration()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.expiration_date < CURRENT_DATE AND NEW.status != 'EXPIRED' THEN
        NEW.status = 'EXPIRED';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_card_expiration
    BEFORE INSERT OR UPDATE ON cards
    FOR EACH ROW
    EXECUTE FUNCTION check_card_expiration();

-- ============================================================================
-- 8. ПРЕДСТАВЛЕНИЯ (VIEWS)
-- ============================================================================

-- Представление для удобного просмотра карт с информацией о владельце
CREATE OR REPLACE VIEW v_cards_with_owners AS
SELECT 
    c.id AS card_id,
    c.card_number_encrypted,
    c.balance,
    c.expiration_date,
    c.status AS card_status,
    c.created_at AS card_created_at,
    u.id AS owner_id,
    u.username AS owner_username,
    u.email AS owner_email
FROM cards c
JOIN users u ON c.owner_id = u.id;

COMMENT ON VIEW v_cards_with_owners IS 'Карты с информацией о владельцах';

-- Представление для удобного просмотра транзакций с информацией о картах
CREATE OR REPLACE VIEW v_transactions_with_cards AS
SELECT 
    t.id AS transaction_id,
    t.amount,
    t.status AS transaction_status,
    t.failure_reason,
    t.timestamp,
    fc.id AS from_card_id,
    fc.card_number_encrypted AS from_card_number,
    fu.username AS from_user_username,
    tc.id AS to_card_id,
    tc.card_number_encrypted AS to_card_number,
    tu.username AS to_user_username
FROM transactions t
JOIN cards fc ON t.from_card_id = fc.id
JOIN users fu ON fc.owner_id = fu.id
JOIN cards tc ON t.to_card_id = tc.id
JOIN users tu ON tc.owner_id = tu.id;

COMMENT ON VIEW v_transactions_with_cards IS 'Транзакции с информацией о картах и владельцах';

-- Представление для статистики по пользователям
CREATE OR REPLACE VIEW v_user_statistics AS
SELECT 
    u.id AS user_id,
    u.username,
    u.email,
    COUNT(DISTINCT c.id) AS total_cards,
    COUNT(DISTINCT CASE WHEN c.status = 'ACTIVE' THEN c.id END) AS active_cards,
    COUNT(DISTINCT CASE WHEN c.status = 'BLOCKED' THEN c.id END) AS blocked_cards,
    COALESCE(SUM(c.balance), 0) AS total_balance,
    COUNT(DISTINCT t_out.id) AS outgoing_transactions,
    COUNT(DISTINCT t_in.id) AS incoming_transactions
FROM users u
LEFT JOIN cards c ON u.id = c.owner_id
LEFT JOIN transactions t_out ON c.id = t_out.from_card_id
LEFT JOIN transactions t_in ON c.id = t_in.to_card_id
GROUP BY u.id, u.username, u.email;

COMMENT ON VIEW v_user_statistics IS 'Статистика по пользователям (карты, транзакции, баланс)';

-- ============================================================================
-- 9. ФУНКЦИИ
-- ============================================================================

-- Функция для получения ролей пользователя
CREATE OR REPLACE FUNCTION get_user_roles(p_user_id UUID)
RETURNS TABLE(role_name VARCHAR) AS $$
BEGIN
    RETURN QUERY
    SELECT r.name
    FROM user_roles ur
    JOIN roles r ON ur.role_id = r.id
    WHERE ur.user_id = p_user_id;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_user_roles IS 'Получение списка ролей пользователя';

-- Функция для проверки, является ли пользователь администратором
CREATE OR REPLACE FUNCTION is_admin(p_user_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1
        FROM user_roles ur
        JOIN roles r ON ur.role_id = r.id
        WHERE ur.user_id = p_user_id AND r.name = 'ADMIN'
    );
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION is_admin IS 'Проверка, является ли пользователь администратором';

-- Функция для получения общей статистики системы
CREATE OR REPLACE FUNCTION get_system_statistics()
RETURNS TABLE(
    total_users BIGINT,
    total_cards BIGINT,
    active_cards BIGINT,
    blocked_cards BIGINT,
    expired_cards BIGINT,
    total_transactions BIGINT,
    successful_transactions BIGINT,
    failed_transactions BIGINT,
    total_volume DECIMAL(15, 2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        (SELECT COUNT(*) FROM users)::BIGINT,
        (SELECT COUNT(*) FROM cards)::BIGINT,
        (SELECT COUNT(*) FROM cards WHERE status = 'ACTIVE')::BIGINT,
        (SELECT COUNT(*) FROM cards WHERE status = 'BLOCKED')::BIGINT,
        (SELECT COUNT(*) FROM cards WHERE status = 'EXPIRED')::BIGINT,
        (SELECT COUNT(*) FROM transactions)::BIGINT,
        (SELECT COUNT(*) FROM transactions WHERE status = 'SUCCESS')::BIGINT,
        (SELECT COUNT(*) FROM transactions WHERE status = 'FAILED')::BIGINT,
        (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE status = 'SUCCESS')::DECIMAL(15, 2);
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_system_statistics IS 'Получение общей статистики системы';

-- ============================================================================
-- 10. ПРАВА ДОСТУПА
-- ============================================================================

-- Создание ролей БД (опционально, для production)
-- CREATE ROLE cms_app_user WITH LOGIN PASSWORD 'secure_password';
-- CREATE ROLE cms_readonly WITH LOGIN PASSWORD 'readonly_password';

-- Права для приложения (полный доступ к таблицам)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO cms_app_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO cms_app_user;

-- Права для readonly пользователя (только чтение)
-- GRANT SELECT ON ALL TABLES IN SCHEMA public TO cms_readonly;

-- Запрет на изменение/удаление логов аудита (только INSERT)
-- REVOKE UPDATE, DELETE ON audit_logs FROM cms_app_user;

-- ============================================================================
-- КОНЕЦ СКРИПТА
-- ============================================================================

-- Вывод информации о созданных объектах
SELECT 'DDL script executed successfully!' AS status;

SELECT 
    'Tables created: ' || COUNT(*) AS info
FROM information_schema.tables 
WHERE table_schema = 'public' AND table_type = 'BASE TABLE';

SELECT 
    'Views created: ' || COUNT(*) AS info
FROM information_schema.views 
WHERE table_schema = 'public';

SELECT 
    'Indexes created: ' || COUNT(*) AS info
FROM pg_indexes 
WHERE schemaname = 'public';

