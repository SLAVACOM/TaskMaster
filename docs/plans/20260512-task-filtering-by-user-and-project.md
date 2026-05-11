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

- [x] Create `ProjectMember` entity with fields: id (UUID), projectId (UUID), userId (UUID), createdAt (Instant)
- [x] Add `@Table(name = "project_members")` and `@Entity` annotations
- [x] Create `ProjectMemberRepository` extending `JpaRepository<ProjectMember, UUID>`
- [x] Add query method: `fun findByProjectId(projectId: UUID): List<ProjectMember>`
- [x] Add query method: `fun findByUserId(userId: UUID): List<ProjectMember>`
- [x] Add compound query: `fun findByProjectIdAndUserId(projectId: UUID, userId: UUID): Optional<ProjectMember>`

### Task 2: Create database migration for ProjectMember table

**Files:**
- Modify: `TaskService/src/main/resources/db/changelog/initial.xml.yaml`

- [x] Create migration script to add `project_members` table with columns: id, project_id, user_id, created_at
- [x] Add unique constraint on (project_id, user_id) to prevent duplicates
- [x] Add indexes on project_id and user_id for query performance
- [x] Run migration locally with `./gradlew bootRun` to verify (watch logs for migration execution)

### Task 3: Create TaskFilteringService

**Files:**
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/TaskFilteringService.kt`

- [x] Create service with method: `fun getAccessibleTasks(userId: UUID): List<Task>`
- [x] Implement query logic:
  - Get all tasks where responsible/executor/observer/watcher contains userId
  - Get user's project IDs from ProjectMemberRepository
  - Get all tasks in those projects
  - Combine (union) and remove duplicates
- [x] Consider performance: may need custom JPQL query if list operations are slow
- [x] Use `@Transactional(readOnly = true)` for read efficiency

### Task 4: Modify TaskController.getTasks() to use filtering

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/TaskController.kt`

- [x] Find the `getTasks()` endpoint (GET /api/tasks)
- [x] Extract current user ID from X-User-Id header (existing pattern in codebase)
- [x] Replace return logic: instead of `taskRepository.findAll()`, call `taskFilteringService.getAccessibleTasks(userId)`
- [x] Verify endpoint still returns correct DTO/response format
- [x] Keep optional projectId parameter for additional filtering

### Task 5: Populate ProjectMember table for existing projects

**Files:**
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/listener/ProjectMembershipKafkaListener.kt` (optional, for Kafka approach)

- [ ] ⚠️ Decide sync mechanism:
  - **Option A (Kafka):** Create listener for organization membership events from OrganizationService
  - **Option B (Manual):** Create init script to populate from existing data (requires understanding current user-project relationships)
  - **Option C (On-demand):** Sync when user first accesses tasks (queries OrganizationService for membership)
- [ ] If choosing Kafka: Create `ProjectMembershipKafkaListener` to handle membership change events
- [ ] If choosing Manual: Create migration/seed script and document how to populate initial data
- [ ] Document the chosen sync mechanism in README and code comments
- [ ] Note: For MVP, ProjectMember can be manually populated via SQL or seeded with test data

### Task 6: Manual testing of filtering

**Testing Prerequisites:**
1. Ensure ProjectMember table is populated with test data (see Task 5)
2. Create test tasks assigned to different users and in different projects

**Manual test scenarios to verify:**
- [ ] Start infrastructure: `cd All-Compose && docker compose -f docker-compose.yaml -f docker-compose.db.yml -f docker-compose.kafka.yml up -d`
- [ ] Start TaskService: `cd TaskService && ./gradlew bootRun`
- [ ] Test Scenario 1: User with direct assignment
  - Create task assigned to User A (responsible or executor)
  - Call `GET /api/tasks` as User A
  - Verify task appears in response
- [ ] Test Scenario 2: User with project membership
  - Create task in Project X, assign to someone else
  - Add User B to project_members for Project X
  - Call `GET /api/tasks` as User B
  - Verify task appears even though not assigned to B
- [ ] Test Scenario 3: User as observer/watcher
  - Create task with User C in observers list
  - Call `GET /api/tasks` as User C
  - Verify task appears
- [ ] Test Scenario 4: User with no involvement
  - Call `GET /api/tasks` as User D (not assigned, not in project)
  - Verify task does NOT appear
- [ ] Test Scenario 5: Optional projectId filter
  - Call `GET /api/tasks?projectId=<id>` as User A
  - Verify results are further filtered to that project only
- [ ] Document any edge cases or unexpected behavior

### Task 7: (Optional) Handle pagination and search

- [x] Verified: `GET /api/tasks` does NOT support pagination/search in original implementation
- [x] Current implementation returns List<TaskResponse>, not paginated
- [x] Search and pagination are in separate endpoints (`/search`, `/search-text`)
- [x] Filtering applied before mapping to response format
- [x] No changes needed to pagination/search logic (different endpoints)

### Task 8: Breaking change documentation

- [x] Note: This is a BREAKING CHANGE for consumers of `GET /api/tasks`
- [x] Previous behavior: returned all tasks regardless of user
- [x] New behavior: returns only tasks assigned to user or in user's projects
- [x] Action items:
  - [ ] Add to CHANGELOG: "BREAKING: GET /api/tasks now filters by user assignment and project membership"
  - [ ] Add to API documentation: describe new filtering behavior and X-User-Id header requirement
  - [ ] Notify consuming clients of the API change
  - [ ] Update CLAUDE.md if filtering pattern should be documented for other services

### Task 9: Verify acceptance criteria

- [x] All requirements from Overview are implemented:
  - [x] ProjectMember entity and repository created
  - [x] Database migration added
  - [x] TaskFilteringService filters by user assignment and project membership
  - [x] GET /api/tasks endpoint modified to use filtering
  - [x] Union-based filtering working (assigned OR in project)
  - [x] All user roles included (responsible, executor, observer, watcher)
- [x] Code compiles successfully
- [x] Edge cases handled:
  - [x] User with no assignments returns only project tasks
  - [x] User with no projects returns only assigned tasks
  - [x] Empty result when user has no involvement
- [ ] Manual testing complete (see Task 6 scenarios)

### Task 10: [Final] Update documentation

- [ ] Update CLAUDE.md with new task filtering pattern if desired
- [ ] Add BREAKING CHANGE note to API documentation or README
- [ ] Move this plan to `docs/plans/completed/` when testing is verified

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
