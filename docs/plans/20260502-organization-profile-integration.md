# Organization Profile Integration & JWT Enhancement

## Status Summary (2026-05-02)

✅ **COMPLETED (6/10 Core Tasks)**
- [x] Task 1: UserServiceClient created with DTOs and error handling
- [x] Task 2: AuthServiceClient created with update capability  
- [x] Task 3: OrganizationService orchestration with transactional rollback
- [x] Task 4: UserService profile creation (pre-existing support verified)
- [x] Task 5: AuthService JWT generation with profileId support
- [x] Task 6: Gateway filter fixes (MutableHttpServerRequest, header propagation)
- [x] Database migrations created for auth-service

🔄 **IN PROGRESS / REMAINING**
- [ ] Task 7: Full integration testing
- [ ] Task 8: End-to-end testing through gateway
- [ ] Task 9: Documentation updates

## Overview
Implement inter-service communication between OrganizationService, UserService, and AuthService to:
1. Create user profiles in UserService when organization is created
2. Update JWT in AuthService to include profileId and organizationId
3. Enable gateway to propagate these IDs as X-Profile-Id and X-Organization-Id headers
4. Ensure transactional consistency across services

This integrates the microservices chain: Organization creation → Profile creation → JWT update → Header propagation.

## Context (from discovery)
**Files involved:**
- `OrganizationService/src/main/java/com/slavacom/organizationservice/service/OrganizationService.kt`
- `OrganizationService/src/main/java/com/slavacom/organizationservice/client/UserServiceClient.java` (NEW)
- `OrganizationService/src/main/java/com/slavacom/organizationservice/client/AuthServiceClient.java` (NEW)
- `OrganizationService/build.gradle` — may need RestClient dependency
- `Auth-service/src/main/java/com/slavacom/auth_service/service/JwtService.java` — needs profileId support
- `UserService/src/main/java/com/slavacom/userservice/service/UserServiceImpl.java` — needs profile creation

**Related patterns found:**
- Spring's RestClient (already used across services)
- Kotlin + Java mixed (OrganizationService uses both)
- MapStruct for DTOs
- Service layer with repository pattern

**Dependencies identified:**
- UserService API: POST /api/profiles, GET /api/users/{userId}/last-profile/{profileId}, PUT /api/users/{userId}/last-profile/{profileId}
- AuthService API: needs endpoint to update profile info in JWT (or relies on JWT refresh)
- Transactional consistency required

## Development Approach
- **Testing approach**: Regular (code first, then tests)
- Complete each task fully before moving to next
- Make small, focused changes
- **CRITICAL: every task MUST include tests** for code changes
- Run tests after each change
- Maintain backward compatibility

## Testing Strategy
- Unit tests for REST clients (mock responses)
- Integration tests for service layer (in-memory DB or testcontainers)
- Test both success and error scenarios
- Test transactional rollback on inter-service failures

## Progress Tracking
- mark completed items with `[x]` immediately when done
- add newly discovered tasks with ➕ prefix
- document issues/blockers with ⚠️ prefix
- update plan if scope changes

## Solution Overview

### Architecture
```
Organization Create Request
    ↓
OrganizationService.create()
    ├→ Save Organization to DB
    ├→ Call UserServiceClient.createProfile()
    │   └→ UserService creates Profile entity linked to organization
    │       Returns profileId
    ├→ Call AuthServiceClient.updateProfile()
    │   └→ AuthService updates user's latest profile
    │       JWT now includes profileId + organizationId
    └→ Return OrganizationResponse with profileId
        ↓
    Gateway can now extract X-Profile-Id from JWT
```

### Key Design Decisions

1. **Transactional Boundaries**: 
   - Organization created in local transaction
   - If UserService fails: don't create organization (rollback)
   - If AuthService fails: log but don't block (profile exists, JWT will be updated on refresh)

2. **REST Clients**: Use Spring's RestClient for synchronous calls (simpler than Feign for this case)

3. **Error Handling**: Specific exceptions for each service failure

4. **Profile Linking**: User profile contains organizationId, allowing "user has multiple profiles across orgs"

## Technical Details

### DTOs for Inter-Service Communication

**To UserService (CreateProfileRequest):**
```kotlin
data class CreateProfileRequest(
    val userId: UUID,
    val organizationId: UUID,
    val name: String = "Default Profile"
)
```

**From UserService (ProfileResponse):**
```kotlin
data class ProfileResponse(
    val id: UUID,
    val userId: UUID,
    val organizationId: UUID,
    val name: String
)
```

**To AuthService (UpdateProfileRequest):**
```kotlin
data class UpdateProfileRequest(
    val userId: UUID,
    val profileId: UUID,
    val organizationId: UUID
)
```

### Service-to-Service Flow

1. **Organization Creation**:
   - Receive CreateOrganizationRequest + X-User-Id header
   - Create Organization entity (accountable = userId)
   - Call UserServiceClient.createProfile(userId, orgId)
   - Call AuthServiceClient.updateProfile(userId, profileId, orgId)
   - Return OrganizationResponse

2. **Profile Creation in UserService**:
   - Receive CreateProfileRequest
   - Create Profile entity with organizationId
   - Return ProfileResponse with id

3. **Profile Update in AuthService**:
   - Receive UpdateProfileRequest
   - Update user's profile info
   - JWT regeneration happens on next login/refresh

## What Goes Where

- **Implementation Steps**: Code changes in OrganizationService, UserService, AuthService
- **Post-Completion**: Manual testing through gateway, verify headers are propagated

## Implementation Steps

### Task 1: Create UserServiceClient for profile operations ✅

**Files:**
- Create: `OrganizationService/src/main/java/com/slavacom/organizationservice/client/UserServiceClient.java`
- Create: `OrganizationService/src/main/java/com/slavacom/organizationservice/dto/CreateProfileRequest.kt`
- Create: `OrganizationService/src/main/java/com/slavacom/organizationservice/dto/ProfileResponse.kt`
- Create: `OrganizationService/src/main/java/com/slavacom/organizationservice/config/RestClientConfig.kt`

- [x] Create CreateProfileRequest DTO (userId, organizationId, name)
- [x] Create ProfileResponse DTO (id, userId, organizationId, name)
- [x] Create RestClientConfig with UserService URL
- [x] Create UserServiceClient with RestClient bean
- [x] Implement createProfile(userId, organizationId) method
- [x] Add error handling with custom exceptions (UserServiceException)
- [x] Write unit tests for UserServiceClient (mock RestClient)
- [x] Tests for success and failure scenarios - PASSED ✓

### Task 2: Create AuthServiceClient for profile updates ✅

**Files:**
- Create: `OrganizationService/src/main/java/com/slavacom/organizationservice/client/AuthServiceClient.java`
- Create: `OrganizationService/src/main/java/com/slavacom/organizationservice/dto/UpdateProfileRequest.kt`

- [x] Create UpdateProfileRequest DTO (userId, profileId, organizationId)
- [x] Create AuthServiceClient with RestClient bean
- [x] Implement updateProfile(userId, profileId, organizationId) method
- [x] Add error handling with custom exceptions (AuthServiceException)
- [x] Write unit tests for AuthServiceClient
- [x] Tests for success and failure scenarios - PASSED ✓

### Task 3: Update OrganizationService to orchestrate profile creation ✅

**Files:**
- Modify: `OrganizationService/src/main/java/com/slavacom/organizationservice/service/OrganizationService.kt`
- Modify: `OrganizationService/src/main/java/com/slavacom/organizationservice/dto/OrganizationResponse.kt`

- [x] Inject UserServiceClient and AuthServiceClient into OrganizationService
- [x] Update create() method to call UserServiceClient.createProfile()
- [x] Update create() method to call AuthServiceClient.updateProfile()
- [x] Add transactional rollback on UserService failure
- [x] Add logging for each step
- [x] Handle exceptions with proper error messages
- [x] Update OrganizationResponse to include profileId
- [x] Write integration tests for create() with mock clients
- [x] Test success scenario: org + profile + jwt updated
- [x] Test failure scenario: UserService fails, org rolled back
- [x] Tests for AuthService non-blocking failure - PASSED ✓

### Task 4: Add UserService support for profile creation ✅

**Files:**
- Already exists: `UserService/src/main/java/com/slavacom/userservice/controller/ProfileController.java`
- Already exists: `UserService/src/main/java/com/slavacom/userservice/service/ProfileService.java`
- Already exists: `UserService/src/main/java/com/slavacom/userservice/entity/Profile.java`

- [x] Profile entity already has organizationId field
- [x] CreateProfileRequest DTO already includes organizationId
- [x] POST /api/profiles endpoint already accepts organizationId
- [x] ProfileService.createProfile() already handles organizationId
- [x] Unique constraint on (user_id, organization_id) ensures data integrity
- [x] No changes needed - UserService ready ✓

### Task 5: Add AuthService support for profile updates ✅

**Files:**
- Modify: `Auth-service/src/main/java/com/slavacom/auth_service/service/JwtService.java` (already supports profileId)
- Modify: `Auth-service/src/main/java/com/slavacom/auth_service/controller/AuthController.java`
- Modify: `Auth-service/src/main/java/com/slavacom/auth_service/entity/User.java`
- Create: `Auth-service/src/main/java/com/slavacom/auth_service/dto/UpdateProfileRequest.java`
- Create: `Auth-service/src/main/resources/db/migration/V4__add_profile_fields_to_user.sql`

- [x] Add latestProfileId and latestOrganizationId fields to User entity
- [x] Create UpdateProfileRequest DTO
- [x] Add PUT /api/auth/users/{userId}/profile endpoint
- [x] Implement updateUserProfile() in AuthService
- [x] JwtService.generateExtendedAccessToken() already includes profileId
- [x] Both login and refreshToken use extended JWT
- [x] Database migration created for new columns
- [x] Tests for JWT generation - PASSED ✓

### Task 6: Update Gateway filters to handle profileId in headers ✅

**Files:**
- Modify: `Gateway-service/src/main/java/com/slavacom/gateway/filter/JwtAuthFilter.java`
- Create: `Gateway-service/src/main/java/com/slavacom/gateway/filter/MutableHttpServerRequest.java`
- Modify: `Gateway-service/src/main/java/com/slavacom/gateway/filter/RequestHeaderFilter.java`

- [x] JwtAuthFilter extracts JWT claims including profileId
- [x] Store profileId in exchange.getAttributes() as X-Profile-Id
- [x] RequestHeaderFilter adds X-Profile-Id header to downstream requests
- [x] Fixed ReadOnlyHttpHeaders issue with MutableHttpServerRequest wrapper
- [x] Tests for header propagation - PASSED ✓

### Task 7: Database migrations for new fields

**Files:**
- Create: `Auth-service/src/main/resources/db/migration/V*__add_latestProfileId_to_user.sql`
- Create: `UserService/src/main/resources/db/migration/V*__add_organizationId_to_profile.sql` (if needed)

- [ ] Create migration for Auth-service (add latestProfileId to user table)
- [ ] Create migration for UserService (add organizationId to profile table if not exists)
- [ ] Verify migrations are named correctly and in sequence
- [ ] Run migrations locally to verify syntax
- [ ] Document migration approach in CLAUDE.md

### Task 8: Integration testing

**Files:**
- Create: `OrganizationService/src/test/java/com/slavacom/organizationservice/OrganizationProfileIntegrationTest.kt`

- [ ] Write integration test: create organization → verify profile created → verify JWT updated
- [ ] Test with testcontainers or @SpringBootTest
- [ ] Mock external services or use test doubles
- [ ] Test error scenarios: what happens if UserService is down
- [ ] Test error scenarios: what happens if AuthService is down
- [ ] Run full test suite: `./gradlew test`

### Task 9: End-to-end testing

**Manual verification:**
- [ ] Start all services (Gateway, AuthService, UserService, OrganizationService)
- [ ] Create an organization via `/api/organizations` with X-User-Id header
- [ ] Verify organization created in DB
- [ ] Verify profile created in UserService DB
- [ ] Get JWT from login endpoint
- [ ] Decode JWT and verify it includes `profileId` and `organizationId`
- [ ] Make request to `/api/users/profile` through gateway
- [ ] Check gateway logs for X-Profile-Id header injection
- [ ] Check downstream service logs for received X-Profile-Id header
- [ ] Verify authorization works based on profileId

### Task 10: [Final] Update documentation and cleanup

**Files:**
- Modify: `CLAUDE.md`
- Modify: `docs/plans/20260502-organization-profile-integration.md`

- [ ] Document the organization creation flow in CLAUDE.md
- [ ] Document JWT structure now includes profileId
- [ ] Document how gateway propagates headers
- [ ] Update this plan with completion notes
- [ ] Move this plan to `docs/plans/completed/`

## Post-Completion

**Manual verification scenarios:**
- Create organization → verify profile exists in UserService
- Verify JWT contains profileId
- Verify gateway extracts and propagates all headers
- Test with multiple organizations for same user
- Verify profile switching works (if implemented)

**External system updates:**
- All services should be compatible with new JWT structure
- Any service consuming X-Profile-Id header should handle it gracefully
