# TaskService: добавить поле projectId к задачам

## Overview

Добавить поле `projectId` (UUID, nullable) к сущности `Task`. Это позволит привязывать задачи к проектам OrganizationService и фильтровать их по проекту. Без валидации через OrganizationService (UUID хранится по доверию, аналогично `sprintId`).

**Проблема:** задачи сейчас не связаны с проектом ни на уровне данных, ни через API. Единственная косвенная связь — через `sprintId`.

**Решение:** nullable UUID-поле + фильтрация через `GET /api/tasks/search?projectId=...` и `GET /api/tasks?projectId=...`.

## Context (from discovery)

- Файлы: TaskService
- Паттерн: аналогично `sprintId` — UUID без FK, хранится как обычная колонка
- `ddl-auto: update` — колонка добавится автоматически, миграция не нужна
- Поиск реализован через `TaskCriteriaRepositoryImpl` (JPA Criteria API)
- MapStruct маппер с `IGNORE` политикой — новое поле подхватится автоматически

## Development Approach

- **Тестирование:** нет
- Менять только то, что нужно — никаких рефакторингов попутно
- Полный список затронутых файлов ниже

## Progress Tracking

- Отмечать `[x]` сразу после выполнения
- Добавлять ➕ для вновь найденных задач
- Добавлять ⚠️ для блокеров

## Solution Overview

1. Добавить `projectId` в `Task` entity
2. Добавить `projectId` в DTOs: `CreateTaskRequest`, `UpdateTaskRequest`, `TaskResponse`
3. Добавить `projectId` в `TaskSearchRequest` + предикат в `TaskCriteriaRepositoryImpl`
4. Обновить `GET /api/tasks` — принимать опциональный `projectId` query param

## Implementation Steps

### Task 1: Добавить поле в сущность Task

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/entity/Task.kt`

- [ ] Добавить поле `projectId: UUID? = null` после `sprintId` в конструктор класса `Task`
- [ ] Добавить аннотацию `@Column(name = "project_id")`

### Task 2: Обновить DTOs

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/CreateTaskRequest.kt`
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/UpdateTaskRequest.kt`
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/TaskResponse.kt`

- [ ] Добавить `val projectId: UUID? = null` в `CreateTaskRequest` (после `sprintId`)
- [ ] Добавить `val projectId: UUID? = null` в `UpdateTaskRequest` (после `sprintId`)
- [ ] Добавить `val projectId: UUID?` в `TaskResponse` (после `sprintId`)

### Task 3: Добавить фильтрацию по projectId в поиск

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/TaskSearchRequest.kt`
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/repository/TaskCriteriaRepositoryImpl.kt`

- [ ] Добавить `val projectId: UUID? = null` в `TaskSearchRequest` (после `sprintId`)
- [ ] Добавить предикат в `buildPredicates()` в `TaskCriteriaRepositoryImpl`:
  ```kotlin
  filter.projectId?.let { predicates += cb.equal(root.get<Any>("projectId"), it) }
  ```
  — разместить рядом с аналогичным предикатом для `sprintId` (строка ~68)

### Task 4: Обновить GET /api/tasks для фильтрации по projectId

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/TaskController.kt`
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/TaskService.kt`

- [ ] Прочитать `TaskService.kt` и понять сигнатуру метода `getAll()`
- [ ] Добавить параметр `@RequestParam projectId: UUID? = null` в `getAll()` контроллера
- [ ] Передать `projectId` в сервис; в сервисе фильтровать: если `projectId != null` — добавить `.filter { it.projectId == projectId }` к результату (или использовать репозиторий)
- [ ] Убедиться что маппер (`TaskMapper`) корректно пробрасывает `projectId` в `TaskResponse` (MapStruct с `IGNORE` должен подхватить автоматически — проверить)

### Task 5: Проверить сборку

- [ ] Запустить `./gradlew :TaskService:build` и убедиться что нет ошибок компиляции
- [ ] Убедиться что MapStruct сгенерировал маппинг для нового поля (проверить в `build/generated`)

### Task N-1: Проверить карту сервиса

- [ ] Обновить `docs/plans/services/task-service.md` — добавить `projectId` в таблицу полей `Task` и убрать из "Точек расширения"

### Task N: Завершение

- [ ] Переместить этот план в `docs/plans/completed/`

## Post-Completion

**Ручная проверка:**
- Создать задачу с `projectId`, убедиться что поле сохраняется и возвращается в ответе
- Запросить `GET /api/tasks?projectId=<id>` — должны вернуться только задачи этого проекта
- Запросить `GET /api/tasks/search?projectId=<id>` — аналогично

**Смежные сервисы:**
- OrganizationService при удалении проекта (`DELETE /api/projects/{id}`) не очищает задачи — задачи остаются с `projectId` удалённого проекта (ожидаемое поведение для soft-delete архитектуры)
