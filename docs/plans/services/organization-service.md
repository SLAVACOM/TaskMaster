# OrganizationService — карта сервиса

## Обзор

| Параметр | Значение |
|---|---|
| Язык | Kotlin (JVM 21) |
| Порт | 8083 |
| База данных | PostgreSQL `organization_db` |
| Фреймворк | Spring Boot 4.0.5 |
| Lombok | да |

**Роль:** Самый крупный сервис. Управляет организациями, проектами, сотрудниками, спринтами, канбан-досками, тегами и приглашениями. Использует UUID-ссылки на UserService (не FK на уровне БД).

---

## Доменные сущности

### `Organization` (`organization`)

| Поле | Тип | Описание |
|---|---|---|
| `id` | UUID | PK |
| `name` | String | Название |
| `description` | String | Описание |
| `accountable` | UUID | Ответственный (userId из UserService) |
| `isActive` | Boolean | Мягкое удаление |
| `createdAt` / `updatedAt` | Instant | Аудит |

### `Employees` (`employees`)

| Поле | Тип | Описание |
|---|---|---|
| `id` | UUID | PK |
| `userId` | UUID | Ссылка на UserService |
| `profileId` | UUID | Ссылка на Profile в UserService |
| `organizationId` | UUID | FK→Organization |
| `role` | String | Роль в организации (строка, не enum) |
| `permissions` | String (TEXT/JSON) | Список разрешений |
| `isActive` | Boolean | Мягкое удаление |

### `Projects` (`projects`)

| Поле | Тип | Описание |
|---|---|---|
| `id` | UUID | PK |
| `organizationId` | UUID | FK→Organization |
| `name` | String | — |
| `description` | String | — |
| `responsible` | UUID | userId ответственного |
| `isActive` | Boolean | Мягкое удаление |

### `OrganizationInvitation`

Приглашение пользователя в организацию (статус: pending/accepted/declined).

### `OrganizationTags` / `ProjectTags`

Теги на уровне организации и проекта.

### `ProjectEmployees`

Участники конкретного проекта (подмножество `Employees`).

### `ProjectGoals`

Цели / OKR проекта.

### `ProjectSprints`

Спринты проекта (статусы: активный, завершённый).

### `ProjectTaskStatuses`

Кастомные статусы задач для проекта (аналог колонок в Jira).

### `KanbanColumns` / `KanbanTaskPositions`

Колонки канбан-доски и позиции задач внутри колонок.

### `ProjectHistory`

Лог изменений проекта.

---

## REST API (64 эндпоинта)

### Organizations

| Метод | URL | Описание |
|---|---|---|
| GET | `/api/organizations` | Список организаций |
| POST | `/api/organizations` | Создать организацию |
| GET | `/api/organizations/{id}` | Получить по ID |
| PUT | `/api/organizations/{id}` | Обновить |
| DELETE | `/api/organizations/{id}` | Деактивировать (soft delete) |

### Employees

| Метод | URL | Описание |
|---|---|---|
| GET | `/api/organizations/{orgId}/employees` | Список сотрудников |
| POST | `/api/organizations/{orgId}/employees` | Добавить сотрудника |
| PUT | `/api/organizations/{orgId}/employees/{employeeId}` | Обновить |
| DELETE | `/api/organizations/{orgId}/employees/{employeeId}` | Удалить |

### Invitations

| Метод | URL | Описание |
|---|---|---|
| POST | `/api/organizations/{orgId}/invitations` | Пригласить пользователя |
| GET | `/api/organizations/{orgId}/invitations` | Список приглашений |
| PUT | `/api/invitations/{id}/accept` | Принять приглашение |
| PUT | `/api/invitations/{id}/decline` | Отклонить приглашение |

### Projects

| Метод | URL | Описание |
|---|---|---|
| GET | `/api/organizations/{orgId}/projects` | Проекты организации |
| POST | `/api/organizations/{orgId}/projects` | Создать проект |
| GET | `/api/projects/{id}` | Получить проект |
| PUT | `/api/projects/{id}` | Обновить |
| DELETE | `/api/projects/{id}` | Деактивировать |

### Project Employees

| Метод | URL | Описание |
|---|---|---|
| GET | `/api/projects/{projectId}/employees` | Участники проекта |
| POST | `/api/projects/{projectId}/employees` | Добавить участника |
| PUT | `/api/projects/{projectId}/employees/{id}` | Обновить |
| DELETE | `/api/projects/{projectId}/employees/{id}` | Удалить |

### Tags (Org + Project)

| Метод | URL | Описание |
|---|---|---|
| GET/POST | `/api/organizations/{orgId}/tags` | Теги организации |
| PUT/DELETE | `/api/organizations/{orgId}/tags/{tagId}` | — |
| GET/POST | `/api/projects/{projectId}/tags` | Теги проекта |
| PUT/DELETE | `/api/projects/{projectId}/tags/{tagId}` | — |

### Goals, Sprints, Statuses, History

| Ресурс | Базовый URL | Операции |
|---|---|---|
| Goals | `/api/projects/{projectId}/goals` | GET, POST, PUT, DELETE |
| Sprints | `/api/projects/{projectId}/sprints` | GET, POST, PUT + activate, complete |
| Statuses | `/api/projects/{projectId}/statuses` | GET, POST, PUT, DELETE |
| History | `/api/projects/{projectId}/history` | GET |

### Kanban

| Метод | URL | Описание |
|---|---|---|
| GET/POST | `/api/projects/{projectId}/kanban/columns` | Колонки |
| PUT | `/api/projects/{projectId}/kanban/columns/reorder` | Переупорядочить |
| PUT/DELETE | `/api/projects/{projectId}/kanban/columns/{columnId}` | Обновить/удалить |
| GET/PUT | `/api/projects/{projectId}/kanban/positions` | Позиции задач (bulk) |

---

## Kafka

_Ни продюсеров, ни консьюмеров._

---

## Межсервисные вызовы (REST)

| Направление | Цель | Когда |
|---|---|---|
| OrgService → UserService | `UserServiceClient` | Валидация userId при добавлении сотрудника / приглашении |

---

## Точки расширения / что отсутствует

- [ ] **Kafka-уведомления** — при принятии/отклонении приглашения не отправляется событие в NotificationService
- [ ] **Связь с TaskService** — `sprintId` в Task ссылается на спринты OrganizationService, но нет обратной связи (сколько задач в спринте, статус выполнения)
- [ ] **Роль сотрудника как enum** — `role` хранится как `String`, нет валидации допустимых значений
- [ ] **Права доступа (permissions)** — поле `permissions` хранится как TEXT/JSON, нет контракта/схемы
- [ ] **Пагинация** — большинство list-эндпоинтов возвращают всё без пагинации
- [ ] **Поиск проектов/организаций** — нет полнотекстового поиска
- [ ] **Мягкое удаление через фильтрацию** — `isActive` есть, но нет гарантии что все list-запросы фильтруют по нему
- [ ] **Связь KanbanColumns ↔ TaskService** — позиции задач хранятся в OrgService, но TaskService ничего об этом не знает
