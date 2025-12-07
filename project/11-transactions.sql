-- ============================================================================
-- Card Management System - Transactions
-- SQL транзакции для критичных операций
-- Версия: 1.0
-- Дата: 2025-12-07
-- ============================================================================

-- ============================================================================
-- ТРАНЗАКЦИЯ 1: Перевод средств между картами (FR-6)
-- ============================================================================
-- Описание: Атомарный перевод средств с одной карты на другую
-- Источник: FR-6 (Перевод средств между своими картами)
-- Требования ACID:
--   - Атомарность: либо все операции выполнены, либо ни одна
--   - Согласованность: баланс не может быть отрицательным
--   - Изолированность: параллельные переводы не конфликтуют (FOR UPDATE)
--   - Долговечность: изменения сохраняются после COMMIT
-- ============================================================================

BEGIN;

-- Переменные (в реальном приложении передаются как параметры)
-- from_card_id: UUID карты-отправителя
-- to_card_id: UUID карты-получателя
-- transfer_amount: DECIMAL сумма перевода
-- user_id: UUID пользователя, выполняющего перевод

-- Шаг 1: Проверка и блокировка карты-отправителя
SELECT 
    c.id,
    c.owner_id,
    c.balance,
    c.status,
    c.expiration_date
FROM cards c
WHERE c.id = 'from_card_id'::UUID
FOR UPDATE;  -- Блокировка строки для предотвращения параллельных изменений

-- Проверки (выполняются на уровне приложения):
-- 1. Карта существует
-- 2. Карта принадлежит пользователю (owner_id = user_id)
-- 3. Статус карты = 'ACTIVE'
-- 4. Срок действия не истек (expiration_date >= CURRENT_DATE)
-- 5. Баланс >= transfer_amount

-- Шаг 2: Проверка и блокировка карты-получателя
SELECT 
    c.id,
    c.owner_id,
    c.status,
    c.expiration_date
FROM cards c
WHERE c.id = 'to_card_id'::UUID
FOR UPDATE;

-- Проверки:
-- 1. Карта существует
-- 2. Карта принадлежит пользователю (owner_id = user_id)
-- 3. Статус карты = 'ACTIVE'
-- 4. Срок действия не истек
-- 5. from_card_id != to_card_id

-- Шаг 3: Списание с карты-отправителя
UPDATE cards
SET balance = balance - 100.00,  -- transfer_amount
    updated_at = CURRENT_TIMESTAMP
WHERE id = 'from_card_id'::UUID
  AND balance >= 100.00  -- Дополнительная проверка на уровне БД
  AND status = 'ACTIVE';

-- Проверка количества обновленных строк (должна быть 1)
-- Если 0, то откат транзакции (ROLLBACK)

-- Шаг 4: Зачисление на карту-получателя
UPDATE cards
SET balance = balance + 100.00,  -- transfer_amount
    updated_at = CURRENT_TIMESTAMP
WHERE id = 'to_card_id'::UUID
  AND status = 'ACTIVE';

-- Проверка количества обновленных строк (должна быть 1)

-- Шаг 5: Запись успешной транзакции
INSERT INTO transactions (
    from_card_id,
    to_card_id,
    amount,
    status,
    failure_reason
)
VALUES (
    'from_card_id'::UUID,
    'to_card_id'::UUID,
    100.00,  -- transfer_amount
    'SUCCESS',
    NULL
)
RETURNING id, timestamp;

-- Шаг 6: Запись в лог аудита
INSERT INTO audit_logs (
    user_id,
    action,
    entity_type,
    entity_id,
    details
)
VALUES (
    'user_id'::UUID,
    'TRANSFER',
    'Transaction',
    'transaction_id'::UUID,  -- ID из предыдущего INSERT
    'Transfer from card_id_1 to card_id_2, amount: 100.00'
);

-- Фиксация транзакции
COMMIT;

-- В случае ошибки на любом шаге:
-- ROLLBACK;

-- ============================================================================
-- ТРАНЗАКЦИЯ 2: Перевод с обработкой ошибки (недостаточно средств)
-- ============================================================================
-- Описание: Попытка перевода при недостаточном балансе
-- Результат: Транзакция записывается со статусом FAILED
-- ============================================================================

BEGIN;

-- Шаг 1: Проверка баланса
SELECT 
    c.id,
    c.balance,
    c.status
FROM cards c
WHERE c.id = 'from_card_id'::UUID
FOR UPDATE;

-- Если balance < transfer_amount, то:

-- Шаг 2: Запись неудачной транзакции
INSERT INTO transactions (
    from_card_id,
    to_card_id,
    amount,
    status,
    failure_reason
)
VALUES (
    'from_card_id'::UUID,
    'to_card_id'::UUID,
    100.00,
    'FAILED',
    'Insufficient balance'
);

-- Шаг 3: Запись в лог аудита
INSERT INTO audit_logs (
    user_id,
    action,
    entity_type,
    entity_id,
    details
)
VALUES (
    'user_id'::UUID,
    'TRANSFER',
    'Transaction',
    'transaction_id'::UUID,
    'Failed transfer: Insufficient balance'
);

-- Фиксация (транзакция записана как FAILED, баланс не изменен)
COMMIT;

-- ============================================================================
-- ТРАНЗАКЦИЯ 3: Создание пользователя с ролью (FR-1)
-- ============================================================================
-- Описание: Атомарное создание пользователя и назначение роли USER
-- Источник: FR-1 (Регистрация в системе)
-- ============================================================================

BEGIN;

-- Шаг 1: Проверка уникальности username и email
SELECT COUNT(*) 
FROM users 
WHERE username = 'new_user' OR email = 'new_user@example.com';

-- Если COUNT > 0, то ROLLBACK (пользователь уже существует)

-- Шаг 2: Вставка пользователя
INSERT INTO users (
    username,
    email,
    password_hash
)
VALUES (
    'new_user',
    'new_user@example.com',
    '$2a$10$hashed_password'
)
RETURNING id;

-- Шаг 3: Назначение роли USER
INSERT INTO user_roles (user_id, role_id)
SELECT 
    'new_user_id'::UUID,  -- ID из предыдущего INSERT
    r.id
FROM roles r
WHERE r.name = 'USER';

-- Шаг 4: Запись в лог аудита
INSERT INTO audit_logs (
    user_id,
    action,
    entity_type,
    entity_id,
    details
)
VALUES (
    'new_user_id'::UUID,
    'CREATE',
    'User',
    'new_user_id'::UUID,
    'User registered with role USER'
);

-- Фиксация
COMMIT;

-- ============================================================================
-- ТРАНЗАКЦИЯ 4: Создание карты с логированием (FR-9)
-- ============================================================================
-- Описание: Атомарное создание карты и запись в лог аудита
-- Источник: FR-9 (Создание новой карты для пользователя)
-- ============================================================================

BEGIN;

-- Шаг 1: Проверка существования пользователя
SELECT id 
FROM users 
WHERE id = 'owner_id'::UUID;

-- Если пользователь не найден, то ROLLBACK

-- Шаг 2: Проверка уникальности номера карты
SELECT COUNT(*) 
FROM cards 
WHERE card_number_encrypted = 'encrypted_card_number';

-- Если COUNT > 0, то ROLLBACK (номер карты уже существует)

-- Шаг 3: Вставка карты
INSERT INTO cards (
    card_number_encrypted,
    owner_id,
    balance,
    expiration_date,
    status
)
VALUES (
    'encrypted_card_number',
    'owner_id'::UUID,
    0.00,
    '2028-12-31'::DATE,
    'ACTIVE'
)
RETURNING id;

-- Шаг 4: Запись в лог аудита
INSERT INTO audit_logs (
    user_id,
    action,
    entity_type,
    entity_id,
    details
)
VALUES (
    'admin_user_id'::UUID,  -- Администратор, создавший карту
    'CREATE',
    'Card',
    'new_card_id'::UUID,  -- ID из предыдущего INSERT
    'Card created for user: owner_id'
);

-- Фиксация
COMMIT;

-- ============================================================================
-- ТРАНЗАКЦИЯ 5: Блокировка карты с логированием (FR-11)
-- ============================================================================
-- Описание: Атомарная блокировка карты и запись в лог аудита
-- Источник: FR-11 (Блокировка карты)
-- ============================================================================

BEGIN;

-- Шаг 1: Проверка существования и статуса карты
SELECT 
    id,
    status
FROM cards
WHERE id = 'card_id'::UUID
FOR UPDATE;

-- Если карта не найдена или уже заблокирована, можно пропустить обновление

-- Шаг 2: Блокировка карты
UPDATE cards
SET status = 'BLOCKED',
    updated_at = CURRENT_TIMESTAMP
WHERE id = 'card_id'::UUID
  AND status != 'BLOCKED';  -- Избегаем лишних обновлений

-- Шаг 3: Запись в лог аудита
INSERT INTO audit_logs (
    user_id,
    action,
    entity_type,
    entity_id,
    details
)
VALUES (
    'admin_user_id'::UUID,
    'BLOCK',
    'Card',
    'card_id'::UUID,
    'Card blocked by admin'
);

-- Фиксация
COMMIT;

-- ============================================================================
-- ТРАНЗАКЦИЯ 6: Удаление карты с логированием (FR-13)
-- ============================================================================
-- Описание: Атомарное удаление карты с предварительным логированием
-- Источник: FR-13 (Удаление карты)
-- Примечание: Связанные транзакции удаляются каскадно (ON DELETE CASCADE)
-- ============================================================================

BEGIN;

-- Шаг 1: Получение информации о карте для лога
SELECT 
    c.id,
    c.card_number_encrypted,
    c.owner_id,
    u.username
FROM cards c
JOIN users u ON c.owner_id = u.id
WHERE c.id = 'card_id'::UUID
FOR UPDATE;

-- Если карта не найдена, то ROLLBACK

-- Шаг 2: Запись в лог аудита (перед удалением)
INSERT INTO audit_logs (
    user_id,
    action,
    entity_type,
    entity_id,
    details
)
VALUES (
    'admin_user_id'::UUID,
    'DELETE',
    'Card',
    'card_id'::UUID,
    'Card deleted by admin. Owner: username'
);

-- Шаг 3: Удаление карты
DELETE FROM cards
WHERE id = 'card_id'::UUID;

-- Каскадное удаление:
-- - Все транзакции с from_card_id = card_id
-- - Все транзакции с to_card_id = card_id

-- Фиксация
COMMIT;

-- ============================================================================
-- ТРАНЗАКЦИЯ 7: Массовое обновление истекших карт (FR-18)
-- ============================================================================
-- Описание: Обновление статуса всех истекших карт (системная задача)
-- Источник: FR-18 (Автоматическая проверка срока действия карты)
-- ============================================================================

BEGIN;

-- Шаг 1: Обновление истекших карт
UPDATE cards
SET status = 'EXPIRED',
    updated_at = CURRENT_TIMESTAMP
WHERE expiration_date < CURRENT_DATE
  AND status != 'EXPIRED'
RETURNING id, card_number_encrypted, expiration_date;

-- Шаг 2: Запись в лог аудита для каждой обновленной карты
INSERT INTO audit_logs (
    user_id,
    action,
    entity_type,
    entity_id,
    details
)
SELECT 
    NULL,  -- Системное действие
    'UPDATE',
    'Card',
    c.id,
    'Card expired automatically on ' || CURRENT_DATE::TEXT
FROM cards c
WHERE c.expiration_date < CURRENT_DATE
  AND c.status = 'EXPIRED';

-- Фиксация
COMMIT;

-- ============================================================================
-- ПРИМЕРЫ ИСПОЛЬЗОВАНИЯ УРОВНЕЙ ИЗОЛЯЦИИ
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Пример 1: READ COMMITTED (по умолчанию в PostgreSQL)
-- ----------------------------------------------------------------------------
-- Описание: Транзакция видит только зафиксированные изменения других транзакций

BEGIN TRANSACTION ISOLATION LEVEL READ COMMITTED;

-- Запросы здесь
SELECT * FROM cards WHERE id = 'card_id'::UUID;

COMMIT;

-- ----------------------------------------------------------------------------
-- Пример 2: REPEATABLE READ
-- ----------------------------------------------------------------------------
-- Описание: Транзакция видит снимок данных на момент начала транзакции

BEGIN TRANSACTION ISOLATION LEVEL REPEATABLE READ;

-- Первый запрос
SELECT balance FROM cards WHERE id = 'card_id'::UUID;
-- Результат: balance = 1000

-- Другая транзакция изменяет balance на 500 и фиксируется

-- Второй запрос (в той же транзакции)
SELECT balance FROM cards WHERE id = 'card_id'::UUID;
-- Результат: balance = 1000 (не изменился, снимок данных)

COMMIT;

-- ----------------------------------------------------------------------------
-- Пример 3: SERIALIZABLE
-- ----------------------------------------------------------------------------
-- Описание: Наивысший уровень изоляции, транзакции выполняются последовательно

BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE;

-- Запросы здесь
-- Если возникает конфликт с другой транзакцией, одна из них откатывается

COMMIT;

-- ============================================================================
-- ОБРАБОТКА ОШИБОК И ОТКАТ
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Пример 4: Откат при ошибке
-- ----------------------------------------------------------------------------

BEGIN;

-- Попытка вставки
INSERT INTO users (username, email, password_hash)
VALUES ('existing_user', 'existing@example.com', 'hash');

-- Если возникает ошибка (например, нарушение уникальности):
-- ERROR: duplicate key value violates unique constraint "users_username_key"

-- Откат всех изменений
ROLLBACK;

-- ----------------------------------------------------------------------------
-- Пример 5: Savepoint (точка сохранения)
-- ----------------------------------------------------------------------------

BEGIN;

-- Операция 1
INSERT INTO users (username, email, password_hash)
VALUES ('user1', 'user1@example.com', 'hash1');

-- Создание точки сохранения
SAVEPOINT sp1;

-- Операция 2
INSERT INTO users (username, email, password_hash)
VALUES ('user2', 'user2@example.com', 'hash2');

-- Если операция 2 не удалась, откатываемся к sp1
ROLLBACK TO SAVEPOINT sp1;

-- Операция 1 сохранена, операция 2 отменена

-- Фиксация
COMMIT;

-- ============================================================================
-- ТЕСТИРОВАНИЕ ТРАНЗАКЦИЙ
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Тест 1: Успешный перевод
-- ----------------------------------------------------------------------------

-- Подготовка: создание тестовых данных
INSERT INTO users (id, username, email, password_hash) 
VALUES ('test_user_id'::UUID, 'test_user', 'test@example.com', 'hash');

INSERT INTO cards (id, card_number_encrypted, owner_id, balance, expiration_date, status)
VALUES 
    ('card1'::UUID, 'enc1', 'test_user_id'::UUID, 1000.00, '2028-12-31', 'ACTIVE'),
    ('card2'::UUID, 'enc2', 'test_user_id'::UUID, 500.00, '2028-12-31', 'ACTIVE');

-- Выполнение перевода (см. ТРАНЗАКЦИЯ 1)
-- from_card_id = 'card1', to_card_id = 'card2', amount = 100.00

-- Проверка результата
SELECT id, balance FROM cards WHERE id IN ('card1'::UUID, 'card2'::UUID);
-- Ожидается: card1.balance = 900.00, card2.balance = 600.00

SELECT * FROM transactions WHERE from_card_id = 'card1'::UUID ORDER BY timestamp DESC LIMIT 1;
-- Ожидается: status = 'SUCCESS', amount = 100.00

-- ----------------------------------------------------------------------------
-- Тест 2: Перевод с недостаточным балансом
-- ----------------------------------------------------------------------------

-- Попытка перевода 2000.00 с card1 (баланс 900.00)
-- Ожидается: транзакция со статусом FAILED, баланс не изменен

SELECT id, balance FROM cards WHERE id = 'card1'::UUID;
-- Ожидается: balance = 900.00 (не изменился)

SELECT * FROM transactions WHERE from_card_id = 'card1'::UUID AND status = 'FAILED' ORDER BY timestamp DESC LIMIT 1;
-- Ожидается: failure_reason = 'Insufficient balance'

-- ----------------------------------------------------------------------------
-- Тест 3: Параллельные переводы (проверка FOR UPDATE)
-- ----------------------------------------------------------------------------

-- Транзакция 1 (в одном сеансе):
BEGIN;
SELECT balance FROM cards WHERE id = 'card1'::UUID FOR UPDATE;
-- Блокирует строку

-- Транзакция 2 (в другом сеансе):
BEGIN;
SELECT balance FROM cards WHERE id = 'card1'::UUID FOR UPDATE;
-- Ожидает освобождения блокировки

-- Транзакция 1 завершается:
UPDATE cards SET balance = balance - 100 WHERE id = 'card1'::UUID;
COMMIT;

-- Транзакция 2 получает доступ и видит обновленный баланс
-- Результат: последовательное выполнение, нет race condition

-- ============================================================================
-- КОНЕЦ ФАЙЛА
-- ============================================================================

