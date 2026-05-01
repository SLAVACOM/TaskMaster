# Fix JWT Docker Wiring

## Overview

JWT-аутентификация не работает в Docker из-за нескольких несвязанных, но накопившихся проблем:
некорректные имена env-переменных в docker-compose, невалидный синтаксис в `application.yml`,
устаревший JJWT API в Auth-service и несогласованные секреты между .env-файлами.

Цель: устранить все корневые причины так, чтобы Auth-service подписывал JWT теми же алгоритмом
и ключом, которые Gateway использует для верификации — и в Docker, и локально.

## Context (from discovery)

Задействованные файлы:
- `Auth-service/src/main/java/com/slavacom/auth_service/service/JwtService.java` — deprecated JJWT API
- `Auth-service/src/main/java/com/slavacom/auth_service/config/JWTConfig.java` — создаёт Key-бины
- `Auth-service/src/main/resources/application.yml` — `{{SERVER_PORT:8080}}` невалидный синтаксис
- `Auth-service/src/main/resources/application-docker.yml` — дефолтные секреты
- `Auth-service/.env` — неверный DB_URL (organization_db / порт 5434 вместо auth_db / 5433)
- `Gateway-service/src/main/java/com/slavacom/gateway/filter/JwtAuthFilter.java` — новый JJWT API (правильный)
- `Gateway-service/src/main/resources/application-docker.yml` — дефолтные секреты
- `Gateway-service/.env` — секреты не совпадают с Auth-service
- `All-Compose/docker-compose.services.yml` — передаёт `JWT_SECRET` вместо `JWT_ACCESS_SECRET` + `JWT_REFRESH_SECRET`

Найденные проблемы:
- **docker-compose**: оба сервиса получают `JWT_SECRET` — переменная, которую никто не читает.
  Из-за этого сервисы падают на дефолтные значения из `application-docker.yml`.
  Дефолты совпадают (`mySuperSecretKeyForDockerEnvironment`), но это хрупко.
- **Auth-service `application.yml`**: `server: port: {{SERVER_PORT:8080}}` — двойные фигурные скобки,
  Spring не распознаёт как placeholder. Строка не парсится в Integer → сбой при старте.
- **Auth-service `JwtService.java`**: использует deprecated API JJWT 0.11.x
  (`setSigningKey`, `parseClaimsJws`, `getBody`), хотя зависимость уже 0.13.0.
  Gateway использует правильный API 0.12.x+ (`verifyWith`, `parseSignedClaims`, `getPayload`).
- **Auth-service `.env`**: `DB_URL=jdbc:postgresql://localhost:5434/organization_db` —
  порт 5434 это organization-db, auth-db торчит на 5433 с базой `auth_db`.
- **Gateway `.env`**: секреты `mySuperSecretAccessKeyForDockerEnvironment` / `mySuperSecretRefreshKeyForDockerEnvironment`
  не совпадают с Auth-service `.env` (`mySuperSecretKeyForDockerEnvironment`).

## Development Approach

- **Testing approach**: None — проверяем вручную через docker compose и логи
- Завершать каждую задачу полностью перед переходом к следующей
- Каждое изменение — минимальное и целевое, не трогать несвязанный код

## Progress Tracking

- Отмечать выполненные пункты `[x]`
- Добавлять новые задачи с префиксом ➕
- Фиксировать блокеры с ⚠️

## Solution Overview

Единая стратегия JWT-секретов:
- `JWT_ACCESS_SECRET` — секрет для access-токенов (нужен Auth-service + Gateway)
- `JWT_REFRESH_SECRET` — секрет для refresh-токенов (нужен только Auth-service)
- Все .env-файлы и docker-compose используют одинаковые имена переменных и согласованные значения

Canonical значения (для dev/docker):
```
JWT_ACCESS_SECRET=TaskMasterAccessSecret2026SuperLongKey
JWT_REFRESH_SECRET=TaskMasterRefreshSecret2026SuperLongKey
```

(≥ 32 байт → подходит для HS256)

Auth-service подписывает access/refresh токены через `jwtAccessSigningKey` / `jwtRefreshSigningKey` бины.
Gateway верифицирует только access-токены через `JWT_ACCESS_SECRET`.

## Technical Details

### JJWT 0.13.x API — правильный вариант (уже в Gateway, нужно перенести в Auth-service)

**Билдер (подписание)** — без изменений, уже корректный:
```java
Jwts.builder()
    .claims(extraClaims)
    .subject(subject)
    .issuedAt(new Date(...))
    .expiration(new Date(...))
    .signWith(signingKey)
    .compact();
```

**Парсер (верификация)** — deprecated → новый:
```java
// Было (deprecated 0.11.x):
Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody()

// Должно быть (0.12.x+):
Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token).getPayload()
```

### Зависимости env-переменных
| Сервис | Читает | Используется для |
|---|---|---|
| Auth-service | `JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET` | подпись access + refresh токенов |
| Gateway-service | `JWT_ACCESS_SECRET` | верификация access-токенов |

## What Goes Where

**Implementation Steps** — все изменения кода и конфигов в этом репо.

**Post-Completion** — ручная верификация через docker compose.

---

## Implementation Steps

### Task 1: Исправить Auth-service application.yml (невалидный server.port)

**Files:**
- Modify: `Auth-service/src/main/resources/application.yml`

- [x] Заменить `server: port: {{SERVER_PORT:8080}}` на `server: port: ${SERVER_PORT:8081}`
  (auth-service слушает 8081 локально, судя по .env)

---

### Task 2: Исправить Auth-service .env (неверный DB_URL)

**Files:**
- Modify: `Auth-service/.env`

- [x] Заменить `DB_URL=jdbc:postgresql://localhost:5434/organization_db`
  на `DB_URL=jdbc:postgresql://localhost:5433/auth_db`
  (auth-db в docker-compose.db.yml: `ports: "5433:5432"`, база `auth_db`)

---

### Task 3: Обновить JwtService — убрать deprecated JJWT API

**Files:**
- Modify: `Auth-service/src/main/java/com/slavacom/auth_service/service/JwtService.java`
- Modify: `Auth-service/src/main/java/com/slavacom/auth_service/config/JWTConfig.java`

- [x] В `JWTConfig.java`: изменить тип возврата бинов с `Key` на `SecretKey`
  (`Keys.hmacShaKeyFor(...)` уже возвращает `SecretKey`)
- [x] В `JwtService.java`: обновить поля `jwtAccessSigningKey` / `jwtRefreshSigningKey`
  на тип `SecretKey` (вместо `Key`)
- [x] В `JwtService.java`, метод `extractAllClaims`: заменить
  `Jwts.parser().setSigningKey(signingKey).build().parseClaimsJws(token).getBody()`
  на `Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload()`
- [x] Проверить: `buildToken()` и `signWith(signingKey)` в билдере уже используют правильный API — не трогать

---

### Task 4: Согласовать JWT-секреты в Auth-service и Gateway .env

**Files:**
- Modify: `Auth-service/.env`
- Modify: `Gateway-service/.env`

- [x] В `Auth-service/.env`: установить
  ```
  JWT_ACCESS_SECRET=TaskMasterAccessSecret2026SuperLongKey
  JWT_REFRESH_SECRET=TaskMasterRefreshSecret2026SuperLongKey
  ```
- [x] В `Gateway-service/.env`: установить
  ```
  JWT_ACCESS_SECRET=TaskMasterAccessSecret2026SuperLongKey
  JWT_REFRESH_SECRET=TaskMasterRefreshSecret2026SuperLongKey
  ```
  (refresh-секрет Gateway не нужен — она не верифицирует refresh-токены)

---

### Task 5: Исправить docker-compose.services.yml — правильные имена JWT env-переменных

**Files:**
- Modify: `All-Compose/docker-compose.services.yml`

- [x] В блоке `auth-service`: заменить `JWT_SECRET: ...` на:
  ```yaml
  JWT_ACCESS_SECRET: TaskMasterAccessSecret2026SuperLongKey
  JWT_REFRESH_SECRET: TaskMasterRefreshSecret2026SuperLongKey
  ```
- [x] В блоке `gateway-service`: заменить `JWT_SECRET: ...` на:
  ```yaml
  JWT_ACCESS_SECRET: TaskMasterAccessSecret2026SuperLongKey
  ```

---

### Task 6: Согласовать дефолтные секреты в application-docker.yml

**Files:**
- Modify: `Auth-service/src/main/resources/application-docker.yml`
- Modify: `Gateway-service/src/main/resources/application-docker.yml`

- [x] В `Auth-service/application-docker.yml`: обновить дефолты:
  ```yaml
  jwt:
    access:
      secret: ${JWT_ACCESS_SECRET:TaskMasterAccessSecret2026SuperLongKey}
    refresh:
      secret: ${JWT_REFRESH_SECRET:TaskMasterRefreshSecret2026SuperLongKey}
  ```
- [x] В `Gateway-service/application-docker.yml`: обновить дефолты:
  ```yaml
  jwt:
    access:
      secret: ${JWT_ACCESS_SECRET:TaskMasterAccessSecret2026SuperLongKey}
    refresh:
      secret: ${JWT_REFRESH_SECRET:TaskMasterRefreshSecret2026SuperLongKey}
  ```

---

### Task 7: Verify acceptance criteria

- [ ] Локальный запуск Auth-service: убедиться, что стартует без ошибок (`./gradlew bootRun` из `Auth-service/`)
- [ ] `POST /api/auth/register` + `POST /api/auth/login` → получить access-токен
- [ ] Запустить полный docker-compose стек:
  ```bash
  cd All-Compose
  docker compose -f docker-compose.yaml -f docker-compose.db.yml -f docker-compose.kafka.yml -f docker-compose.services.yml up -d
  ```
- [ ] Проверить логи auth-service и gateway: убедиться что оба стартовали без ошибок
- [ ] `POST /api/auth/login` через Gateway (порт 8090) → получить JWT
- [ ] `GET /api/users/me` (или любой защищённый эндпоинт) через Gateway с токеном → 200, не 401
- [ ] Запрос без токена → 401
- [ ] Проверить в логах Gateway: `JWT_ACCESS_SECRET` совпадает с тем, что Auth-service использовал для подписи

## Post-Completion

**Ручная верификация**:
- Зайти в контейнер Auth-service и Gateway, сравнить значения `JWT_ACCESS_SECRET` через `env | grep JWT`
- Убедиться что у Auth-service нет ошибок подключения к БД (`auth_db`, а не `organization_db`)

**Безопасность в production**:
Текущие секреты (`TaskMasterAccessSecret2026SuperLongKey`) подходят только для dev/docker.
Для прод-окружения: вынести секреты в Vault / Secret Manager и передавать через env без дефолтов.
