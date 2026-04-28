# Organization Service — Полная реализация на Kotlin

## Overview

Реализация полного REST API для OrganizationService поверх существующих Java JPA-сущностей.
Сервис управляет организациями, их участниками, приглашениями, тегами, проектами и всей
проектной инфраструктурой (спринты, Kanban, цели, статусы задач, история).

Новый код пишется на Kotlin и кладётся в `src/main/java/...` рядом с уже существующими
Kotlin-файлами. Маппинг — MapStruct (как в существующем `OrganizationMapper.kt`).

## Context (from discovery)

**Уже реализовано:**
- 11 Java JPA-сущностей (`Organization`, `Employees`, `Projects`, `OrganizationInvitation`,
  `KanbanColumns`, `KanbanTaskPositions`, `ProjectSprints`, `ProjectGoals`, `ProjectTags`,
  `OrganizationTags`, `ProjectEmployees`, `ProjectHistory`, `ProjectTaskStatuses`)
- `JwtTokenProvider.java` — полностью готов (`extractUserIdFromToken`, `extractRoleFromToken`, etc.)
- `OrganizationService.kt` — `getAll()` + `getById()`
- `OrganizationRepository.kt`, `OrganizationFilter.kt`, `OrganizationMapper.kt`
- `OrganizationController.kt` — стаб (1 эндпоинт, возвращает пустой список)
- `SecurityConfig.java` — JWT-фильтр **не подключён** (anyRequest().authenticated() есть, фильтра нет)
- Старые Java DTO в `dto/old/` — не используются, можно игнорировать

**Зависимости build.gradle:** Spring Security, JWT (jjwt-api 0.13.0), MapStruct 1.6.3, Kotlin JVM

**Пакет:** `com.slavacom.organizationservice`  
**Все новые файлы:** `src/main/java/com/slavacom/organizationservice/`

## Development Approach

- **Тестирование:** None (тесты добавить позже отдельно)
- Завершать каждую задачу полностью перед переходом к следующей
- Kotlin идиоматичный стиль: data classes, null-safety, named parameters
- MapStruct mapper для каждого модуля (abstract class, `componentModel = "spring"`)

## Solution Overview

Каждый модуль состоит из 4 файлов в своём подпакете:
```
<module>/
  <Entity>Dto.kt       — data class Request + Response
  <Entity>Mapper.kt    — MapStruct abstract class
  <Entity>Repository.kt
  <Entity>Service.kt
  <Entity>Controller.kt
```

**JWT:** JWT-фильтр `JwtAuthenticationFilter.kt` читает Bearer-токен, использует
`JwtTokenProvider.java` и кладёт `UsernamePasswordAuthenticationToken` в `SecurityContextHolder`.

## API Design

```
# Organization
GET    /api/organizations                           list (by accountable = current user)
POST   /api/organizations                           create
GET    /api/organizations/{id}                      get
PUT    /api/organizations/{id}                      update
DELETE /api/organizations/{id}                      soft-delete (isActive=false)

# Employees
GET    /api/organizations/{orgId}/employees         list
POST   /api/organizations/{orgId}/employees         add
PUT    /api/organizations/{orgId}/employees/{id}    update role/permissions
DELETE /api/organizations/{orgId}/employees/{id}    deactivate

# Invitations
POST   /api/organizations/{orgId}/invitations       invite user
GET    /api/organizations/{orgId}/invitations       list invitations
PUT    /api/invitations/{id}/accept                 accept
PUT    /api/invitations/{id}/decline                decline

# Organization Tags
GET    /api/organizations/{orgId}/tags              list
POST   /api/organizations/{orgId}/tags              create
PUT    /api/organizations/{orgId}/tags/{tagId}      update
DELETE /api/organizations/{orgId}/tags/{tagId}      delete

# Projects
GET    /api/organizations/{orgId}/projects          list projects in org
POST   /api/organizations/{orgId}/projects          create project
GET    /api/projects/{id}                           get project
PUT    /api/projects/{id}                           update
DELETE /api/projects/{id}                           soft-delete

# Project Employees
GET    /api/projects/{projectId}/employees          list
POST   /api/projects/{projectId}/employees          add
PUT    /api/projects/{projectId}/employees/{id}     update role
DELETE /api/projects/{projectId}/employees/{id}     remove

# Project Tags
GET    /api/projects/{projectId}/tags               list
POST   /api/projects/{projectId}/tags               create
PUT    /api/projects/{projectId}/tags/{tagId}       update
DELETE /api/projects/{projectId}/tags/{tagId}       delete

# Project Goals
GET    /api/projects/{projectId}/goals              list
POST   /api/projects/{projectId}/goals              create
PUT    /api/projects/{projectId}/goals/{goalId}     update
DELETE /api/projects/{projectId}/goals/{goalId}     delete

# Project Sprints
GET    /api/projects/{projectId}/sprints            list
POST   /api/projects/{projectId}/sprints            create
PUT    /api/projects/{projectId}/sprints/{id}       update
PUT    /api/projects/{projectId}/sprints/{id}/activate   activate (deactivates others)
PUT    /api/projects/{projectId}/sprints/{id}/complete   complete sprint

# Kanban Columns
GET    /api/projects/{projectId}/kanban/columns              list
POST   /api/projects/{projectId}/kanban/columns              create
PUT    /api/projects/{projectId}/kanban/columns/{id}         update
PUT    /api/projects/{projectId}/kanban/columns/reorder      bulk reorder (orderIndex)
DELETE /api/projects/{projectId}/kanban/columns/{id}         deactivate

# Kanban Task Positions
GET    /api/projects/{projectId}/kanban/positions            get all positions
PUT    /api/projects/{projectId}/kanban/positions            bulk update

# Project Task Statuses
GET    /api/projects/{projectId}/statuses           list
POST   /api/projects/{projectId}/statuses           create
PUT    /api/projects/{projectId}/statuses/{id}      update
DELETE /api/projects/{projectId}/statuses/{id}      delete

# Project History (read-only)
GET    /api/projects/{projectId}/history            list
```

## Implementation Steps

---

### Task 1: JWT Authentication Filter

**Files:**
- Create: `security/JwtAuthenticationFilter.kt`
- Modify: `config/SecurityConfig.java`

- [ ] создать `JwtAuthenticationFilter.kt` — `OncePerRequestFilter`, читает `Authorization: Bearer <token>`,
      вызывает `JwtTokenProvider.extractClaims()`, кладёт `UsernamePasswordAuthenticationToken` в `SecurityContextHolder`
- [ ] в `SecurityConfig.java` добавить `.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)`
- [ ] проверить: запрос без токена → 403, с токеном → 200

---

### Task 2: Organization CRUD (завершить стаб)

**Files:**
- Modify: `service/OrganizationService.kt`
- Modify: `controller/OrganizationController.kt`
- Modify: `controller/OrganizationResponse.kt`
- Create: `controller/CreateOrganizationRequest.kt`
- Create: `controller/UpdateOrganizationRequest.kt`
- Modify: `mapper/OrganizationMapper.kt`

- [ ] добавить в `OrganizationResponse.kt` все поля (`id`, `name`, `description`, `accountable`, `isActive`, `createdAt`, `updatedAt`)
- [ ] создать `CreateOrganizationRequest.kt` — data class с `name`, `description`
- [ ] создать `UpdateOrganizationRequest.kt` — data class с `name?`, `description?`
- [ ] дополнить `OrganizationMapper.kt` — методы `toResponse(Organization): OrganizationResponse`,
      `toEntity(CreateOrganizationRequest): Organization`
- [ ] в `OrganizationService.kt` добавить:
      - `create(request, accountable: UUID): OrganizationResponse`
      - `update(id, request): OrganizationResponse`
      - `deactivate(id)` — `isActive = false`
      - `getAll()` вернуть `List<OrganizationResponse>` (не Entity)
- [ ] в `OrganizationController.kt` добавить эндпоинты POST, GET /{id}, PUT /{id}, DELETE /{id};
      извлекать userId из JWT через `JwtTokenProvider`

---

### Task 3: Employees Module

**Files:**
- Create: `employees/EmployeesRepository.kt`
- Create: `employees/EmployeesDto.kt`
- Create: `employees/EmployeesMapper.kt`
- Create: `employees/EmployeesService.kt`
- Create: `employees/EmployeesController.kt`

- [ ] создать `EmployeesRepository.kt` — `JpaRepository<Employees, UUID>` +
      `findAllByOrganizationIdAndIsActiveTrue()`, `findByUserIdAndOrganizationId()`
- [ ] создать `EmployeesDto.kt` — `EmployeeResponse`, `AddEmployeeRequest` (`userId`, `profileId?`, `role`, `permissions?`),
      `UpdateEmployeeRequest` (`role?`, `permissions?`)
- [ ] создать `EmployeesMapper.kt` — MapStruct: `toResponse(Employees): EmployeeResponse`,
      `toEntity(AddEmployeeRequest): Employees`
- [ ] создать `EmployeesService.kt` — `list(orgId)`, `add(orgId, request): EmployeeResponse`,
      `update(orgId, employeeId, request): EmployeeResponse`, `remove(orgId, employeeId)`
- [ ] создать `EmployeesController.kt` — `@RequestMapping("/api/organizations/{orgId}/employees")`,
      CRUD эндпоинты

---

### Task 4: Organization Invitations Module

**Files:**
- Create: `invitation/InvitationRepository.kt`
- Create: `invitation/InvitationDto.kt`
- Create: `invitation/InvitationMapper.kt`
- Create: `invitation/InvitationService.kt`
- Create: `invitation/InvitationController.kt`

- [ ] создать `InvitationRepository.kt` — `findAllByOrganizationId()`,
      `findByIdAndStatus()`, `findByInvitedUserId()`
- [ ] создать `InvitationDto.kt` — `InvitationResponse`, `CreateInvitationRequest` (`invitedUserId`, `role`, `message?`, `expiresAt`, `permissions?`)
- [ ] создать `InvitationMapper.kt` — MapStruct
- [ ] создать `InvitationService.kt`:
      - `invite(orgId, request, invitedBy): InvitationResponse`
      - `list(orgId): List<InvitationResponse>`
      - `accept(invitationId)` — проверить expiresAt, статус PENDING, создать Employees-запись
      - `decline(invitationId)`
- [ ] создать `InvitationController.kt` — эндпоинты для создания/просмотра/принятия/отклонения

---

### Task 5: Organization Tags Module

**Files:**
- Create: `orgtags/OrganizationTagsRepository.kt`
- Create: `orgtags/OrganizationTagsDto.kt`
- Create: `orgtags/OrganizationTagsMapper.kt`
- Create: `orgtags/OrganizationTagsService.kt`
- Create: `orgtags/OrganizationTagsController.kt`

- [ ] создать `OrganizationTagsRepository.kt` — `findAllByOrganizationId()`
- [ ] создать `OrganizationTagsDto.kt` — `OrgTagResponse`, `CreateOrgTagRequest` (`name`, `color?`, `description?`),
      `UpdateOrgTagRequest`
- [ ] создать `OrganizationTagsMapper.kt` — MapStruct
- [ ] создать `OrganizationTagsService.kt` — `list`, `create`, `update`, `delete`
- [ ] создать `OrganizationTagsController.kt` — `@RequestMapping("/api/organizations/{orgId}/tags")`

---

### Task 6: Projects Module

**Files:**
- Create: `project/ProjectsRepository.kt`
- Create: `project/ProjectsDto.kt`
- Create: `project/ProjectsMapper.kt`
- Create: `project/ProjectsService.kt`
- Create: `project/ProjectsController.kt`

- [ ] создать `ProjectsRepository.kt` — `findAllByOrganizationIdAndIsActiveTrue()`,
      `findAllByOrganizationId()`, `JpaSpecificationExecutor`
- [ ] создать `ProjectsDto.kt` — `ProjectResponse`, `CreateProjectRequest` (`name`, `description?`, `responsible`),
      `UpdateProjectRequest` (`name?`, `description?`, `responsible?`, `isActive?`)
- [ ] создать `ProjectsMapper.kt` — MapStruct
- [ ] создать `ProjectsService.kt` — `listByOrg(orgId)`, `create(orgId, request): ProjectResponse`,
      `getById(id): ProjectResponse`, `update(id, request): ProjectResponse`, `deactivate(id)`
- [ ] создать `ProjectsController.kt` — вложенные маршруты `/api/organizations/{orgId}/projects` +
      `/api/projects/{id}`

---

### Task 7: Project Employees Module

**Files:**
- Create: `project/employees/ProjectEmployeesRepository.kt`
- Create: `project/employees/ProjectEmployeesDto.kt`
- Create: `project/employees/ProjectEmployeesMapper.kt`
- Create: `project/employees/ProjectEmployeesService.kt`
- Create: `project/employees/ProjectEmployeesController.kt`

- [ ] создать `ProjectEmployeesRepository.kt` — `findAllByProjectIdAndIsActiveTrue()`,
      `findByProjectIdAndProfileId()`
- [ ] создать `ProjectEmployeesDto.kt` — `ProjectEmployeeResponse`, `AddProjectEmployeeRequest`,
      `UpdateProjectEmployeeRequest`
- [ ] создать `ProjectEmployeesMapper.kt`
- [ ] создать `ProjectEmployeesService.kt` — CRUD
- [ ] создать `ProjectEmployeesController.kt` — `@RequestMapping("/api/projects/{projectId}/employees")`

---

### Task 8: Project Tags Module

**Files:**
- Create: `project/tags/ProjectTagsRepository.kt`
- Create: `project/tags/ProjectTagsDto.kt`
- Create: `project/tags/ProjectTagsMapper.kt`
- Create: `project/tags/ProjectTagsService.kt`
- Create: `project/tags/ProjectTagsController.kt`

- [ ] создать `ProjectTagsRepository.kt` — `findAllByProjectId()`
- [ ] создать `ProjectTagsDto.kt` — `ProjectTagResponse`, `CreateProjectTagRequest`, `UpdateProjectTagRequest`
- [ ] создать `ProjectTagsMapper.kt`
- [ ] создать `ProjectTagsService.kt` — `list`, `create`, `update`, `delete`
- [ ] создать `ProjectTagsController.kt` — `@RequestMapping("/api/projects/{projectId}/tags")`

---

### Task 9: Project Goals Module

**Files:**
- Create: `project/goals/ProjectGoalsRepository.kt`
- Create: `project/goals/ProjectGoalsDto.kt`
- Create: `project/goals/ProjectGoalsMapper.kt`
- Create: `project/goals/ProjectGoalsService.kt`
- Create: `project/goals/ProjectGoalsController.kt`

- [ ] создать `ProjectGoalsRepository.kt` — `findAllByProjectId()`
- [ ] создать `ProjectGoalsDto.kt` — `ProjectGoalResponse`, `CreateProjectGoalRequest`,
      `UpdateProjectGoalRequest` (включая `progress`, `isCompleted`)
- [ ] создать `ProjectGoalsMapper.kt`
- [ ] создать `ProjectGoalsService.kt` — CRUD + обновление прогресса
- [ ] создать `ProjectGoalsController.kt` — `@RequestMapping("/api/projects/{projectId}/goals")`

---

### Task 10: Project Sprints Module

**Files:**
- Create: `project/sprints/ProjectSprintsRepository.kt`
- Create: `project/sprints/ProjectSprintsDto.kt`
- Create: `project/sprints/ProjectSprintsMapper.kt`
- Create: `project/sprints/ProjectSprintsService.kt`
- Create: `project/sprints/ProjectSprintsController.kt`

- [ ] создать `ProjectSprintsRepository.kt` — `findAllByProjectId()`, `findByProjectIdAndIsActiveTrue()`
- [ ] создать `ProjectSprintsDto.kt` — `SprintResponse`, `CreateSprintRequest`, `UpdateSprintRequest`
- [ ] создать `ProjectSprintsMapper.kt`
- [ ] создать `ProjectSprintsService.kt`:
      - CRUD
      - `activate(projectId, sprintId)` — деактивирует все остальные спринты проекта, активирует нужный,
        меняет status → ACTIVE
      - `complete(projectId, sprintId)` — status → COMPLETED, isActive = false
- [ ] создать `ProjectSprintsController.kt` — CRUD + `/activate` + `/complete`

---

### Task 11: Kanban Columns Module

**Files:**
- Create: `project/kanban/KanbanColumnsRepository.kt`
- Create: `project/kanban/KanbanColumnsDto.kt`
- Create: `project/kanban/KanbanColumnsMapper.kt`
- Create: `project/kanban/KanbanColumnsService.kt`
- Create: `project/kanban/KanbanColumnsController.kt`

- [ ] создать `KanbanColumnsRepository.kt` — `findAllByProjectIdAndIsActiveTrueOrderByOrderIndexAsc()`
- [ ] создать `KanbanColumnsDto.kt` — `KanbanColumnResponse`, `CreateKanbanColumnRequest`,
      `UpdateKanbanColumnRequest`, `ReorderColumnsRequest` (список `{id, orderIndex}`)
- [ ] создать `KanbanColumnsMapper.kt`
- [ ] создать `KanbanColumnsService.kt` — CRUD + `reorder(projectId, list)` (bulk update orderIndex)
- [ ] создать `KanbanColumnsController.kt` — `@RequestMapping("/api/projects/{projectId}/kanban/columns")`
      + PUT `/reorder`

---

### Task 12: Kanban Task Positions Module

**Files:**
- Create: `project/kanban/KanbanTaskPositionsRepository.kt`
- Create: `project/kanban/KanbanTaskPositionsDto.kt`
- Create: `project/kanban/KanbanTaskPositionsMapper.kt`
- Create: `project/kanban/KanbanTaskPositionsService.kt`
- Modify: `project/kanban/KanbanColumnsController.kt`

- [ ] создать `KanbanTaskPositionsRepository.kt` — `findAllByProjectId()`, `findByTaskId()`
- [ ] создать `KanbanTaskPositionsDto.kt` — `TaskPositionResponse`, `UpdateTaskPositionRequest`,
      `BulkUpdatePositionsRequest`
- [ ] создать `KanbanTaskPositionsMapper.kt`
- [ ] создать `KanbanTaskPositionsService.kt` — `getPositions(projectId)`, `bulkUpdate(projectId, list)`
- [ ] добавить в `KanbanColumnsController.kt` эндпоинты GET/PUT `/api/projects/{id}/kanban/positions`

---

### Task 13: Project Task Statuses Module

**Files:**
- Create: `project/statuses/ProjectTaskStatusesRepository.kt`
- Create: `project/statuses/ProjectTaskStatusesDto.kt`
- Create: `project/statuses/ProjectTaskStatusesMapper.kt`
- Create: `project/statuses/ProjectTaskStatusesService.kt`
- Create: `project/statuses/ProjectTaskStatusesController.kt`

- [ ] создать `ProjectTaskStatusesRepository.kt` — `findAllByProjectId()`
- [ ] создать `ProjectTaskStatusesDto.kt` — `TaskStatusResponse`, `CreateTaskStatusRequest`,
      `UpdateTaskStatusRequest`
- [ ] создать `ProjectTaskStatusesMapper.kt`
- [ ] создать `ProjectTaskStatusesService.kt` — CRUD
- [ ] создать `ProjectTaskStatusesController.kt` — `@RequestMapping("/api/projects/{projectId}/statuses")`

---

### Task 14: Project History Module (read-only)

**Files:**
- Create: `project/history/ProjectHistoryRepository.kt`
- Create: `project/history/ProjectHistoryDto.kt`
- Create: `project/history/ProjectHistoryMapper.kt`
- Create: `project/history/ProjectHistoryService.kt`
- Create: `project/history/ProjectHistoryController.kt`

- [ ] создать `ProjectHistoryRepository.kt` — `findAllByProjectIdOrderByCreatedAtDesc()` + pagination
- [ ] создать `ProjectHistoryDto.kt` — `ProjectHistoryResponse`
- [ ] создать `ProjectHistoryMapper.kt`
- [ ] создать `ProjectHistoryService.kt` — `list(projectId): List<ProjectHistoryResponse>` +
      internal `record(projectId, action, details)` для вызова из других сервисов
- [ ] создать `ProjectHistoryController.kt` — GET `/api/projects/{projectId}/history`

---

### Task 15: Verify — сверка и интеграция

- [ ] убедиться что все 36+ эндпоинтов возвращают корректный JSON
- [ ] убедиться что JWT-фильтр корректно читает userId, profileId, role из токена
- [ ] убедиться что `InvitationService.accept()` корректно создаёт запись `Employees`
- [ ] убедиться что `ProjectSprintsService.activate()` деактивирует предыдущий активный спринт
- [ ] убедиться что `KanbanColumnsService.reorder()` корректно обновляет orderIndex bulk-ом
- [ ] запустить сервис через `./gradlew bootRun` и проверить старт без ошибок

---

### Task 16: [Final] Документация

- [ ] обновить CLAUDE.md — исправить топик Kafka `user-created` → `user-login`
- [ ] переместить этот план в `docs/plans/completed/`

## Post-Completion

**Ручная проверка через Connekt:**
- Зарегистрироваться через Auth-service → получить JWT
- Создать организацию, пригласить пользователя, принять приглашение
- Создать проект, добавить сотрудников, создать спринт, активировать его
- Проверить Kanban: создать колонки, переупорядочить

**Интеграция с TaskService:**
- TaskService использует `projectId` и `userId` — нужно убедиться что статусы задач (`ProjectTaskStatuses`)
  синхронизированы с TaskService при необходимости
