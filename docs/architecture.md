# 🏗️ Архитектура Card Management System

## 📋 Общая информация

**Проект:** Card Management System (CMS)  
**Архитектура:** Многоуровневая (Layered Architecture) + CQRS  
**Технологический стек:** Java 21, Spring Boot 3.5.6, PostgreSQL, Liquibase, JWT  

---

## 📂 Структура проекта

```
src/main/java/com/ayungi/cms/
├── config/              # Конфигурационные классы
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   ├── SwaggerConfig.java
│   ├── CorsConfig.java
│   └── LiquibaseConfig.java
│
├── controller/          # REST-контроллеры
│   ├── AuthController.java
│   ├── CardController.java
│   ├── TransferController.java
│   └── AdminController.java
│
├── dto/                 # Data Transfer Objects
│   ├── request/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── CardCreateRequest.java
│   │   └── TransferRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── CardResponse.java
│       ├── TransactionResponse.java
│       └── ErrorResponse.java
│
├── entity/              # JPA-сущности
│   ├── User.java
│   ├── Role.java
│   ├── Card.java
│   ├── Transaction.java
│   ├── AuditLog.java
│   └── enums/
│       ├── CardStatus.java
│       ├── TransactionStatus.java
│       └── AuditAction.java
│
├── exception/           # Обработка исключений
│   ├── BaseException.java
│   ├── AuthenticationException.java
│   ├── ResourceNotFoundException.java
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
│
├── repository/          # Spring Data JPA
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   ├── CardRepository.java
│   ├── TransactionRepository.java
│   └── AuditLogRepository.java
│
├── security/            # Безопасность
│   ├── JwtUtil.java
│   ├── JwtFilter.java
│   ├── JwtAuthenticationEntryPoint.java
│   └── UserDetailsServiceImpl.java
│
├── service/             # Бизнес-логика
│   ├── AuthService.java
│   ├── CardService.java
│   ├── TransferService.java
│   ├── UserService.java
│   └── AuditService.java
│
├── util/                # Утилиты
│   ├── EncryptionUtil.java
│   ├── CardMaskUtil.java
│   ├── CardNumberGenerator.java
│   └── CardValidator.java
│
└── CmsApplication.java  # Точка входа
```

---

## 🧱 Слои приложения

### 1. Controller Layer (Контроллеры)
**Назначение:** Обработка HTTP-запросов, валидация входных данных, делегирование в сервисы.

**Принципы:**
- Не содержит бизнес-логики
- Работает только с DTO
- Аннотации Swagger для документации
- Исключения обрабатываются глобально

### 2. Service Layer (Сервисы)
**Назначение:** Бизнес-логика, транзакционные операции, оркестрация.

**Принципы:**
- Содержит всю бизнес-логику
- Не работает с HTTP-объектами
- Транзакционность (@Transactional)
- Валидация бизнес-инвариантов

### 3. Repository Layer (Репозитории)
**Назначение:** Доступ к базе данных, CRUD-операции.

**Принципы:**
- Только операции с БД
- Spring Data JPA интерфейсы
- Декларативные методы (findByOwnerId, etc.)

### 4. Entity Layer (Сущности)
**Назначение:** Доменные модели, JPA-маппинг.

**Принципы:**
- Поля и связи между сущностями
- Минимум логики
- snake_case для таблиц и колонок

### 5. Security Layer (Безопасность)
**Назначение:** Аутентификация, авторизация, JWT.

**Принципы:**
- JWT токены (access + refresh)
- Ролевая модель (ADMIN, USER)
- Фильтрация запросов

---

## 🔄 Поток данных

```
HTTP Request
    ↓
Controller (валидация DTO)
    ↓
Service (бизнес-логика)
    ↓
Repository (доступ к БД)
    ↓
Database (PostgreSQL)
    ↓
Repository (маппинг Entity)
    ↓
Service (преобразование в DTO)
    ↓
Controller (формирование ответа)
    ↓
HTTP Response
```

---

## 🗄️ База данных

**СУБД:** PostgreSQL 16  
**Миграции:** Liquibase  

### Основные таблицы:

| Таблица | Описание |
|---------|----------|
| `users` | Пользователи системы |
| `roles` | Роли (ADMIN, USER) |
| `user_roles` | Связь пользователей и ролей |
| `cards` | Банковские карты |
| `transactions` | История переводов |
| `audit_logs` | Системный аудит |

---

## 🔐 Безопасность

### JWT Аутентификация
- **Access Token:** 15 минут
- **Refresh Token:** 7 дней
- **Алгоритм:** HS256
- **Секрет:** Хранится в переменных окружения

### Авторизация
- **ADMIN:** Полный доступ ко всем операциям
- **USER:** Доступ только к собственным ресурсам

### Шифрование
- **Номера карт:** AES-256
- **Пароли:** BCrypt
- **Маскирование:** `**** **** **** 1234`

---

## 📝 Логирование

**Библиотека:** SLF4J + Logback  
**Файлы логов:**
- `logs/app.log` — все логи
- `logs/error.log` — только ошибки

**Уровни:**
- **DEBUG:** Детальная информация (dev)
- **INFO:** Бизнес-события
- **WARN:** Подозрительные ситуации
- **ERROR:** Ошибки и исключения

**Ротация:** 7 дней для app.log, 30 дней для error.log

---

## 🧪 Тестирование

### Unit-тесты
- Сервисы (Mockito)
- Утилиты
- Покрытие ≥ 65%

### Интеграционные тесты
- Контроллеры (@WebMvcTest)
- Репозитории (@DataJpaTest)
- Testcontainers (PostgreSQL)

---

## 🚀 Развертывание

### Docker Compose
```yaml
services:
  - postgres (БД)
  - app (Spring Boot)
```

### Переменные окружения
- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `JWT_SECRET`
- `SPRING_PROFILE` (dev/prod)

---

## 📊 API Документация

**Swagger UI:** http://localhost:8080/swagger-ui.html  
**OpenAPI Spec:** http://localhost:8080/api-docs  

---

## 🎯 Принципы разработки

1. **Clean Architecture** — разделение слоев
2. **SOLID** — каждый класс отвечает за одну задачу
3. **DDD** — доменная логика в сервисах
4. **CQRS** — разделение команд и запросов
5. **Security First** — безопасность на всех уровнях

---

© 2025 Ayungich
