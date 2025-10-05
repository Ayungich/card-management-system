# Пользовательские сценарии работы с CMS API

## Доступ к Swagger UI

После запуска приложения откройте в браузере:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

---

## Сценарий 1: Работа обычного пользователя

### 1.1 Регистрация нового пользователя

**Endpoint**: `POST /api/auth/register`

**Request Body**:
```json
{
  "username": "user1",
  "email": "user1@example.com",
  "password": "SecurePass123!",
}
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "user1",
  "email": "user1@example.com",
  "roles": ["USER"],
  "tokenType": "Bearer"
}
```

**Важно**: Скопируйте `accessToken` для дальнейшего использования.

---

### 1.2 Авторизация в Swagger

1. В правом верхнем углу Swagger UI нажмите кнопку **Authorize** 🔓
2. В поле **Value** введите: `Bearer <ваш_accessToken>`
3. Нажмите **Authorize**, затем **Close**

Теперь все запросы будут выполняться от имени авторизованного пользователя.

---

### 1.3 Создание карты

**Endpoint**: `POST /api/cards`

**Request Body**:
```json
{
  "cardholderName": "IVAN IVANOV",
  "initialBalance": 10000.00
}
```

**Response** (201 Created):
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "cardNumber": "4532********1234",
  "cardholderName": "IVAN IVANOV",
  "balance": 10000.00,
  "currency": "RUB",
  "status": "ACTIVE",
  "createdAt": "2025-10-05T20:30:00",
  "expiryDate": "2028-10-05"
}
```

**Важно**: Сохраните `id` карты для дальнейших операций.

---

### 1.4 Просмотр своих карт

**Endpoint**: `GET /api/cards/my`

**Response** (200 OK):
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "cardNumber": "4532********1234",
    "cardholderName": "IVAN IVANOV",
    "balance": 10000.00,
    "currency": "RUB",
    "status": "ACTIVE",
    "createdAt": "2025-10-05T20:30:00",
    "expiryDate": "2028-10-05"
  }
]
```

---

### 1.5 Просмотр баланса карты

**Endpoint**: `GET /api/cards/{cardId}/balance`

**Path Parameter**: `cardId` = `a1b2c3d4-e5f6-7890-abcd-ef1234567890`

**Response** (200 OK):
```json
{
  "cardId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "maskedCardNumber": "4532********1234",
  "balance": 10000.00,
  "currency": "RUB"
}
```

---

### 1.6 Пополнение карты

**Endpoint**: `POST /api/cards/{cardId}/deposit`

**Path Parameter**: `cardId` = `a1b2c3d4-e5f6-7890-abcd-ef1234567890`

**Request Body**:
```json
{
  "amount": 5000.00
}
```

**Response** (200 OK):
```json
{
  "cardId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "maskedCardNumber": "4532********1234",
  "balance": 15000.00,
  "currency": "RUB"
}
```

---

### 1.7 Снятие средств с карты

**Endpoint**: `POST /api/cards/{cardId}/withdraw`

**Path Parameter**: `cardId` = `a1b2c3d4-e5f6-7890-abcd-ef1234567890`

**Request Body**:
```json
{
  "amount": 2000.00
}
```

**Response** (200 OK):
```json
{
  "cardId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "maskedCardNumber": "4532********1234",
  "balance": 13000.00,
  "currency": "RUB"
}
```

---

### 1.8 Перевод между картами

**Предварительно**: Создайте вторую карту или попросите другого пользователя создать карту.

**Endpoint**: `POST /api/transfers`

**Request Body**:
```json
{
  "fromCardId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "toCardId": "b2c3d4e5-f6g7-8901-bcde-fg2345678901",
  "amount": 1000.00,
  "description": "Перевод другу"
}
```

**Response** (200 OK):
```json
{
  "id": "c3d4e5f6-g7h8-9012-cdef-gh3456789012",
  "fromCardNumber": "4532********1234",
  "toCardNumber": "5105********5678",
  "amount": 1000.00,
  "currency": "RUB",
  "status": "SUCCESS",
  "description": "Перевод другу",
  "timestamp": "2025-10-05T20:45:00"
}
```

---

### 1.9 Просмотр истории транзакций

**Endpoint**: `GET /api/transfers/my`

**Query Parameters** (опционально):
- `page` = 0
- `size` = 10

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "c3d4e5f6-g7h8-9012-cdef-gh3456789012",
      "fromCardNumber": "4532********1234",
      "toCardNumber": "5105********5678",
      "amount": 1000.00,
      "currency": "RUB",
      "status": "SUCCESS",
      "description": "Перевод другу",
      "timestamp": "2025-10-05T20:45:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

---

### 1.10 Блокировка карты

**Endpoint**: `PUT /api/cards/{cardId}/block`

**Path Parameter**: `cardId` = `a1b2c3d4-e5f6-7890-abcd-ef1234567890`

**Response** (200 OK):
```json
{
  "message": "Карта успешно заблокирована"
}
```

---

### 1.11 Разблокировка карты

**Endpoint**: `PUT /api/cards/{cardId}/activate`

**Path Parameter**: `cardId` = `a1b2c3d4-e5f6-7890-abcd-ef1234567890`

**Response** (200 OK):
```json
{
  "message": "Карта успешно активирована"
}
```

---

### 1.12 Обновление профиля

**Endpoint**: `PUT /api/auth/profile`

**Request Body**:
```json
{
  "fullName": "Иван Петрович Иванов",
  "email": "ivan.ivanov@example.com"
}
```

**Response** (200 OK):
```json
{
  "id": "d4e5f6g7-h8i9-0123-defg-hi4567890123",
  "username": "user1",
  "email": "ivan.ivanov@example.com",
  "fullName": "Иван Петрович Иванов",
  "roles": ["USER"],
  "createdAt": "2025-10-05T20:00:00"
}
```

---

### 1.13 Обновление токена

**Endpoint**: `POST /api/auth/refresh`

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "user1",
  "email": "ivan.ivanov@example.com",
  "roles": ["USER"],
  "tokenType": "Bearer"
}
```

---

## Сценарий 2: Работа администратора

### 2.1 Получение роли администратора

#### Вариант 1: Через базу данных (рекомендуется для первого админа)

1. Зарегистрируйтесь как обычный пользователь через API
2. Подключитесь к базе данных PostgreSQL:

```bash
docker exec cms-postgres psql -U cms_user -d cms_db
```

3. Найдите ID вашего пользователя:

```sql
SELECT id, username, email FROM users WHERE username = 'ваш_username';
```

4. Назначьте роль ADMIN:

```sql
INSERT INTO user_roles (user_id, role_id) 
VALUES ('ваш_user_id', 1) 
ON CONFLICT DO NOTHING;
```

5. Проверьте назначение роли:

```sql
SELECT u.username, r.name as role 
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id 
WHERE u.username = 'ваш_username';
```

6. Выйдите из psql: `\q`

7. **Важно**: Войдите заново через `/api/auth/login`, чтобы получить новый токен с ролью ADMIN

#### Вариант 2: Через существующего администратора

Если в системе уже есть администратор, он может назначить роль через SQL или создать соответствующий эндпоинт.

---

### 2.2 Авторизация администратора

**Endpoint**: `POST /api/auth/login`

**Request Body**:
```json
{
  "username": "admin",
  "password": "AdminPass123!"
}
```

**Response** (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "admin",
  "email": "admin@example.com",
  "roles": ["USER", "ADMIN"],
  "tokenType": "Bearer"
}
```

**Важно**: Авторизуйтесь в Swagger с этим токеном (см. п. 1.2).

---

### 2.3 Просмотр всех пользователей

**Endpoint**: `GET /api/admin/users`

**Query Parameters** (опционально):
- `page` = 0
- `size` = 20

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "d4e5f6g7-h8i9-0123-defg-hi4567890123",
      "username": "user1",
      "email": "user1@example.com",
      "fullName": "Иван Иванов",
      "roles": ["USER"],
      "createdAt": "2025-10-05T20:00:00"
    },
    {
      "id": "e5f6g7h8-i9j0-1234-efgh-ij5678901234",
      "username": "admin",
      "email": "admin@example.com",
      "fullName": "Администратор",
      "roles": ["USER", "ADMIN"],
      "createdAt": "2025-10-05T19:00:00"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

### 2.4 Просмотр статистики системы

**Endpoint**: `GET /api/admin/statistics`

**Response** (200 OK):
```json
{
  "totalUsers": 15,
  "totalCards": 28,
  "totalTransactions": 142,
  "totalTransactionAmount": 1250000.00,
  "activeCards": 25,
  "blockedCards": 3
}
```

---

### 2.5 Просмотр логов аудита

**Endpoint**: `GET /api/admin/audit-logs`

**Query Parameters** (опционально):
- `page` = 0
- `size` = 50
- `action` = `TRANSFER` (опционально: CREATE, UPDATE, DELETE, BLOCK, ACTIVATE, TRANSFER, LOGIN, LOGOUT)
- `entityType` = `Card` (опционально: User, Card, Transaction)

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "f6g7h8i9-j0k1-2345-fghi-jk6789012345",
      "action": "TRANSFER",
      "entityType": "Transaction",
      "entityId": "c3d4e5f6-g7h8-9012-cdef-gh3456789012",
      "userId": "d4e5f6g7-h8i9-0123-defg-hi4567890123",
      "username": "user1",
      "details": "Перевод 1000.00 RUB с карты 4532********1234 на карту 5105********5678",
      "ipAddress": "192.168.1.100",
      "timestamp": "2025-10-05T20:45:00"
    },
    {
      "id": "g7h8i9j0-k1l2-3456-ghij-kl7890123456",
      "action": "CREATE",
      "entityType": "Card",
      "entityId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "userId": "d4e5f6g7-h8i9-0123-defg-hi4567890123",
      "username": "user1",
      "details": "Создана карта 4532********1234 с балансом 10000.00 RUB",
      "ipAddress": "192.168.1.100",
      "timestamp": "2025-10-05T20:30:00"
    }
  ],
  "totalElements": 142,
  "totalPages": 3,
  "size": 50,
  "number": 0
}
```

---

### 2.6 Поиск карт с фильтрацией

**Endpoint**: `POST /api/admin/cards/search`

**Request Body**:
```json
{
  "status": "ACTIVE",
  "minBalance": 5000.00,
  "maxBalance": 50000.00
}
```

**Query Parameters**:
- `page` = 0
- `size` = 20

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "cardNumber": "4532********1234",
      "cardholderName": "IVAN IVANOV",
      "balance": 12000.00,
      "currency": "RUB",
      "status": "ACTIVE",
      "createdAt": "2025-10-05T20:30:00",
      "expiryDate": "2028-10-05"
    }
  ],
  "totalElements": 18,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

## Дополнительные возможности

### Health Check

**Endpoint**: `GET /actuator/health`

**Response** (200 OK):
```json
{
  "status": "UP"
}
```

---

## Обработка ошибок

Все ошибки возвращаются в стандартном формате:

```json
{
  "timestamp": "2025-10-05T20:50:00",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Недостаточно средств на карте",
  "path": "/api/cards/a1b2c3d4-e5f6-7890-abcd-ef1234567890/withdraw"
}
```

### Типичные коды ошибок:

- **400 Bad Request** - Ошибка валидации данных
- **401 Unauthorized** - Не авторизован или токен истёк
- **403 Forbidden** - Недостаточно прав доступа
- **404 Not Found** - Ресурс не найден
- **409 Conflict** - Конфликт данных (например, пользователь уже существует)
- **500 Internal Server Error** - Внутренняя ошибка сервера

---

## Полезные советы

1. **Срок действия токенов**:
   - Access Token: 1 час
   - Refresh Token: 7 дней

2. **Лимиты пагинации**:
   - По умолчанию: 10 элементов на страницу
   - Максимум: 100 элементов на страницу

3. **Валидация**:
   - Пароль: минимум 8 символов
   - Email: должен быть валидным
   - Сумма перевода: должна быть положительной
   - Баланс карты: не может быть отрицательным

4. **Безопасность**:
   - Все пароли хешируются с использованием BCrypt
   - Номера карт шифруются в базе данных (AES)
   - Все действия логируются в audit_logs

5. **Тестирование**:
   - Используйте разные браузеры/вкладки для тестирования нескольких пользователей
   - Сохраняйте токены и ID для повторного использования
   - Проверяйте логи аудита после важных операций
