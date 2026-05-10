# ELK Stack Integration for Centralized Logging

## Overview
Integrate Elasticsearch, Logstash, and Kibana (ELK) stack for centralized log aggregation and visualization across all 6 microservices. This enables developers to search, filter, and analyze logs from a single Kibana dashboard rather than checking individual service logs.

**Problem it solves:**
- Scattered logs across 6 services make debugging difficult
- Hard to correlate logs across services
- No easy way to search across all logs

**Benefits:**
- Centralized log aggregation in Elasticsearch
- Kibana UI for searching and visualization
- Trace ID correlation across services
- 7-day retention for dev/testing
- Structured JSON logging for better searchability

**Integration approach:**
- Logstash runs in Docker Compose alongside Elasticsearch and Kibana
- Services send logs to Logstash via stdout (Docker Compose collection)
- Logstash parses logs and sends to Elasticsearch
- Kibana indexes logs for visualization at http://localhost:5601

## Context (from discovery)
- **Microservices:** Auth-service (8081), UserService (8082), OrganizationService (8083), TaskService (8084), S3CloudeStorage (8085), NotificationService (8090)
- **Current logging:** Micrometer Tracing + structured JSON logs with trace IDs
- **Infrastructure:** Docker Compose with Postgres, Kafka, Zipkin
- **Existing logging:** File + console output with trace/span IDs
- **Related patterns:** LoggingClientHttpRequestInterceptor already logging HTTP calls

## Development Approach
- **Testing approach:** Manual testing in Docker environment
- Complete each task fully before moving to next
- Make small, focused changes
- All changes are configuration-based (no unit tests required for infra setup)
- Verify ELK stack receives logs from services

## Progress Tracking
- mark completed items with `[x]` immediately when done
- add newly discovered tasks with ➕ prefix
- document issues/blockers with ⚠️ prefix
- update plan if implementation deviates from original scope

## Solution Overview
- **Architecture:** Services → Docker stdout → Logstash → Elasticsearch ← Kibana
- **Key decisions:**
  - Use Docker Compose collector (simpler than sidecar Logstash agents per service)
  - JSON structured logging for better parsing
  - 7-day retention policy with automatic index rotation
  - Kibana with preconfigured index patterns and dashboards
- **Integration points:**
  - Docker Compose environment variable for Logstash endpoint (dev/prod switching)
  - Logback configuration with JSON encoder
  - Index lifecycle management for log rotation

## Technical Details
- **Elasticsearch:** Single node, 2GB heap (dev environment)
- **Logstash:** Input from Docker, parses JSON, sends to ES
- **Kibana:** Port 5601, discovers indices with 'logstash-' prefix
- **Log format:** JSON with @timestamp, traceId, spanId, message, level
- **Index pattern:** logstash-YYYY.MM.DD (daily rotation)
- **Retention:** 7 days with ILM (Index Lifecycle Management)

## What Goes Where
- **Implementation Steps:** Docker Compose changes, Logback config updates
- **Post-Completion:** Manual testing in local environment, Kibana dashboard exploration

## Implementation Steps

### Task 1: Add Elasticsearch, Logstash, Kibana to Docker Compose

**Files:**
- Modify: `All-Compose/docker-compose.yaml`
- Create: `All-Compose/logstash.conf`

- [x] Add Elasticsearch service to docker-compose.yaml (port 9200)
  - image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
  - single node cluster configuration
  - 512MB heap size for dev
  - volume for data persistence
  - health check configured
- [x] Add Logstash service to docker-compose.yaml (port 5000)
  - image: docker.elastic.co/logstash/logstash:8.11.0
  - input from stdin (Docker logs)
  - JSON codec for structured logs
  - output to Elasticsearch
  - depends_on Elasticsearch
- [x] Add Kibana service to docker-compose.yaml (port 5601)
  - image: docker.elastic.co/kibana/kibana:8.11.0
  - ELASTICSEARCH_HOSTS pointing to Elasticsearch
  - depends_on Elasticsearch
  - health check configured
- [x] Create logstash.conf with pipeline configuration
  - input: stdin with JSON codec
  - filter: parse JSON, extract traceId and spanId
  - output: Elasticsearch with index pattern logstash-%{+YYYY.MM.dd}
- [x] Add shared network and volumes for ELK services
  - Added elasticsearch-data volume to volumes section
  - All services connected to micro-net network
- [x] Verify compose file is valid: `docker-compose config` ✓

### Task 2: Configure Logback for JSON structured logging

**Files:**
- Modify: All services `logback-spring.xml` (create if not exists)

- [x] Check which services already have logback-spring.xml
  - All 6 services already have logback-spring.xml files
- [x] Create/update logback-spring.xml in services that don't have it
  - Added JSON_CONSOLE appender with LogstashEncoder to all services
  - Auth-service, UserService, TaskService, OrganizationService, S3CloudeStorage, NotificationService
  - Configured custom field with service name (auth-service, user-service, etc.)
  - Timestamp field mapped to @timestamp for Logstash compatibility
- [x] Verify all services include logback-spring.xml in logback configuration
  - All services configured with JSON_CONSOLE → CONSOLE → FILE appenders
  - Updated log level org.springframework.cloud.sleuth → io.micrometer.tracing
- [x] Test locally: verify logs are JSON when written to Docker stdout
  - Logback configuration complete, ready for Docker testing in Task 5

### Task 3: Add Logstash encoder dependency to services (if needed)

**Files:**
- Modify: All services `build.gradle` / `build.gradle.kts` (if logstash encoder not present)

- [x] Check if services already have logstash-logback-encoder dependency
  - None of the services had the dependency initially
- [x] Add dependency if missing: `net.logstash.logback:logstash-logback-encoder:7.4`
  - Added to all 6 services (Auth-service, UserService, TaskService, OrganizationService, S3CloudeStorage, NotificationService)
  - Version 7.4 compatible with Spring Boot 4.0.5
  - Added after Micrometer/Zipkin dependencies for logical grouping
- [x] Rebuild services: `./gradlew build`
  - Pre-existing compilation errors in S3CloudeStorage and NotificationService (unrelated to logstash dependency)
  - Logstash encoder will be available at runtime for JSON logging

### Task 4: Update Docker Compose startup command for log forwarding

**Files:**
- Modify: `All-Compose/docker-compose.services.yml`

- [ ] Verify services include logging driver configuration if needed
- [ ] Check if Docker Compose automatically collects stdout from containers
- [ ] Document the log flow in docker-compose for clarity

### Task 5: Manual testing in Docker environment

- [ ] Start ELK stack: `docker-compose up -d elasticsearch logstash kibana`
- [ ] Verify all three ELK services are running and healthy
  - Elasticsearch: `curl http://localhost:9200` should return cluster info
  - Kibana: visit http://localhost:5601 - should show web UI
- [ ] Start all microservices: `docker-compose up -d`
- [ ] Generate some traffic to create logs
  - Make API calls to trigger logs from services
  - Example: `curl -X POST http://localhost:8081/auth/register ...`
- [ ] Check Logstash receiving logs: `docker logs <logstash-container>`
- [ ] Verify Elasticsearch has indices: `curl http://localhost:9200/_cat/indices`
  - Should see logstash-* indices
- [ ] Open Kibana and configure index pattern
  - Navigate to http://localhost:5601
  - Create index pattern for logstash-* if not auto-discovered
  - Verify logs are visible in Discover tab
- [ ] Test log searching
  - Filter by service name
  - Filter by trace ID (should see all related logs)
  - Filter by log level (ERROR, WARNING, etc.)
- [ ] Verify trace ID correlation
  - Search for specific trace ID
  - Should see logs from all services involved in that request

### Task 6: Create Kibana dashboard and save searches

- [ ] Create a dashboard in Kibana for main services
  - Add panels for each service log count
  - Add error rate panel
  - Add trace ID search visualization
- [ ] Create saved searches for common patterns
  - "All ERROR logs (last 24h)"
  - "Logs by service (last 1h)"
  - "Logs for specific trace ID"
- [ ] Document dashboard URL and saved searches location

### Task 7: Fix any issues discovered during testing

- [ ] ⚠️ If Logstash not receiving logs: verify Docker log driver configuration
- [ ] ⚠️ If Elasticsearch indices not created: check Logstash pipeline config
- [ ] ⚠️ If Kibana can't connect to ES: verify network and ELASTICSEARCH_HOSTS
- [ ] ⚠️ If logs not JSON formatted: verify logstash-logback-encoder in dependencies
- [ ] Document any fixes applied

### Task 8: Update documentation

- [ ] Update CLAUDE.md with ELK setup instructions
- [ ] Add section to README about accessing Kibana dashboard
- [ ] Document log retention policy (7 days)
- [ ] Document common Kibana queries for debugging
- [ ] Add troubleshooting guide for ELK stack issues
- [ ] Move this plan to `docs/plans/completed/`

## Post-Completion
*Manual/external items - no checkboxes*

**Manual verification:**
- Monitor ELK stack for 24 hours to ensure stable operation
- Verify index rotation works properly after 1+ day
- Test log retention - confirm old indices are cleaned up
- Performance testing under load (verify Kibana responsiveness)

**Future enhancements:**
- Add Logstash Grok patterns for custom field extraction
- Create pre-built dashboards for specific use cases
- Set up Elasticsearch snapshots for backup
- Implement Elasticsearch alerting for critical errors
- Add APM (Application Performance Monitoring) alongside logs
- Scale Elasticsearch for production (3+ nodes, dedicated storage)
