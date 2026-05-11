# Task Filtering by User and Project

## Overview

Currently, `GET /api/tasks` returns **all tasks** in the system, regardless of user access. This change implements scoped task filtering so that each user sees only:

1. Tasks where they have a role (responsible, executor, observer, watcher)
2. Tasks in projects where they are a member

This improves security and reduces cognitive load—users see only tasks relevant to them.

## Context (from discovery)

**Files involved:**
- `TaskService/src/main/kotlin/com/slavacom/taskservice/entity/Task.kt`
- `TaskService/src/main/kotlin/com/slavacom/taskservice/repository/TaskRepository.kt`
- `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/TaskController.kt`
- `TaskService/src/main/resources/db/migration/` (new)

**Related patterns:**
- Spring Security (`SecurityContextHolder` for current user)
- JPA query methods with custom JPQL
- Kotlin entities with `@Entity`
- MapStruct DTOs

**Dependencies:**
- `org.springframework.security:spring-security-core` (existing)
- `javax.persistence` (existing)

## Development Approach

- **Testing approach:** Manual testing only (no automated tests)
- Complete each task fully before moving to the next
- Make small, focused changes
- Run application after each major change to verify behavior

## Solution Overview

**Architecture:**
1. Create `ProjectMember` entity to track which users are members of which projects
2. Sync ProjectMember data when users are added to organizations/projects (via Kafka or existing flows)
3. Add a filtering service that combines two queries:
   - Find tasks where user is responsible/executor/observer/watcher
   - Find tasks in projects where user is a member
4. Modify `TaskController.getTasks()` to use filtered results
5. Mark this as a **breaking change** in release notes

**Design decisions:**
- **ProjectMember table** provides fast access control queries without calling OrganizationService on each request
- **Union-based filtering** (assigned to me OR in my projects) is permissive—shows all relevant work
- **All roles included** (responsible, executor, observer, watcher) so users see tasks they're invested in
- **Existing endpoint modified** rather than creating new one—cleaner API, requires version bump

## What Goes Where

**Implementation Steps:** Code changes, database migrations, controller updates
**Post-Completion:** Manual testing scenarios, documentation of breaking change, possible release notes update

## Implementation Steps

### Task 1: Create ProjectMember entity and repository

**Files:**
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/entity/ProjectMember.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/repository/ProjectMemberRepository.kt`

- [ ] Create `ProjectMember` entity with fields: id (UUID), projectId (UUID), userId (UUID), createdAt (Instant)
- [ ] Add `@Table(name = "project_members")` and `@Entity` annotations
- [ ] Create `ProjectMemberRepository` extending `JpaRepository<ProjectMember, UUID>`
- [ ] Add query method: `fun findByProjectId(projectId: UUID): List<ProjectMember>`
- [ ] Add query method: `fun findByUserId(userId: UUID): List<ProjectMember>`
- [ ] Add compound query: `fun findByProjectIdAndUserId(projectId: UUID, userId: UUID): Optional<ProjectMember>`

### Task 2: Create database migration for ProjectMember table

**Files:**
- Create: `TaskService/src/main/resources/db/migration/V[N]__create_project_members_table.sql`

- [ ] Create migration script to add `project_members` table with columns: id, project_id, user_id, created_at
- [ ] Add unique constraint on (project_id, user_id) to prevent duplicates
- [ ] Add indexes on project_id and user_id for query performance
- [ ] Run migration locally with `./gradlew bootRun` to verify (watch logs for migration execution)

### Task 3: Create TaskFilteringService

**Files:**
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/TaskFilteringService.kt`

- [ ] Create service with method: `fun getAccessibleTasks(userId: UUID): List<Task>`
- [ ] Implement query logic:
  - Get all tasks where responsible/executor/observer/watcher contains userId
  - Get user's project IDs from ProjectMemberRepository
  - Get all tasks in those projects
  - Combine (union) and remove duplicates
- [ ] Consider performance: may need custom JPQL query if list operations are slow
- [ ] Use `@Transactional(readOnly = true)` for read efficiency

### Task 4: Modify TaskController.getTasks() to use filtering

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/TaskController.kt`

- [ ] Find the `getTasks()` endpoint (GET /api/tasks)
- [ ] Extract current user ID: `SecurityContextHolder.getContext().authentication.principal as String` (or similar, verify actual pattern in codebase)
- [ ] Replace return logic: instead of `taskRepository.findAll()`, call `taskFilteringService.getAccessibleTasks(userId)`
- [ ] Verify endpoint still returns correct DTO/response format
- [ ] Test locally by calling endpoint with different users (if test data available)

### Task 5: Populate ProjectMember table for existing projects

**Files:**
- Create: `TaskService/src/main/resources/db/migration/V[N+1]__populate_project_members_from_organization.sql` (optional)

- [ ] Decide: should ProjectMember be synced via Kafka when OrganizationService adds users? OR manually populated once?
- [ ] If manual: write SQL migration to insert rows based on existing project-user relationships (may require data analysis first)
- [ ] If Kafka: create Kafka listener in TaskService for organization membership events
  - Listen to topic (e.g., `user-added-to-org` or similar)
  - Insert ProjectMember rows on each membership event
  - Update/delete on removal events
- [ ] Document the sync mechanism in code comments

### Task 6: Manual testing of filtering

- [ ] Start infrastructure: `cd All-Compose && docker compose -f docker-compose.yaml -f docker-compose.db.yml -f docker-compose.kafka.yml up -d`
- [ ] Start TaskService: `cd TaskService && ./gradlew bootRun`
- [ ] Create test data: multiple users, multiple projects, assign users to projects and tasks
- [ ] Test as User A: GET /api/tasks — verify only shows tasks assigned to A or in A's projects
- [ ] Test as User B: GET /api/tasks — verify shows different tasks (not A's private work)
- [ ] Test edge case: user with no project membership — verify returns only directly assigned tasks
- [ ] Test edge case: user assigned to task but no project membership — verify task shows (union logic)
- [ ] Test edge case: user is observer only — verify task shows
- [ ] Document results and any edge cases encountered

### Task 7: (Optional) Handle pagination and search

- [ ] If existing `GET /api/tasks` supported pagination/search parameters, verify they still work with filtered results
- [ ] Consider: pagination should be applied **after** filtering for correct behavior
- [ ] Update any existing documentation or OpenAPI specs

### Task 8: Breaking change documentation

- [ ] Add note to release notes / CHANGELOG: "BREAKING: GET /api/tasks now returns only tasks assigned to you or in your projects. Previous behavior returned all tasks."
- [ ] Update CLAUDE.md if task filtering pattern becomes standardized
- [ ] Consider: add deprecation header or version requirement if versioning system exists

## Post-Completion

**Manual verification scenarios:**
- User A has 3 assigned tasks and is in 2 projects (5 project tasks) — endpoint shows all 8 tasks
- User B has no assignments but is in 1 shared project — endpoint shows 3 project tasks
- User C has no involvement — endpoint shows 0 tasks (empty list)
- Verify `GET /api/projects/{projectId}/tasks` still works (project-scoped endpoint unchanged)
- Verify `GET /api/tasks/my` still works (personal tasks endpoint unchanged)

**External updates:**
- Frontend may be calling `GET /api/tasks` expecting full list — verify frontend filters/displays correctly
- Any consuming services calling `GET /api/tasks` may be affected — communicate breaking change
- Update API documentation / OpenAPI spec if it describes the old behavior

**Sync mechanism choice:**
- Decide: on-demand sync (when task is first fetched) vs. background Kafka listener vs. periodic batch
- Document the chosen approach in code comments and README
