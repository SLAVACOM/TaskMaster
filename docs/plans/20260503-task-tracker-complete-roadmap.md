# Task Tracker: Complete Feature Roadmap

## Overview

Полная реализация функционального task tracker на базе существующей микросервисной архитектуры. Фокус на backend (TaskService), расширение функционала до production-ready системы для управления задачами с поддержкой:
- Агрегированного Dashboard с фильтрацией и поиском
- Назначения задач, наблюдателей, комментариев и workflow статусов
- Управления временем (estimates, time spent, burndown charts)
- Блокирующих зависимостей и критического пути

**Frontend:** Не в scope этого плана (API-first, будет позже)

**Scope:** Только backend расширение TaskService  
**Testing:** Manual via API (без автоматических тестов)  
**Priority:** Реализовать все 5 компонентов в правильном порядке

## Context

**Текущее состояние:**
- TaskService: CRUD endpoints, history tracking, project-scoped endpoints
- Task entity: projectId, sprintId, status, priority, assignee (executor), observers
- Database: PostgreSQL с `ddl-auto: update`
- REST clients: готовы для inter-service communication

**Компоненты для обновления:**
- Task entity (добавить: dependencies, estimates, time_spent, watchers, comments)
- TaskService (новые методы для всех 5 компонентов)
- TaskRepository (новые queries для dashboard, filtering, aggregation)
- TaskController (новые endpoints для каждого компонента)
- DTOs (CreateTask/UpdateTask/TaskResponse обновить)

**Зависимости:**
- Существует TaskHistory entity для аудита (переиспользуем)
- Существует SprintId в Task (использовать для burndown)
- Existing executor/responsible/observers (переиспользуем)

## Development Approach

- **Тестирование:** Manual testing only (no automated tests)
- Каждый компонент реализуется отдельно, но в правильном порядке
- После каждого компонента проверка компиляции и manual testing
- Обновлять план при обнаружении новых требований
- Backward compatibility: старые endpoints продолжают работать

## Solution Overview

**Архитектура:**

TaskService будет расширена четырьмя основными компонентами:

1. **Dashboard Component** — агрегирующие endpoints для получения data для dashboard
   - Tasks по user (мой), по project, по sprint
   - Фильтрация по статусу, assignee, priority, дате
   - Поиск по названию/описанию

2. **Assignment & Workflow Component** — управление назначением и статусами
   - Assign task to user
   - Add/remove watchers
   - Comments on tasks
   - Transition statuses (TODO → IN_PROGRESS → DONE)
   - History tracking (уже есть TaskHistory)

3. **Time Tracking Component** — оценки и фактические затраты
   - Set estimate (story points / hours)
   - Log time spent
   - Calculate burndown (по sprint)
   - Velocity tracking

4. **Dependencies & Blocking Component** — связи между задачами
   - Mark task as blocked by another task
   - Get blocking/blocked tasks
   - Critical path calculation
   - Detect circular dependencies

5. **Dashboard Aggregation** — интегрирующие endpoints
   - /api/projects/{projectId}/dashboard — полный статус проекта
   - /api/sprints/{sprintId}/dashboard — sprint burndown, velocity
   - /api/users/me/dashboard — личный dashboard

## Technical Details

### New Task Entity Fields (Phase 1-4)

```kotlin
// Phase 2: Assignment & Workflow
@Column(name = "watchers", columnDefinition = "TEXT") // JSON array of UUIDs
open var watchers: List<UUID> = emptyList()

// Phase 3: Time Tracking  
@Column(name = "estimated_hours")
open var estimatedHours: Int? = null // или story points

@Column(name = "time_spent_hours")
open var timeSpentHours: Int? = 0

// Phase 4: Dependencies
@Convert(converter = UuidListJsonConverter::class)
@Column(name = "blocking_tasks", columnDefinition = "TEXT")
open var blockingTasks: List<UUID> = emptyList() // какие задачи блокируют эту

@Convert(converter = UuidListJsonConverter::class)
@Column(name = "blocked_by_tasks", columnDefinition = "TEXT")
open var blockedByTasks: List<UUID> = emptyList() // какие задачи были заблокированы этой
```

### New DTOs

```kotlin
// Phase 1: Dashboard Response
data class TaskDashboardResponse(
    val id: UUID,
    val name: String,
    val projectId: UUID,
    val sprintId: UUID?,
    val status: TaskStatus,
    val assignee: UUID?,
    val priority: TaskPriority,
    val createdAt: Instant,
    val deadline: Instant?,
    val estimatedHours: Int?,
    val timeSpentHours: Int?
)

// Phase 2: Assignment
data class AssignTaskRequest(val assigneeId: UUID)
data class AddWatcherRequest(val watcherId: UUID)
data class CommentRequest(val text: String)
data class TaskCommentResponse(
    val id: UUID,
    val taskId: UUID,
    val authorId: UUID,
    val text: String,
    val createdAt: Instant
)

// Phase 3: Time Tracking
data class LogTimeRequest(val hours: Int)
data class EstimateRequest(val estimatedHours: Int)

// Phase 4: Dependencies  
data class BlockTaskRequest(val blockingTaskId: UUID)
data class TaskDependencyResponse(
    val id: UUID,
    val blockingTaskId: UUID,
    val blockedTaskId: UUID
)
```

## What Goes Where

**Implementation Steps** — Backend changes in TaskService  
**Post-Completion** — Manual testing & verification

## Implementation Steps

### Phase 1: Dashboard & Analytics Endpoints ✅ COMPLETE

**Phase 1.1: Update Task entity & Repository queries** ✅

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/repository/TaskRepository.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/DashboardDto.kt`

- [x] Обновить TaskRepository: добавить methods:
  - `findByProjectIdAndStatusOrderByCreatedAtDesc(projectId, status): List<Task>`
  - `findByExecutorAndStatusOrderByDeadlineAsc(executor, status): List<Task>`
  - `findBySprintIdAndStatusOrderByPriorityDesc(sprintId, status): List<Task>`
  - `findByObserversContainsOrderByCreatedAtDesc(userId): List<Task>`
  - `searchByNameOrDescriptionAndProjectId(search, projectId): List<Task>`
- [ ] Убедиться компиляция успешна

**Phase 1.2: Dashboard Service & Controller endpoints** ✅

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/TaskService.kt`
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/TaskController.kt`

- [x] TaskService: добавить методы:
  - [x] `getMyTasks(userId): List<TaskResponse>` — мои задачи
  - [x] `getProjectDashboard(projectId): Map<String, Any>` — статус проекта с counts & stats
  - [x] `getSprintDashboard(sprintId): Map<String, Any>` — статус спринта с completion %
  - [x] `searchTasks(projectId, query): List<TaskResponse>` — поиск по имени/описанию
- [x] TaskController: добавить endpoints:
  - [x] `GET /api/tasks/my` — мои задачи
  - [x] `GET /api/projects/{projectId}/tasks/dashboard` — dashboard проекта
  - [x] `GET /api/sprints/{sprintId}/dashboard` — dashboard спринта
  - [x] `GET /api/tasks/search-text?q=...&projectId=...` — поиск
- [x] Compilation check: BUILD SUCCESSFUL ✅

---

### Phase 2: Task Assignment & Workflow ✅ COMPLETE

**Phase 2.1: Update Task entity with watchers** ✅

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/entity/Task.kt`

- [x] Добавить field `watchers: List<UUID>` с @Convert(UuidListJsonConverter)
- [x] Убедиться Entity компилируется

**Phase 2.2: Assignment endpoints** ✅

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/TaskService.kt`
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/TaskController.kt`

- [x] TaskService: добавить методы:
  - [x] `assignTask(taskId, userId, changedBy): TaskResponse`
  - [x] `unassignTask(taskId, changedBy): TaskResponse`
  - [x] `addWatcher(taskId, watcherId, changedBy): TaskResponse`
  - [x] `removeWatcher(taskId, watcherId, changedBy): TaskResponse`
  - [x] `transitionStatus(taskId, newStatus, changedBy): TaskResponse`
- [x] TaskController: добавить endpoints:
  - [x] `POST /api/tasks/{taskId}/assign` — assign
  - [x] `POST /api/tasks/{taskId}/unassign` — unassign
  - [x] `POST /api/tasks/{taskId}/watchers` — add watcher
  - [x] `DELETE /api/tasks/{taskId}/watchers/{watcherId}` — remove watcher
  - [x] `POST /api/tasks/{taskId}/transition` — change status
- [x] Compilation check: BUILD SUCCESSFUL ✅

**Phase 2.3: Comments (optional, use TaskHistory)** ✅

- [x] Переиспользовать TaskHistory для комментариев (action=COMMENT)
  - [x] Added COMMENT to HistoryAction enum
- [x] Endpoint: `POST /api/tasks/{taskId}/comments` — добавить комментарий
- [x] Compilation check: BUILD SUCCESSFUL ✅

---

### Phase 3: Time Tracking

**Phase 3.1: Update Task entity with time fields**

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/entity/Task.kt`

- [ ] Добавить fields:
  - `estimatedHours: Int?` — оценка в часах
  - `timeSpentHours: Int? = 0` — фактические часы
- [ ] Убедиться Entity компилируется

**Phase 3.2: Time tracking endpoints**

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/TaskService.kt`
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/TaskController.kt`

- [ ] TaskService: добавить методы:
  - `setEstimate(taskId, hours, changedBy): TaskResponse`
  - `logTime(taskId, hours, changedBy): TaskResponse` — добавить потраченное время
  - `getSprintBurndown(sprintId): BurndownData` — график burndown
  - `getVelocity(projectId): VelocityData` — velocity tracking
- [ ] TaskController: добавить endpoints:
  - `POST /api/tasks/{taskId}/estimate` — set estimate
  - `POST /api/tasks/{taskId}/log-time` — log time
  - `GET /api/sprints/{sprintId}/burndown` — burndown chart data
  - `GET /api/projects/{projectId}/velocity` — velocity data
- [ ] Manual test всех endpoints

---

### Phase 4: Dependencies & Blocking

**Phase 4.1: Update Task entity with dependencies**

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/entity/Task.kt`

- [ ] Добавить fields:
  - `blockingTasks: List<UUID>` — какие задачи блокируют эту
  - `blockedByTasks: List<UUID>` — какие задачи были заблокированы этой (денормализованно для читаемости)
- [ ] Убедиться Entity компилируется

**Phase 4.2: Dependency endpoints**

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/TaskService.kt`
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/TaskController.kt`

- [ ] TaskService: добавить методы:
  - `blockTask(taskId, blockingTaskId, changedBy): TaskResponse` — mark as blocked by
  - `unblockTask(taskId, blockingTaskId, changedBy): TaskResponse` — remove blocking
  - `getBlockingTasks(taskId): List<TaskResponse>` — какие блокируют эту
  - `getBlockedTasks(taskId): List<TaskResponse>` — какие заблокированы этой
  - `detectCircularDependencies(taskId): Boolean` — проверить циклы
  - `getCriticalPath(projectId): List<TaskResponse>` — критический путь
- [ ] TaskController: добавить endpoints:
  - `POST /api/tasks/{taskId}/block` — block task
  - `POST /api/tasks/{taskId}/unblock/{blockingTaskId}` — unblock
  - `GET /api/tasks/{taskId}/blocking` — get blocking tasks
  - `GET /api/tasks/{taskId}/blocked-by` — get blocked by tasks
  - `GET /api/projects/{projectId}/critical-path` — critical path
  - Detect cycles при create blocking relationship
- [ ] Manual test всех endpoints

---

### Phase 5: Compilation & Integration Check

**Files:** All modified TaskService files

- [ ] Откомпилировать TaskService: `./gradlew compileKotlin`
- [ ] Убедиться нет errors
- [ ] Проверить что все 4 phases интегрируются правильно

---

### Phase 6: Manual Testing & Verification

**Manual Testing Workflow:**

1. **Phase 1 (Dashboard):**
   - GET /api/tasks/my — список моих задач
   - GET /api/projects/{id}/dashboard — dashboard проекта
   - GET /api/sprints/{id}/dashboard — dashboard спринта
   - GET /api/tasks/search?q=bug — поиск задач

2. **Phase 2 (Assignment):**
   - POST /api/tasks/{id}/assign — assign себе
   - POST /api/tasks/{id}/watchers — add as watcher
   - POST /api/tasks/{id}/transition — change to IN_PROGRESS
   - DELETE /api/tasks/{id}/watchers/{userId} — remove watcher

3. **Phase 3 (Time Tracking):**
   - POST /api/tasks/{id}/estimate — set estimate 8 hours
   - POST /api/tasks/{id}/log-time — log 2 hours
   - GET /api/sprints/{id}/burndown — see burndown
   - GET /api/projects/{id}/velocity — see velocity

4. **Phase 4 (Dependencies):**
   - POST /api/tasks/{id1}/block — block task 1 by task 2
   - GET /api/tasks/{id}/blocking — see what blocks this
   - GET /api/projects/{id}/critical-path — see critical tasks
   - Попробовать создать cycle (должно быть ошибкой)

5. **Database Check:**
   - Проверить что все новые columns созданы (`ddl-auto` должен это сделать)
   - Spot check данные в БД соответствуют API responses

---

### Phase 7: [Final] Documentation & Deployment Prep

- [ ] Обновить план файл с завершёнными phases
- [ ] Убедиться что все endpoints задокументированы (код)
- [ ] Prepare для deployment (конфиги env vars if needed)
- [ ] Move plan to `docs/plans/completed/`

## Post-Completion

**Manual verification checklist:**
- All 4 dashboard views работают и возвращают корректные данные
- Assignment/workflow endpoints изменяют статус в БД и в responses
- Time tracking рассчитывает burndown и velocity правильно
- Dependencies не допускают circular blocking
- Все endpoints вернут 404 если projectId/taskId неверный
- Все endpoints вернут 400 если invalid request body

**Integration points:**
- TaskHistory автоматически логирует все транзакции
- ProjectId проверяется на каждом шаге
- X-User-Id header используется для audit trail
- Backward compatibility: старые endpoints продолжают работать

**Performance considerations:**
- Dashboard queries могут потребовать индексы:
  - INDEX ON tasks(project_id, status)
  - INDEX ON tasks(executor, status)
  - INDEX ON tasks(sprint_id, status)
- Добавить indices в миграции если нужно

**Что будет ПОСЛЕ этого плана:**
- Frontend: React app для всех endpoints
- Real-time: WebSocket для live updates
- Advanced: AI-powered task recommendations, time estimate learning
- Mobile: Мобильное приложение
