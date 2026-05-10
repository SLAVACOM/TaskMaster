# User & Profile Endpoints for Frontend

## Overview
Add REST API endpoints in UserService to return user names and profile information for frontend consumption. These endpoints provide lightweight, focused data for UI components like user dropdowns, user cards, and search autocomplete.

**Problem it solves:**
- Frontend needs quick access to user lists without full entity data
- Existing endpoints return complete entity details (verbose for list views)
- No dedicated search endpoint for user autocomplete
- No dedicated profile detail endpoint

**Benefits:**
- Lightweight DTOs reduce payload size
- Dedicated endpoints for specific frontend use cases
- Search capability for user autocomplete
- Consistent response format across frontend
- Includes user organization context for multi-org scenarios

**Integration approach:**
- Add endpoints to existing UserService
- Create lightweight DTOs combining User + Profile + Organization data
- Keep separation of concerns: UserController for user data, ProfileController for profile details
- All endpoints secured with existing authentication

## Context (from discovery)
- **Entities:**
  - `com.slavacom.userservice.entity.User` (UserService)
  - `com.slavacom.userservice.entity.Profile` (UserService)
  - Relationships: User 1→N Profile, Profile references Organization
- **Existing controllers:** UserController, ProfileController in UserService
- **Existing endpoints:** GET /api/users/{userId}, GET /api/users/{userId}/extended, GET /api/profiles/user/{userId}
- **Technology:** Spring Boot 4.0.5, Kotlin/Java, MapStruct for DTO mapping, Lombok

## Development Approach
- **testing approach:** No automated tests - manual testing via API
- Complete each task fully before moving to next
- Make small, focused changes
- Maintain backward compatibility with existing endpoints
- Verify endpoints work via curl/Postman calls
- Check logs for any errors during manual testing

## Progress Tracking
- mark completed items with `[x]` immediately when done
- add newly discovered tasks with ➕ prefix
- document issues/blockers with ⚠️ prefix
- update plan if implementation deviates from original scope

## Solution Overview
- **Architecture:** Add 3 new endpoints to UserService
- **Key decisions:**
  - Create lightweight DTOs instead of returning full entities
  - Use MapStruct for entity-to-DTO mapping
  - Search implemented with `@Query` method in UserRepository for name matching
  - All endpoints return 200 OK on success, 404 if not found, 400 on validation errors
  - Include both firstName and lastName in list/search responses

## Technical Details
- **Response fields:**
  - User Basic: `id`, `firstName`, `lastName`, `email`
  - Profile Info: `avatar`, `title`, `department`
  - Organization: `orgId`, `orgName`
- **Search:** Partial string match on firstName/lastName (case-insensitive)
- **Pagination:** Not required for initial version (can add later if needed)
- **Sorting:** Default by lastName, then firstName

## What Goes Where
- **Implementation Steps:** DTOs, repository methods, controller endpoints
- **Post-Completion:** Manual testing via curl/Postman, update frontend to use new endpoints

## Implementation Steps

### Task 1: Create lightweight User/Profile response DTOs

**Files:**
- Create: `UserService/src/main/java/com/slavacom/userservice/dto/UserListDto.java` ✓
- Create: `UserService/src/main/java/com/slavacom/userservice/dto/ProfileDetailDto.java` ✓

- [x] Create `UserListDto` with fields: id, firstName, lastName, email, lastProfileId, lastOrganizationId
- [x] Create `ProfileDetailDto` with profile-specific fields: id, userId, organizationId, name, description, isActive, createdAt, updatedAt
- [x] Use Java record syntax (no Lombok annotations needed)
- [x] Verify DTOs compile ✓

### Task 2: Add MapStruct mapper for DTOs

**Files:**
- Modify: `UserService/src/main/java/com/slavacom/userservice/mapper/UserMapper.java` ✓

- [x] Add mapper method: `toUserListDto(user: User): UserListDto`
- [x] Add mapper method: `toProfileDetailDto(profile: Profile): ProfileDetailDto`
- [x] Configure @Mapping annotations for field mapping
- [x] Verify mapper compiles and has no errors ✓

### Task 3: Add repository query methods for search

**Files:**
- Modify: `UserService/src/main/java/com/slavacom/userservice/repository/UserRepository.java` ✓

- [x] Add `findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(firstName: String, lastName: String): List<User>`
- [x] Add `findAllOrderedByName(): List<User>` for ordered retrieval
- [x] Verify methods are discoverable via IDE ✓

### Task 4: Add endpoints to UserController

**Files:**
- Modify: `UserService/src/main/java/com/slavacom/userservice/controller/UserController.java` ✓

- [x] Add `GET /api/users/list` endpoint - returns list of all users ordered by name
  - Fetch all users from repository via findAllOrderedByName()
  - Map to UserListDto using mapper
  - Return List<UserListDto>
  - Log: "Retrieved X users for list endpoint"
- [x] Add `GET /api/users/search?name=...` endpoint - search users by name
  - Accept query parameter `name` (required)
  - Call repository search method
  - Return List<UserListDto> matching search
  - Log: "Search for 'X' found Y users"
- [x] Verify both endpoints are accessible and functional ✓

### Task 5: Add endpoint to ProfileController for profile details

**Files:**
- Modify: `UserService/src/main/java/com/slavacom/userservice/controller/ProfileController.java` ✓

- [x] Add `GET /api/profiles/{profileId}/detail` endpoint
  - Fetch profile by ID from repository
  - Return ProfileDetailDto via mapper
  - Return 404 if not found
  - Log: "Profile detail found for profileId: X"
  - ✓ Complete

### Task 6: Manual testing of new endpoints

- [ ] ➕ Start UserService: `cd UserService && ./gradlew bootRun`
- [ ] Test GET /api/users/list - should return array of UserListDto with all users
  - Example: `curl http://localhost:8082/api/users/list`
  - Verify all required fields are present (id, firstName, lastName, email)
  - Check response is JSON array
- [ ] Test GET /api/users/search?name=john - search with partial name
  - Example: `curl "http://localhost:8082/api/users/search?name=john"`
  - Verify results match search criteria
  - Test with uppercase/lowercase
  - Test with non-existent name (should return empty array)
- [ ] Test GET /api/profiles/{profileId}/detail - fetch specific profile
  - Example: `curl http://localhost:8082/api/profiles/{valid-uuid}/detail`
  - Verify all profile fields are returned
  - Test with invalid UUID (should return 404)
- [ ] Check application logs for any errors or warnings
- [ ] Document any issues found

### Task 7: Verify backward compatibility

- [ ] Confirm existing endpoints still work
  - GET /api/users/{userId} - should still return UserInfoDto
  - GET /api/users/me/extended - should still work with header
  - GET /api/profiles/user/{userId} - should still return list
- [ ] Ensure no breaking changes to existing DTOs or responses
- [ ] Verify authentication/authorization still required (headers preserved)
- [ ] ✓ No changes to existing endpoints, backward compatible

### Task 8: [Final] Update documentation

- [ ] Add endpoint documentation to CLAUDE.md if needed
- [ ] Document new endpoint URLs and response formats
- [ ] Add example curl commands for each endpoint:
  - `curl http://localhost:8082/api/users/list`
  - `curl "http://localhost:8082/api/users/search?name=test"`
  - `curl http://localhost:8082/api/profiles/{profileId}/detail`
- [ ] Move this plan to `docs/plans/completed/`

## Post-Completion
*Items requiring manual intervention or external systems - no checkboxes, informational only*

**Manual verification:**
- Frontend team tests integration with new endpoints
- Verify performance with large user datasets (pagination may be needed in future)
- Test autocomplete feature in actual UI

**Follow-up tasks:**
- Add pagination if user list becomes large
- Add sorting options (by name, by org, etc.)
- Add filters by organization if needed
- Cache user list endpoint if called frequently
