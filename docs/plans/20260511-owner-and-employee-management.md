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

- [ ] Add getById method to EmployeesService that returns single employee
- [ ] Add GET `/api/organizations/{orgId}/employees/{employeeId}` endpoint
- [ ] Verify employee exists in specified organization (404 if not)
- [ ] Return EmployeeResponse with full employee details

### Task 3: Add getOrganizationOwner method to EmployeesService

**Files:**
- Modify: `EmployeesService.kt`
- Modify: `EmployeesRepository.kt`

- [ ] Add findByOrganizationIdAndRole method to EmployeesRepository (query by role)
- [ ] Add getOrganizationOwner method to EmployeesService
- [ ] Return EmployeeResponse for the OWNER of the organization

### Task 4: Update OrganizationResponse to include owner details

**Files:**
- Modify: `OrganizationResponse.kt`
- Modify: `OrganizationMapper.kt`

- [ ] Add owner field to OrganizationResponse (can be nullable UUID or OwnerInfo object)
- [ ] Update OrganizationMapper to populate owner field from `accountable` field
- [ ] Verify response includes owner information in all organization endpoints

### Task 5: Add proper error handling for employee operations

**Files:**
- Modify: `EmployeesController.kt`
- Create error response DTO if needed

- [ ] Ensure all exception scenarios return proper HTTP status codes
- [ ] Return 404 for employee not found
- [ ] Return 409 for employee already exists
- [ ] Return 403 for unauthorized operations
- [ ] Include error message in response body

### Task 6: Verify organization creation still works correctly

- [ ] Start OrganizationService: `./gradlew bootRun` from OrganizationService directory
- [ ] Create new organization via POST `/api/organizations` with X-User-Id header
- [ ] Verify creator becomes organization owner (accountable field set)
- [ ] Verify owner employee record created with OWNER role
- [ ] Check organization response includes accountable/owner field

### Task 7: Test employee management authorization

- [ ] List employees in organization (GET) — should return all active employees
- [ ] Add employee as organization owner (POST) — should succeed
- [ ] Update employee role as organization owner (PUT) — should succeed
- [ ] Remove employee as organization owner (DELETE) — should succeed
- [ ] Try adding employee as non-owner user — should return 403 Forbidden
- [ ] Try updating employee as non-owner user — should return 403 Forbidden
- [ ] Try removing employee as non-owner user — should return 403 Forbidden

### Task 8: Test single employee detail endpoint

- [ ] GET `/api/organizations/{orgId}/employees/{employeeId}` — should return employee details
- [ ] GET with invalid employee ID — should return 404
- [ ] GET with different organization ID — should return 404

### Task 9: Verify backward compatibility

- [ ] Verify existing GET `/api/organizations/{orgId}/employees` still works
- [ ] Verify POST `/api/organizations` creation flow intact
- [ ] Check no breaking changes to existing DTOs or endpoints
- [ ] Test organization updates (PUT) still work correctly

## Post-Completion

*Manual verification items — no checkboxes*

**Manual testing scenarios:**
- Create organization and verify owner is automatically assigned
- Add multiple employees and test role-based filtering
- Verify permission errors returned correctly (403 Forbidden)
- Test with different user roles to confirm authorization works
- Verify invitation system still works with new employee management

**Integration verification:**
- Ensure UserService can resolve owner user IDs if needed
- Check Kafka events (if any) are triggered on employee changes
- Verify authorization headers propagate correctly across service calls