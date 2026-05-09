# TaskMaster Distributed Logging Guide

## Overview

TaskMaster uses **Spring Cloud Sleuth** and **Zipkin** for distributed tracing and comprehensive logging across all microservices. Every request is automatically assigned a unique **trace ID** that flows through all services, making it easy to track issues and understand system behavior.

## Quick Start

### View Logs in Zipkin UI

1. **Open Zipkin UI:** http://localhost:9411
2. **Find traces by service:**
   - Select "Service Name" dropdown
   - Choose service (e.g., "Auth-service", "TaskService")
3. **View trace details:**
   - Click on any trace to see the complete request flow
   - Each service shows as a separate "span" with timing information

### Log Format

All logs automatically include trace ID and span ID:

```
2026-05-09 10:30:45.123 [traceId=abc123def456,spanId=ghi789] [main] INFO com.slavacom.auth_service.service.AuthService - User registered successfully with userId: user-123
```

**Components:**
- `[traceId=...]` — Unique identifier for the entire request across all services
- `[spanId=...]` — Identifier for this specific service's work on the request
- `[main]` — Thread name
- `INFO` — Log level
- `com.slavacom...` — Package/class name
- Message with context

## Understanding Traces in Zipkin

### Trace Timeline View

```
GET /api/tasks (200 OK) — 250ms total
├── Auth-service: validate token (45ms)
├── TaskService: create task (150ms)
│   ├── TaskRepository.save() (60ms)
│   └── TaskHistoryRepository.save() (40ms)
└── OrganizationService: update project (35ms)
```

Each operation shows:
- **Duration** — Execution time (ms)
- **Status** — HTTP status code or success/error
- **Service** — Which service handled it
- **Nested operations** — What it called internally

### Search Traces by Criteria

In Zipkin UI:
1. **By Service:** Select from "Service Name" dropdown
2. **By Span Name:** Select "Span Name" (e.g., "POST /api/auth/login")
3. **By Tags:**
   - Click "Tags" and add filters
   - Example: `http.status_code=500` (find errors)
4. **By Duration:** Set min/max time range
5. **By Error:** Check "Error" checkbox to show only failed requests

### Example Searches

| Goal | How to Find |
|------|-------------|
| Find slow tasks | Service: TaskService, Min Duration: 1000ms |
| Find all login failures | Span: "POST /api/auth/login", Error: checked |
| Find a specific user's activity | Add tag: `userId=user-123` |
| Find all requests to a service | Service: OrganizationService |
| Find errors in last hour | Error: checked, time range: last 1h |

## Log Levels

Each service logs at different levels depending on importance:

### Root Level: `INFO`
Default level for all output except debug packages

### Service-Specific Levels

| Package | Level | What It Logs |
|---------|-------|-------------|
| `com.slavacom.*` | DEBUG | Service method entry/exit, decisions, state changes |
| `org.springframework.cloud.sleuth` | DEBUG | Trace creation, span information, propagation |
| `org.springframework.kafka` | DEBUG | Kafka message publish/consume |
| `org.springframework.web` | WARN | HTTP warnings and errors only |
| `org.springframework.security` | DEBUG | Authentication/authorization details |
| `org.hibernate.SQL` | DEBUG | Database queries (SQL statements) |

### Adjusting Log Levels at Runtime

#### Option 1: Modify `application.yml`

```yaml
logging:
  level:
    com.slavacom.taskservice: DEBUG        # More verbose
    org.springframework.cloud.sleuth: INFO # Less verbose
```

#### Option 2: Environment Variables (Docker)

```bash
docker compose up -d auth-service -e LOGGING_LEVEL_COM_SLAVACOM=INFO
```

#### Option 3: Production Configuration

Use `application-docker.yml` or environment-specific overrides:

```yaml
logging:
  level:
    root: WARN                                  # Only warnings/errors in prod
    com.slavacom: INFO                         # Service info only
    org.springframework.cloud.sleuth: INFO      # Trace info only
```

## Log Files

Logs are written to local files with rolling policy:

| Service | Log File | Retention |
|---------|----------|-----------|
| Auth-service | `logs/auth-service.log` | 30 days, max 100MB per file |
| TaskService | `logs/task-service.log` | 30 days, max 100MB per file |
| All services | `logs/<service>.%date.%index.log` | 3GB total per service |

### View Log Files

```bash
# Tail real-time logs
tail -f logs/task-service.log

# Search logs for specific user
grep "userId=user-123" logs/task-service.log

# Find errors
grep "ERROR" logs/task-service.log

# Show last 100 lines with context
tail -100 logs/task-service.log | grep "traceId=abc123"
```

## Common Logging Patterns

### 1. Tracing a Request Through Services

**Task:** User reports "task creation failed"

**Steps:**
1. Get trace ID from error message or Zipkin
2. Open Zipkin UI
3. Search for that trace ID
4. See all services involved and their execution times
5. Identify which service failed and why

**Example:**
```
Trace ID: abc123def456
├── Auth-service: 45ms ✓
├── TaskService: 150ms ✗ (error: "ProjectId not found")
└── (OrganizationService not called)
```

### 2. Finding Performance Bottlenecks

**Goal:** Requests to `/api/tasks` are slow

1. Open Zipkin UI
2. Select Service: "TaskService"
3. Select Span: "GET /api/tasks"
4. Set Min Duration: 500ms (find slow ones)
5. Click on a slow trace
6. Look at the timeline to see which operation is slow:
   - Database query too slow? → Add index
   - External API call too slow? → Check network
   - Business logic too slow? → Optimize code

### 3. Debugging Distributed Failures

**Problem:** Error in OrganizationService after successful TaskService call

1. Find the trace in Zipkin
2. Check TaskService span:
   - What data did it pass to OrganizationService?
   - Check logs: `log.info("Calling OrganizationService with organizationId=...")`
3. Check OrganizationService span:
   - Did it receive the data correctly?
   - Check logs: `log.info("Processing organizationId=...")`
4. Compare what was sent vs. what was received
5. Look for data transformation issues

## Troubleshooting Logging Issues

### Problem: No Logs Appearing

**Cause 1:** Service not running
- Check: `docker compose ps` (should show all services as "Up")

**Cause 2:** Logs directory missing
- Fix: Create logs directory
  ```bash
  mkdir -p logs
  chmod 755 logs
  ```

**Cause 3:** Log level too high
- Check: application.yml has `logging.level.root: INFO` or lower
- Fix: Lower the root log level

### Problem: Zipkin Shows No Traces

**Cause 1:** Zipkin not running
- Fix: `docker compose up -d zipkin`
- Verify: http://localhost:9411 loads

**Cause 2:** Services can't reach Zipkin
- Check: Services have `spring.zipkin.base-url=http://localhost:9411`
- Fix: Update application.yml

**Cause 3:** Sampling rate is 0%
- Check: `spring.sleuth.sampler.probability=1.0`
- Fix: Ensure it's 1.0 (100%) for development

### Problem: Logs Are Not Structured (No Trace ID)

**Cause:** Logback configuration not loaded
- Fix: Verify `logback-spring.xml` exists in `src/main/resources/`
- Pattern should include: `[%X{traceId:-},%X{spanId:-}]`

### Problem: Sensitive Data in Logs

**Issue:** Passwords or tokens appearing in logs
- Fix: Add checks in logging statements:
  ```kotlin
  logger.info { "User created: email=${user.email}, userId=${user.id}" }
  // Don't do this: logger.info { "User: $user" } // Exposes password!
  ```

## Production Setup

### Environment Variables

```bash
# Zipkin location (use your production Zipkin server)
ZIPKIN_BASE_URL=https://zipkin.prod.example.com

# Sampling rate (reduce in production to save resources)
SLEUTH_SAMPLING_RATE=0.1  # Log 10% of requests

# Log level (less verbose in production)
LOGGING_LEVEL_ROOT=WARN
LOGGING_LEVEL_COM_SLAVACOM=INFO
```

### Docker Compose Example

```yaml
services:
  task-service:
    environment:
      ZIPKIN_BASE_URL: http://zipkin:9411
      SLEUTH_SAMPLING_RATE: 0.1
      LOGGING_LEVEL_ROOT: WARN
```

### Zipkin Retention Policy

Set in Zipkin configuration:
```yaml
zipkin:
  storage:
    type: elasticsearch  # Use persistent storage
    elasticsearch:
      hosts: elasticsearch:9200
      index: zipkin
      retention: 7  # Keep traces for 7 days
```

## Integration with External Systems

### Send Logs to ELK Stack (Elasticsearch, Logstash, Kibana)

Add to `logback-spring.xml`:
```xml
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
  <destination>logstash:5000</destination>
  <encoder class="net.logstash.logback.encoder.LogstashEncoder" />
</appender>
```

### Send Traces to Splunk

Configure in `application.yml`:
```yaml
spring:
  zipkin:
    baseUrl: http://splunk-collector:9411
```

## Best Practices

### ✅ DO

- ✅ Log at entry point with input parameters (except sensitive data)
- ✅ Log at exit point with result
- ✅ Log decision points and state changes
- ✅ Include timing information for expensive operations
- ✅ Use appropriate log levels (INFO for important, DEBUG for detailed)
- ✅ Include context (IDs, timestamps, durations)
- ✅ Check trace ID for related logs across services

### ❌ DON'T

- ❌ Log passwords, tokens, API keys, credit card numbers
- ❌ Log entire objects (use specific fields instead)
- ❌ Use DEBUG level for important production info
- ❌ Create new loggers in every method
- ❌ Log the same information twice in one code path
- ❌ Ignore errors without logging them

## Example: Complete Request Flow with Logging

```
USER REQUEST: POST /api/tasks

1. Auth-service (validates token)
   DEBUG: Validating JWT token
   DEBUG: Token decoded: userId=user-123, role=USER
   INFO: Token validated successfully for userId=user-123

2. TaskService (creates task)
   DEBUG: Creating task: name=Fix bug, projectId=proj-456
   DEBUG: Executing database transaction
   INFO: Task created successfully: taskId=task-789, duration=45ms

3. OrganizationService (updates project stats)
   DEBUG: Fetching project: projectId=proj-456
   INFO: Project stats updated: taskCount=25, duration=30ms

4. Response sent to user
   INFO: POST /api/tasks completed in 250ms with status 201
   
ZIPKIN TRACE:
- Trace ID: abc123def456
- Total Duration: 250ms
- Services involved: 3 (Auth, Task, Organization)
- All operations successful ✓
```

## Support & Further Reading

- **Zipkin Documentation:** https://zipkin.io
- **Spring Cloud Sleuth:** https://spring.io/projects/spring-cloud-sleuth
- **Logback:** http://logback.qos.ch
- **Best Practices:** See CLAUDE.md - Logging Patterns section

---

**Last Updated:** 2026-05-09  
**Created By:** Claude Code  
**For:** TaskMaster Microservices Platform
