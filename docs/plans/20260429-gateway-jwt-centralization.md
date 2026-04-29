# Gateway: централизованная JWT-валидация

## Overview
JWT-токен сейчас парсится независимо в каждом сервисе (OrganizationService, TaskService, UserService).
Это означает, что `JWT_SECRET` хранится везде, логика дублируется, и изменение claims требует правок
во всех сервисах.

**Цель**: создать `Gateway-service` (Spring Cloud Gateway), который единственный валидирует JWT,
извлекает claims и передаёт их downstream-сервисам в виде заголовков (`X-User-Id`, `X-User-Role`,
`X-Profile-Id`, `X-Organization-Id`). Downstream-сервисы убирают JWT-логику и читают заголовки напрямую.

## Context (from discovery)
- **Gateway**: закомментированный блок уже есть в `All-Compose/docker-compose.services.yml`
- **JWT claims в токене**: `userId`, `role`, `profileId`, `organizationId` (из `Auth-service/JwtService.java`)
- **Текущие точки JWT-парсинга**:
  - `TaskService/TaskController.kt` — `@RequestHeader("Authorization")` → `jwtTokenProvider.extractUserId()`
  - `OrganizationService/JwtAuthenticationFilter.kt` — парсит JWT → помещает в `SecurityContextHolder`; `OrganizationController` читает через `currentUserId()`
  - `UserService/UserController.java` `/me/extended` — `@RequestHeader("Authorization")` → `jwtTokenProvider.extractUserId()`
- **Файлы JWT-логики, которые удаляются**:
  - `OrganizationService/security/JwtAuthenticationFilter.kt`
  - `OrganizationService/security/JwtTokenProvider.java`
  - `TaskService/security/JwtTokenProvider.kt`
  - `UserService/security/JwtTokenProvider.java`

## Development Approach
- **Testing approach**: None — проверяем вручную через docker-compose
- Завершать каждую задачу полностью перед переходом к следующей

## Progress Tracking
- Отмечать выполненные пункты `[x]`
- Добавлять новые задачи с префиксом ➕
- Фиксировать блокеры с ⚠️

## Solution Overview
```
Client → Gateway-service :8080
         ├─ /api/auth/** → Auth-service (без JWT-проверки)
         ├─ /api/users/** → UserService (+ X-User-Id header)
         ├─ /api/organizations/** → OrganizationService (+ X-User-Id header)
         └─ /api/tasks/** → TaskService (+ X-User-Id header)

Заголовки, добавляемые Gateway:
  X-User-Id: <UUID>
  X-User-Role: ADMIN | USER
  X-Profile-Id: <UUID> (nullable)
  X-Organization-Id: <UUID> (nullable)
```

Downstream-сервисы: убирают JWT, читают `X-User-Id` через `@RequestHeader`.

## Technical Details

### Заголовки Gateway → Downstream
| Header | Тип | Источник claim |
|---|---|---|
| `X-User-Id` | UUID string | `userId` |
| `X-User-Role` | string | `role` |
| `X-Profile-Id` | UUID string (опц.) | `profileId` |
| `X-Organization-Id` | UUID string (опц.) | `organizationId` |

### Маршруты Gateway
| Путь | Цель | Auth |
|---|---|---|
| `/api/auth/**` | `auth-service:8080` | нет (публичный) |
| `/api/users/**` | `user-service:8080` | да |
| `/api/organizations/**` | `organization-service:8080` | да |
| `/api/tasks/**` | `task-service:8080` | да |

### JWT — только в Auth-service и Gateway
`JWT_SECRET` убирается из UserService, OrganizationService, TaskService.

---

## Implementation Steps

### Task 1: Создать Gateway-service модуль

**Files:**
- Create: `Gateway-service/build.gradle`
- Create: `Gateway-service/src/main/resources/application.yml`
- Create: `Gateway-service/src/main/resources/application-docker.yml`
- Create: `Gateway-service/src/main/java/com/slavacom/gateway/GatewayApplication.java`
- Create: `Gateway-service/.env`
- Create: `Gateway-service/Dockerfile`

- [x] Создать `Gateway-service/build.gradle` с зависимостями:
  `spring-cloud-starter-gateway`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
- [x] Создать `GatewayApplication.java` — стандартный `@SpringBootApplication`
- [x] Создать `application.yml`: порт 8080, маршруты для auth/user/org/task сервисов,
  переменные `jwt.secret`, `*_service.url`
- [x] Создать `application-docker.yml`: docker-uri для каждого сервиса
- [x] Создать `.env` для локального запуска (PORT, JWT_SECRET, service URLs)
- [x] Создать `Dockerfile` (multi-stage: gradle build + JRE runtime)

---

### Task 2: JWT GlobalFilter в Gateway

**Files:**
- Create: `Gateway-service/src/main/java/com/slavacom/gateway/filter/JwtAuthFilter.java`
- Create: `Gateway-service/src/main/java/com/slavacom/gateway/config/SecurityConfig.java`

- [x] Создать `JwtAuthFilter implements GlobalFilter, Ordered`:
  - Пропускать запросы, чей путь начинается с `/api/auth/` (без проверки)
  - Извлекать `Authorization: Bearer <token>` из заголовка
  - Вернуть 401 если заголовка нет или токен невалиден
  - Парсить claims через JJWT (`userId`, `role`, `profileId`, `organizationId`)
  - Добавить в запрос: `X-User-Id`, `X-User-Role`, `X-Profile-Id`, `X-Organization-Id`
- [x] Создать `SecurityConfig` (WebFlux): отключить CSRF, все маршруты `permitAll`
  (Gateway сам контролирует доступ через фильтр)

---

### Task 3: Обновить OrganizationService

**Files:**
- Modify: `OrganizationService/src/main/java/com/slavacom/organizationservice/controller/OrganizationController.kt`
- Delete: `OrganizationService/src/main/java/com/slavacom/organizationservice/security/JwtAuthenticationFilter.kt`
- Delete: `OrganizationService/src/main/java/com/slavacom/organizationservice/security/JwtTokenProvider.java`
- Modify: `OrganizationService/src/main/java/com/slavacom/organizationservice/config/SecurityConfig.java`

- [x] В `OrganizationController.kt`: удалить `currentUserId()` через `SecurityContextHolder`,
  добавить `@RequestHeader("X-User-Id") userId: UUID` параметр в метод `create()`
- [x] Удалить файлы `JwtAuthenticationFilter.kt` и `JwtTokenProvider.java`
- [x] В `SecurityConfig.java`: убрать `jwtAuthenticationFilter` и его регистрацию через `addFilterBefore`
- [x] Убрать `JWT_SECRET` из `OrganizationService/.env` (его там не было)

---

### Task 4: Обновить TaskService

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/TaskController.kt`
- Delete: `TaskService/src/main/kotlin/com/slavacom/taskservice/security/JwtTokenProvider.kt`

- [x] В `TaskController.kt`: заменить `@RequestHeader("Authorization") token: String` +
  `jwtTokenProvider.extractUserId(token)` → `@RequestHeader("X-User-Id") changedBy: UUID`
  в методах `create`, `update`, `delete`
- [x] Удалить `JwtTokenProvider.kt`
- [x] Убрать `jwt.secret` из `application.yml` и jjwt из `build.gradle`

---

### Task 5: Обновить UserService

**Files:**
- Modify: `UserService/src/main/java/com/slavacom/userservice/controller/UserController.java`
- Delete: `UserService/src/main/java/com/slavacom/userservice/security/JwtTokenProvider.java`

- [x] В `UserController.java`, метод `getCurrentUserExtendedInfo` (`GET /me/extended`):
  заменить `@RequestHeader("Authorization") String authorizationHeader` + `jwtTokenProvider.extractUserId()`
  → `@RequestHeader("X-User-Id") UUID userId`
- [x] Удалить `JwtTokenProvider` из инжектирования в `UserController`
- [x] Удалить файл `JwtTokenProvider.java`
- [x] Убрать jjwt из `UserService/build.gradle`

---

### Task 6: Обновить docker-compose

**Files:**
- Modify: `All-Compose/docker-compose.services.yml`

- [x] Раскомментировать и актуализировать блок `gateway-service`
  (порт `8080:8080`, `JWT_SECRET`, `*_SERVICE_URL` для каждого сервиса)
- [x] Убрать `JWT_SECRET` из env-блоков `user-service`, `organization-service`
- [x] Добавить `Gateway-service` в `settings.gradle`

---

### Task 7: Verify acceptance criteria

- [ ] Собрать `Gateway-service` локально: `./gradlew bootRun`
- [ ] Запустить полный стек через docker-compose и выполнить:
  - `POST /api/auth/login` → получить JWT
  - `GET /api/users/{id}/extended` через Gateway — убедиться, что X-User-Id передаётся и ответ корректен
  - `POST /api/tasks` через Gateway — убедиться, что задача создаётся с правильным changedBy
  - `POST /api/organizations` через Gateway — убедиться, что создаётся с правильным accountable
- [ ] Убедиться, что запрос без токена возвращает 401 (кроме `/api/auth/**`)
- [ ] Убедиться, что expired токен возвращает 401

## Post-Completion

**Downstream-сервисы без gateway**:
Если какой-то сервис вызывается напрямую (обходя Gateway) с сервис-на-сервис запросами —
убедиться, что они тоже передают `X-User-Id` заголовок, либо эти эндпоинты публичные (внутренние).

**Порты**:
После внедрения Gateway единственный внешний порт — `8080`. Остальные сервисы можно убрать из
публичных `ports` в docker-compose (оставить только в `networks: micro-net`).
