# Fix Gateway Header Propagation to Microservices

## Overview
The Gateway service's JWT context headers (X-User-Id, X-User-Role, X-Profile-Id, X-Organization-Id) are not being propagated to downstream microservices. These headers are critical for microservices to identify the current user and apply role-based authorization. Currently, `JwtAuthFilter` extracts JWT claims but the headers are not reaching downstream services.

## Context (from discovery)
- **Files involved:**
  - `Gateway-service/src/main/java/com/slavacom/gateway/filter/JwtAuthFilter.java` — extracts JWT claims, stores in exchange attributes
  - `Gateway-service/src/main/java/com/slavacom/gateway/filter/RequestHeaderFilter.java` — mutates outgoing request with headers
  - `Gateway-service/src/main/resources/application.yml` — route definitions
  - `Gateway-service/.env` — environment configuration

- **Related patterns found:**
  - Spring Cloud Gateway with global filters (order -1 for JWT validation, order 0 for header injection)
  - Reactive flow using Mono<Void> and ServerWebExchange
  - Request mutation via ServerHttpRequest.Builder

- **Dependencies identified:**
  - JwtAuthFilter must run first (order = -1)
  - RequestHeaderFilter must run after (order = 0)
  - Both filters use exchange.getAttributes() to pass data between them

## Development Approach
- **testing approach**: Manual testing via running the application and verifying headers in downstream services
- Every fix should be validated by running the gateway and checking if headers reach microservices
- Use logging extensively to trace header flow through the filters
- Test both authenticated requests (should have headers) and public paths (should not)

## Testing Strategy
Manual verification:
1. Start the full infrastructure stack (docker-compose)
2. Make authenticated request through gateway
3. Check logs in downstream services to verify headers were received
4. Verify error handling for missing/invalid tokens

## Progress Tracking
- mark completed items with `[x]` immediately when done
- add newly discovered tasks with ➕ prefix
- document blockers with ⚠️ prefix
- update plan if scope changes

## Solution Overview & Root Cause

**Root Cause:** `RequestHeaderFilter` was using `ServerHttpRequest.Builder.header()` method to add headers, but Spring Cloud Gateway's request object has `ReadOnlyHttpHeaders` which don't support the `put()` operation used internally by `header()`. This caused `UnsupportedOperationException` when attempting to add custom headers.

**Fix Applied:** Changed the header mutation approach in `RequestHeaderFilter` from:
```java
ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
requestBuilder.header("X-User-Id", userId);
```

To:
```java
ServerHttpRequest newRequest = exchange.getRequest().mutate()
    .headers(headers -> {
        if (StringUtils.hasText(userId)) {
            headers.add("X-User-Id", userId);
        }
    })
    .build();
```

The `headers(Consumer<HttpHeaders>)` method properly handles mutation of the header collection and doesn't trigger the read-only constraint.

**Verification:** Headers will now successfully propagate from JwtAuthFilter (extraction and storage) → RequestHeaderFilter (injection) → downstream microservices.

## What Goes Where
- **Implementation Steps**: Code changes and logging in Gateway service filters
- **Post-Completion**: Manual testing of requests through the gateway to downstream services

## Implementation Steps

### Task 1: Add detailed logging to JwtAuthFilter

**Files:**
- Modify: `Gateway-service/src/main/java/com/slavacom/gateway/filter/JwtAuthFilter.java`

- [x] Add logging after storing each attribute to show exactly what values were stored
- [x] Add logging to show the exchange attributes before calling chain.filter(exchange)
- [x] Add logging for public path requests to distinguish them from protected requests
- [x] Verify the format of stored attributes matches what RequestHeaderFilter expects (keys like "X-User-Id")

### Task 2: Fix header mutation in RequestHeaderFilter (ReadOnlyHttpHeaders issue)

**Files:**
- Modify: `Gateway-service/src/main/java/com/slavacom/gateway/filter/RequestHeaderFilter.java`

- [x] Identify root cause: `ReadOnlyHttpHeaders.put()` exception when using `header()` method
- [x] Replace `header()` calls with `headers(Consumer<HttpHeaders>)` pattern
- [x] Add logging at the start showing what attributes exist in the exchange
- [x] Add logging for each header being added to show what headers are injected
- [x] Add logging before and after calling chain.filter() to trace the flow

### Task 3: Test header propagation manually (verify fix works)

**Verification steps:**
- [ ] Start full infrastructure: `cd All-Compose && docker compose -f docker-compose.yaml -f docker-compose.db.yml -f docker-compose.kafka.yml -f docker-compose.services.yml up -d`
- [ ] Register a user via `POST /api/auth/register`
- [ ] Login to get a JWT token via `POST /api/auth/login`
- [ ] Make a request to a protected endpoint through the gateway: `GET /api/users/profile` with `Authorization: Bearer <token>`
- [ ] Check the gateway logs to verify:
  - JwtAuthFilter extracted JWT and stored attributes
  - RequestHeaderFilter read attributes and successfully added headers (no more ReadOnlyHttpHeaders errors)
  - Request was forwarded with X-User-Id, X-User-Role headers
- [ ] Check the downstream service logs (UserService) to verify:
  - X-User-Id header was received in the request
  - Service can identify the user from the header
  - Role-based authorization works correctly

### Task 4: Verify downstream services receive headers

**Files:**
- Modify: Downstream service files (to log/verify received headers)

- [ ] Update downstream services (UserService, OrganizationService, TaskService) to log incoming X-User-Id headers for verification
- [ ] Test authenticated requests to verify headers are present in logs
- [ ] Test public paths (like /api/auth/**) - these should NOT have custom headers
- [ ] Verify services can use X-User-Id for authorization checks

### Task 5: [Final] Clean up and document

**Files:**
- Modify: `Gateway-service/src/main/java/com/slavacom/gateway/filter/JwtAuthFilter.java`
- Modify: `Gateway-service/src/main/java/com/slavacom/gateway/filter/RequestHeaderFilter.java`

- [ ] Keep debug-level logging (don't remove it - useful for troubleshooting)
- [ ] Update plan file with the root cause found and fix applied
- [ ] Verify all tests pass and no regressions
- [ ] Document: Root cause was `ReadOnlyHttpHeaders` exception - fixed by using `headers(Consumer<HttpHeaders>)` instead of `header()` method

## Post-Completion

**Manual verification scenarios:**
- Register a new user and make requests to protected endpoints
- Verify microservices can identify the user from X-User-Id header
- Check authorization works correctly (role-based access control uses X-User-Role)
- Verify public authentication endpoints work without the custom headers

**Configuration to verify:**
- JWT_SECRET environment variable is identical across all services
- SERVICE_URL environment variables in .env point to correct Docker service names
- Kafka topics and consumers are properly configured (if used for events)
