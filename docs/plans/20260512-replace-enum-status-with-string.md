# Замена TaskStatus enum на String для гибкости статусов

## Overview

Текущая проблема: жесткий enum `TaskStatus` с фиксированными значениями (DONE, BLOCKED, TODO, IN_PROGRESS, IN_REVIEW) конфликтует с гибкостью канбан доски, которая может иметь произвольные названия столбцов (например "PROGRESS" вместо "IN_PROGRESS").

**Решение:** Заменить enum `TaskStatus` на `String`, чтобы позволить динамические статусы, синхронизируемые с названиями столбцов канбан доски.

**Эффект:**
- Произвольные названия статусов из канбан доски
- Нет ошибок десериализации JSON
- Полная гибкость в управлении статусами

## Context

**Затронутые сервисы:**
- TaskService — основной сервис с Task entity
- OrganizationService — через канбан доску
- Возможны потребители через REST API

**Текущая структура TaskStatus:**
```kotlin
enum class TaskStatus {
    TODO, IN_PROGRESS, IN_REVIEW, DONE, BLOCKED
}
```

**Проблема:**
- Попытка использовать "PROGRESS" → ошибка десериализации
- Нет синхронизации с именами столбцов канбан

## Development Approach

- **Testing approach:** Ручное тестирование (без автоматизированных тестов)
- **Валидация:** Без списка разрешенных значений
- Полная замена enum на String везде, где он используется
- Обновить сериализацию/десериализацию JSON
- Обновить миграции базы данных (если требуется изменение типа колонки)

## Solution Overview

**Архитектура:**
1. Удалить enum класс `TaskStatus`
2. Заменить `var status: TaskStatus` на `var status: String`
3. Обновить все references в коде (mappers, DTOs, controllers)
4. Обновить JSON serialization/deserialization (если используется Jackson с аннотациями)
5. Обновить database migration для смены типа колонки с ENUM на VARCHAR

**Миграционная стратегия:**
- Текущие enum значения в БД остаются совместимы с String (просто как текст)
- Новые значения могут быть любыми String

## Implementation Steps

### Task 1: Обновить Task entity — замена TaskStatus на String

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/entity/Task.kt`

- [ ] Заменить `var status: TaskStatus` на `var status: String` в Task entity
- [ ] Удалить импорт `TaskStatus` enum
- [ ] Обновить default значение статуса на `"TODO"` (String вместо `TaskStatus.TODO`)
- [ ] Проверить, что сущность компилируется

### Task 2: Обновить TaskMapper для работы со String статусами

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/mapper/TaskMapper.kt`

- [ ] Обновить маппинг `task.status` чтобы работал со String (не требует преобразования из enum)
- [ ] Проверить методы `toResponse()`, `toEntity()`, `fromCreateRequest()` — убедиться работают со String

### Task 3: Обновить DTO классы для Task

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/TaskResponse.kt` (если существует)
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/CreateTaskRequest.kt` (если существует)
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/UpdateTaskRequest.kt` (если существует)

- [ ] Найти все DTO классы содержащие `status: TaskStatus`
- [ ] Заменить на `status: String`
- [ ] Обновить документацию/примеры в DTO если есть

### Task 4: Обновить контроллеры TaskService

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/TaskController.kt`
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/ProjectTaskController.kt` (если есть)

- [ ] Проверить эндпоинты на предмет явного парсинга/валидации TaskStatus enum
- [ ] Убрать любые попытки валидации enum значений
- [ ] Заменить на простую работу со String

### Task 5: Обновить сервис Task

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/TaskService.kt`

- [ ] Обновить логику обработки статусов на работу со String
- [ ] Если есть методы вроде `transitionStatus()` — обновить их сигнатуры
- [ ] Убрать любые switch/when по enum значениям (заменить на String сравнение если нужно)

### Task 6: Обновить миграцию БД (если использует миграции)

**Files:**
- Create/Modify: `TaskService/src/main/resources/db/migration/V*__change_task_status_to_string.sql` (создать новую миграцию)

- [ ] Проверить текущий тип колонки `status` в таблице `tasks`
- [ ] Если текущий тип ENUM — создать миграцию для изменения на VARCHAR/TEXT
- [ ] Если уже VARCHAR — никаких изменений БД не требуется
- [ ] Миграция должна обратима или содержать комментарий о необратимости

### Task 7: Проверить все references в других сервисах

**Files:**
- Search: Все импорты `TaskStatus` в OrganizationService, другие сервисы

- [ ] Найти все места где используется `TaskStatus` через grep/intellij
- [ ] Обновить любые imports в других сервисах если есть
- [ ] Обновить контракты REST API в комментариях если есть документация

### Task 8: Синхронизация статуса с канбан доской (опционально)

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/TaskService.kt`
- Modify: `OrganizationService/src/main/java/com/slavacom/organizationservice/project/kanban/KanbanColumnsService.kt`

- [ ] Добавить логику: при изменении позиции задачи на канбан доске обновлять `task.status` на имя столбца
- [ ] Или добавить endpoint для явного обновления статуса = название столбца
- [ ] *(Опционально)* пока не реализовать если не требуется для устранения ошибки

### Task 9: Ручное тестирование

- [ ] Запустить TaskService: `./gradlew bootRun`
- [ ] Проверить создание задачи с любым String статусом (через POST /api/tasks)
- [ ] Проверить обновление статуса на произвольное значение
- [ ] Проверить получение задач со статусом (GET /api/tasks)
- [ ] Проверить перемещение задачи в канбан столбец и обновление статуса
- [ ] Проверить сериализацию JSON (статус должен быть корректным String в response)
- [ ] Проверить that старые enum значения все еще работают (например "IN_PROGRESS")

### Task 10: Обновить документацию

- [ ] Обновить CLAUDE.md если там есть информация о TaskStatus enum
- [ ] Обновить любые README файлы в TaskService
- [ ] Добавить комментарий в код Task entity объясняющий что status теперь String (без ограничений)

## Post-Completion

*Ручное тестирование в локальной среде требуется для проверки всех сценариев. Автоматизированные тесты не входят в объем.*

**Возможные extend-scope работы:**
- Добавление валидации списка разрешенных статусов (если потребуется позже)
- Автоматизация синхронизации между столбцами канбан и статусами задач
- Документирование всех возможных значений статусов (если зафиксируются)
