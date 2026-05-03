# Fix: Employee Profile Update Workflow

**Date:** 2026-05-02  
**Status:** Implemented  
**Issue:** When adding employees to an organization, the X-User-Id header was not being propagated to downstream services because the JWT was not updated with profileId and organizationId.

## Problem Summary

When a user was added as an employee to an organization without refreshing their token:
- JWT did not contain `profileId` and `organizationId` claims
- Gateway filter `JwtAuthFilter` could not add `X-User-Id`, `X-Profile-Id`, `X-Organization-Id` headers
- Downstream services received requests without these crucial headers and rejected them

The root cause was that `AuthService` was not being informed about the user's new profile when they were added as an employee in `OrganizationService`.

## Solution

### 1. **EmployeesService.kt** (OrganizationService)

**Changes:**
- Added dependency injection for `UserServiceClient` and `AuthServiceClient`
- Modified `add()` method to:
  1. Create a new profile in `UserService` if `profileId` not provided
  2. Save the employee with the profile association
  3. Call `authServiceClient.updateProfile()` to update Auth-service with `latestProfileId` and `latestOrganizationId`

**Code Flow:**
```kotlin
fun add(orgId: UUID, request: AddEmployeeRequest): EmployeeResponse {
    // 1. Validate no duplicate
    // 2. Create profile if needed
    val profileId = request.profileId ?: userServiceClient.createProfile(userId, orgId).id
    // 3. Save employee with profileId
    val savedEmployee = employeesRepository.save(employee)
    // 4. Update Auth-service (non-blocking)
    authServiceClient.updateProfile(userId, profileId, orgId)
    return employeesMapper.toResponse(savedEmployee)
}
```

**Non-blocking**: If Auth-service call fails, employee addition still succeeds. User will get updated JWT on next login/refresh.

### 2. **AuthService.java** (Auth-service)

**Changes:**
- Modified `login()` and `refreshToken()` methods to use `latestProfileId` and `latestOrganizationId` from User entity
- This ensures JWT includes current profile/organization data even if token is refreshed before user logs in

**Code Logic:**
```java
// Use latestProfileId from User entity if set, otherwise use UserService data (for backward compatibility)
UUID profileIdForToken = user.getLatestProfileId() != null ? 
        user.getLatestProfileId() : extendedUserInfo.getProfileId();
UUID organizationIdForToken = user.getLatestOrganizationId() != null ? 
        user.getLatestOrganizationId() : extendedUserInfo.getOrganizationId();

// Generate token with profile/org data
String accessToken = jwtService.generateExtendedAccessToken(
    userId, user.getRole(), profileIdForToken, organizationIdForToken
);
```

## Data Flow

### When Adding an Employee:
1. `POST /api/organizations/{orgId}/employees`
2. `EmployeesService.add()`:
   - Creates profile in `UserService` if not provided
   - Saves employee record
   - Calls `PUT /api/auth/users/{userId}/profile` → updates Auth-service
3. Auth-service updates `User.latestProfileId` and `User.latestOrganizationId`

### When User Logs In or Refreshes Token:
1. `AuthService.login()` or `.refreshToken()`
2. Checks `User.latestProfileId` and `User.latestOrganizationId`
3. If set, uses these values; otherwise uses UserService data
4. Calls `jwtService.generateExtendedAccessToken()` with profile/org data
5. JWT now contains `profileId` and `organizationId` claims

### When Gateway Validates JWT:
1. `JwtAuthFilter` extracts claims from token
2. Adds headers:
   - `X-User-Id: <userId>`
   - `X-Profile-Id: <profileId>`
   - `X-Organization-Id: <organizationId>`
3. Downstream services receive complete context

## Files Modified

| File | Changes |
|------|---------|
| `OrganizationService/.../employees/EmployeesService.kt` | Added profile creation + Auth-service update |
| `Auth-service/.../service/AuthService.java` | Use `latestProfileId`/`latestOrganizationId` in login/refresh |

## Backward Compatibility

✅ If `latestProfileId` and `latestOrganizationId` are not set, system falls back to UserService data  
✅ Existing users without these fields will continue to work  
✅ Non-blocking auth-service updates won't break employee addition

## Testing Recommendations

1. **Add Employee to Organization:**
   ```bash
   POST /api/organizations/{orgId}/employees
   Body: { "userId": "...", "role": "...", "profileId": null }
   ```
   - Verify profile created in UserService
   - Verify Auth-service updated with profileId

2. **Login After Adding Employee:**
   - User should receive JWT with `profileId` and `organizationId`
   - Verify Gateway adds headers to downstream requests

3. **Token Refresh:**
   - Refresh token should include profile/org data from Auth-service (not just UserService)

4. **Backward Compatibility:**
   - Test login for users without `latestProfileId` set
   - Verify fallback to UserService data still works

