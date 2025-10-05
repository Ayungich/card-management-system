# Frontend Integration Guide

## 📋 Общая информация

**Backend URL**: `http://localhost:8080`  
**API Base Path**: `/api`  
**OpenAPI Specification**: `http://localhost:8080/api-docs`  
**Swagger UI**: `http://localhost:8080/swagger-ui.html`

---

## 🔐 Аутентификация

### Механизм
- **Тип**: JWT (Bearer Token)
- **Header**: `Authorization: Bearer <access_token>`
- **Access Token**: срок действия 1 час
- **Refresh Token**: срок действия 7 дней

### Flow аутентификации

```javascript
// 1. Регистрация
POST /api/auth/register
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!"
}

// Response
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}

// 2. Логин
POST /api/auth/login
{
  "username": "john_doe",
  "password": "SecurePass123!"
}

// 3. Обновление токена
POST /api/auth/refresh
{
  "refreshToken": "eyJhbGc..."
}

// 4. Логаут
POST /api/auth/logout
Authorization: Bearer <access_token>
```

### Пример Axios Interceptor

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

// Request interceptor - добавляем токен
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor - обработка 401
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem('refreshToken');
        const { data } = await axios.post(
          'http://localhost:8080/api/auth/refresh',
          { refreshToken }
        );

        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);

        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return api(originalRequest);
      } catch (err) {
        // Redirect to login
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(err);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
```

---

## 🎯 Основные API Endpoints

### Auth (Public)
```
POST   /api/auth/register     - Регистрация
POST   /api/auth/login        - Вход
POST   /api/auth/refresh      - Обновление токена
POST   /api/auth/logout       - Выход
```

### Cards (Authenticated)
```
GET    /api/cards             - Список карт пользователя
GET    /api/cards/{id}        - Детали карты
POST   /api/cards             - Создать карту
PUT    /api/cards/{id}/block  - Заблокировать карту
PUT    /api/cards/{id}/activate - Активировать карту
DELETE /api/cards/{id}        - Удалить карту
GET    /api/cards/{id}/balance - Баланс карты
GET    /api/cards/{id}/transactions - История транзакций карты
GET    /api/cards/search      - Поиск карт (query params)
```

### Transfers (Authenticated)
```
POST   /api/transfers         - Создать перевод
GET    /api/transfers/{id}    - Детали транзакции
GET    /api/transfers/history - История переводов
```

### Admin (ROLE_ADMIN only)
```
GET    /api/admin/users                    - Все пользователи
GET    /api/admin/users/{id}               - Детали пользователя
PUT    /api/admin/users/{id}               - Обновить пользователя
DELETE /api/admin/users/{id}               - Удалить пользователя
GET    /api/admin/cards                    - Все карты
GET    /api/admin/transactions             - Все транзакции
GET    /api/admin/statistics               - Статистика системы
GET    /api/admin/audit-logs               - Логи аудита
POST   /api/admin/users/{id}/roles/{role}  - Добавить роль
DELETE /api/admin/users/{id}/roles/{role}  - Удалить роль
```

---

## 📦 Модели данных

### User
```typescript
interface User {
  id: string;              // UUID
  username: string;
  email: string;
  enabled: boolean;
  roles: string[];         // ["USER", "ADMIN"]
  createdAt: string;       // ISO 8601
  updatedAt: string;
}
```

### Card
```typescript
interface Card {
  id: number;
  cardNumber: string;      // Замаскирован: "1234 56** **** 7890"
  ownerId: string;         // UUID пользователя
  expirationDate: string;  // "2025-12-31"
  status: 'ACTIVE' | 'BLOCKED' | 'EXPIRED';
  balance: number;         // Decimal
  createdAt: string;
  updatedAt: string;
}
```

### Transaction
```typescript
interface Transaction {
  id: number;
  fromCardId: number;
  toCardId: number;
  amount: number;
  status: 'SUCCESS' | 'FAILED';
  failureReason?: string;
  timestamp: string;       // ISO 8601
}
```

### Statistics (Admin only)
```typescript
interface Statistics {
  totalUsers: number;
  totalCards: number;
  totalTransactions: number;
  successfulTransactions: number;
  failedTransactions: number;
  totalTransferredAmount: number;
  activeCards: number;
  blockedCards: number;
}
```

---

## 🔒 Роли и права доступа

### USER (по умолчанию)
- ✅ Управление своими картами (CRUD)
- ✅ Просмотр своего баланса
- ✅ Создание переводов между картами
- ✅ Просмотр истории своих транзакций
- ❌ Доступ к данным других пользователей
- ❌ Административные функции

### ADMIN
- ✅ Все права USER
- ✅ Просмотр всех пользователей
- ✅ Управление пользователями
- ✅ Просмотр всех карт и транзакций
- ✅ Статистика системы
- ✅ Логи аудита
- ✅ Управление ролями пользователей

---

## 🚨 Обработка ошибок

### Формат ошибки
```typescript
interface ErrorResponse {
  timestamp: string;       // ISO 8601
  status: number;          // HTTP status code
  error: string;           // "Bad Request", "Unauthorized", etc.
  message: string;         // Человекочитаемое сообщение
  path: string;           // API endpoint
  details?: string[];     // Детали валидации (опционально)
}
```

### HTTP Status Codes

| Code | Значение | Когда возникает |
|------|----------|-----------------|
| 200 | OK | Успешный запрос |
| 201 | Created | Ресурс создан |
| 204 | No Content | Успешное удаление |
| 400 | Bad Request | Ошибка валидации |
| 401 | Unauthorized | Не авторизован / токен истек |
| 403 | Forbidden | Недостаточно прав |
| 404 | Not Found | Ресурс не найден |
| 409 | Conflict | Конфликт бизнес-логики |
| 500 | Internal Server Error | Ошибка сервера |

### Примеры ошибок

```javascript
// 400 - Ошибка валидации
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/cards",
  "details": [
    "expirationDate: must be a future date"
  ]
}

// 401 - Не авторизован
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials",
  "path": "/api/auth/login"
}

// 403 - Недостаточно прав
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied",
  "path": "/api/admin/users"
}

// 409 - Бизнес-логика
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Insufficient balance",
  "path": "/api/transfers"
}
```

---

## 🎨 Валидация форм

### Регистрация
```javascript
const validationRules = {
  username: {
    required: true,
    minLength: 3,
    maxLength: 50,
    pattern: /^[a-zA-Z0-9_]+$/,
    message: "Username must be 3-50 characters, alphanumeric and underscore only"
  },
  email: {
    required: true,
    pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    message: "Invalid email format"
  },
  password: {
    required: true,
    minLength: 8,
    maxLength: 100,
    message: "Password must be 8-100 characters"
  }
};
```

### Создание карты
```javascript
const validationRules = {
  expirationDate: {
    required: true,
    format: 'YYYY-MM-DD',
    minDate: new Date(), // Будущая дата
    message: "Expiration date must be in the future"
  }
};
```

### Перевод средств
```javascript
const validationRules = {
  fromCardId: {
    required: true,
    type: 'number',
    message: "Source card is required"
  },
  toCardId: {
    required: true,
    type: 'number',
    message: "Destination card is required"
  },
  amount: {
    required: true,
    min: 0.01,
    max: 1000000,
    precision: 2,
    message: "Amount must be between 0.01 and 1,000,000"
  }
};
```

---

## 🌐 CORS

Backend настроен для работы с фронтендом:

```javascript
// Разрешенные origins (настраиваются в application.yml)
const allowedOrigins = [
  'http://localhost:3000',  // React dev server
  'http://localhost:4200',  // Angular dev server
  'http://localhost:5173',  // Vite dev server
];

// Разрешенные методы
const allowedMethods = ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'];

// Разрешенные headers
const allowedHeaders = ['Authorization', 'Content-Type'];

// Credentials
const allowCredentials = true;
```

---

## 📝 Примеры использования

### React + TypeScript пример

```typescript
// services/api.ts
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export const api = axios.create({
  baseURL: API_BASE_URL,
});

// types/api.types.ts
export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface Card {
  id: number;
  cardNumber: string;
  expirationDate: string;
  status: 'ACTIVE' | 'BLOCKED' | 'EXPIRED';
  balance: number;
  createdAt: string;
  updatedAt: string;
}

// services/auth.service.ts
import { api } from './api';
import { LoginRequest, AuthResponse } from '../types/api.types';

export const authService = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const { data } = await api.post<AuthResponse>('/auth/login', credentials);
    return data;
  },

  register: async (userData: RegisterRequest): Promise<AuthResponse> => {
    const { data } = await api.post<AuthResponse>('/auth/register', userData);
    return data;
  },

  logout: async (): Promise<void> => {
    await api.post('/auth/logout');
  },

  refreshToken: async (refreshToken: string): Promise<AuthResponse> => {
    const { data } = await api.post<AuthResponse>('/auth/refresh', { refreshToken });
    return data;
  },
};

// services/card.service.ts
import { api } from './api';
import { Card } from '../types/api.types';

export const cardService = {
  getCards: async (): Promise<Card[]> => {
    const { data } = await api.get<Card[]>('/cards');
    return data;
  },

  getCard: async (id: number): Promise<Card> => {
    const { data } = await api.get<Card>(`/cards/${id}`);
    return data;
  },

  createCard: async (expirationDate: string): Promise<Card> => {
    const { data } = await api.post<Card>('/cards', { expirationDate });
    return data;
  },

  blockCard: async (id: number): Promise<void> => {
    await api.put(`/cards/${id}/block`);
  },

  activateCard: async (id: number): Promise<void> => {
    await api.put(`/cards/${id}/activate`);
  },

  deleteCard: async (id: number): Promise<void> => {
    await api.delete(`/cards/${id}`);
  },
};

// services/transfer.service.ts
import { api } from './api';
import { Transaction } from '../types/api.types';

export const transferService = {
  createTransfer: async (
    fromCardId: number,
    toCardId: number,
    amount: number
  ): Promise<Transaction> => {
    const { data } = await api.post<Transaction>('/transfers', {
      fromCardId,
      toCardId,
      amount,
    });
    return data;
  },

  getTransferHistory: async (): Promise<Transaction[]> => {
    const { data } = await api.get<Transaction[]>('/transfers/history');
    return data;
  },
};

// hooks/useAuth.ts
import { useState, useEffect } from 'react';
import { authService } from '../services/auth.service';

export const useAuth = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    setIsAuthenticated(!!token);
    setIsLoading(false);
  }, []);

  const login = async (username: string, password: string) => {
    try {
      const response = await authService.login({ username, password });
      localStorage.setItem('accessToken', response.accessToken);
      localStorage.setItem('refreshToken', response.refreshToken);
      setIsAuthenticated(true);
      return response;
    } catch (error) {
      throw error;
    }
  };

  const logout = async () => {
    try {
      await authService.logout();
    } finally {
      localStorage.clear();
      setIsAuthenticated(false);
    }
  };

  return { isAuthenticated, isLoading, login, logout };
};
```

---

## 🧪 Тестирование

### Postman Collection
OpenAPI спецификация доступна по адресу:
```
http://localhost:8080/api-docs
```

Импортируйте её в Postman:
1. File → Import
2. Вставьте URL: `http://localhost:8080/api-docs`
3. Import

### Тестовые данные

После первого запуска в БД будут только роли. Создайте пользователя через:

```bash
# Регистрация
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!"
  }'
```

---

## 📚 Дополнительные ресурсы

1. **OpenAPI документация**: http://localhost:8080/api-docs
2. **Swagger UI**: http://localhost:8080/swagger-ui.html
3. **Health Check**: http://localhost:8080/actuator/health
4. **Метрики**: http://localhost:8080/actuator/metrics

---

## 💡 Best Practices

### 1. Хранение токенов
```javascript
// ✅ Хорошо
localStorage.setItem('accessToken', token);

// ❌ Плохо (XSS уязвимость)
document.cookie = `token=${token}`;
```

### 2. Обработка loading состояний
```javascript
const [isLoading, setIsLoading] = useState(false);

const handleTransfer = async () => {
  setIsLoading(true);
  try {
    await transferService.createTransfer(fromId, toId, amount);
    // Success handling
  } catch (error) {
    // Error handling
  } finally {
    setIsLoading(false);
  }
};
```

### 3. Оптимистичные обновления
```javascript
// Сразу обновляем UI, откатываем при ошибке
const handleBlock = async (cardId: number) => {
  const originalCard = cards.find(c => c.id === cardId);
  
  // Optimistic update
  setCards(cards.map(c => 
    c.id === cardId ? { ...c, status: 'BLOCKED' } : c
  ));

  try {
    await cardService.blockCard(cardId);
  } catch (error) {
    // Rollback
    setCards(cards.map(c => 
      c.id === cardId ? originalCard : c
    ));
    showError('Failed to block card');
  }
};
```

### 4. Маскирование номеров карт
```javascript
// Backend возвращает уже замаскированные номера
// "1234 56** **** 7890"
// Не нужно дополнительно маскировать на фронте
```

### 5. Форматирование сумм
```javascript
const formatAmount = (amount: number): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(amount);
};

// 1234.56 → "$1,234.56"
```

---

## 🐛 Troubleshooting

### CORS ошибки
Убедитесь, что ваш origin добавлен в `application.yml`:
```yaml
cors:
  allowed-origins: http://localhost:3000
```

### 401 Unauthorized
- Проверьте, что токен отправляется в заголовке `Authorization: Bearer <token>`
- Проверьте срок действия токена (1 час)
- Используйте refresh token для обновления

### 403 Forbidden
- Проверьте роль пользователя
- Некоторые endpoints требуют ADMIN роль

### Нет данных после регистрации
- Новый пользователь не имеет карт - создайте через POST `/api/cards`
- По умолчанию роль USER - для ADMIN нужно обновить в БД

---

## 📞 Поддержка

При возникновении проблем:
1. Проверьте логи backend: `logs/app.log`
2. Проверьте Swagger UI для тестирования API
3. Используйте browser DevTools → Network для отладки запросов
