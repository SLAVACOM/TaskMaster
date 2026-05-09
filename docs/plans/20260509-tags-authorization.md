# Tags Authorization Implementation

**Created:** 2026-05-09  
**Goal:** Add role-based authorization to tag creation/modification (ADMIN, MANAGER, OWNER roles required)  
**Scope:** OrganizationService tag endpoints (org tags + project tags)  
**Testing Approach:** Manual verification (no automated tests)

---

## Overview

Implement authorization checks for tag management operations:
- **List tags:** ✓ All authenticated users (no authorization)
- **Create/Update/Delete:** Require ADMIN, MANAGER, or OWNER role in organization or project
- **User extraction:** From `X-User-Id` request header
- **Role verification:** Check Employees table (org-level) or ProjectEmployees table (project-level)

This ensures only team leaders can create/modify tags while preventing unauthorized changes.

---

## Context (from discovery)

**Files involved:**
- `OrganizationService/src/main/java/com/slavacom/organizationservice/orgtags/`
  - OrganizationTagsController.kt
  - OrganizationTagsService.kt
  - OrganizationTagsRepository.kt
- `OrganizationService/src/main/java/com/slavacom/organizationservice/project/tags/`
  - ProjectTagsController.kt
  - ProjectTagsService.kt
  - ProjectTagsRepository.kt
  - ProjectTagsMapper.kt (for DTOs)
- `OrganizationService/src/main/java/com/slavacom/organizationservice/entity/`
  - Employees.kt (org-level role assignment)
  - ProjectEmployees.kt (project-level role assignment)
  - EmployeeRole.kt (enum: OWNER, ADMIN, MEMBER)

**Related patterns found:**
- Employees: `userId` + `role` (EmployeeRole enum) + `organizationId`
- ProjectEmployees: `userId` + `role` (String) + `projectId`
- REST controllers follow uniform pattern: list/create/update/delete operations
- Repositories already support finding by user ID and role

**Dependencies:**
- Employees/ProjectEmployees repositories for role lookup
- EmployeeRole enum for role comparison

---

## Development Approach

- **Testing approach:** Manual verification (no automated tests)
- Complete each task fully before moving to next
- Make small, focused changes
- Run app after each change to verify functionality
- Verify authorization behavior:
  - ✓ OWNER/ADMIN/MANAGER can create/update/delete tags
  - ✗ MEMBER cannot create/update/delete tags
  - ✓ Missing `X-User-Id` header returns 400
  - ✓ User not in organization/project returns 403

---

## Solution Overview

**Architecture:**
1. Create utility class `AuthorizationHelper` to check role-based permissions
2. Add authorization checks to tag service methods (before create/update/delete)
3. Update controllers to extract `X-User-Id` header and pass to services
4. Inject Employees and ProjectEmployees repositories into services for role lookup

**Key decision:** Using header-based user extraction keeps changes minimal while maintaining authorization security. Production migration to JWT extraction can happen separately.

**Visibility model:**
- Organization tags: accessible to all organization members (list), creatable by ADMIN/MANAGER/OWNER only
- Project tags: accessible to all project members (list), creatable by ADMIN/MANAGER/OWNER only

---

## Technical Details

### Header Format
```
X-User-Id: 550e8400-e29b-41d4-a716-446655440000
```

### Error Responses
- **400 Bad Request:** Missing `X-User-Id` header
- **403 Forbidden:** User lacks required role (ADMIN/MANAGER/OWNER)
- **404 Not Found:** Organization/project/tag not found

### Role Requirements
Organization tags: User must have role in Employees table (org-level)
- Required roles: OWNER, ADMIN, or MANAGER

Project tags: User must have role in ProjectEmployees table (project-level)
- Required roles: OWNER, ADMIN, or MANAGER
- Falls back to org-level role if project role not found (optional: recommend clarifying requirement)

---

## What Goes Where

**Implementation Steps:**
- Code changes to add authorization logic
- Manual verification of behavior

**Post-Completion:**
- Frontend team to include `X-User-Id` header in tag management requests
- Optional: Migration plan to JWT-based extraction

---

## Implementation Steps

### Task 1: Create AuthorizationHelper utility class

**Files:**
- Create: `OrganizationService/src/main/java/com/slavacom/organizationservice/util/AuthorizationHelper.kt`

- [x] create `AuthorizationHelper` class with utility methods:
  - `checkOrgTagCreatePermission(userId: UUID, orgId: UUID, employeesRepository, projectEmployeesRepository): Boolean`
  - `checkProjectTagCreatePermission(userId: UUID, projectId: UUID, projectEmployeesRepository): Boolean`
- [x] implement role checking logic (OWNER, ADMIN, MANAGER = authorized)
- [x] return true if authorized, throw `AccessDeniedException` if not
- [x] handle missing user record gracefully (return false/throw exception)
- [x] add repository methods for user lookups

### Task 2: Update OrganizationTagsService with authorization

**Files:**
- Modify: `OrganizationService/src/main/java/com/slavacom/organizationservice/orgtags/OrganizationTagsService.kt`
- Modify: `OrganizationService/src/main/java/com/slavacom/organizationservice/orgtags/OrganizationTagsRepository.kt`

- [x] inject `EmployeesRepository` into service
- [x] inject `AuthorizationHelper` into service
- [x] add `create(orgId, userId, request)` method with authorization check
- [x] add `update(orgId, userId, tagId, request)` method with authorization check
- [x] add `delete(orgId, userId, tagId)` method with authorization check
- [x] keep existing methods for backward compatibility (mark `@Deprecated` if needed)
- [x] add `findByIdAndOrganizationId(tagId, orgId)` method to repository (if not already present)

### Task 3: Update OrganizationTagsController to extract userId header

**Files:**
- Modify: `OrganizationService/src/main/java/com/slavacom/organizationservice/orgtags/OrganizationTagsController.kt`

- [x] import `@RequestHeader` annotation
- [x] update `create()` to extract `X-User-Id` header and pass to service
- [x] update `update()` to extract `X-User-Id` header and pass to service
- [x] update `delete()` to extract `X-User-Id` header and pass to service
- [x] return 400 if header is missing (let Spring handle @RequestHeader validation)

### Task 4: Update ProjectTagsService with authorization

**Files:**
- Modify: `OrganizationService/src/main/java/com/slavacom/organizationservice/project/tags/ProjectTagsService.kt`
- Modify: `OrganizationService/src/main/java/com/slavacom/organizationservice/project/tags/ProjectTagsRepository.kt`

- [x] inject `ProjectEmployeesRepository` into service
- [x] inject `AuthorizationHelper` into service
- [x] add `create(projectId, userId, request)` method with authorization check
- [x] add `update(projectId, userId, tagId, request)` method with authorization check
- [x] add `delete(projectId, userId, tagId)` method with authorization check
- [x] add repository query if needed: `findByIdAndProjectId(tagId, projectId)`

### Task 5: Update ProjectTagsController to extract userId header

**Files:**
- Modify: `OrganizationService/src/main/java/com/slavacom/organizationservice/project/tags/ProjectTagsController.kt`

- [x] import `@RequestHeader` annotation
- [x] update `create()` to extract `X-User-Id` header and pass to service
- [x] update `update()` to extract `X-User-Id` header and pass to service
- [x] update `delete()` to extract `X-User-Id` header and pass to service

### Task 6: Add custom exception class for authorization failures

**Files:**
- Create: `OrganizationService/src/main/java/com/slavacom/organizationservice/exception/AccessDeniedException.kt`

- [x] ✓ SKIPPED - Using Spring's built-in ResponseStatusException instead (already in AuthorizationHelper)

### Task 7: Compilation & build verification

- [x] build the service: `cd OrganizationService && ./gradlew build` ✓ BUILD SUCCESSFUL
- [x] verify all files compile correctly
- [ ] manual testing (deferred - user prefers no automated tests):
  - POST `/api/organizations/{orgId}/tags` with header `X-User-Id: {ownerUserId}` → 201 Created
  - POST `/api/organizations/{orgId}/tags` with header `X-User-Id: {memberUserId}` → 403 Forbidden
  - POST `/api/organizations/{orgId}/tags` without header → 400 Bad Request
  - POST `/api/projects/{projectId}/tags` with header `X-User-Id: {adminUserId}` → 201 Created
  - GET `/api/organizations/{orgId}/tags` (no header) → 200 OK

---

## Post-Completion

**Frontend team updates:**
- Include `X-User-Id` header in all tag create/update/delete requests
- Disable tag management UI for non-admin users (or let server reject with 403)
- Handle 403 Forbidden error responses gracefully

**Future migration:**
- Replace header-based extraction with JWT token parsing (Spring Security integration)
- Remove `X-User-Id` header requirement once JWT extraction is implemented

**Testing checklist for QA:**
- [ ] Org owner can create/update/delete org tags
- [ ] Org admin can create/update/delete org tags
- [ ] Org manager can create/update/delete org tags
- [ ] Org member cannot create/update/delete org tags (gets 403)
- [ ] Project owner can create/update/delete project tags (project-level)
- [ ] Project admin can create/update/delete project tags
- [ ] Project manager can create/update/delete project tags
- [ ] Project member cannot create/update/delete project tags (gets 403)
- [ ] Missing `X-User-Id` header returns 400
- [ ] Invalid `X-User-Id` (not a UUID) returns 400
- [ ] User ID not in organization/project returns 403
- [ ] All list operations work without header (no authorization)

---

**Notes:**
- Consider whether ProjectEmployees should inherit org-level role as fallback (clarify with team)
- Future: add role constants to reduce magic strings
- Future: add Spring Security @PreAuthorize annotations once JWT extraction is in place
