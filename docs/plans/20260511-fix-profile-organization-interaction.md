# Fix Profile-Organization Interaction Error Handling

## Overview
When OrganizationService requests UserService to create a profile, the request fails with a 500 error because ProfileService throws custom exceptions (OrganizationNotFoundException, ServiceUnavailableException) that aren't handled. These exceptions bubble up as generic RuntimeExceptions, causing the entire organization creation transaction to rollback. The fix adds proper exception handlers for these specific errors so they return appropriate HTTP status codes (404, 503) instead of 500, allowing OrganizationService to handle them gracefully.

## Context
- **files involved**: 
  - `UserService/src/main/java/com/slavacom/userservice/service/ProfileService.java`
  - `UserService/src/main/java/com/slavacom/userservice/client/OrganizationServiceClient.java`
  - `UserService/src/main/java/com/slavacom/userservice/exception/GlobalExceptionHandler.java`
  - `UserService/src/main/java/com/slavacom/userservice/exception/OrganizationNotFoundException.java`
  - `UserService/src/main/java/com/slavacom/userservice/exception/ServiceUnavailableException.java`

- **related patterns**: GlobalExceptionHandler with @ControllerAdvice already exists
- **dependencies**: Spring Framework exception handling, RestClient error propagation

## Development Approach
- **testing approach**: Manual verification (no new tests)
- Complete each task fully before moving to the next
- Make focused changes to exception handling
- Run application locally and test the organization creation flow
- Verify error responses are proper HTTP status codes instead of 500

## Testing Strategy
- Manual testing: create organization request → verify proper error response (404 or 503)
- Check UserService logs for proper error logging
- Verify OrganizationService receives correct error status and handles it gracefully

## Solution Overview
The fix has two parts:
1. **GlobalExceptionHandler enhancement**: Add specific handlers for OrganizationNotFoundException (404) and ServiceUnavailableException (503) 
2. **ProfileService error handling**: Ensure exceptions from OrganizationServiceClient propagate cleanly to the exception handler

The approach is to add dedicated @ExceptionHandler methods in GlobalExceptionHandler for the custom exceptions, positioned before the generic RuntimeException handler so they take precedence.

## What Goes Where
- **Implementation Steps**: Code changes in exception handler
- **Post-Completion**: Manual testing of organization creation flow

## Implementation Steps

### Task 1: Add exception handlers to GlobalExceptionHandler

**Files:**
- Modify: `UserService/src/main/java/com/slavacom/userservice/exception/GlobalExceptionHandler.java`

- [x] add @ExceptionHandler method for OrganizationNotFoundException returning 404 NOT_FOUND
- [x] add @ExceptionHandler method for ServiceUnavailableException returning 503 SERVICE_UNAVAILABLE
- [x] position new handlers before generic RuntimeException handler (order matters)
- [x] include appropriate logging with error context
- [x] use consistent error response format with others

### Task 2: Verify ProfileService error propagation

**Files:**
- Read: `UserService/src/main/java/com/slavacom/userservice/service/ProfileService.java`

- [x] confirm createProfile() calls organizationServiceClient.getOrganizationById() without catching exceptions
- [x] confirm exceptions from OrganizationServiceClient are properly typed (not wrapped in RuntimeException)
- [x] verify other ProfileService methods that interact with external services follow same pattern

### Task 3: Manual testing

- [ ] start UserService locally
- [ ] start OrganizationService locally
- [ ] attempt to create organization with invalid organization ID → expect 404 response with proper error message
- [ ] attempt to create organization with unavailable organization service → expect 503 response
- [ ] verify organization creation succeeds with valid organization ID
- [ ] check UserService logs for appropriate error logging (not 500 errors)

### Task 4: Final verification

- [ ] confirm organization creation flow works end-to-end
- [ ] verify OrganizationService properly handles 404/503 from UserService
- [ ] check error messages are descriptive and logged properly

## Post-Completion
*Items requiring manual verification - no checkboxes*

**Manual testing:**
- Test invalid organization ID during organization creation
- Test organization service unavailable scenario
- Verify successful organization creation end-to-end
- Check logs for proper error context and tracing information
