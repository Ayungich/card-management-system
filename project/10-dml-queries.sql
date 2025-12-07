-- ============================================================================
-- Card Management System - DML Queries
-- SQL запросы для реализации функциональных требований
-- Версия: 1.0
-- Дата: 2025-12-07
-- ============================================================================

-- ============================================================================
-- РАЗДЕЛ 1: ЗАПРОСЫ ДЛЯ ПОЛЬЗОВАТЕЛЕЙ (USER)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Запрос 1: Регистрация нового пользователя (FR-1)
-- ----------------------------------------------------------------------------
-- Описание: Создание нового пользователя с автоматическим назначением роли USER
-- Источник: FR-1 (Регистрация в системе)
-- ----------------------------------------------------------------------------

-- Вставка пользователя
INSERT INTO users (username, email, password_hash)
VALUES (
    'john_doe',
    'john@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'  -- BCrypt hash для 'password123'
)
RETURNING id, username, email, created_at;

-- Назначение роли USER (предполагаем, что id пользователя = 'user_id_here')
INSERT INTO user_roles (user_id, role_id)
SELECT 
    'user_id_here'::UUID,  -- Замените на реальный UUID из предыдущего INSERT
    r.id
FROM roles r
WHERE r.name = 'USER';

-- Альтернативный вариант с подзапросом (если username известен)
INSERT INTO user_roles (user_id, role_id)
SELECT 
    u.id,
    r.id
FROM users u, roles r
WHERE u.username = 'john_doe' AND r.name = 'USER';

-- ----------------------------------------------------------------------------
-- Запрос 2: Авторизация пользователя (FR-2)
-- ----------------------------------------------------------------------------
-- Описание: Получение информации о пользователе и его ролях для проверки пароля
-- Источник: FR-2 (Авторизация в системе)
-- ----------------------------------------------------------------------------

SELECT 
    u.id,
    u.username,
    u.email,
    u.password_hash,
    ARRAY_AGG(r.name) AS roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'john_doe' OR u.email = 'john@example.com'
GROUP BY u.id, u.username, u.email, u.password_hash;

-- ----------------------------------------------------------------------------
-- Запрос 3: Просмотр списка своих карт с фильтрацией (FR-3)
-- ----------------------------------------------------------------------------
-- Описание: Получение всех карт пользователя с пагинацией и фильтрацией по статусу
-- Источник: FR-3 (Просмотр списка своих карт)
-- ----------------------------------------------------------------------------

-- Без фильтрации (все карты пользователя)
SELECT 
    c.id,
    c.card_number_encrypted,
    c.balance,
    c.expiration_date,
    c.status,
    c.created_at,
    c.updated_at
FROM cards c
WHERE c.owner_id = 'user_id_here'::UUID
ORDER BY c.created_at DESC
LIMIT 10 OFFSET 0;  -- Пагинация: page=0, size=10

-- С фильтрацией по статусу
SELECT 
    c.id,
    c.card_number_encrypted,
    c.balance,
    c.expiration_date,
    c.status,
    c.created_at
FROM cards c
WHERE c.owner_id = 'user_id_here'::UUID
  AND c.status = 'ACTIVE'
ORDER BY c.created_at DESC
LIMIT 10 OFFSET 0;

-- Подсчет общего количества карт (для пагинации)
SELECT COUNT(*) AS total_cards
FROM cards c
WHERE c.owner_id = 'user_id_here'::UUID
  AND (c.status = 'ACTIVE' OR 'ACTIVE' IS NULL);  -- Фильтр опционален

-- ----------------------------------------------------------------------------
-- Запрос 4: Просмотр детальной информации по карте (FR-4)
-- ----------------------------------------------------------------------------
-- Описание: Получение полной информации о конкретной карте
-- Источник: FR-4 (Просмотр детальной информации по карте)
-- ----------------------------------------------------------------------------

SELECT 
    c.id,
    c.card_number_encrypted,
    c.balance,
    c.expiration_date,
    c.status,
    c.created_at,
    c.updated_at
FROM cards c
WHERE c.id = 'card_id_here'::UUID
  AND c.owner_id = 'user_id_here'::UUID;  -- Проверка владения

-- ----------------------------------------------------------------------------
-- Запрос 5: Просмотр баланса карты (FR-5)
-- ----------------------------------------------------------------------------
-- Описание: Получение текущего баланса карты
-- Источник: FR-5 (Просмотр баланса карты)
-- ----------------------------------------------------------------------------

SELECT 
    c.id,
    c.balance,
    'RUB' AS currency,
    c.updated_at AS last_updated
FROM cards c
WHERE c.id = 'card_id_here'::UUID
  AND c.owner_id = 'user_id_here'::UUID;

-- ----------------------------------------------------------------------------
-- Запрос 6: История транзакций пользователя (FR-7)
-- ----------------------------------------------------------------------------
-- Описание: Получение истории транзакций по картам пользователя
-- Источник: FR-7 (Просмотр истории транзакций)
-- ----------------------------------------------------------------------------

-- История транзакций по конкретной карте
SELECT 
    t.id,
    t.from_card_id,
    t.to_card_id,
    t.amount,
    t.status,
    t.failure_reason,
    t.timestamp,
    CASE 
        WHEN t.from_card_id = 'card_id_here'::UUID THEN 'OUTGOING'
        ELSE 'INCOMING'
    END AS direction
FROM transactions t
WHERE t.from_card_id = 'card_id_here'::UUID
   OR t.to_card_id = 'card_id_here'::UUID
ORDER BY t.timestamp DESC
LIMIT 20 OFFSET 0;

-- История всех транзакций пользователя (по всем его картам)
SELECT 
    t.id,
    t.from_card_id,
    t.to_card_id,
    t.amount,
    t.status,
    t.failure_reason,
    t.timestamp,
    CASE 
        WHEN fc.owner_id = 'user_id_here'::UUID THEN 'OUTGOING'
        ELSE 'INCOMING'
    END AS direction
FROM transactions t
JOIN cards fc ON t.from_card_id = fc.id
JOIN cards tc ON t.to_card_id = tc.id
WHERE fc.owner_id = 'user_id_here'::UUID
   OR tc.owner_id = 'user_id_here'::UUID
ORDER BY t.timestamp DESC
LIMIT 20 OFFSET 0;

-- С фильтрацией по статусу и периоду
SELECT 
    t.id,
    t.from_card_id,
    t.to_card_id,
    t.amount,
    t.status,
    t.timestamp
FROM transactions t
JOIN cards fc ON t.from_card_id = fc.id
WHERE fc.owner_id = 'user_id_here'::UUID
  AND (t.status = 'SUCCESS' OR 'SUCCESS' IS NULL)  -- Фильтр по статусу
  AND (t.timestamp >= '2025-01-01'::TIMESTAMP OR '2025-01-01'::TIMESTAMP IS NULL)  -- Начало периода
  AND (t.timestamp <= '2025-12-31'::TIMESTAMP OR '2025-12-31'::TIMESTAMP IS NULL)  -- Конец периода
ORDER BY t.timestamp DESC
LIMIT 20 OFFSET 0;

-- ============================================================================
-- РАЗДЕЛ 2: ЗАПРОСЫ ДЛЯ АДМИНИСТРАТОРОВ (ADMIN)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Запрос 7: Создание новой карты для пользователя (FR-9)
-- ----------------------------------------------------------------------------
-- Описание: Администратор создает карту для пользователя
-- Источник: FR-9 (Создание новой карты для пользователя)
-- ----------------------------------------------------------------------------

INSERT INTO cards (
    card_number_encrypted,
    owner_id,
    balance,
    expiration_date,
    status
)
VALUES (
    'encrypted_card_number_here',  -- Номер карты, зашифрованный AES-256
    'user_id_here'::UUID,
    0.00,  -- Начальный баланс
    '2028-12-31'::DATE,  -- Срок действия (+3 года)
    'ACTIVE'
)
RETURNING id, card_number_encrypted, balance, expiration_date, status, created_at;

-- Запись в лог аудита
INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details)
VALUES (
    'admin_user_id'::UUID,
    'CREATE',
    'Card',
    'new_card_id'::UUID,  -- ID созданной карты
    'Card created for user: user_id_here'
);

-- ----------------------------------------------------------------------------
-- Запрос 8: Просмотр всех пользователей (FR-10)
-- ----------------------------------------------------------------------------
-- Описание: Получение списка всех пользователей с их ролями
-- Источник: FR-10 (Просмотр всех пользователей)
-- ----------------------------------------------------------------------------

SELECT 
    u.id,
    u.username,
    u.email,
    u.created_at,
    ARRAY_AGG(r.name) AS roles,
    COUNT(DISTINCT c.id) AS total_cards
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
LEFT JOIN cards c ON u.id = c.owner_id
GROUP BY u.id, u.username, u.email, u.created_at
ORDER BY u.created_at DESC
LIMIT 10 OFFSET 0;

-- С фильтрацией по роли
SELECT 
    u.id,
    u.username,
    u.email,
    u.created_at
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE r.name = 'ADMIN'
ORDER BY u.created_at DESC;

-- С поиском по username или email
SELECT 
    u.id,
    u.username,
    u.email,
    u.created_at
FROM users u
WHERE u.username ILIKE '%john%'
   OR u.email ILIKE '%john%'
ORDER BY u.created_at DESC
LIMIT 10 OFFSET 0;

-- ----------------------------------------------------------------------------
-- Запрос 9: Блокировка карты (FR-11)
-- ----------------------------------------------------------------------------
-- Описание: Администратор блокирует карту
-- Источник: FR-11 (Блокировка карты)
-- ----------------------------------------------------------------------------

UPDATE cards
SET status = 'BLOCKED',
    updated_at = CURRENT_TIMESTAMP
WHERE id = 'card_id_here'::UUID
RETURNING id, status, updated_at;

-- Запись в лог аудита
INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details)
VALUES (
    'admin_user_id'::UUID,
    'BLOCK',
    'Card',
    'card_id_here'::UUID,
    'Card blocked by admin'
);

-- ----------------------------------------------------------------------------
-- Запрос 10: Активация карты (FR-12)
-- ----------------------------------------------------------------------------
-- Описание: Администратор активирует заблокированную карту
-- Источник: FR-12 (Активация карты)
-- ----------------------------------------------------------------------------

UPDATE cards
SET status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE id = 'card_id_here'::UUID
  AND status = 'BLOCKED'  -- Нельзя активировать истекшую карту
  AND expiration_date >= CURRENT_DATE  -- Проверка срока действия
RETURNING id, status, updated_at;

-- Запись в лог аудита
INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details)
VALUES (
    'admin_user_id'::UUID,
    'ACTIVATE',
    'Card',
    'card_id_here'::UUID,
    'Card activated by admin'
);

-- ----------------------------------------------------------------------------
-- Запрос 11: Удаление карты (FR-13)
-- ----------------------------------------------------------------------------
-- Описание: Администратор удаляет карту из системы
-- Источник: FR-13 (Удаление карты)
-- ----------------------------------------------------------------------------

-- Запись в лог аудита (перед удалением)
INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details)
SELECT 
    'admin_user_id'::UUID,
    'DELETE',
    'Card',
    c.id,
    'Card deleted by admin. Owner: ' || u.username
FROM cards c
JOIN users u ON c.owner_id = u.id
WHERE c.id = 'card_id_here'::UUID;

-- Удаление карты
DELETE FROM cards
WHERE id = 'card_id_here'::UUID
RETURNING id;

-- ----------------------------------------------------------------------------
-- Запрос 12: Просмотр всех карт (FR-14)
-- ----------------------------------------------------------------------------
-- Описание: Получение списка всех карт в системе с информацией о владельцах
-- Источник: FR-14 (Просмотр всех карт)
-- ----------------------------------------------------------------------------

-- Все карты с информацией о владельцах
SELECT 
    c.id,
    c.card_number_encrypted,
    c.balance,
    c.expiration_date,
    c.status,
    c.created_at,
    u.id AS owner_id,
    u.username AS owner_username,
    u.email AS owner_email
FROM cards c
JOIN users u ON c.owner_id = u.id
ORDER BY c.created_at DESC
LIMIT 10 OFFSET 0;

-- С фильтрацией по владельцу
SELECT 
    c.id,
    c.card_number_encrypted,
    c.balance,
    c.status,
    c.created_at
FROM cards c
WHERE c.owner_id = 'user_id_here'::UUID
ORDER BY c.created_at DESC;

-- С фильтрацией по статусу
SELECT 
    c.id,
    c.card_number_encrypted,
    c.balance,
    c.status,
    u.username AS owner_username
FROM cards c
JOIN users u ON c.owner_id = u.id
WHERE c.status = 'BLOCKED'
ORDER BY c.created_at DESC;

-- С фильтрацией по периоду создания
SELECT 
    c.id,
    c.card_number_encrypted,
    c.status,
    c.created_at,
    u.username AS owner_username
FROM cards c
JOIN users u ON c.owner_id = u.id
WHERE c.created_at >= '2025-01-01'::TIMESTAMP
  AND c.created_at <= '2025-12-31'::TIMESTAMP
ORDER BY c.created_at DESC;

-- ----------------------------------------------------------------------------
-- Запрос 13: Просмотр всех транзакций (FR-15)
-- ----------------------------------------------------------------------------
-- Описание: Получение списка всех транзакций с фильтрацией
-- Источник: FR-15 (Просмотр всех транзакций)
-- ----------------------------------------------------------------------------

-- Все транзакции с информацией о картах
SELECT 
    t.id,
    t.from_card_id,
    t.to_card_id,
    t.amount,
    t.status,
    t.failure_reason,
    t.timestamp,
    fu.username AS from_user,
    tu.username AS to_user
FROM transactions t
JOIN cards fc ON t.from_card_id = fc.id
JOIN users fu ON fc.owner_id = fu.id
JOIN cards tc ON t.to_card_id = tc.id
JOIN users tu ON tc.owner_id = tu.id
ORDER BY t.timestamp DESC
LIMIT 10 OFFSET 0;

-- С фильтрацией по статусу
SELECT 
    t.id,
    t.from_card_id,
    t.to_card_id,
    t.amount,
    t.status,
    t.failure_reason,
    t.timestamp
FROM transactions t
WHERE t.status = 'FAILED'
ORDER BY t.timestamp DESC;

-- С фильтрацией по периоду
SELECT 
    t.id,
    t.amount,
    t.status,
    t.timestamp
FROM transactions t
WHERE t.timestamp >= '2025-01-01'::TIMESTAMP
  AND t.timestamp <= '2025-12-31'::TIMESTAMP
ORDER BY t.timestamp DESC;

-- С комбинированной фильтрацией (статус + период)
SELECT 
    t.id,
    t.from_card_id,
    t.to_card_id,
    t.amount,
    t.status,
    t.timestamp
FROM transactions t
WHERE (t.status = 'SUCCESS' OR 'SUCCESS' IS NULL)
  AND (t.timestamp >= '2025-01-01'::TIMESTAMP OR '2025-01-01'::TIMESTAMP IS NULL)
  AND (t.timestamp <= '2025-12-31'::TIMESTAMP OR '2025-12-31'::TIMESTAMP IS NULL)
ORDER BY t.timestamp DESC
LIMIT 10 OFFSET 0;

-- ----------------------------------------------------------------------------
-- Запрос 14: Просмотр логов аудита (FR-16)
-- ----------------------------------------------------------------------------
-- Описание: Получение логов всех действий в системе
-- Источник: FR-16 (Просмотр логов аудита)
-- ----------------------------------------------------------------------------

-- Все логи с информацией о пользователях
SELECT 
    al.id,
    al.user_id,
    u.username,
    al.action,
    al.entity_type,
    al.entity_id,
    al.details,
    al.timestamp
FROM audit_logs al
LEFT JOIN users u ON al.user_id = u.id
ORDER BY al.timestamp DESC
LIMIT 50 OFFSET 0;

-- С фильтрацией по пользователю
SELECT 
    al.id,
    al.action,
    al.entity_type,
    al.entity_id,
    al.details,
    al.timestamp
FROM audit_logs al
WHERE al.user_id = 'user_id_here'::UUID
ORDER BY al.timestamp DESC;

-- С фильтрацией по типу действия
SELECT 
    al.id,
    al.user_id,
    u.username,
    al.entity_type,
    al.entity_id,
    al.details,
    al.timestamp
FROM audit_logs al
LEFT JOIN users u ON al.user_id = u.id
WHERE al.action = 'BLOCK'
ORDER BY al.timestamp DESC;

-- С фильтрацией по типу сущности
SELECT 
    al.id,
    al.action,
    al.entity_id,
    al.details,
    al.timestamp
FROM audit_logs al
WHERE al.entity_type = 'Card'
ORDER BY al.timestamp DESC;

-- С комбинированной фильтрацией
SELECT 
    al.id,
    al.user_id,
    u.username,
    al.action,
    al.entity_type,
    al.entity_id,
    al.timestamp
FROM audit_logs al
LEFT JOIN users u ON al.user_id = u.id
WHERE (al.user_id = 'user_id_here'::UUID OR 'user_id_here'::UUID IS NULL)
  AND (al.action = 'CREATE' OR 'CREATE' IS NULL)
  AND (al.entity_type = 'Card' OR 'Card' IS NULL)
  AND (al.timestamp >= '2025-01-01'::TIMESTAMP OR '2025-01-01'::TIMESTAMP IS NULL)
  AND (al.timestamp <= '2025-12-31'::TIMESTAMP OR '2025-12-31'::TIMESTAMP IS NULL)
ORDER BY al.timestamp DESC
LIMIT 50 OFFSET 0;

-- ----------------------------------------------------------------------------
-- Запрос 15: Получение статистики по системе (FR-17)
-- ----------------------------------------------------------------------------
-- Описание: Общая статистика системы
-- Источник: FR-17 (Получение статистики по системе)
-- ----------------------------------------------------------------------------

-- Использование функции get_system_statistics()
SELECT * FROM get_system_statistics();

-- Альтернативный вариант (без функции)
SELECT 
    (SELECT COUNT(*) FROM users) AS total_users,
    (SELECT COUNT(*) FROM cards) AS total_cards,
    (SELECT COUNT(*) FROM cards WHERE status = 'ACTIVE') AS active_cards,
    (SELECT COUNT(*) FROM cards WHERE status = 'BLOCKED') AS blocked_cards,
    (SELECT COUNT(*) FROM cards WHERE status = 'EXPIRED') AS expired_cards,
    (SELECT COUNT(*) FROM transactions) AS total_transactions,
    (SELECT COUNT(*) FROM transactions WHERE status = 'SUCCESS') AS successful_transactions,
    (SELECT COUNT(*) FROM transactions WHERE status = 'FAILED') AS failed_transactions,
    (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE status = 'SUCCESS') AS total_volume;

-- Статистика по пользователям (топ-10 по балансу)
SELECT 
    u.id,
    u.username,
    COUNT(c.id) AS total_cards,
    COALESCE(SUM(c.balance), 0) AS total_balance
FROM users u
LEFT JOIN cards c ON u.id = c.owner_id
GROUP BY u.id, u.username
ORDER BY total_balance DESC
LIMIT 10;

-- Статистика по транзакциям (по дням)
SELECT 
    DATE(t.timestamp) AS transaction_date,
    COUNT(*) AS total_transactions,
    COUNT(CASE WHEN t.status = 'SUCCESS' THEN 1 END) AS successful,
    COUNT(CASE WHEN t.status = 'FAILED' THEN 1 END) AS failed,
    COALESCE(SUM(CASE WHEN t.status = 'SUCCESS' THEN t.amount ELSE 0 END), 0) AS total_volume
FROM transactions t
WHERE t.timestamp >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY DATE(t.timestamp)
ORDER BY transaction_date DESC;

-- ============================================================================
-- РАЗДЕЛ 3: СИСТЕМНЫЕ ЗАПРОСЫ
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Запрос 16: Автоматическая проверка истекших карт (FR-18)
-- ----------------------------------------------------------------------------
-- Описание: Обновление статуса истекших карт (запускается по расписанию)
-- Источник: FR-18 (Автоматическая проверка срока действия карты)
-- ----------------------------------------------------------------------------

UPDATE cards
SET status = 'EXPIRED',
    updated_at = CURRENT_TIMESTAMP
WHERE expiration_date < CURRENT_DATE
  AND status != 'EXPIRED'
RETURNING id, card_number_encrypted, expiration_date;

-- Запись в лог аудита для каждой истекшей карты
INSERT INTO audit_logs (user_id, action, entity_type, entity_id, details)
SELECT 
    NULL,  -- Системное действие
    'UPDATE',
    'Card',
    c.id,
    'Card expired automatically'
FROM cards c
WHERE c.expiration_date < CURRENT_DATE
  AND c.status = 'EXPIRED';

-- ----------------------------------------------------------------------------
-- Запрос 17: Поиск карт по номеру (частичное совпадение)
-- ----------------------------------------------------------------------------
-- Описание: Поиск карт по последним 4 цифрам номера
-- Примечание: В реальной системе номер зашифрован, поиск выполняется на уровне приложения
-- ----------------------------------------------------------------------------

-- Поиск по зашифрованному номеру (точное совпадение)
SELECT 
    c.id,
    c.card_number_encrypted,
    c.balance,
    c.status,
    u.username AS owner_username
FROM cards c
JOIN users u ON c.owner_id = u.id
WHERE c.card_number_encrypted = 'encrypted_card_number_here';

-- ----------------------------------------------------------------------------
-- Запрос 18: Получение топ-10 самых активных пользователей
-- ----------------------------------------------------------------------------
-- Описание: Пользователи с наибольшим количеством транзакций
-- ----------------------------------------------------------------------------

SELECT 
    u.id,
    u.username,
    COUNT(DISTINCT t.id) AS total_transactions,
    COALESCE(SUM(CASE WHEN t.status = 'SUCCESS' THEN t.amount ELSE 0 END), 0) AS total_volume
FROM users u
JOIN cards c ON u.id = c.owner_id
JOIN transactions t ON c.id = t.from_card_id
GROUP BY u.id, u.username
ORDER BY total_transactions DESC
LIMIT 10;

-- ============================================================================
-- КОНЕЦ ФАЙЛА
-- ============================================================================

