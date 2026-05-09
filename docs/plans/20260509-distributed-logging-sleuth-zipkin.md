# Distributed Logging with Spring Cloud Sleuth + Zipkin

## Overview
Implement distributed tracing and comprehensive logging across all 6 TaskMaster services using Spring Cloud Sleuth and Zipkin. This enables:
- **Automatic trace ID propagation** across service boundaries
- **Visual trace timeline** showing which services handled each request
- **Performance metrics** with execution times per service
- **Request flow tracking** to understand service interactions
- **Human-readable logging** with structured data for debugging

All requests will be traceable through the entire system, making it easy to debug issues and monitor performance.

## Context (from discovery)
- **Services:** Auth-service, UserService, OrganizationService, TaskService, S3CloudeStorage, NotificationService
- **Languages:** Mix of Java (Auth, User, Organization) and Kotlin (Task, S3, Notification)
- **Spring Boot version:** 4.0.5 (all services)
- **Current state:** Minimal logging; no distributed tracing
- **Target:** Centralized logging with human-readable format

## Development Approach
- **Testing approach:** Regular (code first, then tests)
- Complete each service fully before moving to the next
- Make small, focused changes
- All tests must pass before next task
- Update plan if scope changes during implementation

## Solution Overview

### Architecture
```
┌─────────────────────────────────────────────┐
│         Zipkin Server (Standalone)          │  Port 9411
│  Stores & visualizes traces and spans       │
└────────────────────┬────────────────────────┘
                     │
      ┌──────────────┼──────────────┐
      │              │              │
 ┌────▼────┐   ┌────▼────┐   ┌────▼────┐
 │Auth Svc  │   │Task Svc  │   │  Org Svc │  Each service:
 │ Sleuth   │   │ Sleuth   │   │ Sleuth   │  - Auto-adds trace ID
 │ Logback  │   │ Logback  │   │ Logback  │  - Sends spans to Zipkin
 └──────────┘   └──────────┘   └──────────┘  - Logs locally + centralized
      
Flow: Request → Service A → (Trace ID auto-propagated) → Service B
      → Spans recorded → Sent to Zipkin → Visualized in UI
```

### Key Components
1. **Spring Cloud Sleuth** — Adds trace ID and span ID to logs, propagates across service calls
2. **Zipkin** — Collects and visualizes traces
3. **Logback** — Logs with structured format (human-readable)
4. **RestClient integration** — Sleuth automatically adds trace headers to inter-service calls

## Technical Details

### Zipkin Integration
- Zipkin server: `http://localhost:9411` (Docker container)
- Sampling rate: 100% (all requests traced for development)
- Async span reporting to minimize latency

### Log Format
```
[traceid=abc123,spanid=def456] 2026-05-09 10:30:45.123 [Auth-service,main] INFO  com.slavacom.auth_service.controller.AuthController - POST /api/auth/login - User registration complete for userId=user123
```

### Configuration
- `spring.application.name=<service-name>` — Identifies service in Zipkin
- `spring.zipkin.base-url=http://localhost:9411` — Zipkin server endpoint
- `spring.sleuth.sampler.probability=1.0` — 100% sampling (dev mode)
- `logging.pattern.console` — Custom pattern with trace ID

## What Goes Where
- **Implementation Steps:** Tasks to complete in this codebase
- **Post-Completion:** External setup and manual verification

## Implementation Steps

### Task 1: Set up Zipkin Server (Docker)

**Files:**
- Modify: `All-Compose/docker-compose.yaml`

- [ ] Add Zipkin service to docker-compose.yaml with port 9411
- [ ] Ensure it connects to same network as other services
- [ ] Start Zipkin: `docker compose up -d zipkin`
- [ ] Verify Zipkin UI is accessible at http://localhost:9411
- [ ] Run docker services test - verify Zipkin container is healthy

### Task 2: Add Spring Cloud Sleuth + Zipkin dependencies to all services

**Files:**
- Modify: `Auth-service/build.gradle`
- Modify: `UserService/build.gradle`
- Modify: `OrganizationService/build.gradle`
- Modify: `TaskService/build.gradle`
- Modify: `S3CloudeStorage/build.gradle.kts`
- Modify: `NotificationService/build.gradle`

- [ ] Add `org.springframework.cloud:spring-cloud-starter-sleuth:4.1.5` to all services
- [ ] Add `org.springframework.cloud:spring-cloud-starter-zipkin:4.1.5` to all services
- [ ] Refresh gradle build system for all services
- [ ] Verify dependencies are resolved (no conflicts with Spring Boot 4.0.5)
- [ ] Run `./gradlew build` on each service to confirm compilation

### Task 3: Configure Sleuth + Zipkin in application.yml for all services

**Files:**
- Modify: `Auth-service/src/main/resources/application.yml`
- Modify: `UserService/src/main/resources/application.yml`
- Modify: `OrganizationService/src/main/resources/application.yml`
- Modify: `TaskService/src/main/resources/application-local.yml`
- Modify: `S3CloudeStorage/src/main/resources/application.yml`
- Modify: `NotificationService/src/main/resources/application.yml`

- [ ] Add spring.application.name property to each service
- [ ] Add spring.zipkin.base-url=http://localhost:9411 to all services
- [ ] Add spring.zipkin.sender.type=web for async reporting
- [ ] Add spring.sleuth.sampler.probability=1.0 for 100% sampling (dev)
- [ ] Configure spring.sleuth.trace-id128=true for longer trace IDs
- [ ] Add logging.level.root=INFO and org.springframework.cloud.sleuth=DEBUG
- [ ] Also add to docker profiles (application-docker.yml) with docker service hostname
- [ ] Run each service locally and verify logs contain [traceid=xxx,spanid=yyy]

### Task 4: Configure custom logging format with Logback

**Files:**
- Modify: `Auth-service/src/main/resources/logback-spring.xml` (create if not exists)
- Modify: `UserService/src/main/resources/logback-spring.xml` (create if not exists)
- Modify: `OrganizationService/src/main/resources/logback-spring.xml` (create if not exists)
- Modify: `TaskService/src/main/resources/logback-spring.xml` (create if not exists)
- Modify: `S3CloudeStorage/src/main/resources/logback-spring.xml` (create if not exists)
- Modify: `NotificationService/src/main/resources/logback-spring.xml` (create if not exists)

- [ ] Create logback-spring.xml for each service with pattern: `%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{traceId:-},%X{spanId:-}] [%thread] %-5level %logger{36} - %msg%n`
- [ ] Add console appender for human-readable output
- [ ] Add file appender for /logs directory (rolling files)
- [ ] Configure Sleuth pattern to include trace ID and span ID
- [ ] Set appropriate log levels for different packages
- [ ] Verify each service outputs trace ID in logs: `[traceId=abc123,spanId=def456]`

### Task 5: Add request/response logging interceptor to each service

**Files:**
- Create: `Auth-service/src/main/java/com/slavacom/auth_service/config/LoggingInterceptor.kt`
- Create: `UserService/src/main/java/com/slavacom/userservice/config/LoggingInterceptor.kt`
- Create: `OrganizationService/src/main/java/com/slavacom/organizationservice/config/LoggingInterceptor.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/config/LoggingInterceptor.kt`
- Create: `S3CloudeStorage/src/main/kotlin/com/slavacom/s3cloudestorage/config/LoggingInterceptor.kt`
- Create: `NotificationService/src/main/kotlin/com/slavacom/notificationservice/config/LoggingInterceptor.kt`

- [ ] Create RestClientCustomizer bean for each service to add request logging interceptor
- [ ] Log method, path, query params (no sensitive data)
- [ ] Log response status and execution time
- [ ] Ensure interceptor doesn't break existing RestClient functionality
- [ ] Test RestClient calls still work with logging interceptor
- [ ] Verify logs show: `POST /api/auth/login completed in 145ms with status 200`

### Task 6: Add logging to service methods and business logic

**Files:**
- Modify: `Auth-service/src/main/java/com/slavacom/auth_service/service/AuthService.kt` (and similar for each service's main service classes)
- Modify: `UserService/src/main/java/com/slavacom/userservice/service/UserServiceImpl.kt`
- Modify: `OrganizationService/src/main/java/com/slavacom/organizationservice/service/OrganizationService.kt`
- Modify: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/TaskService.kt`
- Modify: `S3CloudeStorage/src/main/kotlin/com/slavacom/s3cloudestorage/service/S3StorageService.kt`
- Modify: `NotificationService/src/main/kotlin/com/slavacom/notificationservice/service/NotificationService.kt`

- [ ] Add logger instances to each service class
- [ ] Log entry point with input parameters: `log.info("Creating user with email={}", email)`
- [ ] Log key decisions and state changes: `log.info("User registered successfully, userId={}", userId)`
- [ ] Log error conditions with context: `log.error("Failed to create task, projectId={}, error={}", projectId, e.message, e)`
- [ ] Include execution time for expensive operations: `log.info("Task query executed in {}ms for projectId={}", duration, projectId)`
- [ ] Avoid logging sensitive data (passwords, tokens, SSNs)
- [ ] Run tests for each service to verify logging output

### Task 7: Create comprehensive logging guide and documentation

**Files:**
- Create: `docs/LOGGING.md`
- Modify: `docs/CLAUDE.md`

- [ ] Document how to access Zipkin UI: http://localhost:9411
- [ ] Explain how to search for traces by service name, span name, duration, error
- [ ] Provide examples of common debugging scenarios (e.g., "trace a slow request")
- [ ] Document log levels and how to adjust them at runtime
- [ ] Add environment variables for production Zipkin URL configuration
- [ ] Include troubleshooting section for common issues
- [ ] Update CLAUDE.md with logging patterns and conventions for future development

### Task 8: End-to-end testing and verification

**Files:**
- (No files created; verification only)

- [ ] Start all services with docker compose including Zipkin
- [ ] Make a request from frontend to any endpoint (e.g., GET /api/tasks)
- [ ] Follow the trace ID from request logs
- [ ] Open Zipkin UI (http://localhost:9411) and search for the trace
- [ ] Verify trace shows all services involved: Auth → Task → Organization (if applicable)
- [ ] Verify timing information is accurate for each service
- [ ] Verify logs in all services show the same trace ID
- [ ] Test error scenario: make request that fails, verify error is traceable
- [ ] Verify RestClient calls between services show in Zipkin as separate spans
- [ ] Test with concurrent requests: verify each has unique trace ID

### Task 9: Verify acceptance criteria
- [ ] All 6 services have Sleuth + Zipkin configured
- [ ] Zipkin server running and accessible at http://localhost:9411
- [ ] All services output logs with [traceId,spanId] format
- [ ] Inter-service calls show as nested spans in Zipkin
- [ ] Request/response logging works without sensitive data leaks
- [ ] Business logic logging provides good debugging context
- [ ] Performance metrics (execution times) are captured
- [ ] No compilation errors or test failures in any service
- [ ] Run full test suite: `cd TaskService && ./gradlew test && cd ../Auth-service && ./gradlew test` (etc. for all)

### Task 10: Update documentation and cleanup

**Files:**
- Modify: `CLAUDE.md` - add logging patterns section
- Modify: `README.md` - add logging setup instructions
- Create: `docs/LOGGING.md` - comprehensive logging guide
- Create: `.github/LOGGING_TROUBLESHOOTING.md` - debug common issues

- [ ] Add logging patterns and best practices to CLAUDE.md
- [ ] Update README with "View Logs" section pointing to Zipkin
- [ ] Create troubleshooting guide for developers
- [ ] Document how to configure Zipkin URL for different environments (dev, staging, prod)
- [ ] Move plan to `docs/plans/completed/20260509-distributed-logging-sleuth-zipkin.md`

## Post-Completion
*Items requiring manual intervention or external systems*

**Environment Setup:**
- For **production deployment**, update `spring.zipkin.base-url` to point to production Zipkin server (not localhost)
- Configure sampling rate: reduce `spring.sleuth.sampler.probability` to 0.1 (10% sampling) for production to reduce Zipkin load
- Set up Zipkin retention policy (default: 7 days) based on your needs
- Consider using `spring.zipkin.discovery-client-enabled=true` for service discovery if using Eureka/Consul

**Monitoring Setup:**
- Consider alerting on Zipkin metrics (high error rates, slow spans)
- Set up log aggregation beyond Zipkin (ELK, Splunk) for long-term archival
- Configure Zipkin UI authentication for production

**Team Training:**
- Demonstrate to team how to use Zipkin for debugging
- Share troubleshooting examples and common patterns
- Update on-boarding docs for new developers

---

## Estimated Effort
- **Task 1 (Zipkin setup):** 15 minutes
- **Tasks 2-3 (Dependencies + config):** 30 minutes (repeated for 6 services)
- **Task 4 (Logback):** 45 minutes
- **Task 5 (Interceptors):** 1 hour
- **Task 6 (Logging in services):** 2 hours
- **Task 7 (Documentation):** 1 hour
- **Task 8-10 (Testing + cleanup):** 1 hour
- **Total:** ~6-7 hours

