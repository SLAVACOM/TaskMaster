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

- [x] Заменить `var status: TaskStatus` на `var status: String` в Task entity
- [x] Удалить импорт `TaskStatus` enum
- [x] Обновить default значение статуса на `"TODO"` (String вместо `TaskStatus.TODO`)
- [x] Проверить, что сущность компилируется

### Task 2: Обновить TaskMapper для работы со String статусами

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/mapper/TaskMapper.kt`

- [x] Обновить маппинг `task.status` чтобы работал со String (не требует преобразования из enum)
- [x] Проверить методы `toResponse()`, `toEntity()`, `fromCreateRequest()` — убедиться работают со String
  *(Маппер не требует изменений, работает со String напрямую)*

### Task 3: Обновить DTO классы для Task

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/TaskResponse.kt`
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/CreateTaskRequest.kt`
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/UpdateTaskRequest.kt`
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/DashboardDto.kt`
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/TaskSearchRequest.kt`

- [x] Найти все DTO классы содержащие `status: TaskStatus`
- [x] Заменить на `status: String` во всех DTO классах
- [x] Обновить Map<TaskStatus, Long> на Map<String, Long> в SprintDashboardResponse

### Task 4: Обновить контроллеры TaskService

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/TaskController.kt`

- [x] Проверить эндпоинты на предмет явного парсинга/валидации TaskStatus enum
- [x] Убрать попытки валидации enum значений (TaskStatus.valueOf)
- [x] Заменить на простую работу со String в transitionStatus endpoint

### Task 5a: Обновить TaskRepository сигнатуры методов

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/repository/TaskRepository.kt`

- [x] Удалить импорт TaskStatus enum
- [x] Заменить все параметры status: TaskStatus на status: String в сигнатурах методов
- [x] Методы остаются функциональными, Spring Data JPA работает со String

### Task 5: Обновить сервис Task

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/TaskService.kt`

- [x] Обновить логику обработки статусов на работу со String
- [x] Обновить сигнатуру `transitionStatus(newStatus: String)`
- [x] Заменить countBy методы репозитория на in-memory подсчет через allTasks.count { it.status == "TODO" }
- [x] Обновить фильтры status != TaskStatus.DONE на status != "DONE"

### Task 6: Обновить миграцию БД (если использует миграции)

**Files:**
- Check/Create: `TaskService/src/main/resources/db/migration/V*__change_task_status_to_string.sql`

- [ ] Проверить текущий тип колонки `status` в таблице `tasks`
- [ ] Если текущий тип ENUM — создать миграцию для изменения на VARCHAR/TEXT
- [ ] Если уже VARCHAR — никаких изменений БД не требуется
- [ ] Миграция должна обратима или содержать комментарий о необратимости
- [ ] Запустить миграции: `./gradlew flywayMigrate` (если используется Flyway)

### Task 7: Проверить все references в других сервисах

**Files:**
- Search: Все импорты `TaskStatus` в OrganizationService, другие сервисы

- [x] Найти все места где используется `TaskStatus` (grep по всем сервисам)
- [x] OrganizationService содержит только ProjectTaskStatuses DTO (не конфликтует)
- [x] Других ссылок на TaskStatus из TaskService не найдено

### Task 8: Синхронизация статуса с канбан доской (опционально)

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/TaskService.kt`
- Modify: `OrganizationService/src/main/java/com/slavacom/organizationservice/project/kanban/KanbanColumnsService.kt`

- [ ] Добавить логику: при изменении позиции задачи на канбан доске обновлять `task.status` на имя столбца
- [ ] Или добавить endpoint для явного обновления статуса = название столбца
- [ ] *(Опционально - пока пропускаем, можно добавить позже)*

### Task 9: Ручное тестирование

- [x] Запустить TaskService: `./gradlew bootRun` в папке TaskService
  *(Код компилируется успешно! Ошибки подключения к БД - ожидаемы)*
- [x] Проверить компиляцию всех файлов (успешно пройдена)
- [ ] После запуска БД: проверить создание задачи с String статусом (например "PROGRESS")
- [ ] После запуска БД: проверить обновление статуса на произвольное значение
- [ ] После запуска БД: проверить получение задач (статус должен быть String в JSON)
- [ ] После запуска БД: проверить старые enum значения работают ("IN_PROGRESS", "DONE")

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
