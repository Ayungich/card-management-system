# 🏦 Card Management System (CMS)

Система управления банковскими картами для физических лиц с поддержкой выпуска, блокировки, переводов и контроля баланса.

## 📋 Описание

Card Management System — это backend REST API для автоматизации управления банковскими картами. Система поддерживает ролевую модель доступа, JWT аутентификацию, шифрование чувствительных данных и полный аудит операций.

## 🎯 Основные возможности

- ✅ Управление банковскими картами (создание, блокировка, активация)
- ✅ Переводы между собственными картами
- ✅ Ролевая модель (ADMIN, USER)
- ✅ JWT аутентификация
- ✅ Шифрование номеров карт (AES-256)
- ✅ Маскирование чувствительных данных
- ✅ Системный аудит операций
- ✅ Swagger/OpenAPI документация

## 🛠️ Технологический стек

- **Java:** 21
- **Framework:** Spring Boot 3.5.6
- **Database:** PostgreSQL 16
- **Migrations:** Liquibase
- **Security:** Spring Security + JWT
- **Documentation:** Swagger/OpenAPI
- **Testing:** JUnit 5, Mockito, Testcontainers
- **Containerization:** Docker, Docker Compose

## 📦 Требования

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16 (если запуск без Docker)

## 🚀 Быстрый старт

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd cms
```

### 2. Настройка переменных окружения

Создайте файл `.env` на основе `.env.example`:

```bash
cp .env.example .env
```

Отредактируйте `.env` и установите значения:

```env
JWT_SECRET=your-very-strong-secret-key-minimum-256-bits
DB_PASSWORD=your-secure-password
```

### 3. Запуск через Docker Compose

```bash
docker-compose up -d
```

Приложение будет доступно по адресу: `http://localhost:8080`

### 4. Запуск локально (без Docker)

#### Запустите PostgreSQL:

```bash
docker-compose up -d postgres
```

#### Соберите и запустите приложение:

```bash
mvn clean install
mvn spring-boot:run
```

## 📚 API Документация

После запуска приложения документация доступна по адресу:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

### Для фронтенд-разработчиков

- **[Frontend Integration Guide](docs/frontend-integration.md)** - полное руководство по интеграции с API
- **[API Examples](docs/api-examples.md)** - практические примеры использования всех endpoints

## 🔐 Аутентификация

### Регистрация пользователя

```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "user",
  "email": "user@example.com",
  "password": "password123"
}
```

### Вход в систему

```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "user",
  "password": "password123"
}
```

Ответ содержит JWT токены:

```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "username": "user",
  "roles": ["USER"]
}
```

### Использование токена

Добавьте токен в заголовок запроса:

```bash
Authorization: Bearer <accessToken>
```

## 📖 Основные эндпоинты

### Карты (USER)

- `GET /api/cards` — список собственных карт
- `GET /api/cards/{id}` — информация о карте
- `GET /api/cards/{id}/balance` — баланс карты
- `PATCH /api/cards/{id}/block` — запрос на блокировку

### Переводы (USER)

- `POST /api/transfers` — перевод между своими картами
- `GET /api/transfers/history` — история переводов

### Администрирование (ADMIN)

- `POST /api/cards` — создание карты
- `PATCH /api/cards/{id}/activate` — активация карты
- `DELETE /api/cards/{id}` — удаление карты
- `GET /api/admin/users` — список пользователей
- `GET /api/admin/cards` — все карты
- `GET /api/admin/audit-logs` — системный аудит

## 🗄️ База данных

### Миграции

Миграции выполняются автоматически при запуске через Liquibase.

Файлы миграций: `src/main/resources/db/changelog/`

### Структура БД

- `users` — пользователи
- `roles` — роли
- `user_roles` — связь пользователей и ролей
- `cards` — банковские карты
- `transactions` — история переводов
- `audit_logs` — системный аудит

## 🧪 Тестирование

> **Важно:** Проект использует Java 21. Для запуска тестов используйте готовые скрипты, которые автоматически запускают тесты в Docker с нужной версией Java.

### Запуск всех тестов

**Windows:**
```bash
test.bat test
```

**Linux/Mac:**
```bash
./test.sh test
```

### Запуск с покрытием

**Windows:**
```bash
test.bat clean test jacoco:report
```

**Linux/Mac:**
```bash
./test.sh clean test jacoco:report
```

Отчет будет доступен в `target/site/jacoco/index.html`

### Альтернативный способ (если установлена Java 21)

```bash
mvn test
```

### Подробности

Подробное руководство по тестированию см. в [docs/test-guide.md](docs/test-guide.md)

## 📝 Логирование

Логи сохраняются в директории `logs/`:

- `logs/app.log` — все логи
- `logs/error.log` — только ошибки

Ротация логов: 7 дней для app.log, 30 дней для error.log

## 🐳 Docker команды

```bash
# Запуск
docker-compose up -d

# Остановка
docker-compose down

# Просмотр логов
docker-compose logs -f app

# Пересборка
docker-compose up -d --build

# Очистка
docker-compose down -v
```

## 📂 Структура проекта

```
cms/
├── docs/                    # Документация
├── src/
│   ├── main/
│   │   ├── java/com/ayungi/cms/
│   │   │   ├── config/      # Конфигурация
│   │   │   ├── controller/  # REST контроллеры
│   │   │   ├── dto/         # DTO объекты
│   │   │   ├── entity/      # JPA сущности
│   │   │   ├── exception/   # Обработка исключений
│   │   │   ├── repository/  # Репозитории
│   │   │   ├── security/    # Безопасность
│   │   │   ├── service/     # Бизнес-логика
│   │   │   └── util/        # Утилиты
│   │   └── resources/
│   │       ├── db/changelog/  # Liquibase миграции
│   │       ├── application.yml
│   │       └── logback-spring.xml
│   └── test/                # Тесты
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## 🔒 Безопасность

- Пароли хешируются через BCrypt
- Номера карт шифруются AES-256
- JWT токены с ограниченным сроком действия
- Ролевая модель доступа
- Валидация всех входных данных
- Системный аудит операций

## 📊 Мониторинг

Spring Boot Actuator эндпоинты:

- `/actuator/health` — статус приложения
- `/actuator/info` — информация о приложении

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте ветку для фичи (`git checkout -b feature/amazing-feature`)
3. Закоммитьте изменения (`git commit -m 'Add amazing feature'`)
4. Запушьте в ветку (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## 📄 Лицензия

© 2025 Ayungich

## 📞 Контакты

Если у вас есть вопросы или предложения, создайте issue в репозитории.

---

