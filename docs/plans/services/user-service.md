# UserService — карта сервиса

## Обзор

| Параметр | Значение |
|---|---|
| Язык | Java 25 (target JVM 21) |
| Порт | 8082 |
| База данных | PostgreSQL `user_db` |
| Фреймворк | Spring Boot 4.0.5 |
| Lombok | да |

**Роль:** Хранит публичный профиль пользователя (имя, email, username). Является источником истины о пользователе для остальных сервисов. Auth-service создаёт здесь запись при регистрации; OrganizationService читает данные пользователей через REST.

---

## Доменные сущности

### `User` (`user_db.users`)

| Поле | Тип | Ограничения |
|---|---|---|
| `id` | UUID | PK, auto |
| `firstName` | String | NOT NULL |
| `lastName` | String | NOT NULL |
| `email` | String | NOT NULL, UNIQUE |
| `username` | String | NOT NULL, UNIQUE |
| `isVerifiedEmail` | boolean | NOT NULL, default false |
| `active` | boolean | NOT NULL, default true |
| `lastProfileId` | UUID | nullable (FK→Profile логически) |
| `createdAt` | Instant | NOT NULL, immutable |
| `updatedAt` | Instant | — |

### `Profile` (`user_db.profiles`)

| Поле | Тип | Ограничения |
|---|---|---|
| `id` | UUID | PK, auto |
| `userId` | UUID | NOT NULL |
| `organizationId` | UUID | NOT NULL |
| `name` | String | NOT NULL |
| `description` | String | nullable |
| `isActive` | Boolean | NOT NULL, default true |
| `createdAt` | Instant | NOT NULL, immutable |
| `updatedAt` | Instant | — |

**Индексы:** `(user_id, organization_id)` UNIQUE; отдельно по `user_id`, `organization_id`

> Профиль — это "личность" пользователя в рамках конкретной организации. Один пользователь может иметь несколько профилей (по одному на организацию).

---

## REST API

### UserController

| Метод | URL | Описание |
|---|---|---|
| POST | `/api/users/can-register` | Проверка доступности email/username перед регистрацией |
| POST | `/api/users/register` | Создание записи пользователя (вызывается Auth-service) |
| GET | `/api/users/{userId}` | Базовая информация о пользователе |
| GET | `/api/users/{userId}/exists` | Проверка существования пользователя |
| POST | `/api/users/findUser/login/{login}` | Поиск по username |
| POST | `/api/users/findUser/email/{email}` | Поиск по email |
| GET | `/api/users/{userId}/extended` | Расширенная информация (+ профили) |
| PUT | `/api/users/{userId}/last-profile/{profileId}` | Обновление активного профиля |
| GET | `/api/users/me/extended` | Текущий пользователь + профили (из JWT) |

### ProfileController

| Метод | URL | Описание |
|---|---|---|
| POST | `/api/profiles` | Создание профиля в организации |
| GET | `/api/profiles` | Список всех профилей |
| GET | `/api/profiles/user/{userId}` | Профили конкретного пользователя |
| PUT | `/api/profiles/{profileId}` | Обновление профиля |
| PUT | `/api/profiles/{profileId}/activate` | Активация профиля |

---

## Kafka

### Продюсеры

_Нет_

### Консьюмеры

_Нет_ (CLAUDE.md упоминает топик `user-created`, но в коде консьюмер не найден)

---

## Межсервисные вызовы (REST)

| Направление | Инициатор | Когда |
|---|---|---|
| Auth → UserService | Auth-service | Регистрация, проверка перед логином |
| OrgService → UserService | OrganizationService (`UserServiceClient`) | Валидация userId при добавлении сотрудника/приглашении |

---

## Точки расширения / что отсутствует

- [ ] **Email-верификация** — поле `isVerifiedEmail` есть, но нет эндпоинта для подтверждения и отправки письма
- [ ] **Аватар/фото** — нет поддержки медиа (S3CloudeStorage не интегрирован)
- [ ] **Поиск пользователей** — нет полнотекстового поиска, только точный поиск по email/username
- [ ] **Удаление / деактивация** — поле `active` есть, но нет эндпоинта деактивации из UserService (только через Auth-service)
- [ ] **Kafka consumer** — при регистрации через Auth-service UserService не получает событие асинхронно; создание идёт синхронным REST-вызовом, что создаёт coupling
- [ ] **Pagination** — `/api/profiles` возвращает всё без пагинации
