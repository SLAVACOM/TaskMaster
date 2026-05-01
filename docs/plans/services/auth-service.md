# Auth-service — карта сервиса

## Обзор

| Параметр | Значение |
|---|---|
| Язык | Java 21 |
| Порт | 8081 |
| База данных | PostgreSQL `auth_db` |
| Фреймворк | Spring Boot 4.0.5 |
| Lombok | да |

**Роль:** Единственная точка входа для аутентификации. Хранит `passwordHash` и `role`. Профиль пользователя (имя, email) живёт в UserService — Auth-service знает только UUID пользователя.

---

## Доменные сущности

### `User` (`auth_db.users`)

| Поле | Тип | Ограничения |
|---|---|---|
| `id` | UUID | PK, auto |
| `userId` | UUID | NOT NULL, UNIQUE (FK→UserService логически) |
| `passwordHash` | String | NOT NULL |
| `role` | Enum(`Role`) | NOT NULL, default USER |
| `createdAt` | Instant | NOT NULL, immutable |
| `updatedAt` | Instant | — |

**Индексы:** `idx_user_id` на `user_id`

**Enum `Role`:** USER, (предположительно ADMIN)

---

## REST API

| Метод | URL | Описание |
|---|---|---|
| POST | `/api/auth/register` | Регистрация — создаёт User в auth_db, вызывает UserService |
| POST | `/api/auth/login` | Логин — возвращает access + refresh токены, пишет в Kafka |
| POST | `/api/auth/refresh` | Обновление access-токена по refresh-токену |
| GET | `/api/auth/validate` | Валидация JWT (для gateway или сервисов) |
| PUT | `/api/auth/users/{userId}/password` | Смена пароля |
| PUT | `/api/auth/users/{userId}/role` | Изменение роли пользователя |
| DELETE | `/api/auth/users/{userId}` | Удаление пользователя |

---

## Kafka

### Продюсеры

| Топик | Когда | Файл |
|---|---|---|
| `user-login` | После успешного логина | `UserEventProducer.java` |

### Консьюмеры

_Нет_

---

## Межсервисные вызовы (REST)

| Направление | Цель | Когда |
|---|---|---|
| Auth → UserService | `RestClient` → `USER_SERVICE_URL` | При регистрации: создание записи пользователя |

---

## Точки расширения / что отсутствует

- [ ] **Email-верификация** — поле `isVerifiedEmail` есть в UserService, но Auth-service не отправляет письмо и не проверяет статус при логине
- [ ] **Revoke/blacklist токенов** — нет механизма инвалидации refresh-токенов (logout не реализован)
- [ ] **Rate limiting** — нет защиты от брутфорса на `/login`
- [ ] **OAuth2 / SSO** — только логин по паролю
- [ ] **2FA** — не реализовано
- [ ] **Audit log** — нет записи попыток входа/выхода
- [ ] **Kafka: `user-created`** — CLAUDE.md упоминает этот топик, но в коде продюсер не найден (только `user-login`)
