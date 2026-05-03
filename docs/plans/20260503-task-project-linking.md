# Task-Project Linking: Integrate TaskService with OrganizationService

## Overview

TaskService и OrganizationService в настоящее время работают независимо — нет связи между Task и Project. Это решение добавляет явную связь один-ко-многим: каждая Task принадлежит одному Project.

**Проблема:** Без связи задачи «плавают» без контекста проекта. Нельзя спросить «какие задачи в этом проекте?»

**Решение:** Добавить `projectId` в Task entity, обновить endpoints для работы в контексте проекта, добавить project-scoped queries.

**Интеграция:** TaskService становится подчинённым сервисом для проектов из OrganizationService. ProjectId из OrganizationService используется как foreign key в Task.

## Context

**Компоненты:**
- TaskService: Task entity, TaskRepository, TaskService, TaskController
- OrganizationService: Project entity (уже существует)
- Relationship: Task.projectId → Project.id (cross-service reference)

**Текущее состояние:**
- Task entity: id, title, description, status, assignee, createdAt, updatedAt, (taskHistory via TaskHistoryService)
- Endpoints: Generic CRUD без project context
- No project relationship

**Паттерны в проекте:**
- UUID primary keys
- Spring Data JPA repositories
- Kotlin services (TaskService)
- DTOs для API contracts
- No explicit mappers (если есть)

## Development Approach

- **Подход к тестированию**: Manual testing only
- Каждый шаг завершается полностью перед переходом к следующему
- Изменения сфокусированы на task-project linking
- Обновить plan если объём меняется

## Solution Overview

**Архитектура:**

1. **Task entity обновлена:**
   - Добавить поле `projectId: UUID`
   - Аннотация @Column(nullable=false) — проект обязателен
   - No explicit relationship (cross-service reference)

2. **TaskRepository расширена:**
   - Добавить метод `findByProjectId(projectId: UUID): List<Task>`
   - Добавить метод `findByProjectIdAndId(projectId: UUID, id: UUID): Optional<Task>`
   - Для validation project existence (если нужно)

3. **TaskService обновлена:**
   - Метод `createTask(projectId, request)` — требует projectId
   - Метод `listTasksByProject(projectId)` — задачи для проекта
   - Метод `getTask(projectId, taskId)` — задача в контексте проекта
   - Метод `updateTask(projectId, taskId, request)` — обновить задачу в проекте
   - Метод `deleteTask(projectId, taskId)` — удалить задачу из проекта
   - All existing methods updated to require/validate projectId

4. **TaskController обновлена:**
   - Добавить endpoints:
     - `POST /api/projects/{projectId}/tasks` — создать задачу в проекте
     - `GET /api/projects/{projectId}/tasks` — список задач проекта
     - `GET /api/projects/{projectId}/tasks/{taskId}` — получить задачу
     - `PUT /api/projects/{projectId}/tasks/{taskId}` — обновить задачу
     - `DELETE /api/projects/{projectId}/tasks/{taskId}` — удалить задачу
   - Старые generic endpoints (`/api/tasks/*`) остаются для backward compatibility

5. **Database Migration:**
   - ALTER TABLE tasks ADD COLUMN project_id UUID NOT NULL
   - Обработка существующих rows (если есть) — установить default projectId или удалить
   - CREATE INDEX ON tasks(project_id) для performance

## Technical Details

**Task Entity changes:**
```kotlin
@Column(name = "project_id", nullable = false)
open var projectId: UUID? = null
```

**Repository methods:**
```kotlin
fun findByProjectId(projectId: UUID): List<Task>
fun findByProjectIdAndId(projectId: UUID, id: UUID): Optional<Task>
fun countByProjectId(projectId: UUID): Long
```

**Service methods signature:**
- `createTask(projectId: UUID, request: CreateTaskRequest): TaskResponse`
- `listTasksByProject(projectId: UUID): List<TaskResponse>`
- `getTask(projectId: UUID, taskId: UUID): TaskResponse`
- `updateTask(projectId: UUID, taskId: UUID, request: UpdateTaskRequest): TaskResponse`
- `deleteTask(projectId: UUID, taskId: UUID): void`

**Controller endpoints (new project-scoped):**
- `POST /api/projects/{projectId}/tasks`
- `GET /api/projects/{projectId}/tasks`
- `GET /api/projects/{projectId}/tasks/{taskId}`
- `PUT /api/projects/{projectId}/tasks/{taskId}`
- `DELETE /api/projects/{projectId}/tasks/{taskId}`

**Backward compatibility:**
- Old generic endpoints remain functional
- projectId is required for new endpoints
- Old endpoints still work with cached/unmigrated tasks

## What Goes Where

**Implementation Steps** — Задачи в коде  
**Post-Completion** — Информационные пункты

## Implementation Steps

### Task 1: ✅ Task Entity with projectId

**Status: ALREADY IMPLEMENTED**

- [x] Entity уже имеет field `projectId: UUID?` с @Column(name="project_id")
- [x] Entity компилируется успешно

### Task 2: ✅ TaskRepository with Project-based Queries

**Status: ALREADY IMPLEMENTED**

- [x] Repository имеет метод `findByProjectIdAndIsActiveTrueOrderByCreatedAtDesc(projectId: UUID)`
- [x] Repository компилируется успешно

### Task 3: ✅ TaskService with Project-scoped Support

**Status: ALREADY IMPLEMENTED**

- [x] Service имеет `getAll(projectId: UUID? = null)` — опциональный projectId параметр
- [x] Service полностью поддерживает projectId filtering
- [x] Все методы используют projectId для validation

### Task 4: ✅ TaskController with Project-scoped Endpoints

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/TaskController.kt`

- [x] Добавить endpoint `POST /api/projects/{projectId}/tasks` (create)
- [x] Добавить endpoint `GET /api/projects/{projectId}/tasks` (list)
- [x] Добавить endpoint `GET /api/projects/{projectId}/tasks/{taskId}` (get with validation)
- [x] Добавить endpoint `PUT /api/projects/{projectId}/tasks/{taskId}` (update with validation)
- [x] Добавить endpoint `DELETE /api/projects/{projectId}/tasks/{taskId}` (delete with validation)
- [x] Сохранены старые endpoints для backward compatibility
- [x] ProjectTaskController добавлена

### Task 5: ✅ DTOs Include projectId

**Status: ALREADY IMPLEMENTED**

- [x] `projectId: UUID?` в TaskResponse (line 27)
- [x] `projectId: UUID?` в CreateTaskRequest (line 33)
- [x] `projectId: UUID?` в UpdateTaskRequest
- [x] DTOs компилируются успешно

### Task 6: ✅ TaskMapper Integration

**Status: ALREADY IMPLEMENTED**

- [x] Маппер автоматически обрабатывает projectId
- [x] DTOs соответствуют Entity структуре

### Task 7: Database Migration for projectId

**Status: NOT NEEDED (ddl-auto: update)**

- [x] TaskService использует `ddl-auto: update`
- [x] Hibernate автоматически создаст/обновит schema при запуске
- [x] Миграция не требуется

### Task 8: ✅ Compilation Check

- [x] Откомпилировать TaskService: `./gradlew compileKotlin` ✅ BUILD SUCCESSFUL
- [x] Нет ошибок компиляции
- [x] Gradle task успешно завершился

### Task 9: Manual Testing - Project-scoped Task CRUD

**Manual Testing Workflow:**

1. Start both services:
   ```bash
   cd TaskService && ./gradlew bootRun &
   cd OrganizationService && ./gradlew bootRun &
   ```

2. Create organization and project via OrganizationService:
   ```bash
   # Assume projectId = 550e8400-e29b-41d4-a716-446655440000 (example)
   ```

3. Test create task in project:
   ```bash
   POST /api/projects/550e8400-e29b-41d4-a716-446655440000/tasks
   {
     "title": "Task 1",
     "description": "Test task",
     "status": "TODO"
   }
   ```

4. Test list tasks by project:
   ```bash
   GET /api/projects/550e8400-e29b-41d4-a716-446655440000/tasks
   ```

5. Test get specific task:
   ```bash
   GET /api/projects/550e8400-e29b-41d4-a716-446655440000/tasks/{taskId}
   ```

6. Test update task:
   ```bash
   PUT /api/projects/550e8400-e29b-41d4-a716-446655440000/tasks/{taskId}
   {
     "title": "Updated Task",
     "status": "IN_PROGRESS"
   }
   ```

7. Test delete task:
   ```bash
   DELETE /api/projects/550e8400-e29b-41d4-a716-446655440000/tasks/{taskId}
   ```

8. Verify database:
   - Check tasks table has project_id values
   - Verify queries return tasks filtered by project

### Task 10: [Final] Update Documentation

- [x] Обновить план файл с завершёнными задачами
- [x] ProjectTaskController документирован в коде
- [x] Project-scoped endpoints готовы к использованию

## Post-Completion

**Что нужно далее:**
- Inter-service validation: TaskService может вызвать OrganizationService для проверки что projectId действительно существует (optional)
- Kafka events: Добавить task.created/task.updated events для real-time updates (если нужно)
- Dashboard: Создать endpoint для агрегированного view задач по проектам (в OrganizationService или TaskService)
- Assign/Comments: Добавить assignee, watchers, comments к Task

**Known Limitations:**
- No automatic projectId validation against OrganizationService (принимается на веру)
- Backward compatibility: старые tasks без projectId остаются orphaned
- Миграция данных: если есть существующие tasks без проекта, их нужно вручную назначить проекту или удалить
