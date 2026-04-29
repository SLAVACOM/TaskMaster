# Fix: OrganizationService missing `/api/organizations/user/{userId}/info` endpoint

## Overview
UserService calls `GET /api/organizations/user/{userId}/info` on OrganizationService to build `ExtendedUserInfoDto`.
OrganizationController has no such route → 404. The fix adds the missing endpoint plus all supporting layers.

## Context (from discovery)
- **Caller**: `UserService/src/main/java/com/slavacom/userservice/client/OrganizationServiceClient.java`
  - calls `GET /api/organizations/user/{userId}/info`
  - deserialises into `OrganizationInfoDto(id, name, description, userId, currentUserId, role)`
- **Missing route**: `OrganizationService` `OrganizationController.kt` — no `user/{userId}/info` mapping
- **Data available**:
  - `Employees` entity: `userId`, `organizationId`, `role`, `isActive`
  - `Organization` entity: `id`, `name`, `description`, `accountable` (owner userId)
  - `EmployeesRepository`: needs a `findByUserIdAndIsActiveTrue(userId)` query
  - `OrganizationRepository`: `findById` already exists

## Development Approach
- Testing approach: **None** (targeted bug fix)
- Complete each task fully before moving to the next
- No new tests required

## Progress Tracking
- Mark completed items with `[x]` immediately when done
- Add ➕ for newly discovered tasks
- Add ⚠️ for blockers

## Solution Overview
1. Add `findByUserIdAndIsActiveTrue(userId: UUID): Optional<Employees>` to `EmployeesRepository`
2. Create a response DTO `UserOrganizationInfoResponse` in OrganizationService matching the UserService contract
3. Add service method `getUserOrganizationInfo(userId: UUID)` to `OrganizationService`
4. Add `GET /api/organizations/user/{userId}/info` endpoint to `OrganizationController`

## Implementation Steps

### Task 1: Add repository query for employee by userId

**Files:**
- Modify: `OrganizationService/src/main/java/com/slavacom/organizationservice/employees/EmployeesRepository.kt`

- [x] Add `findByUserIdAndIsActiveTrue(userId: UUID): Optional<Employees>` to `EmployeesRepository`

---

### Task 2: Create the response DTO

**Files:**
- Create: `OrganizationService/src/main/java/com/slavacom/organizationservice/controller/UserOrganizationInfoResponse.kt`

- [x] Create `data class UserOrganizationInfoResponse` with fields matching the UserService `OrganizationInfoDto`:
  `id: String, name: String, description: String?, userId: String, currentUserId: String, role: String`

---

### Task 3: Add service method

**Files:**
- Modify: `OrganizationService/src/main/java/com/slavacom/organizationservice/service/OrganizationService.kt`

- [x] Inject `EmployeesRepository` into `OrganizationService`
- [x] Add `getUserOrganizationInfo(userId: UUID): UserOrganizationInfoResponse?`:
  - Find active `Employees` record by userId (return `null` if not found)
  - Find `Organization` by `organizationId` (throw `OrganizationNotFoundException` if missing)
  - Map to `UserOrganizationInfoResponse`

---

### Task 4: Add the missing endpoint

**Files:**
- Modify: `OrganizationService/src/main/java/com/slavacom/organizationservice/controller/OrganizationController.kt`

- [x] Add `GET /user/{userId}/info` mapping to `OrganizationController`
- [x] Return `ResponseEntity<UserOrganizationInfoResponse>` — 200 with body if found, 204/404 if user has no active org membership

---

### Task 5: Verify end-to-end

- [ ] Start OrganizationService and UserService (or run via docker-compose)
- [ ] Call `GET /api/users/{userId}/extended` on UserService and confirm `organizationInfo` is populated
- [ ] Confirm no 404 in OrganizationService logs

## Post-Completion

**Manual verification:**
- Test with a userId that has no active `Employees` record → confirm graceful null/empty response in UserService
- Test with a valid userId → confirm all fields (`id`, `name`, `description`, `userId`, `currentUserId`, `role`) are correct
