# Zipkin & Distributed Logging Integration

## Overview
Enable distributed tracing across all microservices using Spring Cloud Sleuth and Zipkin. Services have configuration in place but are missing critical dependencies, preventing trace propagation and Zipkin integration. This work adds the missing dependencies, implements request/response logging interceptors, and verifies traces flow correctly through all services.

**Problem:** Configuration exists but will fail at runtime—Sleuth and Zipkin dependencies are absent from all services.
**Solution:** Add Spring Cloud dependencies, implement logging interceptor for REST calls, verify integration.
**Benefit:** Full request tracing across all 6 services, centralized trace visualization in Zipkin, easier debugging of distributed issues.

## Context (from discovery)
- **Files involved:** `build.gradle`/`build.gradle.kts` in each service (6 total), `application.yml` in each service
- **Services affected:** Auth-service, UserService, OrganizationService, TaskService, S3CloudeStorage, NotificationService
- **Infrastructure:** Zipkin already running in Docker Compose on port 9411, configured for in-memory storage
- **Current state:** Zipkin/Sleuth config present in all `application.yml` files but dependencies missing
- **Key finding:** LoggingClientHttpRequestInterceptor mentioned in CLAUDE.md not yet implemented

## Development Approach
- **Testing approach:** Manual testing in Docker Compose environment
- Make small, focused changes
- Update one service at a time
- Verify Zipkin receives traces after each service fix
- No automated tests (per user preference—manual Docker testing)

## Progress Tracking
- Mark completed items with `[x]` immediately when done
- Add newly discovered tasks with ➕ prefix
- Document issues/blockers with ⚠️ prefix
- Update plan if implementation deviates from original scope

## Solution Overview
- **Approach:** Add Spring Cloud Sleuth/Zipkin starter to all services, implement REST client logging interceptor
- **Architecture:** Spring Cloud auto-configures Sleuth and Zipkin client; RestTemplate/RestClient calls are intercepted to add trace headers
- **Integration:** Traces sent via HTTP to Zipkin server (configured in application.yml)
- **Verification:** View traces in Zipkin UI (http://localhost:9411) after making inter-service REST calls

## What Goes Where

### Implementation Steps (code changes in this repo)
- Add dependencies to build files
- Create logging interceptor for REST clients
- Verify logging configuration
- Manual testing in Docker

### Post-Completion (external/manual)
- Verify Zipkin UI shows traces
- Monitor logs for trace ID propagation
- Set up dashboard queries in Zipkin if needed

## Implementation Steps

### Task 1: Add Micrometer + Zipkin dependencies to all services

**Files:**
- Modify: `Auth-service/build.gradle`
- Modify: `UserService/build.gradle`
- Modify: `OrganizationService/build.gradle`
- Modify: `TaskService/build.gradle`
- Modify: `S3CloudeStorage/build.gradle.kts`
- Modify: `NotificationService/build.gradle`
- Modify: All `application.yml` files (6 services)

- [x] Added Micrometer Tracing + Zipkin dependencies (Spring Boot 4.0+ uses Micrometer, not Sleuth)
  - `io.micrometer:micrometer-tracing-bridge-brave:1.4.1`
  - `io.zipkin.reporter2:zipkin-reporter-brave:3.4.0`
  - `io.zipkin.brave:brave:5.18.1`
- [x] Updated all 6 service `application.yml` files to use Micrometer config
  - Replaced old Sleuth config (`spring.zipkin.base-url`, `spring.sleuth.sampler`)
  - Added new config (`management.tracing.sampling.probability`, `management.zipkin.tracing.endpoint`)
  - Updated logging level from `org.springframework.cloud.sleuth` to `io.micrometer.tracing`

### Task 2: Create/Register LoggingClientHttpRequestInterceptor for REST client logging

**Files:**
- Modify: `Auth-service/src/main/java/com/slavacom/auth_service/config/RestClientConfig.java`
- Verify: `TaskService/src/main/kotlin/com/slavacom/taskservice/config/LoggingClientHttpRequestInterceptor.kt` (already exists)
- Verify: `S3CloudeStorage/src/main/kotlin/com/slavacom/storage/config/LoggingClientHttpRequestInterceptor.kt` (already exists)

- [x] LoggingClientHttpRequestInterceptor already exists in all services that need it (Auth, Task, S3, Organization, UserService, NotificationService)
- [x] Updated Auth-service RestClientConfig to register the logging interceptor
  - Changed from simple RestClient.builder() to RestClientCustomizer with interceptor
- [x] Added KotlinLogging dependency (io.github.microutils:kotlin-logging-jvm:3.2.0) to Kotlin services that use it
  - TaskService, S3CloudeStorage, NotificationService

### Task 3: Verify logging configuration in application.yml files

**Files:**
- Verify: All 6 service `application.yml` files

- [x] Verified and updated all services with Micrometer Tracing configuration:
  - Added `management.tracing.sampling.probability: ${TRACING_SAMPLING_RATE:1.0}`
  - Added `management.zipkin.tracing.endpoint: ${ZIPKIN_ENDPOINT:http://localhost:9411/api/v2/spans}`
  - Updated logging level from `org.springframework.cloud.sleuth: DEBUG` to `io.micrometer.tracing: DEBUG`
- [x] Removed old Sleuth configuration from all services
  - Deleted `spring.zipkin.base-url`
  - Deleted `spring.sleuth.sampler` and `spring.sleuth.trace-id128`
- [x] Services configured: Auth-service, UserService, OrganizationService, TaskService, S3CloudeStorage, NotificationService
- [ ] Check docker profile (`application-docker.yml`) if needed (optional)

### Task 4: Manual verification in Docker environment

**Next steps for testing (manual—requires Docker environment):**
- [ ] Start Docker Compose stack: `cd All-Compose && docker-compose -f docker-compose.yaml -f docker-compose.db.yml -f docker-compose.kafka.yml up -d`
- [ ] Verify Zipkin is running: visit http://localhost:9411 (should show Zipkin UI)
- [ ] Build all services: `./gradlew build` from each service directory
- [ ] Start at least 2 services that communicate (e.g., Auth-service on 8081 and UserService on 8082)
- [ ] Make a request that triggers inter-service call (e.g., POST /auth/register → calls UserService internally)
- [ ] Check Zipkin UI for trace: http://localhost:9411 should show service names and timeline
- [ ] Check logs for trace ID propagation: should see Micrometer trace IDs in each service log
- [ ] Verify LoggingClientHttpRequestInterceptor logs appear: `POST http://localhost:8082/... completed in XXms with status 200`

### Task 5: Fix any issues discovered during testing

- [ ] ⚠️ If Zipkin shows no traces: verify `ZIPKIN_ENDPOINT` env var or `management.zipkin.tracing.endpoint` in config
- [ ] ⚠️ If logs don't show trace IDs: verify Micrometer dependencies are in classpath (check `gradle dependencies`)
- [ ] ⚠️ If services can't reach Zipkin: verify Docker network `micro-net` is created and running
- [ ] ⚠️ If KotlinLogging not found: verify `io.github.microutils:kotlin-logging-jvm:3.2.0` is in build.gradle
- [ ] Document any fixes applied

### Task 6: Update documentation

- [x] Updated `docs/plans/20260510-zipkin-logging-integration.md` with Micrometer migration details
- [ ] Update `docs/LOGGING.md` with new Micrometer configuration properties (if file exists)
- [ ] Update `CLAUDE.md` if needed to reflect Micrometer instead of Sleuth
- [ ] Move this plan to `docs/plans/completed/` when manual testing is complete

## Post-Completion
*Manual/external verification items — no checkboxes*

**Manual verification:**
- Monitor Zipkin UI for 24 hours to confirm traces are being received consistently
- Verify no performance degradation from 100% trace sampling
- Check disk usage of Zipkin (in-memory mode, won't persist)

**Future optimization:**
- Reduce sampling rate to 0.1 (10%) in production for cost/performance
- Consider switching from in-memory to persistent storage (e.g., Elasticsearch) in production
- Set up Zipkin alerts/dashboards for SLA monitoring
