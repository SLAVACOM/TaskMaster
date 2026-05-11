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

### Task 1: Add owner field to Organization entity

**Files:**
- Modify: `OrganizationService/src/main/kotlin/com/slavacom/organizationservice/entity/Organization.kt`
- Create: Database migration file for adding `owner_id` column

- [ ] Add `ownerId: UUID?` field to Organization entity
- [ ] Add appropriate JPA annotations (@Column, @NotNull after migration)
- [ ] Create Liquibase/Flyway migration to add `owner_id` column to organizations table
- [ ] Migration should make column NOT NULL with DEFAULT for existing records

### Task 2: Update Organization DTOs and mapper

**Files:**
- Modify: `CreateOrganizationRequest.kt` — add owner field if needed
- Modify: `OrganizationResponse.kt` — include owner info in response
- Modify: `OrganizationMapper.kt` — map owner field to DTO

- [ ] Update OrganizationResponse to include owner information (id, name/email if available)
- [ ] Update OrganizationMapper to map owner field from entity to response
- [ ] Verify CreateOrganizationRequest doesn't require explicit owner (should be automatic)

### Task 3: Update OrganizationService to assign owner on creation

**Files:**
- Modify: `OrganizationService.kt` (in service package)

- [ ] Find create/save organization method
- [ ] Extract current user ID from X-User-Id header or authentication context
- [ ] Set ownerId to current user before saving organization
- [ ] Verify existing organization queries don't break

### Task 4: Create Employee entity (or verify structure)

**Files:**
- Read: `OrganizationService/src/main/kotlin/com/slavacom/organizationservice/entity/` — check if Employee entity exists
- Create or Modify: Employee entity file

- [ ] Verify Employee entity structure (id, organizationId, userId, role, status fields)
- [ ] If Employee entity doesn't exist, create it with required fields
- [ ] Add `role` field (ENUM with OWNER, ADMIN, MEMBER values)
- [ ] Add `status` field (ENUM with ACTIVE, INACTIVE values)
- [ ] Add JPA annotations and relationships to Organization
- [ ] Create migration if entity is new

### Task 5: Create/update Employee DTOs

**Files:**
- Create or Modify: `EmployeeResponse.kt` — response DTO with all employee details
- Create or Modify: `CreateEmployeeRequest.kt` — request DTO for adding employees
- Create or Modify: `UpdateEmployeeRequest.kt` — request DTO for updating employees

- [ ] Create EmployeeResponse DTO with id, userId, role, status, joinDate fields
- [ ] Create CreateEmployeeRequest DTO with userId, role fields
- [ ] Create UpdateEmployeeRequest DTO with role, status fields
- [ ] Ensure DTOs follow project conventions (camelCase, proper validation annotations)

### Task 6: Create EmployeeMapper

**Files:**
- Create or Modify: `EmployeeMapper.kt` in mapper package

- [ ] Create MapStruct EmployeeMapper for Entity ↔ DTO conversions
- [ ] Implement mapping for all EmployeeResponse/CreateEmployeeRequest/UpdateEmployeeRequest
- [ ] Handle role and status ENUM conversions

### Task 7: Create EmployeeService with authorization

**Files:**
- Create or Modify: `EmployeeService.kt` in service package

- [ ] Create service with methods: getEmployeesByOrg, addEmployee, updateEmployee, removeEmployee
- [ ] Add authorization check: only OWNER can add/remove/modify employees
- [ ] Implement role assignment logic (default to MEMBER for new employees)
- [ ] Handle user context from X-User-Id header for authorization checks
- [ ] Verify user is OWNER of organization before allowing changes

### Task 8: Update/create EmployeeController with authorization

**Files:**
- Create or Modify: `EmployeeController.kt` in controller package

- [ ] Create REST endpoints:
    - GET `/api/organizations/{orgId}/employees` — list all employees
    - POST `/api/organizations/{orgId}/employees` — add new employee (owner only)
    - GET `/api/organizations/{orgId}/employees/{employeeId}` — get employee details
    - PUT `/api/organizations/{orgId}/employees/{employeeId}` — update employee (owner only)
    - DELETE `/api/organizations/{orgId}/employees/{employeeId}` — remove employee (owner only)
- [ ] Add authorization checks via @PreAuthorize or custom security logic
- [ ] Return proper HTTP status codes (403 Forbidden for unauthorized, 404 for not found)
- [ ] Include error response messages in responses
- [ ] Verify authorization headers passed correctly to service

### Task 9: Update EmployeeRepository

**Files:**
- Create or Modify: `EmployeeRepository.kt` in repository package

- [ ] Create/verify EmployeeRepository extends JpaRepository
- [ ] Add query methods: findByOrganizationId, findByOrganizationIdAndId, deleteByOrganizationIdAndId
- [ ] Add custom queries if needed for role-based queries

### Task 10: Verify organization creation flow end-to-end

- [ ] Start OrganizationService: `./gradlew bootRun` from OrganizationService directory
- [ ] Create new organization via POST `/api/organizations`
- [ ] Verify ownerId is set to request user ID
- [ ] Check organization response includes owner information
- [ ] Verify created organization owner can manage employees
- [ ] Verify non-owner users cannot manage employees

### Task 11: Test employee management endpoints

- [ ] Add employee to organization (as owner)
- [ ] Verify employee appears in GET `/api/organizations/{orgId}/employees`
- [ ] Update employee role and status
- [ ] Remove employee from organization
- [ ] Test authorization: non-owner attempting to manage employees returns 403
- [ ] Verify response structure matches expected format

### Task 12: Verify backward compatibility

- [ ] Test existing organization queries still work
- [ ] Verify existing employee list endpoint returns correct format
- [ ] Check no breaking changes to existing DTOs or endpoints
- [ ] Test organization updates (PUT) don't affect owner assignment

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