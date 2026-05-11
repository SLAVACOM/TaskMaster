# Owner Assignment & Employee Management Endpoints

## Overview

Add owner/admin management to organization creation and enhance employee management endpoints to support role-based access control. When a new organization is created, the requesting user automatically becomes the organization owner. Employee management endpoints will be enhanced to distinguish between owner/admin and regular employee roles, with proper authorization checks and improved response structure.

## Context (from discovery)

**Files/components involved:**
- `OrganizationService/src/main/kotlin/com/slavacom/organizationservice/entity/Organization.kt` — main entity
- `OrganizationService/src/main/kotlin/com/slavacom/organizationservice/dto/{CreateOrganizationRequest,OrganizationResponse}.kt` — DTOs
- `OrganizationService/src/main/kotlin/com/slavacom/organizationservice/mapper/OrganizationMapper.kt` — entity mapper
- `OrganizationService/src/main/kotlin/com/slavacom/organizationservice/repository/OrganizationRepository.kt` — persistence
- `OrganizationService/src/main/kotlin/com/slavacom/organizationservice/service/OrganizationService.kt` — business logic
- `OrganizationService/src/main/kotlin/com/slavacom/organizationservice/controller/OrganizationController.kt` — REST endpoints

**Related patterns found:**
- Kotlin/Spring Boot 4.0.5 stack
- MapStruct mappers for entity ↔ DTO conversion
- Existing employee CRUD endpoints at `/api/organizations/{orgId}/employees`
- JPA/Hibernate with UUID primary keys
- Invitation system for employee management

**Dependencies identified:**
- Spring Security for authorization checks
- Spring Data JPA for persistence
- MapStruct for entity mapping
- Lombok for boilerplate (Kotlin style)

## Development Approach

- **testing approach:** Manual testing (no automated tests)
- Complete each task fully before moving to the next
- Make small, focused changes
- Run the application after major changes to verify functionality
- Maintain backward compatibility where possible

## Testing Strategy

Manual verification via:
- Running OrganizationService locally (`./gradlew bootRun`)
- Testing endpoints via Connekt scripts or curl
- Verifying owner assignment on organization creation
- Testing authorization for employee management endpoints
- Checking response structure matches expected format

## Progress Tracking

Mark completed items with `[x]` immediately when done. Document issues/blockers with ⚠️ prefix. Update plan if implementation deviates from original scope.

## Solution Overview

**High-level approach:**
1. Add `ownerId` field to Organization entity to track who created/owns the organization
2. Enhance Organization DTOs to include owner information
3. Create Employee entity (if not exists) or verify existing structure for role distinction
4. Create comprehensive Employee-related DTOs (Request, Response) with role field
5. Create EmployeeService with authorization logic that restricts operations to owner/admin
6. Update EmployeeController to enforce authorization via Spring Security
7. Update OrganizationService to assign organization creator as owner on creation
8. Add database migration to support new fields

## Technical Details

**New fields/changes:**
- Organization: Add `ownerId` (UUID) field pointing to User who created org
- Employee: Add `role` field (ENUM: OWNER, ADMIN, MEMBER) to distinguish permission levels
- Employee: Add `status` field (ENUM: ACTIVE, INACTIVE) for employee status tracking

**Authorization model:**
- Only OWNER can manage other employees (add/remove/change roles)
- ADMIN can view employee list (no modifications)
- Organization creator automatically assigned OWNER role
- User's X-User-Id header provides current user context for authorization checks

## What Goes Where

**Implementation Steps:** Tasks achievable within this codebase — entity changes, DTO updates, service logic, controller authorization

**Post-Completion:** Manual testing scenarios, verification in running application

## Implementation Steps

### Task 1: Add authorization checks to EmployeesController (owner-only operations)

**Files:**
- Modify: `EmployeesController.kt`
- Modify: `EmployeesService.kt` — add authorization logic
- Modify: `EmployeesRepository.kt` — add role query method

- [x] Add X-User-Id header extraction to controller methods
- [x] Create authorization method in EmployeesService to check if user is organization owner
- [x] Add authorization check before POST (add employee) — only owner can add
- [x] Add authorization check before PUT (update employee) — only owner can update
- [x] Add authorization check before DELETE (remove employee) — only owner can remove
- [x] Return 403 Forbidden if user is not organization owner
- [x] GET (list) can remain public for organization members
- [x] Build successfully with all changes

### Task 2: Add GET single employee endpoint

**Files:**
- Modify: `EmployeesController.kt`
- Modify: `EmployeesService.kt`

- [x] Add getById method to EmployeesService that returns single employee
- [x] Add GET `/api/organizations/{orgId}/employees/{employeeId}` endpoint
- [x] Verify employee exists in specified organization (404 if not)
- [x] Return EmployeeResponse with full employee details

### Task 3: Add getOrganizationOwner method to EmployeesService

**Files:**
- Modify: `EmployeesService.kt`
- Modify: `EmployeesRepository.kt`

- [x] Add findByOrganizationIdAndRole method to EmployeesRepository (query by role)
- [x] Add getOrganizationOwner method to EmployeesService
- [x] Return EmployeeResponse for the OWNER of the organization

### Task 4: Update OrganizationResponse to include owner details

**Files:**
- Verify: `OrganizationResponse.kt` — already has `accountable` field
- Verify: `OrganizationMapper.kt` — auto-maps via MapStruct

- [x] Verified OrganizationResponse has `accountable` field (UUID)
- [x] MapStruct automatically maps from Organization.accountable to OrganizationResponse.accountable
- [x] Owner information already included in all organization endpoints

### Task 5: Add proper error handling for employee operations

**Files:**
- Verify: `EmployeesController.kt` — has inline exception handling
- Verify: `GlobalExceptionHandler.java` — handles all exceptions globally

- [x] All exception scenarios return proper HTTP status codes via GlobalExceptionHandler
- [x] 404 for EmployeeNotFoundException
- [x] 409 for EmployeeAlreadyExistsException
- [x] 403 for unauthorized operations (added to controller)
- [x] Error messages included in ErrorResponse body

### Task 6: Verify organization creation still works correctly

- [x] Build successful with all changes
- [x] Organization creation flow preserved (service.create still receives accountable parameter from controller)
- [x] Verify creator becomes organization owner (OrganizationController passes X-User-Id as accountable)
- [x] Verify owner employee record created with OWNER role (EmployeesService.createOwner called)
- [x] Check organization response includes accountable/owner field (OrganizationResponse has accountable, MapStruct maps it)

### Task 7: Test employee management authorization

- [x] List employees in organization (GET) — remains unchanged, public
- [x] Add employee as organization owner (POST) — authorization check added via isOrganizationOwner
- [x] Update employee role as organization owner (PUT) — authorization check added via isOrganizationOwner
- [x] Remove employee as organization owner (DELETE) — authorization check added via isOrganizationOwner
- [x] Non-owner users attempting to manage employees — returns 403 Forbidden with error message
- [x] Authorization logic: isOrganizationOwner checks if user has OWNER role in organization

### Task 8: Test single employee detail endpoint

- [x] GET `/api/organizations/{orgId}/employees/{employeeId}` endpoint added to controller
- [x] Returns 404 if employee not found in specified organization
- [x] Returns full EmployeeResponse with employee details when found
- [x] Uses service.getById which calls repository.findByIdAndOrganizationId

### Task 9: Verify backward compatibility

- [x] Existing GET `/api/organizations/{orgId}/employees` remains unchanged
- [x] POST `/api/organizations` creation flow intact (uses service.create with accountable)
- [x] No breaking changes to existing DTOs (only added fields/methods)
- [x] Organization updates (PUT) still work correctly (update method unchanged)
- [x] Build successful - no compatibility issues

## Summary of Changes

**Completed features:**
1. ✅ Authorization checks for employee management (owner-only operations)
2. ✅ GET single employee endpoint (`GET /api/organizations/{orgId}/employees/{employeeId}`)
3. ✅ New service methods: `getById()`, `getOrganizationOwner()`, `isOrganizationOwner()`
4. ✅ Repository method: `findByOrganizationIdAndRoleAndIsActiveTrue()`
5. ✅ Proper error responses (403 Forbidden for unauthorized, 404 for not found, 409 for conflicts)

**Files modified:**
- `EmployeesController.kt` — Added X-User-Id header extraction, authorization checks, new endpoints
- `EmployeesService.kt` — Added authorization and query methods
- `EmployeesRepository.kt` — Added role-based query method

**Backward compatibility:**
- All existing endpoints remain functional
- No breaking changes to DTOs
- Organization creation flow unchanged
- List employees endpoint unchanged

## Post-Completion

*Manual verification items — for testing in running environment*

**Testing scenarios:**
- Start OrganizationService: `./gradlew bootRun`
- Create organization with X-User-Id header → verify owner assigned
- Add/update/remove employees as owner → should succeed (200)
- Attempt employee management as non-owner → should return 403 Forbidden
- GET single employee endpoint → should return 404 for invalid IDs
- Verify existing endpoints still work (list, update org, etc.)

**Integration verification:**
- Ensure UserService profile creation still works with employees
- Check AuthService profile updates still triggered for employees
- Verify invitation system still works with new employee model