# 7. Нормализация схемы базы данных

## 7.1 Введение

Нормализация — процесс организации данных в БД для устранения избыточности и аномалий. В этом разделе показан пошаговый процесс нормализации схемы Card Management System от 1НФ до 4НФ.

---

## 7.2 Исходная ненормализованная схема (0НФ)

Представим, что изначально все данные хранились в одной таблице:

```
USER_CARD_DATA (
    user_id,
    username,
    email,
    password_hash,
    roles,                      -- список ролей (повторяющаяся группа)
    cards                       -- список карт (повторяющаяся группа)
        ├─ card_id
        ├─ card_number
        ├─ balance
        ├─ status
        └─ transactions         -- список транзакций (вложенная повторяющаяся группа)
            ├─ transaction_id
            ├─ to_card_id
            ├─ amount
            └─ timestamp
)
```

**Проблемы:**
- ❌ Повторяющиеся группы (roles, cards, transactions)
- ❌ Вложенные структуры
- ❌ Невозможность хранить карту без транзакций
- ❌ Дублирование данных пользователя для каждой карты

---

## 7.3 Приведение к 1НФ (Первая нормальная форма)

### 7.3.1 Требования 1НФ
1. Все атрибуты должны быть атомарными
2. Нет повторяющихся групп
3. Есть первичный ключ

### 7.3.2 Устранение повторяющихся групп

**Шаг 1: Разделение на плоские таблицы**

```sql
-- Таблица пользователей
USERS_1NF (
    user_id PK,
    username,
    email,
    password_hash,
    role_1,        -- первая роль
    role_2,        -- вторая роль (может быть NULL)
    created_at,
    updated_at
)

-- Таблица карт
CARDS_1NF (
    card_id PK,
    card_number,
    user_id FK,
    username,      -- дублирование!
    email,         -- дублирование!
    balance,
    expiration_date,
    status,
    created_at,
    updated_at
)

-- Таблица транзакций
TRANSACTIONS_1NF (
    transaction_id PK,
    from_card_id FK,
    from_card_number,  -- дублирование!
    to_card_id FK,
    to_card_number,    -- дублирование!
    amount,
    status,
    failure_reason,
    timestamp
)
```

**Проблемы 1НФ:**
- ✅ Нет повторяющихся групп
- ✅ Все атрибуты атомарны
- ❌ Дублирование данных (username, email в CARDS_1NF)
- ❌ Ограничение на количество ролей (role_1, role_2)
- ❌ Аномалии обновления (изменение email требует обновления в нескольких местах)

---

## 7.4 Приведение к 2НФ (Вторая нормальная форма)

### 7.4.1 Требования 2НФ
1. Таблица в 1НФ
2. Все неключевые атрибуты полностью зависят от первичного ключа (нет частичных зависимостей)

### 7.4.2 Устранение частичных зависимостей

**Анализ:**
- USERS_1NF: PK = user_id (одиночный), все атрибуты зависят от user_id ✅
- CARDS_1NF: PK = card_id (одиночный), но username и email зависят от user_id, а не от card_id ❌
- TRANSACTIONS_1NF: PK = transaction_id (одиночный), но card_number зависит от card_id ❌

**Шаг 2: Удаление дублирования**

```sql
-- Таблица пользователей (без изменений)
USERS_2NF (
    user_id PK,
    username,
    email,
    password_hash,
    role_1,
    role_2,
    created_at,
    updated_at
)

-- Таблица карт (удалены username, email)
CARDS_2NF (
    card_id PK,
    card_number,
    user_id FK → USERS_2NF(user_id),
    balance,
    expiration_date,
    status,
    created_at,
    updated_at
)

-- Таблица транзакций (удалены card_number)
TRANSACTIONS_2NF (
    transaction_id PK,
    from_card_id FK → CARDS_2NF(card_id),
    to_card_id FK → CARDS_2NF(card_id),
    amount,
    status,
    failure_reason,
    timestamp
)
```

**Проблемы 2НФ:**
- ✅ Нет частичных зависимостей
- ✅ Устранено дублирование пользовательских данных в CARDS
- ❌ Ограничение на количество ролей (role_1, role_2)
- ❌ Невозможность добавить третью роль без изменения схемы

---

## 7.5 Приведение к 3НФ (Третья нормальная форма)

### 7.5.1 Требования 3НФ
1. Таблица в 2НФ
2. Нет транзитивных зависимостей (неключевые атрибуты не зависят от других неключевых атрибутов)

### 7.5.2 Проверка транзитивных зависимостей

**Анализ:**
- USERS_2NF: role_1, role_2 не зависят от других неключевых атрибутов ✅
- CARDS_2NF: все атрибуты зависят только от card_id ✅
- TRANSACTIONS_2NF: все атрибуты зависят только от transaction_id ✅

**Вывод:** Таблицы уже в 3НФ, но есть проблема с ролями (не транзитивная зависимость, а многозначная).

---

## 7.6 Приведение к BCNF (Нормальная форма Бойса-Кодда)

### 7.6.1 Требования BCNF
Для каждой функциональной зависимости X → Y, X должен быть суперключом.

### 7.6.2 Проверка

**USERS_2NF:**
- user_id → {username, email, password_hash, role_1, role_2, ...} ✅
- username → user_id (username уникален) ✅
- email → user_id (email уникален) ✅

Все детерминанты являются суперключами → таблица в BCNF ✅

**CARDS_2NF:**
- card_id → {card_number, user_id, balance, ...} ✅
- card_number → card_id (card_number уникален) ✅

Все детерминанты являются суперключами → таблица в BCNF ✅

**TRANSACTIONS_2NF:**
- transaction_id → {from_card_id, to_card_id, amount, ...} ✅

Единственный детерминант — суперключ → таблица в BCNF ✅

**Вывод:** Все таблицы в BCNF

---

## 7.7 Приведение к 4НФ (Четвертая нормальная форма)

### 7.7.1 Требования 4НФ
1. Таблица в BCNF
2. Нет нетривиальных многозначных зависимостей

### 7.7.2 Выявление многозначных зависимостей

**Проблема в USERS_2NF:**

Многозначная зависимость: `user_id →→ role`

Пример:
```
user_id | username | email           | role_1 | role_2
--------|----------|-----------------|--------|-------
u1      | john     | john@mail.com   | USER   | ADMIN
u2      | jane     | jane@mail.com   | USER   | NULL
```

Если нужно добавить третью роль пользователю u1, придется изменять схему (добавлять role_3).

**Шаг 3: Устранение многозначной зависимости User →→ Role**

```sql
-- Таблица пользователей (без ролей)
USERS_4NF (
    user_id PK,
    username UNIQUE,
    email UNIQUE,
    password_hash,
    created_at,
    updated_at
)

-- Таблица ролей (справочник)
ROLES_4NF (
    role_id PK,
    role_name UNIQUE
)

-- Таблица связи пользователей и ролей (Many-to-Many)
USER_ROLES_4NF (
    user_id FK → USERS_4NF(user_id),
    role_id FK → ROLES_4NF(role_id),
    PRIMARY KEY (user_id, role_id)
)
```

**Пример данных:**
```
-- USERS_4NF
user_id | username | email
--------|----------|----------------
u1      | john     | john@mail.com
u2      | jane     | jane@mail.com

-- ROLES_4NF
role_id | role_name
--------|----------
r1      | USER
r2      | ADMIN

-- USER_ROLES_4NF
user_id | role_id
--------|--------
u1      | r1      -- john is USER
u1      | r2      -- john is ADMIN
u2      | r1      | jane is USER
```

**Преимущества:**
- ✅ Неограниченное количество ролей
- ✅ Легко добавлять/удалять роли
- ✅ Нет дублирования
- ✅ Нет NULL значений

---

### 7.7.3 Добавление таблицы AUDIT_LOGS

Для соответствия FR-20 (логирование критичных операций) добавляем таблицу:

```sql
AUDIT_LOGS_4NF (
    audit_log_id PK,
    user_id FK → USERS_4NF(user_id) ON DELETE SET NULL,
    action,
    entity_type,
    entity_id,
    details,
    timestamp
)
```

**Многозначная зависимость:** `user_id →→ audit_log_id`

Эта зависимость уже устранена выделением в отдельную таблицу ✅

---

## 7.8 Финальная нормализованная схема (4НФ)

```sql
-- 1. Пользователи
USERS (
    id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
)

-- 2. Роли
ROLES (
    id UUID PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL
)

-- 3. Связь пользователей и ролей
USER_ROLES (
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
)

-- 4. Карты
CARDS (
    id UUID PRIMARY KEY,
    card_number_encrypted VARCHAR(255) UNIQUE NOT NULL,
    owner_id UUID REFERENCES users(id) ON DELETE CASCADE NOT NULL,
    balance DECIMAL(15, 2) NOT NULL CHECK (balance >= 0),
    expiration_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'BLOCKED', 'EXPIRED')),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
)

-- 5. Транзакции
TRANSACTIONS (
    id UUID PRIMARY KEY,
    from_card_id UUID REFERENCES cards(id) ON DELETE CASCADE NOT NULL,
    to_card_id UUID REFERENCES cards(id) ON DELETE CASCADE NOT NULL,
    amount DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('SUCCESS', 'FAILED')),
    failure_reason VARCHAR(255),
    timestamp TIMESTAMP NOT NULL
)

-- 6. Логи аудита
AUDIT_LOGS (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    details TEXT,
    timestamp TIMESTAMP NOT NULL
)
```

---

## 7.9 Проверка устранения аномалий

### 7.9.1 Аномалия обновления

**До нормализации (1НФ):**
```
CARDS_1NF:
card_id | card_number | user_id | username | email
--------|-------------|---------|----------|----------------
c1      | 1234...     | u1      | john     | john@mail.com
c2      | 5678...     | u1      | john     | john@mail.com
```

Если john меняет email, нужно обновить обе строки → риск несогласованности.

**После нормализации (4НФ):**
```
USERS:
user_id | username | email
--------|----------|----------------
u1      | john     | john@mail.com

CARDS:
card_id | card_number | user_id
--------|-------------|--------
c1      | 1234...     | u1
c2      | 5678...     | u1
```

Email хранится в одном месте → обновление в одной строке ✅

---

### 7.9.2 Аномалия удаления

**До нормализации (1НФ):**
```
CARDS_1NF:
card_id | card_number | user_id | username | email
--------|-------------|---------|----------|----------------
c1      | 1234...     | u1      | john     | john@mail.com
```

Если удалить последнюю карту john, потеряется информация о пользователе john.

**После нормализации (4НФ):**
```
USERS:
user_id | username | email
--------|----------|----------------
u1      | john     | john@mail.com

CARDS:
(пусто)
```

Пользователь john существует независимо от карт ✅

---

### 7.9.3 Аномалия вставки

**До нормализации (1НФ):**
```
CARDS_1NF:
card_id | card_number | user_id | username | email
--------|-------------|---------|----------|----------------
```

Невозможно создать пользователя без карты (нет строки в CARDS_1NF).

**После нормализации (4НФ):**
```
USERS:
user_id | username | email
--------|----------|----------------
u1      | john     | john@mail.com

CARDS:
(пусто)
```

Пользователь создается независимо от карт ✅

---

## 7.10 Сравнение нормальных форм

| Нормальная форма | Требования | Устраняемые проблемы | Статус схемы |
|------------------|------------|----------------------|--------------|
| **1НФ** | Атомарность, нет повторяющихся групп | Вложенные структуры | ✅ Достигнута |
| **2НФ** | 1НФ + нет частичных зависимостей | Дублирование данных | ✅ Достигнута |
| **3НФ** | 2НФ + нет транзитивных зависимостей | Косвенные зависимости | ✅ Достигнута |
| **BCNF** | 3НФ + все детерминанты — суперключи | Аномалии обновления | ✅ Достигнута |
| **4НФ** | BCNF + нет многозначных зависимостей | Ограничения на связи | ✅ Достигнута |

---

## 7.11 Граф нормализации

```
0НФ (Ненормализованная)
    USER_CARD_DATA (все в одной таблице)
    ↓
    [Устранение повторяющихся групп]
    ↓
1НФ
    USERS_1NF, CARDS_1NF, TRANSACTIONS_1NF
    (дублирование: username, email в CARDS)
    ↓
    [Устранение частичных зависимостей]
    ↓
2НФ
    USERS_2NF, CARDS_2NF, TRANSACTIONS_2NF
    (проблема: role_1, role_2 — ограничение)
    ↓
    [Проверка транзитивных зависимостей]
    ↓
3НФ
    USERS_2NF, CARDS_2NF, TRANSACTIONS_2NF
    (нет транзитивных зависимостей)
    ↓
    [Проверка детерминантов]
    ↓
BCNF
    USERS_2NF, CARDS_2NF, TRANSACTIONS_2NF
    (все детерминанты — суперключи)
    ↓
    [Устранение многозначных зависимостей]
    ↓
4НФ
    USERS, ROLES, USER_ROLES, CARDS, TRANSACTIONS, AUDIT_LOGS
    (финальная схема)
```

---

## 7.12 Выводы

1. **Схема полностью нормализована до 4НФ**
   - Устранены все повторяющиеся группы
   - Устранены частичные и транзитивные зависимости
   - Устранены многозначные зависимости

2. **Устранены все аномалии:**
   - ✅ Аномалия обновления — данные хранятся в одном месте
   - ✅ Аномалия удаления — сущности независимы
   - ✅ Аномалия вставки — можно создавать сущности независимо

3. **Преимущества нормализованной схемы:**
   - Минимальная избыточность данных
   - Целостность данных через внешние ключи
   - Гибкость (легко добавлять роли, карты, транзакции)
   - Масштабируемость

4. **Соответствие требованиям:**
   - Все функциональные требования (FR-1...FR-20) реализуемы
   - Все ограничения (C-1...C-36) выполнены
   - Все функциональные и многозначные зависимости корректны

**Итого:** Схема готова к реализации на SQL без дальнейших изменений.

