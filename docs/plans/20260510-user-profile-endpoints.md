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
- Create: `UserService/src/main/java/com/slavacom/userservice/dto/UserListDto.java`
- Create: `UserService/src/main/java/com/slavacom/userservice/dto/ProfileDetailDto.java`

- [ ] Create `UserListDto` with fields: id, firstName, lastName, email, avatar, title, department, orgId, orgName
- [ ] Create `ProfileDetailDto` with profile-specific fields: id, userId, title, department, bio, avatar, profilePictureUrl
- [ ] Add @Data/@AllArgsConstructor/@NoArgsConstructor (Lombok) annotations
- [ ] Verify DTOs compile

### Task 2: Add MapStruct mapper for DTOs

**Files:**
- Create: `UserService/src/main/java/com/slavacom/userservice/mapper/UserProfileMapper.kt`

- [ ] Create mapper interface with methods: `userToUserListDto(user: User, profiles: List<Profile>): UserListDto`
- [ ] Add method to map Profile entity to ProfileDetailDto
- [ ] Configure mapper to include organization mapping (may require joining in query)
- [ ] Verify mapper compiles and has no errors

### Task 3: Add repository query methods for search

**Files:**
- Modify: `UserService/src/main/java/com/slavacom/userservice/repository/UserRepository.java`

- [ ] Add `findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(firstName: String, lastName: String): List<User>`
- [ ] Add `findAll(): List<User>` if not already present (Spring Data default)
- [ ] Verify methods are discoverable via IDE

### Task 4: Add endpoints to UserController

**Files:**
- Modify: `UserService/src/main/java/com/slavacom/userservice/controller/UserController.java`

- [ ] Add `GET /api/users` endpoint - returns list of all users with basic+profile+org info
  - Fetch all users from repository
  - Enrich with profile and organization data
  - Return List<UserListDto>
  - Log: "Fetching all users"
- [ ] Add `GET /api/users/search?name=...` endpoint - search users by name
  - Accept query parameter `name` (required)
  - Call repository search method
  - Return List<UserListDto> matching search
  - Log: "Searching users by name: $name, found X results"
- [ ] Verify both endpoints are accessible at correct URLs

### Task 5: Add endpoint to ProfileController for profile details

**Files:**
- Modify: `UserService/src/main/java/com/slavacom/userservice/controller/ProfileController.java`

- [ ] Add/enhance `GET /api/profiles/{profileId}` endpoint if not present
  - Fetch profile by ID
  - Return ProfileDetailDto
  - Return 404 if not found
  - Log: "Fetching profile: $profileId"

### Task 6: Manual testing of new endpoints

- [ ] Start UserService in Docker or locally: `./gradlew bootRun`
- [ ] Test GET /api/users - should return array of UserListDto with all users
  - Verify all required fields are present
  - Check that organization info is populated
- [ ] Test GET /api/users/search?name=john - search with partial name
  - Verify results match search criteria
  - Test with uppercase/lowercase
  - Test with non-existent name (should return empty array)
- [ ] Test GET /api/profiles/{profileId} - fetch specific profile
  - Verify all profile fields are returned
  - Test with invalid ID (should return 404)
- [ ] Check application logs for any errors or warnings
- [ ] Document any issues found

### Task 7: Verify backward compatibility

- [ ] Confirm existing endpoints still work (GET /api/users/{userId}, etc.)
- [ ] Ensure no breaking changes to existing DTOs or responses
- [ ] Verify authentication/authorization still required for all endpoints

### Task 8: [Final] Update documentation

- [ ] Add endpoint documentation to API docs/README if present
- [ ] Document new endpoint URLs and response formats
- [ ] Add example curl commands for each endpoint
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
