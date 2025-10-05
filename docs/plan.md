# 📋 План реализации Card Management System

## 🎯 Этапы реализации

---

## Этап 1: Настройка инфраструктуры и зависимостей

### 1.1 Обновление pom.xml
- Добавить Spring Data JPA для работы с БД
- Добавить Liquibase для миграций
- Добавить JWT библиотеки (jjwt-api, jjwt-impl, jjwt-jackson)
- Добавить Swagger/OpenAPI (springdoc-openapi)
- Добавить Testcontainers для интеграционных тестов
- Добавить библиотеку для шифрования (javax.crypto включена в JDK)

### 1.2 Конфигурация приложения
- Создать `application.yml` с профилями (dev, prod)
- Настроить подключение к PostgreSQL
- Настроить Liquibase
- Настроить логирование (logback-spring.xml)
- Создать `.env.example` для переменных окружения

### 1.3 Docker-окружение
- Создать `docker-compose.yml` с PostgreSQL
- Настроить переменные окружения для БД
- Добавить volume для персистентности данных

---

## Этап 2: Создание доменной модели (Entity)

### 2.1 Базовые сущности
- **User** — пользователь системы (id, username, email, password, createdAt, updatedAt)
- **Role** — роли (id, name: ADMIN/USER)
- **Card** — банковская карта (id, cardNumber, ownerId, expirationDate, status, balance, createdAt, updatedAt)
- **Transaction** — история переводов (id, fromCardId, toCardId, amount, timestamp, status)
- **AuditLog** — системный аудит (id, userId, action, entityType, entityId, timestamp, details)

### 2.2 Enum-классы
- **CardStatus** — ACTIVE, BLOCKED, EXPIRED
- **TransactionStatus** — SUCCESS, FAILED
- **AuditAction** — CREATE, UPDATE, DELETE, BLOCK, ACTIVATE, TRANSFER

### 2.3 Связи между сущностями
- User ↔ Role (ManyToMany)
- User ↔ Card (OneToMany)
- Card ↔ Transaction (OneToMany для fromCard и toCard)

---

## Этап 3: Миграции базы данных (Liquibase)

### 3.1 Создание структуры миграций
- `db/changelog/db.changelog-master.yaml` — главный файл
- `db/changelog/v1.0/01-create-users-table.yaml`
- `db/changelog/v1.0/02-create-roles-table.yaml`
- `db/changelog/v1.0/03-create-user-roles-table.yaml`
- `db/changelog/v1.0/04-create-cards-table.yaml`
- `db/changelog/v1.0/05-create-transactions-table.yaml`
- `db/changelog/v1.0/06-create-audit-logs-table.yaml`
- `db/changelog/v1.0/07-insert-default-roles.yaml`

### 3.2 Индексы и ограничения
- Индексы на owner_id, card_number, status
- Уникальные ограничения на card_number
- Foreign keys с ON DELETE CASCADE где необходимо

---

## Этап 4: Репозитории (Repository Layer)

### 4.1 Создание интерфейсов
- **UserRepository** — findByUsername, findByEmail, existsByUsername
- **RoleRepository** — findByName
- **CardRepository** — findByOwnerId, findByCardNumber, findByStatus, findByOwnerIdAndId
- **TransactionRepository** — findByFromCardId, findByToCardId, findByTimestampBetween
- **AuditLogRepository** — findByUserId, findByEntityTypeAndEntityId

### 4.2 Кастомные запросы
- Пагинация и фильтрация карт
- Поиск транзакций по периоду
- Статистика по картам для админа

---

## Этап 5: DTO и маппинг

### 5.1 Request DTO
- **RegisterRequest** — username, email, password
- **LoginRequest** — username, password
- **CardCreateRequest** — ownerId, expirationDate, initialBalance
- **TransferRequest** — fromCardId, toCardId, amount
- **CardFilterRequest** — status, ownerId, page, size

### 5.2 Response DTO
- **AuthResponse** — accessToken, refreshToken, username, roles
- **CardResponse** — id, maskedCardNumber, expirationDate, status, balance, createdAt
- **TransactionResponse** — id, fromCardId, toCardId, amount, timestamp, status
- **UserResponse** — id, username, email, roles, createdAt
- **ErrorResponse** — timestamp, status, error, code, message, path

### 5.3 Маппинг
- Создать MapperUtil или использовать MapStruct
- Маппинг Entity ↔ DTO

---

## Этап 6: Утилиты (Util Layer)

### 6.1 Шифрование
- **EncryptionUtil** — encrypt/decrypt номеров карт (AES-256)
- Хранение ключа шифрования в переменных окружения

### 6.2 Маскирование
- **CardMaskUtil** — maskCardNumber (отображение `**** **** **** 1234`)

### 6.3 Генерация
- **CardNumberGenerator** — генерация валидного номера карты (16 цифр, алгоритм Луна)

### 6.4 Валидация
- **CardValidator** — проверка срока действия, статуса
- **BalanceValidator** — проверка достаточности средств

---

## Этап 7: Безопасность (Security Layer)

### 7.1 JWT
- **JwtUtil** — генерация и валидация токенов (access: 15 мин, refresh: 7 дней)
- **JwtFilter** — фильтр для проверки токенов в запросах
- **JwtAuthenticationEntryPoint** — обработка 401 ошибок

### 7.2 Spring Security
- **SecurityConfig** — конфигурация цепочки фильтров
- **UserDetailsServiceImpl** — загрузка пользователя для аутентификации
- **PasswordEncoder** — BCrypt для хеширования паролей

### 7.3 Авторизация
- Настройка доступа по ролям (ADMIN, USER)
- Method-level security (@PreAuthorize)

---

## Этап 8: Бизнес-логика (Service Layer)

### 8.1 AuthService
- register() — регистрация пользователя с ролью USER
- login() — аутентификация и выдача JWT
- refreshToken() — обновление access token

### 8.2 CardService
- createCard() — создание карты (только ADMIN)
- getCardsByOwner() — получение карт пользователя с пагинацией
- getCardById() — получение карты по ID
- blockCard() — блокировка карты
- activateCard() — активация карты
- deleteCard() — удаление карты (только ADMIN)
- getBalance() — получение баланса

### 8.3 TransferService
- transfer() — перевод между своими картами
- validateTransfer() — проверка возможности перевода
- Транзакционность операции (@Transactional)

### 8.4 UserService
- getAllUsers() — список пользователей (только ADMIN)
- getUserById() — получение пользователя
- updateUser() — обновление профиля
- deleteUser() — удаление пользователя (только ADMIN)

### 8.5 AuditService
- logAction() — запись действия в audit_logs
- getAuditLogs() — получение логов (только ADMIN)

---

## Этап 9: Обработка исключений (Exception Layer)

### 9.1 Кастомные исключения
- **BaseException** — базовое исключение с кодом
- **AuthenticationException** — AUTH_INVALID_TOKEN, AUTH_ACCESS_DENIED
- **ResourceNotFoundException** — USER_NOT_FOUND, CARD_NOT_FOUND
- **BusinessException** — CARD_BLOCKED, INSUFFICIENT_BALANCE, TRANSFER_SELF_ONLY
- **ValidationException** — VALIDATION_ERROR

### 9.2 Глобальный обработчик
- **GlobalExceptionHandler** (@ControllerAdvice)
- Обработка всех типов исключений
- Формирование единого ErrorResponse
- Логирование ошибок

---

## Этап 10: REST API (Controller Layer)

### 10.1 AuthController
- `POST /api/auth/register` — регистрация
- `POST /api/auth/login` — авторизация
- `POST /api/auth/refresh` — обновление токена

### 10.2 CardController
- `GET /api/cards` — список карт текущего пользователя (USER)
- `GET /api/cards/{id}` — получение карты по ID (USER)
- `POST /api/cards` — создание карты (ADMIN)
- `PATCH /api/cards/{id}/block` — блокировка карты (ADMIN/USER)
- `PATCH /api/cards/{id}/activate` — активация карты (ADMIN)
- `DELETE /api/cards/{id}` — удаление карты (ADMIN)
- `GET /api/cards/{id}/balance` — получение баланса (USER)

### 10.3 TransferController
- `POST /api/transfers` — перевод между картами (USER)
- `GET /api/transfers/history` — история переводов (USER)

### 10.4 AdminController
- `GET /api/admin/users` — список пользователей (ADMIN)
- `GET /api/admin/cards` — все карты с фильтрацией (ADMIN)
- `GET /api/admin/audit-logs` — системный аудит (ADMIN)
- `GET /api/admin/statistics` — статистика по картам (ADMIN)

---

## Этап 11: Конфигурация (Config Layer)

### 11.1 Основные конфигурации
- **SecurityConfig** — настройка Spring Security
- **JwtConfig** — параметры JWT (secret, expiration)
- **SwaggerConfig** — настройка OpenAPI документации
- **CorsConfig** — настройка CORS
- **LiquibaseConfig** — конфигурация миграций

### 11.2 Дополнительные конфигурации
- **LoggingConfig** — настройка логирования
- **ValidationConfig** — кастомные валидаторы
- **AsyncConfig** — асинхронные операции (опционально)

---

## Этап 12: Документация API (OpenAPI/Swagger)

### 12.1 Настройка Swagger
- Добавить аннотации @Operation, @ApiResponse
- Настроить SecurityScheme для JWT
- Группировка эндпоинтов по тегам

### 12.2 Создание OpenAPI спецификации
- Экспорт в `docs/openapi.yaml`
- Описание всех эндпоинтов
- Примеры запросов и ответов

---

## Этап 13: Логирование

### 13.1 Настройка Logback
- Создать `logback-spring.xml`
- Настроить appenders (console, file, error-file)
- Настроить ротацию логов (7 дней)
- Форматирование логов

### 13.2 Логирование в коде
- Добавить логирование в сервисы (INFO для успешных операций)
- Логирование ошибок (ERROR в GlobalExceptionHandler)
- Логирование безопасности (WARN для неудачных попыток входа)
- Исключить чувствительные данные из логов

---

## Этап 14: Тестирование

### 14.1 Unit-тесты
- Тесты сервисов (CardService, TransferService, AuthService)
- Тесты утилит (EncryptionUtil, CardMaskUtil, CardNumberGenerator)
- Мокирование зависимостей (Mockito)
- Покрытие ≥ 65%

### 14.2 Интеграционные тесты
- Тесты контроллеров (@WebMvcTest)
- Тесты репозиториев (@DataJpaTest)
- Тесты с Testcontainers (PostgreSQL)
- Тесты безопасности (аутентификация, авторизация)

### 14.3 Тестовые данные
- Создание тестовых пользователей
- Создание тестовых карт
- Сценарии переводов

---

## Этап 15: Docker и развертывание

### 15.1 Dockerfile
- Создать multi-stage Dockerfile
- Оптимизация образа (использование Alpine)
- Настройка переменных окружения

### 15.2 Docker Compose
- Сервис приложения (app)
- Сервис БД (postgres)
- Volumes для персистентности
- Networks для изоляции
- Health checks

### 15.3 Скрипты запуска
- `start.sh` — запуск приложения
- `stop.sh` — остановка
- `logs.sh` — просмотр логов

---

## Этап 16: Документация проекта

### 16.1 README.md
- Описание проекта
- Требования к окружению
- Инструкция по запуску (локально и через Docker)
- Примеры использования API
- Структура проекта

### 16.2 Дополнительная документация
- `CONTRIBUTING.md` — правила контрибуции
- `CHANGELOG.md` — история изменений
- `API.md` — детальное описание API
- Комментарии в коде (Javadoc)

---

## Этап 17: Финальная проверка и оптимизация

### 17.1 Проверка требований
- Соответствие всем бизнес-требованиям
- Проверка всех эндпоинтов
- Проверка ролевой модели
- Проверка безопасности

### 17.2 Оптимизация
- Оптимизация запросов к БД (N+1 проблема)
- Добавление индексов
- Кэширование (опционально)
- Проверка производительности

### 17.3 Code Review
- Проверка соответствия архитектурным правилам
- Проверка стиля кода
- Проверка покрытия тестами
- Устранение code smells

---

## 📊 Оценка времени реализации

| Этап | Время (часы) |
|------|--------------|
| Этап 1: Инфраструктура | 2-3 |
| Этап 2: Доменная модель | 2-3 |
| Этап 3: Миграции БД | 2-3 |
| Этап 4: Репозитории | 2-3 |
| Этап 5: DTO и маппинг | 3-4 |
| Этап 6: Утилиты | 3-4 |
| Этап 7: Безопасность | 5-6 |
| Этап 8: Бизнес-логика | 8-10 |
| Этап 9: Обработка исключений | 2-3 |
| Этап 10: REST API | 5-6 |
| Этап 11: Конфигурация | 2-3 |
| Этап 12: Swagger | 2-3 |
| Этап 13: Логирование | 2-3 |
| Этап 14: Тестирование | 10-12 |
| Этап 15: Docker | 3-4 |
| Этап 16: Документация | 3-4 |
| Этап 17: Финальная проверка | 3-4 |
| **ИТОГО** | **60-75 часов** |

---

## 🎯 Приоритеты реализации

### Критический путь (MVP):
1. Инфраструктура и БД (Этапы 1-3)
2. Базовые сущности и репозитории (Этап 4)
3. Безопасность и аутентификация (Этап 7)
4. Основная бизнес-логика (Этап 8)
5. REST API (Этап 10)

### Второстепенные задачи:
- Swagger документация
- Расширенное логирование
- Дополнительные фильтры и статистика

### Опциональные улучшения:
- Кэширование (Redis)
- История транзакций для пользователей
- Push-уведомления
- 2FA аутентификация

---

## ✅ Критерии готовности

- ✅ Все эндпоинты работают согласно спецификации
- ✅ Реализована ролевая модель (ADMIN, USER)
- ✅ Номера карт шифруются и маскируются
- ✅ JWT аутентификация работает корректно
- ✅ Все бизнес-инварианты соблюдаются
- ✅ Покрытие тестами ≥ 65%
- ✅ Приложение запускается через Docker Compose
- ✅ Swagger UI доступен и корректен
- ✅ Логирование настроено и работает
- ✅ README содержит полную инструкцию по запуску

---

© 2025 Ayungich
