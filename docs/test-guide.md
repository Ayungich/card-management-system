# Руководство по запуску тестов

## Запуск тестов через Docker

Поскольку проект использует Java 21, а локально может быть установлена другая версия Java, рекомендуется запускать тесты через Docker контейнер с Maven и Java 21.

### Команда для запуска всех тестов

```bash
docker run --rm -v E:\projects\cms:/app -w /app maven:3.9-eclipse-temurin-21-alpine mvn test
```

**Параметры:**
- `--rm` - автоматически удаляет контейнер после завершения
- `-v E:\projects\cms:/app` - монтирует текущую директорию проекта в контейнер
- `-w /app` - устанавливает рабочую директорию в контейнере
- `maven:3.9-eclipse-temurin-21-alpine` - образ с Maven и Java 21

### Запуск конкретного теста

Для запуска конкретного тестового класса:

```bash
docker run --rm -v E:\projects\cms:/app -w /app maven:3.9-eclipse-temurin-21-alpine mvn test -Dtest=AuthServiceTest
```

Для запуска конкретного метода теста:

```bash
docker run --rm -v E:\projects\cms:/app -w /app maven:3.9-eclipse-temurin-21-alpine mvn test -Dtest=AuthServiceTest#login_WithValidCredentials_ShouldReturnAuthResponse
```

### Запуск тестов с отчетом о покрытии (JaCoCo)

```bash
docker run --rm -v E:\projects\cms:/app -w /app maven:3.9-eclipse-temurin-21-alpine mvn clean test jacoco:report
```

Отчет будет доступен в `target/site/jacoco/index.html`

### Просмотр результатов тестов

Результаты тестов сохраняются в:
- `target/surefire-reports/` - текстовые отчеты
- `target/surefire-reports/*.xml` - XML отчеты

### Запуск тестов с пропуском компиляции

Если код уже скомпилирован:

```bash
docker run --rm -v E:\projects\cms:/app -w /app maven:3.9-eclipse-temurin-21-alpine mvn surefire:test
```

## Альтернативный способ - запуск в существующем контейнере

Если приложение уже запущено в Docker Compose, можно создать отдельный контейнер для тестов:

### 1. Создать временный контейнер

```bash
docker run -d --name cms-test --network cms_cms-network -v E:\projects\cms:/app -w /app maven:3.9-eclipse-temurin-21-alpine sleep infinity
```

### 2. Запустить тесты

```bash
docker exec cms-test mvn test
```

### 3. Удалить контейнер после завершения

```bash
docker rm -f cms-test
```

## Локальный запуск (если установлена Java 21)

Если локально установлена Java 21:

```bash
mvn test
```

Проверить версию Java:

```bash
java -version
```

## Устранение неполадок

### Ошибка "class file version 65.0"

Эта ошибка означает, что тесты были скомпилированы с Java 21, но запускаются на более старой версии. Решение:

```bash
mvn clean
```

Затем запустите тесты через Docker.

### Тесты падают из-за отсутствия БД

Интеграционные тесты используют Testcontainers и автоматически поднимают PostgreSQL в Docker. Убедитесь, что:
1. Docker Desktop запущен
2. Docker daemon доступен из контейнера с тестами

### Долгая загрузка зависимостей

При первом запуске Maven скачивает все зависимости. Последующие запуски будут быстрее благодаря кешированию в `~/.m2/repository`.

## Структура тестов

```
src/test/java/com/ayungi/cms/
├── CmsApplicationTests.java          # Интеграционный тест контекста Spring
├── service/
│   └── AuthServiceTest.java         # Unit-тесты сервиса аутентификации
└── util/
    ├── CardNumberGeneratorTest.java  # Unit-тесты генератора номеров карт
    └── CardValidatorTest.java        # Unit-тесты валидатора карт
```

## Минимальное покрытие кода

Проект настроен на минимальное покрытие 50% (конфигурация в `pom.xml`). При недостаточном покрытии сборка упадет.

Проверить текущее покрытие:

```bash
docker run --rm -v E:\projects\cms:/app -w /app maven:3.9-eclipse-temurin-21-alpine mvn clean test jacoco:report
```

Затем открыть `target/site/jacoco/index.html` в браузере.

