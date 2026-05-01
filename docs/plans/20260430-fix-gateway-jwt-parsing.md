# Fix Gateway JWT Parsing Failure

## Overview
The Gateway's `JwtAuthFilter` fails to parse JWT access tokens, returning 401 for requests that
should be authorized. The reactor stack trace shows the error surfaces inside the WebFlux
completion chain; the root JWT exception (key mismatch, expired token, malformed token) is
swallowed by the catch block and not visible without enhanced logging.

Two problems drive this:
1. `docker-compose.services.yml` wires `JWT_ACCESS_SECRET` for the gateway but omits
   `JWT_REFRESH_SECRET` — Spring Boot falls back to the application.yml default, which may
   differ from Auth-service if the variable is ever set inconsistently.
2. `JwtAuthFilter` calls the synchronous `parseClaims()` directly inside the reactive
   `Mono<Void>` return path. Exceptions are caught by a try-catch, but the catch block calls
   `exchange.getResponse().setComplete()` which can race with other WebFlux infrastructure
   writing to the response. The correct pattern is `Mono.fromCallable` + `onErrorResume`.

## Context (from discovery)
- Files/components involved:
  - `Gateway-service/src/main/java/.../filter/JwtAuthFilter.java`
  - `Gateway-service/src/main/java/.../config/GatewaySecurityConfig.java`
  - `Gateway-service/src/main/resources/application.yml`
  - `Gateway-service/src/main/resources/application-docker.yml`
  - `Gateway-service/.env`
  - `Auth-service/src/main/java/.../config/JWTConfig.java`
  - `Auth-service/src/main/java/.../service/JwtService.java`
  - `All-Compose/docker-compose.services.yml`
- Related patterns: GlobalFilter + Ordered, `Keys.hmacShaKeyFor`, `@Value` injection
- Dependencies: `io.jsonwebtoken:jjwt-api:0.13.0`, Spring Cloud Gateway, reactor-core 3.6.11

## Development Approach
- **Testing approach**: None (no automated tests)
- Complete each task fully before moving to the next
- Verify via log output and manual curl/Connekt test after each change

## Solution Overview
1. Add key-fingerprint logging at startup in both Auth-service and Gateway so secret mismatches
   are immediately visible without reading env vars by hand.
2. Fix docker-compose to explicitly pass both JWT secrets to the gateway; remove the dead
   `REFRESH_SECRET` field from `JwtAuthFilter`.
3. Refactor `JwtAuthFilter.filter()` to use `Mono.fromCallable` + `onErrorResume` so JWT
   exceptions propagate safely through the reactor chain and the 401 response is written
   atomically.

## Implementation Steps

### Task 1: Add startup key-fingerprint logging

Identify secret mismatches between Auth-service and Gateway before debugging the reactive layer.

**Files:**
- Modify: `Auth-service/src/main/java/com/slavacom/auth_service/config/JWTConfig.java`
- Modify: `Gateway-service/src/main/java/com/slavacom/gateway/filter/JwtAuthFilter.java`

- [ ] In `JWTConfig.jwtAccessSigningKey()` add `log.info("JWT access key fingerprint: {}",
      HexFormat.of().formatHex(key.getEncoded(), 0, 4))` after key creation (first 4 bytes only)
- [ ] Add the same fingerprint log in `JwtAuthFilter` when `ACCESS_SECRET` is first used
      (inject via `@PostConstruct` or at first-filter call behind a flag)
- [ ] Restart both services, compare the logged fingerprints — they must match

### Task 2: Fix docker-compose env wiring + remove dead code

**Files:**
- Modify: `All-Compose/docker-compose.services.yml`
- Modify: `Gateway-service/src/main/java/com/slavacom/gateway/filter/JwtAuthFilter.java`

- [ ] In `docker-compose.services.yml` under `gateway-service.environment`, add
      `JWT_REFRESH_SECRET: TaskMasterRefreshSecret2026SuperLongKey` (same value as Auth-service)
- [ ] In `JwtAuthFilter` remove the `REFRESH_SECRET` `@Value` field — it is declared but never
      read in `parseClaims()`, causing a misleading `@Value` injection that could throw at startup
      if the property is absent without a default
- [ ] Verify `application.yml` and `application-docker.yml` for Gateway both have correct
      defaults for `jwt.access.secret` and `jwt.refresh.secret`

### Task 3: Refactor JwtAuthFilter to reactive-safe pattern

**Files:**
- Modify: `Gateway-service/src/main/java/com/slavacom/gateway/filter/JwtAuthFilter.java`

- [ ] Replace the `try { Claims claims = parseClaims(authHeader); ... } catch (Exception e) { ... }`
      block with:
      ```java
      return Mono.fromCallable(() -> parseClaims(authHeader))
          .flatMap(claims -> {
              String userId = claims.get("userId", String.class);
              String role = claims.get("role", String.class);
              String profileId = claims.get("profileId", String.class);
              String organizationId = claims.get("organizationId", String.class);
              ServerHttpRequest mutated = exchange.getRequest().mutate()
                  .header("X-User-Id", userId != null ? userId : "")
                  .header("X-User-Role", role != null ? role : "")
                  .header("X-Profile-Id", profileId != null ? profileId : "")
                  .header("X-Organization-Id", organizationId != null ? organizationId : "")
                  .build();
              log.info("JWT validated: path={} userId={} role={}", path, userId, role);
              return chain.filter(exchange.mutate().request(mutated).build());
          })
          .onErrorResume(e -> {
              log.warn("JWT validation failed: path={} error={} type={}", path,
                       e.getMessage(), e.getClass().getSimpleName());
              exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
              return exchange.getResponse().setComplete();
          });
      ```
- [ ] Remove the now-dead `try/catch` block
- [ ] Keep `parseClaims()` as a private helper — it is called from `Mono.fromCallable` which
      wraps it safely so checked/unchecked exceptions both propagate to `onErrorResume`

### Task 4: Verify acceptance criteria

- [ ] Restart gateway (local or Docker)
- [ ] Send request with no token → expect 401 with log: "Missing or invalid Authorization header"
- [ ] Send request with expired/invalid token → expect 401 with log showing exception type
- [ ] Send request with valid token from Auth-service → expect request forwarded with correct
      `X-User-Id`, `X-User-Role` headers visible in downstream service logs
- [ ] Confirm key fingerprints match in startup logs of Auth-service and Gateway

### Task N: Update documentation

- [ ] Update CLAUDE.md if any new env var conventions were established
- [ ] Move this plan to `docs/plans/completed/`

## Post-Completion

**Manual verification:**
- Run a full auth flow: register → login → call a protected endpoint through the gateway
- Verify token refresh still works (refresh endpoint is in the public path `/api/auth/`)

**External system updates:**
- If deploying to Docker, rebuild gateway image after changes: `docker compose build gateway-service`
