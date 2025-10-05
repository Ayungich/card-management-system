# CI/CD Configuration

Этот проект использует GitHub Actions для автоматизации процессов сборки, тестирования и деплоя.

## Workflows

### 1. CI/CD Pipeline (`ci.yml`)

Основной pipeline, который запускается при каждом push и pull request в ветки `main` и `dev`.

**Этапы:**

#### Build and Test
- Сборка проекта с Maven
- Запуск unit-тестов
- Генерация отчета о покрытии кода (JaCoCo)
- Загрузка покрытия в Codecov
- Сборка Docker образа
- Сохранение образа как артефакт

#### Code Quality Analysis
- Проверка качества кода
- Запуск статического анализа

#### Security Scan
- Сканирование уязвимостей с помощью Trivy
- Загрузка результатов в GitHub Security

#### Deploy to Development
- Автоматический деплой в dev-окружение при push в ветку `dev`
- Требует настройки environment secrets

#### Deploy to Production
- Автоматический деплой в prod-окружение при push в ветку `main`
- Требует настройки environment secrets и approvals

### 2. Docker Image Publish (`docker-publish.yml`)

Публикация Docker образа в GitHub Container Registry.

**Триггеры:**
- При создании release
- Ручной запуск (workflow_dispatch)

**Возможности:**
- Multi-platform сборка (amd64, arm64)
- Автоматическое тегирование по версиям
- Кеширование слоев для ускорения сборки

### 3. Dependency Review (`dependency-review.yml`)

Проверка зависимостей в pull requests.

**Проверяет:**
- Уязвимости в зависимостях
- Лицензии (блокирует GPL-2.0, GPL-3.0)
- Severity level: moderate и выше

## Настройка

### Secrets

Для работы CI/CD необходимо настроить следующие secrets в GitHub:

#### Repository Secrets
- `CODECOV_TOKEN` - токен для Codecov (опционально)

#### Environment Secrets (Development)
- `DEV_SERVER_HOST` - хост dev-сервера
- `DEV_SERVER_USER` - пользователь для SSH
- `DEV_SSH_KEY` - SSH ключ для доступа
- `DEV_DB_PASSWORD` - пароль БД
- `DEV_JWT_SECRET` - JWT секрет

#### Environment Secrets (Production)
- `PROD_SERVER_HOST` - хост prod-сервера
- `PROD_SERVER_USER` - пользователь для SSH
- `PROD_SSH_KEY` - SSH ключ для доступа
- `PROD_DB_PASSWORD` - пароль БД
- `PROD_JWT_SECRET` - JWT секрет

### Environments

Создайте environments в настройках репозитория:

1. **development**
   - URL: https://dev.cms.example.com
   - Auto-deploy: включен

2. **production**
   - URL: https://cms.example.com
   - Required reviewers: настроить список
   - Auto-deploy: включен после approval

## Badges

Добавьте в основной README.md:

```markdown
![CI/CD](https://github.com/Ayungich/card-management-system/workflows/CI/CD%20Pipeline/badge.svg)
![Docker](https://github.com/Ayungich/card-management-system/workflows/Docker%20Image%20Publish/badge.svg)
[![codecov](https://codecov.io/gh/Ayungich/card-management-system/branch/main/graph/badge.svg)](https://codecov.io/gh/Ayungich/card-management-system)
```

## Локальный запуск CI

Для локального тестирования CI можно использовать [act](https://github.com/nektos/act):

```bash
# Установка act
# Windows (chocolatey)
choco install act-cli

# Запуск workflow локально
act -j build-and-test

# Запуск с secrets
act -j build-and-test --secret-file .secrets
```

## Мониторинг

- **GitHub Actions**: https://github.com/Ayungich/card-management-system/actions
- **Security Alerts**: https://github.com/Ayungich/card-management-system/security
- **Codecov**: https://codecov.io/gh/Ayungich/card-management-system

## Troubleshooting

### Тесты падают в CI, но работают локально

1. Проверьте переменные окружения
2. Убедитесь, что PostgreSQL сервис запустился
3. Проверьте версию Java (должна быть 21)

### Docker образ не собирается

1. Проверьте Dockerfile
2. Убедитесь, что все файлы доступны в контексте сборки
3. Проверьте .dockerignore

### Деплой не работает

1. Проверьте secrets в environment
2. Убедитесь, что SSH ключ корректный
3. Проверьте доступность сервера

## Дополнительные ресурсы

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Build Push Action](https://github.com/docker/build-push-action)
- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)
