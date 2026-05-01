# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Context Navigation
1. ALWAYS query the knowledge graph first
2. Only read code raw files if the graph does not have the necessary information or if I explicitly ask you to read a file.
3. Use graphify-out/wiki/index.md

## Project Overview

TaskMaster is a microservices-based task management system. Services are independent Gradle projects loosely coupled via REST and Kafka.

## Services

| Service | Language | Port | Role |
|---|---|---|---|
| `Auth-service` | Java 21 | 8081 | JWT auth, registration, token refresh |
| `UserService` | Java 25 | 8082 | User profiles, Kafka consumer for user events |
| `OrganizationService` | Java 21 | 8083 | Organizations, employee management |
| `TaskService` | Kotlin 21 | 8084 | Task CRUD, history, dependencies |
| `S3CloudeStorage` | Kotlin 21 | 8085 | Presigned S3 URLs, Redis caching |
| `NotificationService` | Kotlin 21 | 8090 | Email + Telegram notifications via Kafka |

## Build Commands

Each service is a standalone Gradle project. Run commands from the service directory:

```bash
./gradlew build           # compile + test
./gradlew test            # tests only
./gradlew bootRun         # run locally
./gradlew bootJar         # build fat JAR
```

To target a service from the repo root (if root settings.gradle includes it):

```bash
./gradlew :Auth-service:build
./gradlew :Auth-service:test
```

## Infrastructure (Docker Compose)

All compose files are in `All-Compose/`. Start the full infrastructure stack:

```bash
cd All-Compose
docker compose -f docker-compose.yaml -f docker-compose.db.yml -f docker-compose.kafka.yml up -d
```

To include services:

```bash
docker compose -f docker-compose.yaml -f docker-compose.db.yml -f docker-compose.kafka.yml -f docker-compose.services.yml up -d
```

**Key ports**: Postgres DBs on 5432–5436, Redis on 6379, Kafka on 9092, Kafka UI on 8000.

Each service has its own database: `auth_db`, `user_db`, `task_db`, `event_db`, `organization_db`.

## Architecture

### Authentication flow
Auth-service issues JWT access tokens (15 min) and refresh tokens (7 days). The shared `JWT_SECRET` env var must be identical across all services that validate tokens. Auth-service calls UserService via REST (`RestClient`) to create/retrieve user records.

### Inter-service REST
Services call each other via `RestClient` beans configured in `RestClientConfig`. Base URLs come from environment variables (e.g., `USER_SERVICE_URL`).

### Kafka event flow
Auth-service produces to topic `user-created` after registration. UserService and NotificationService consume from this topic. Consumer groups: `notification-service-group`, `user-service-group`. Messages are JSON with type headers.

### Database
All services use PostgreSQL with JPA/Hibernate (`ddl-auto: update`). UUID primary keys everywhere. MapStruct handles entity↔DTO mapping. Lombok is used extensively.

### Profiles
Each service has `application.yml` (local dev) and `application-docker.yml` (docker). Activate docker profile with `SPRING_PROFILES_ACTIVE=docker`.

## Environment Variables

Each service reads a `.env` file. Common variables:

```
DB_URL=jdbc:postgresql://localhost:5432/<service_db>
DB_USERNAME=postgres
DB_PASSWORD=postgres
SERVER_PORT=<port>
JWT_SECRET=<shared secret>
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

NotificationService additionally needs `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, and `TELEGRAM_BOT_TOKEN`.

S3CloudeStorage needs S3 endpoint, credentials, bucket name, and Redis connection details.

## Key Conventions

- DTOs are separate from entities; MapStruct mappers live in a `mapper` package.
- `StringListJsonConverter` / `UuidListJsonConverter` handle JSON-serialized list columns.
- Kotlin services use `build.gradle.kts`; Java services use `build.gradle`.
- Spring Boot version: 4.0.x across all services. Java target: 21 (even for Java 25 toolchain services, due to Kotlin compatibility).