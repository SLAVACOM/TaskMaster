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

## Solution Overview
The fix involves:
1. **Verify attribute storage** — ensure JwtAuthFilter correctly stores extracted JWT claims in exchange.getAttributes()
2. **Verify header injection** — ensure RequestHeaderFilter correctly reads attributes and mutates the outgoing request
3. **Add comprehensive logging** — trace the flow from JWT extraction through to header injection
4. **Test the full flow** — make requests through gateway and verify headers reach downstream services

The core issue is likely that headers are being mutated correctly at the gateway level, but either:
- The attributes aren't being populated by JwtAuthFilter
- The request mutation in RequestHeaderFilter isn't working as expected
- Headers are being lost during the HTTP forward to downstream services

## What Goes Where
- **Implementation Steps**: Code changes and logging in Gateway service filters
- **Post-Completion**: Manual testing of requests through the gateway to downstream services

## Implementation Steps

### Task 1: Add detailed logging to JwtAuthFilter

**Files:**
- Modify: `Gateway-service/src/main/java/com/slavacom/gateway/filter/JwtAuthFilter.java`

- [ ] Add logging after storing each attribute to show exactly what values were stored
- [ ] Add logging to show the exchange attributes before calling chain.filter(exchange)
- [ ] Add logging for public path requests to distinguish them from protected requests
- [ ] Verify the format of stored attributes matches what RequestHeaderFilter expects (keys like "X-User-Id")

### Task 2: Add detailed logging to RequestHeaderFilter

**Files:**
- Modify: `Gateway-service/src/main/java/com/slavacom/gateway/filter/RequestHeaderFilter.java`

- [ ] Add logging at the start showing what attributes exist in the exchange
- [ ] Add logging after reading each attribute from exchange to show the values retrieved
- [ ] Add logging after adding each header to show what headers were added to the request builder
- [ ] Add logging after building the new request to show the final headers in the request
- [ ] Add logging before and after calling chain.filter() to trace the flow

### Task 3: Verify filter order and configuration

**Files:**
- Modify: `Gateway-service/src/main/resources/application.yml`

- [ ] Verify that route predicates are correctly matching requests to downstream services
- [ ] Check that no other filters are interfering with header propagation
- [ ] Ensure RequestHeaderFilter is not accidentally stripping or overwriting headers
- [ ] Verify environment variables for service URLs are correctly set in .env

### Task 4: Test header propagation manually

**Verification steps:**
- [ ] Start full infrastructure: `cd All-Compose && docker compose -f docker-compose.yaml -f docker-compose.db.yml -f docker-compose.kafka.yml -f docker-compose.services.yml up -d`
- [ ] Register a user via `POST /api/auth/register`
- [ ] Login to get a JWT token via `POST /api/auth/login`
- [ ] Make a request to a protected endpoint through the gateway: `GET /api/users/profile` with `Authorization: Bearer <token>`
- [ ] Check the gateway logs to verify:
  - JwtAuthFilter extracted the JWT and stored attributes
  - RequestHeaderFilter read those attributes and added headers
- [ ] Check the downstream service logs (e.g., UserService) to verify:
  - X-User-Id header was received
  - X-User-Role header was received
  - Other context headers were received
- [ ] If headers are missing, review the logs to identify where the flow breaks

### Task 5: Fix identified issues

**Files:**
- Modify: `Gateway-service/src/main/java/com/slavacom/gateway/filter/JwtAuthFilter.java` (if needed)
- Modify: `Gateway-service/src/main/java/com/slavacom/gateway/filter/RequestHeaderFilter.java` (if needed)
- Modify: `Gateway-service/src/main/resources/application.yml` (if needed)

- [ ] Based on logging output, identify the root cause of missing headers
- [ ] Common issues to check:
  - Attributes not being stored (check attribute keys match between filters)
  - Request mutation not working (check if header builder is correctly creating the new request)
  - Filter order issue (verify JwtAuthFilter runs before RequestHeaderFilter)
  - Route not matching (verify the request path matches a route predicate)
- [ ] Apply fix based on root cause
- [ ] Run a manual test to verify headers now propagate

### Task 6: Verify downstream services receive headers

**Files:**
- Modify: Downstream service files if needed (to log received headers)

- [ ] Update downstream services (UserService, OrganizationService, etc.) to log incoming X-User-Id headers
- [ ] Make authenticated requests and verify logs show the headers were received
- [ ] Test both successful cases and edge cases:
  - Authenticated user with all claims (userId, role, etc.)
  - Public paths that should NOT have headers
  - Missing claims that should be omitted from headers

### Task 7: Clean up logging and document findings

**Files:**
- Modify: `Gateway-service/src/main/java/com/slavacom/gateway/filter/JwtAuthFilter.java`
- Modify: `Gateway-service/src/main/java/com/slavacom/gateway/filter/RequestHeaderFilter.java`

- [ ] Keep diagnostic logging for debug level (not info level)
- [ ] Add comments explaining the filter purpose and flow
- [ ] Update this plan file with findings and root cause of the issue
- [ ] Document any configuration changes or workarounds discovered

### Task 8: [Final] Verify complete integration

- [ ] Make a request as a new user through the full stack and verify headers flow end-to-end
- [ ] Test that public paths (like /api/auth/**) work without the custom headers
- [ ] Test that protected paths receive all expected headers
- [ ] Verify no performance degradation from added logging

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
