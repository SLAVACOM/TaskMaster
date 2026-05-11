# Fix Profile-Organization Service Interaction

## Overview
When creating a user profile in UserService with an organizationId, the service should verify that the organization exists in OrganizationService and notify it of the new profile creation. Currently, ProfileService creates profiles without verifying the organization exists, leading to inconsistent state and potential errors when profiles reference non-existent organizations.

**Problem:** After profile creation, UserService doesn't confirm the organization exists or establish the relationship with OrganizationService.

**Solution:** Add validation to ProfileService that:
1. Verifies the organization exists before creating the profile
2. Handles errors gracefully when OrganizationService is unavailable
3. Notifies OrganizationService of the new profile (if API exists)

## Context (from discovery)
- **Entities involved:** Profile (UserService), Organization (OrganizationService)
- **Services:** ProfileService, OrganizationServiceClient
- **Key finding:** OrganizationServiceClient exists but only has `getUserOrganizationInfo()` method
- **Current gap:** No method to verify organization exists by ID; no call to OrganizationService in createProfile flow
- **Architecture:** Services communicate via RestClient; each has separate PostgreSQL database

## Development Approach
- **Testing approach:** Regular (code first, then tests)
- Complete each task fully before moving to the next
- Make small, focused changes
- **CRITICAL: every task MUST include new/updated tests** for code changes
  - Unit tests for RestClient calls
  - Unit tests for validation logic
  - Integration tests for profile creation with organization verification
- **CRITICAL: all tests must pass before starting next task**
- Run tests after each change
- Maintain backward compatibility where applicable

## Testing Strategy
- **Unit tests:** Mock OrganizationServiceClient to test ProfileService validation logic
- **Integration tests:** Use RestClient mock or test container to verify inter-service interaction
- **Error scenarios:** Verify proper exception handling when organization not found or service unavailable

## Progress Tracking
- Mark completed items with `[x]` immediately when done
- Add newly discovered tasks with ➕ prefix
- Document issues/blockers with ⚠️ prefix
- Update plan if implementation deviates from original scope

## Solution Overview

### Architecture
1. **OrganizationServiceClient** enhanced with method to check organization existence
2. **ProfileService** calls OrganizationServiceClient to validate organization before creating profile
3. **Error handling:** Throws appropriate exceptions (with clear messages) if organization not found or service unavailable
4. **Transaction management:** Uses @Transactional to ensure consistency

### Design Decisions
- **Validation timing:** Organization validation happens BEFORE profile creation to avoid orphaned profiles
- **Error handling:** Fails fast (throws exception) rather than creating profile with invalid organizationId
- **Logging:** All calls to OrganizationService are logged for debugging distributed tracing
- **Header propagation:** RestClient includes X-User-Id and Authorization headers for proper service-to-service communication

### Data Flow
```
Client POST /api/profiles
  ↓
ProfileController.createProfile()
  ↓
ProfileService.createProfile()
  ├→ OrganizationServiceClient.getOrganizationById(organizationId)
  │  └→ GET /api/organizations/{organizationId} to OrganizationService
  │     (Verify organization exists)
  ├→ if organization not found → throw OrganizationNotFoundException
  ├→ if service unavailable → throw ServiceUnavailableException
  ├→ Profile.save() to database
  ├→ (Optional) NotifyOrganizationService.addProfileToOrganization()
  └→ return ProfileResponse
```

## Implementation Steps

### Task 1: Add method to OrganizationServiceClient to verify organization exists

**Files:**
- Modify: `UserService/src/main/java/com/slavacom/userservice/client/OrganizationServiceClient.java`
- Create: `UserService/src/main/java/com/slavacom/userservice/exception/OrganizationNotFoundException.java`
- Create: `UserService/src/main/java/com/slavacom/userservice/exception/ServiceUnavailableException.java`
- Create: `UserService/src/test/java/com/slavacom/userservice/client/OrganizationServiceClientTest.java`

- [x] Create OrganizationNotFoundException (extends RuntimeException)
- [x] Create ServiceUnavailableException (extends RuntimeException)
- [x] Add getOrganizationById(UUID organizationId) method to OrganizationServiceClient
  - Calls GET /api/organizations/{organizationId}
  - Returns OrganizationInfoDto on success
  - Throws OrganizationNotFoundException if 404 returned
  - Logs all requests/responses for tracing
- [x] Handle service unavailability (RestClientException)
  - Wrap in custom exception or propagate with clear message
  - Ensure error is not swallowed silently
- [x] Write unit tests for exception classes
- [x] Run tests - PASSED ✅

### Task 2: Update ProfileService to validate organization before creating profile

**Files:**
- Modify: `UserService/src/main/java/com/slavacom/userservice/service/ProfileService.java`
- Modify: `UserService/src/main/java/com/slavacom/userservice/controller/ProfileController.java` (if needed for error handling)

- [ ] Inject OrganizationServiceClient into ProfileService
- [ ] Update createProfile() method to call organizationServiceClient.getOrganizationById()
  - Call happens BEFORE profile.save()
  - Add log entry with organizationId and result
  - Let exceptions propagate (they'll be caught by GlobalExceptionHandler)
- [ ] Verify proper error messages in exceptions (helpful for debugging)
- [ ] Write unit tests for ProfileService.createProfile()
  - Test success case: organization exists, profile created
  - Test failure case: organization not found (404)
  - Test failure case: OrganizationService unavailable
  - Mock OrganizationServiceClient using Mockito
- [ ] Write integration tests if time permits (optional)
- [ ] Run all tests - must pass before task 3

### Task 3: Update GlobalExceptionHandler to handle new exceptions

**Files:**
- Modify: `UserService/src/main/java/com/slavacom/userservice/exception/GlobalExceptionHandler.java`

- [ ] Add @ExceptionHandler for OrganizationNotFoundException
  - Return 400 Bad Request with clear message: "Organization with ID {id} not found"
  - Include timestamp and error code for tracking
- [ ] Add @ExceptionHandler for ServiceUnavailableException (if created in task 1)
  - Return 503 Service Unavailable or 500 with message about OrganizationService
  - Log the error for monitoring
- [ ] Write unit tests for exception handlers
  - Test each handler returns correct status code and message format
- [ ] Run tests - must pass before task 4

### Task 4: Verify RestClient configuration includes proper headers

**Files:**
- Check: `UserService/src/main/java/com/slavacom/userservice/config/RestClientConfig.java`
- Check: `CLAUDE.md` for header requirements

- [ ] Review RestClientConfig to ensure it:
  - Includes X-User-Id header (required by CLAUDE.md)
  - Includes Authorization/JWT token if needed
  - Has proper timeout settings
  - Includes any other headers needed for inter-service communication
- [ ] If headers missing, add ClientHttpRequestInterceptor to RestClient.builder()
  - Set X-User-Id from current request context (if available)
  - Set Authorization header for service-to-service auth
- [ ] Add logging interceptor if not present (for debugging)
- [ ] Write unit tests for RestClient bean creation
- [ ] Run tests - must pass before task 5

### Task 5: Verify acceptance criteria

- [ ] Verify organization validation happens before profile creation
- [ ] Verify appropriate exceptions are thrown for missing organizations
- [ ] Verify error responses include helpful messages for debugging
- [ ] Verify logs include trace IDs for distributed tracing (Sleuth integration)
- [ ] Run full test suite: `./gradlew -p UserService test`
- [ ] Verify no breaking changes to existing profile endpoints
- [ ] Test manually: create profile with valid organizationId (should succeed)
- [ ] Test manually: create profile with invalid organizationId (should fail with 400)

### Task 6: [Final] Update documentation

- [ ] Update docs/LOGGING.md if new logging patterns added
- [ ] Update CLAUDE.md if new architectural patterns discovered
- [ ] Add comment in ProfileService explaining organization validation logic
- [ ] Move this plan to `docs/plans/completed/`

## Post-Completion

**Integration testing:**
- Start full docker-compose stack: `cd All-Compose && docker-compose ... up -d`
- Test profile creation via Postman/curl with valid and invalid organizationIds
- Verify OrganizationService logs show incoming organization lookup requests
- Check Zipkin at http://localhost:9411 for distributed trace IDs

**Monitoring:**
- Monitor logs for any "Organization not found" errors in production
- Set up alert if profile creation error rate exceeds threshold
- Consider adding Prometheus metrics for inter-service call latency

**Future enhancements (not in this plan):**
- Add retry logic for transient OrganizationService failures (circuit breaker pattern)
- Cache organization existence checks (Redis) to reduce inter-service calls
- Implement bidirectional sync: OrganizationService also records profile relationships