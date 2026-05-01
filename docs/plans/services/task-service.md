# TaskService — карта сервиса

## Обзор

| Параметр | Значение |
|---|---|
| Язык | Kotlin (JVM 21) |
| Порт | 8084 |
| База данных | PostgreSQL `task_db` |
| Фреймворк | Spring Boot 4.0.5 |
| Lombok | нет (Kotlin data classes) |

**Роль:** Управляет жизненным циклом задач: создание, обновление, поиск, история изменений. Хранит ссылки на пользователей (UUID), спринты (UUID из OrganizationService) и файлы (ключи S3) в JSON-полях.

---

## Доменные сущности

### `Task` (`task_db.tasks`)

| Поле | Тип | Описание |
|---|---|---|
| `id` | UUID | PK |
| `name` | String | Название задачи |
| `description` | String (TEXT) | Описание |
| `status` | Enum(`TaskStatus`) | TODO, IN_PROGRESS, ... |
| `priority` | Enum(`TaskPriority`) | LOW, MEDIUM, HIGH, ... |
| `responsible` | UUID | Ответственный (userId) |
| `executor` | UUID | Исполнитель (userId) |
| `observers` | List\<UUID\> (JSON) | Наблюдатели |
| `files` | List\<String\> (JSON) | Ключи файлов в S3 |
| `depends` | List\<UUID\> (JSON) | Зависимые задачи |
| `tags` | List\<String\> (JSON) | Теги (строки) |
| `sprintId` | UUID | Спринт (из OrganizationService) |
| `projectId` | UUID | Проект (из OrganizationService) |
| `storyPoint` | Int | Оценка |
| `start` | Instant | Дата начала |
| `end` | Instant | Дата окончания |
| `deadline` | Instant | Дедлайн |
| `isActive` | Boolean | Мягкое удаление |
| `createdAt` / `updatedAt` | Instant | Аудит |

**Конвертеры:** `StringListJsonConverter`, `UuidListJsonConverter` — сериализуют списки в JSON-строку в TEXT-колонке.

### `TaskHistory` (`task_db.task_history`)

| Поле | Тип | Описание |
|---|---|---|
| `id` | UUID | PK |
| `taskId` | UUID | FK→Task |
| `changedBy` | UUID | userId автора изменения |
| `changedAt` | Instant | Время изменения |
| `action` | Enum(`HistoryAction`) | CREATE, UPDATE, DELETE, ... |
| `changes` | List\<FieldChange\> (JSON) | Список изменённых полей: `{field, oldValue, newValue}` |

---

## REST API

### TaskController

| Метод | URL | Описание |
|---|---|---|
| POST | `/api/tasks` | Создать задачу |
| GET | `/api/tasks` | Список всех задач |
| GET | `/api/tasks/{id}` | Получить по ID |
| GET | `/api/tasks/search` | Поиск (параметры запроса) |
| PUT | `/api/tasks/{id}` | Обновить задачу |
| DELETE | `/api/tasks/{id}` | Деактивировать (soft delete) |

### TaskHistoryController

| Метод | URL | Описание |
|---|---|---|
| GET | `/api/tasks/{taskId}/history` | История задачи |
| GET | `/api/tasks/history/{historyId}` | Запись истории по ID |
| GET | `/api/tasks/history/by-user/{userId}` | История изменений пользователя |

---

## Kafka

_Ни продюсеров, ни консьюмеров._

---

## Межсервисные вызовы (REST)

_Нет прямых вызовов к другим сервисам._

> **Примечание:** `sprintId`, `responsible`, `executor`, `observers` хранятся как UUID без валидации через REST-вызовы к OrganizationService или UserService. Консистентность обеспечивается только на уровне бизнес-логики клиента.

---

## Точки расширения / что отсутствует

- [ ] **Kafka-события при изменении задачи** — нет событий `task.created`, `task.updated`, `task.assigned`; NotificationService не может реагировать на изменения задач
- [ ] **Фильтрация в getAll** — `/api/tasks` без параметров возвращает все задачи; нет фильтрации по организации/проекту/исполнителю
- [ ] **Фильтрация по спринту** — нет эндпоинта `GET /api/tasks?sprintId=...`
- [ ] **Валидация зависимостей** — `depends` (список UUID задач) не проверяется на существование и цикличность
- [ ] **Уведомление о назначении** — при изменении `executor` / `responsible` нет вызова NotificationService
- [ ] **Загрузка файлов** — `files` хранит ключи S3, но нет интеграции с S3CloudeStorage для генерации URL
- [ ] **Pagination** — список задач без пагинации
- [x] **Привязка к проекту** — добавлено поле `projectId`, фильтрация через `GET /api/tasks?projectId=` и `/api/tasks/search?projectId=`
