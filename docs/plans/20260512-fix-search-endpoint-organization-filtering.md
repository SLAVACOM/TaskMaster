# Fix Search Endpoint Organization Filtering

## Overview

The `GET /api/tasks/search` endpoint returns **all matching tasks** regardless of the user's organization membership. This is a security issue — users can see tasks from organizations they don't belong to.

The fix applies organization-level filtering + user-level access control (assignments + project membership) to ensure:
1. Tasks must be in the user's organization
2. Within that org, only tasks assigned to user or in user's accessible projects

## Context (from discovery)

**Files involved:**
- `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/TaskController.kt` — search endpoint
- `TaskService/src/main/kotlin/com/slavacom/taskservice/service/TaskService.kt` — search implementation
- `TaskService/src/main/kotlin/com/slavacom/taskservice/repository/TaskRepository.kt` — query methods
- `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/TaskSearchRequest.kt` — search filter DTO

**Related to:**
- Previous task filtering implementation (ProjectMember, TaskFilteringService)
- OrganizationService — owns organization data

**Question:** How do we determine user's organization?
- Option A: Extract from JWT token or security context
- Option B: Query user profile from UserService
- Option C: User's organization stored in X-Organization-Id header

## Development Approach

- **testing approach:** Manual testing only (per user preference)
- **scope:** Fix the search endpoint (`/api/tasks/search`) to apply org + user filtering
- **approach:** Filter search results after query, before pagination response
- **maintain backward compatibility:** Endpoint signature unchanged, just return fewer results

## Solution Overview

**Architecture:**
1. Determine user's organization (need to clarify with user)
2. In `TaskController.search()`:
   - Get user ID from X-User-Id header
   - Get user's organization ID
   - Get user's accessible tasks via TaskFilteringService
   - Filter those results by the search criteria (text, status, priority, etc.)
   - Return paginated results

**Design rationale:**
- Reuse TaskFilteringService for consistent access control
- Apply org filter before user filter for efficiency (narrower scope first)
- Keep pagination logic intact for UX consistency

## What Goes Where

**Implementation Steps:** Code changes to controller and service
**Post-Completion:** Manual testing, verify filtering works

## Implementation Steps

### Task 1: Clarify organization context extraction

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/TaskController.kt`

- [x] Determined: X-Organization-Id header is sent by frontend
- [x] Applied to search endpoint

### Task 2: Modify search endpoint to apply filtering

**Files:**
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/TaskController.kt`
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/TaskService.kt`

- [x] Update `TaskController.search()` method:
  - Extract userId and organizationId from headers
  - Get user's accessible tasks via taskFilteringService.getAccessibleTasks()
  - Filter by organization and pass to searchFiltered()
- [x] Create new `TaskService.searchFiltered()` method:
  - Accepts filtered task list, search criteria, and organizationId
  - Applies additional filtering (name, status, priority, responsible, etc.)
  - Handles sorting and pagination manually on filtered results
  - Returns TaskPageResponse with correct pagination
- [x] Verify endpoint returns TaskPageResponse with correct pagination
- [x] Code compiles successfully

### Task 3: Test search filtering scenarios

**Manual testing checklist:**
- [ ] Start infrastructure and TaskService
- [ ] Scenario 1: User in Org A searches — sees only Org A tasks
- [ ] Scenario 2: User in Org B searches — does NOT see Org A tasks  
- [ ] Scenario 3: Pagination works correctly (page 0, 1, etc.)
- [ ] Scenario 4: Search with filters (status, priority) and org filtering combined
- [ ] Scenario 5: Edge case: user with no org — returns empty results

## Post-Completion

**Verification:**
- Confirm search endpoint returns org-filtered results
- Verify other search endpoints (`/search-text`) don't need same fix or apply org filtering too
- Check if pagination parameters (page, size) work correctly with filtered results

**Note:** This fix depends on clarifying how to extract user's organization. If it's not available in headers/JWT, may need to add OrganizationService call in controller.
