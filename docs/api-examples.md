# API Examples & Use Cases

## 🎯 Типичные сценарии использования

### Сценарий 1: Регистрация и создание первой карты

```bash
# Шаг 1: Регистрация
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'

# Response:
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}

# Шаг 2: Создание карты
curl -X POST http://localhost:8080/api/cards \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "expirationDate": "2025-12-31"
  }'

# Response:
{
  "id": 1,
  "cardNumber": "1234 56** **** 7890",
  "ownerId": "550e8400-e29b-41d4-a716-446655440000",
  "expirationDate": "2025-12-31",
  "status": "ACTIVE",
  "balance": 0.00,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

---

### Сценарий 2: Пополнение и перевод между картами

```javascript
// JavaScript пример
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
});

// 1. Получить список карт
const { data: cards } = await api.get('/cards');
console.log('My cards:', cards);

// 2. Создать перевод
const transfer = await api.post('/transfers', {
  fromCardId: 1,
  toCardId: 2,
  amount: 100.50
});

console.log('Transfer result:', transfer.data);
// {
//   "id": 10,
//   "fromCardId": 1,
//   "toCardId": 2,
//   "amount": 100.50,
//   "status": "SUCCESS",
//   "timestamp": "2024-01-15T11:00:00"
// }

// 3. Проверить баланс
const { data: balance } = await api.get('/cards/1/balance');
console.log('Balance:', balance);
// {
//   "cardId": 1,
//   "balance": 899.50,
//   "currency": "USD"
// }
```

---

### Сценарий 3: Блокировка и разблокировка карты

```bash
# Блокировка карты (при потере/краже)
curl -X PUT http://localhost:8080/api/cards/1/block \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# Response: 200 OK
{
  "message": "Card blocked successfully"
}

# Проверка статуса
curl -X GET http://localhost:8080/api/cards/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# Response:
{
  "id": 1,
  "cardNumber": "1234 56** **** 7890",
  "status": "BLOCKED",  # Изменился статус
  ...
}

# Разблокировка
curl -X PUT http://localhost:8080/api/cards/1/activate \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

### Сценарий 4: Просмотр истории транзакций

```javascript
// React компонент
const TransactionHistory = () => {
  const [transactions, setTransactions] = useState([]);
  const cardId = 1;

  useEffect(() => {
    const fetchTransactions = async () => {
      try {
        const { data } = await api.get(`/cards/${cardId}/transactions`);
        setTransactions(data);
      } catch (error) {
        console.error('Failed to fetch transactions:', error);
      }
    };

    fetchTransactions();
  }, [cardId]);

  return (
    <div>
      <h2>Transaction History</h2>
      {transactions.map(tx => (
        <div key={tx.id}>
          <p>Amount: ${tx.amount}</p>
          <p>Status: {tx.status}</p>
          <p>Date: {new Date(tx.timestamp).toLocaleString()}</p>
        </div>
      ))}
    </div>
  );
};
```

---

### Сценарий 5: Поиск карт (с фильтрацией)

```bash
# Поиск всех активных карт
curl -X GET "http://localhost:8080/api/cards/search?status=ACTIVE" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# Поиск карт с балансом больше 1000
curl -X GET "http://localhost:8080/api/cards/search?minBalance=1000" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# Поиск заблокированных карт
curl -X GET "http://localhost:8080/api/cards/search?status=BLOCKED" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

### Сценарий 6: Admin - управление пользователями

```bash
# Получить всех пользователей (только ADMIN)
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer <admin_token>"

# Response:
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "enabled": true,
    "roles": ["USER"],
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-15T10:00:00"
  },
  ...
]

# Назначить роль ADMIN
curl -X POST "http://localhost:8080/api/admin/users/550e8400-e29b-41d4-a716-446655440000/roles/ADMIN" \
  -H "Authorization: Bearer <admin_token>"

# Получить статистику
curl -X GET http://localhost:8080/api/admin/statistics \
  -H "Authorization: Bearer <admin_token>"

# Response:
{
  "totalUsers": 150,
  "totalCards": 320,
  "totalTransactions": 1543,
  "successfulTransactions": 1520,
  "failedTransactions": 23,
  "totalTransferredAmount": 1250000.00,
  "activeCards": 280,
  "blockedCards": 40
}
```

---

### Сценарий 7: Обновление токена

```javascript
// Автоматическое обновление токена
let isRefreshing = false;
let refreshSubscribers = [];

const subscribeTokenRefresh = (cb) => {
  refreshSubscribers.push(cb);
};

const onRefreshed = (token) => {
  refreshSubscribers.forEach((cb) => cb(token));
  refreshSubscribers = [];
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { config, response: { status } } = error;
    const originalRequest = config;

    if (status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // Ждем обновления токена
        return new Promise((resolve) => {
          subscribeTokenRefresh((token) => {
            originalRequest.headers['Authorization'] = `Bearer ${token}`;
            resolve(api(originalRequest));
          });
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const refreshToken = localStorage.getItem('refreshToken');
        const { data } = await axios.post(
          'http://localhost:8080/api/auth/refresh',
          { refreshToken }
        );

        const { accessToken } = data;
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);

        isRefreshing = false;
        onRefreshed(accessToken);

        originalRequest.headers['Authorization'] = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (err) {
        isRefreshing = false;
        localStorage.clear();
        window.location.href = '/login';
        return Promise.reject(err);
      }
    }

    return Promise.reject(error);
  }
);
```

---

### Сценарий 8: Валидация и обработка ошибок

```javascript
// React Hook Form + Yup пример
import { useForm } from 'react-hook-form';
import * as yup from 'yup';

const transferSchema = yup.object({
  fromCardId: yup.number().required('Source card is required'),
  toCardId: yup.number()
    .required('Destination card is required')
    .notOneOf([yup.ref('fromCardId')], 'Cannot transfer to the same card'),
  amount: yup.number()
    .required('Amount is required')
    .min(0.01, 'Minimum amount is 0.01')
    .max(1000000, 'Maximum amount is 1,000,000')
});

const TransferForm = () => {
  const { register, handleSubmit, formState: { errors }, setError } = useForm({
    resolver: yupResolver(transferSchema)
  });

  const onSubmit = async (data) => {
    try {
      await api.post('/transfers', data);
      toast.success('Transfer successful!');
    } catch (error) {
      if (error.response?.status === 409) {
        // Бизнес-логика ошибка
        setError('amount', {
          type: 'manual',
          message: error.response.data.message // "Insufficient balance"
        });
      } else if (error.response?.status === 400) {
        // Ошибка валидации
        const { details } = error.response.data;
        details?.forEach(detail => {
          const field = detail.split(':')[0];
          setError(field, { type: 'manual', message: detail });
        });
      } else {
        toast.error('Transfer failed. Please try again.');
      }
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <select {...register('fromCardId')}>
        {/* options */}
      </select>
      {errors.fromCardId && <span>{errors.fromCardId.message}</span>}

      <select {...register('toCardId')}>
        {/* options */}
      </select>
      {errors.toCardId && <span>{errors.toCardId.message}</span>}

      <input type="number" step="0.01" {...register('amount')} />
      {errors.amount && <span>{errors.amount.message}</span>}

      <button type="submit">Transfer</button>
    </form>
  );
};
```

---

### Сценарий 9: Пагинация и сортировка (Admin)

```bash
# Получить пользователей с пагинацией
curl -X GET "http://localhost:8080/api/admin/users?page=0&size=20&sort=createdAt,desc" \
  -H "Authorization: Bearer <admin_token>"

# Получить транзакции с фильтрацией
curl -X GET "http://localhost:8080/api/admin/transactions?status=FAILED&page=0&size=50" \
  -H "Authorization: Bearer <admin_token>"
```

---

### Сценарий 10: WebSocket для real-time обновлений (если требуется)

```javascript
// Если в будущем добавите WebSocket для real-time balance updates
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const connectWebSocket = (token) => {
  const socket = new SockJS('http://localhost:8080/ws');
  const stompClient = Stomp.over(socket);

  stompClient.connect(
    { Authorization: `Bearer ${token}` },
    () => {
      // Подписка на обновления баланса
      stompClient.subscribe('/user/queue/balance', (message) => {
        const balanceUpdate = JSON.parse(message.body);
        console.log('Balance updated:', balanceUpdate);
        // Обновить UI
      });

      // Подписка на транзакции
      stompClient.subscribe('/user/queue/transactions', (message) => {
        const transaction = JSON.parse(message.body);
        console.log('New transaction:', transaction);
        // Обновить список транзакций
      });
    },
    (error) => {
      console.error('WebSocket error:', error);
    }
  );

  return stompClient;
};
```

---

## 🎨 UI/UX рекомендации

### Отображение номеров карт
```javascript
// Backend возвращает уже замаскированные номера
cardNumber: "1234 56** **** 7890"

// Отображайте как есть, можно добавить иконку платежной системы
const CardDisplay = ({ card }) => (
  <div className="card">
    <div className="card-number">
      <CreditCardIcon />
      {card.cardNumber}
    </div>
    <div className="card-expiry">{card.expirationDate}</div>
    <div className={`card-status status-${card.status.toLowerCase()}`}>
      {card.status}
    </div>
  </div>
);
```

### Форматирование баланса
```javascript
const formatBalance = (balance, currency = 'USD') => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(balance);
};

// 1234.5 → "$1,234.50"
```

### Статусы карт - цветовая схема
```css
.status-active {
  color: #22c55e; /* green */
}

.status-blocked {
  color: #ef4444; /* red */
}

.status-expired {
  color: #6b7280; /* gray */
}
```

### Индикаторы транзакций
```javascript
const TransactionStatus = ({ status }) => {
  const icons = {
    SUCCESS: '✓',
    FAILED: '✗'
  };
  
  const colors = {
    SUCCESS: 'text-green-600',
    FAILED: 'text-red-600'
  };

  return (
    <span className={colors[status]}>
      {icons[status]} {status}
    </span>
  );
};
```

---

## 📱 Responsive Design

### Breakpoints рекомендации
```css
/* Mobile first */
.card-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
}

/* Tablet */
@media (min-width: 768px) {
  .card-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* Desktop */
@media (min-width: 1024px) {
  .card-list {
    grid-template-columns: repeat(3, 1fr);
  }
}
```

---

## 🔐 Security Best Practices

### 1. Не храните чувствительные данные
```javascript
// ❌ Плохо
localStorage.setItem('password', password);
localStorage.setItem('cardNumber', fullCardNumber);

// ✅ Хорошо
localStorage.setItem('accessToken', token);
localStorage.setItem('refreshToken', refreshToken);
// Backend вернет замаскированный номер карты
```

### 2. Очищайте токены при logout
```javascript
const logout = async () => {
  try {
    await api.post('/auth/logout');
  } finally {
    // Всегда очищайте
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    // или
    localStorage.clear();
  }
};
```

### 3. Валидируйте на фронте, но не доверяйте
```javascript
// Валидация на фронте для UX
if (amount <= 0) {
  setError('Amount must be positive');
  return;
}

// Backend также валидирует - это основная защита
const response = await api.post('/transfers', { amount });
```

---

## 🧪 Testing Examples

### Unit тесты для API service
```javascript
// __tests__/services/card.service.test.js
import { cardService } from '../services/card.service';
import { api } from '../services/api';

jest.mock('../services/api');

describe('CardService', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  test('getCards returns list of cards', async () => {
    const mockCards = [
      { id: 1, cardNumber: '1234 56** **** 7890', balance: 1000 },
      { id: 2, cardNumber: '9876 54** **** 3210', balance: 500 }
    ];

    api.get.mockResolvedValue({ data: mockCards });

    const cards = await cardService.getCards();

    expect(api.get).toHaveBeenCalledWith('/cards');
    expect(cards).toEqual(mockCards);
    expect(cards).toHaveLength(2);
  });

  test('createCard sends correct payload', async () => {
    const expirationDate = '2025-12-31';
    const mockCard = { id: 1, expirationDate, balance: 0 };

    api.post.mockResolvedValue({ data: mockCard });

    const card = await cardService.createCard(expirationDate);

    expect(api.post).toHaveBeenCalledWith('/cards', { expirationDate });
    expect(card).toEqual(mockCard);
  });
});
```

---

## 📊 Performance Optimization

### 1. Кеширование запросов
```javascript
import { useQuery } from '@tanstack/react-query';

const useCards = () => {
  return useQuery({
    queryKey: ['cards'],
    queryFn: () => cardService.getCards(),
    staleTime: 5 * 60 * 1000, // 5 минут
    cacheTime: 10 * 60 * 1000, // 10 минут
  });
};

// Использование
const CardList = () => {
  const { data: cards, isLoading, error } = useCards();

  if (isLoading) return <Spinner />;
  if (error) return <Error message={error.message} />;

  return <div>{cards.map(card => <Card key={card.id} {...card} />)}</div>;
};
```

### 2. Debounce для поиска
```javascript
import { debounce } from 'lodash';

const SearchCards = () => {
  const [results, setResults] = useState([]);

  const searchCards = debounce(async (query) => {
    if (query.length < 3) return;
    
    const { data } = await api.get(`/cards/search?query=${query}`);
    setResults(data);
  }, 500);

  return (
    <input
      type="text"
      onChange={(e) => searchCards(e.target.value)}
      placeholder="Search cards..."
    />
  );
};
```

---

## 🎯 Готовые компоненты для интеграции

Все примеры готовы к копированию и адаптации под ваш проект (React, Vue, Angular)!
