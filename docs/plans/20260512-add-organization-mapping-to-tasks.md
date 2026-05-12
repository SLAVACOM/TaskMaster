# Add Organization Mapping to Tasks

## Overview

Currently, tasks only have `projectId`, making it difficult to filter by organization directly. When filtering `GET /api/tasks/search`, we have to traverse project relationships or rely on external organization context.

The fix adds an explicit `organizationId` field to the Task entity so that:
1. Organization membership is directly queryable on tasks
2. Task filtering by organization is efficient (direct field check)
3. Task creation automatically captures the organization context
4. Database queries can filter by `organizationId` directly

## Context

**Files involved:**
- `TaskService/src/main/kotlin/com/slavacom/taskservice/entity/Task.kt` — entity
- `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/CreateTaskRequest.kt` — creation DTO
- `TaskService/src/main/kotlin/com/slavacom/taskservice/service/TaskService.kt` — business logic
- `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/TaskController.kt` — endpoints
- `TaskService/src/main/resources/db/changelog/initial.xml.yaml` — database migration

**Current state:**
- Task entity has projectId but no organizationId
- Organization filtering in search is done via header, not via task field
- Task creation doesn't capture organization context

## Development Approach

- **testing approach:** Manual testing only (per user preference)
- **approach:** Add organizationId field to Task entity and populate on creation
- **database:** Add Liquibase migration for new column
- **maintain backward compatibility:** Make organizationId nullable initially, then populate via migration

## Solution Overview

**Architecture:**
1. Add `organizationId: UUID?` field to Task entity
2. Update Liquibase migration to add column to tasks table
3. Modify task creation to accept and store organizationId
4. Update search filtering to use direct organizationId check
5. Backfill existing tasks with organizationId from their projects

## Implementation Steps

### Task 1: Add organizationId field to Task entity

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/entity/Task.kt`

- [ ] Add `organizationId: UUID?` field to Task class after projectId
- [ ] Add `@Column(name = "organization_id")` annotation
- [ ] Default value: null (for backward compatibility)

### Task 2: Create Liquibase migration for organizationId column

**Files:**
- Modify: `TaskService/src/main/resources/db/changelog/initial.xml.yaml`

- [ ] Add new changeSet to create `organization_id` column in tasks table
- [ ] Column type: UUID, nullable
- [ ] Add index on organization_id for query performance

### Task 3: Update CreateTaskRequest to accept organizationId

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/CreateTaskRequest.kt`

- [ ] Add `organizationId: UUID?` field to request DTO
- [ ] Document that either organizationId or projectId should be provided

### Task 4: Modify task creation logic

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/TaskService.kt`
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/TaskController.kt`

- [ ] Update `TaskController.create()` to accept X-Organization-Id header (optional if projectId provided)
- [ ] Update `TaskService.create()` to:
  - Accept organizationId parameter
  - If projectId provided but organizationId not: derive from project (if needed)
  - Store organizationId on task
- [ ] Update task response DTO to include organizationId if needed

### Task 5: Update search filtering to use organizationId directly

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/TaskService.kt`

- [ ] In `searchFiltered()`: check task.organizationId directly instead of relying on header
- [ ] Add organizationId filter to search criteria

### Task 6: Create migration to backfill existing tasks

**Files:**
- Modify: `TaskService/src/main/resources/db/changelog/initial.xml.yaml`

- [ ] Add changeSet to backfill organizationId from project relationships (if project→organization mapping exists)
- [ ] OR: Document manual process to populate organizationId via SQL script

### Task 7: Manual testing

- [ ] Create task with organizationId header → verify organizationId stored
- [ ] Create task with projectId → verify organizationId populated (if derived)
- [ ] Search tasks → verify filtered by task.organizationId, not header
- [ ] Query `/api/tasks/search` as user in Org A → only sees Org A tasks
- [ ] Query `/api/tasks/search` as user in Org B → only sees Org B tasks

## Post-Completion

**Verification:**
- All new tasks have organizationId set
- Existing tasks backfilled with organizationId (if applicable)
- Search filtering uses task.organizationId directly
- Backward compatibility maintained (nullable field)

**Migration notes:**
- If tasks don't have project relationships, organizationId may need to be set manually
- Backfill migration depends on how projects map to organizations
