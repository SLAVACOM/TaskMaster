# TaskMaster API Endpoints Reference

**Created:** 2026-05-09  
**Services:** 6 microservices  
**Total Endpoints:** 91  
**Framework:** Spring Boot 4.0.5  
**Architecture:** JWT-authenticated REST + Kafka events

---

## Table of Contents

1. [Quick Start](#quick-start)
2. [Authentication](#authentication)
3. [Service Overview](#service-overview)
4. [Endpoints by Service](#endpoints-by-service)
5. [Common Patterns](#common-patterns)
6. [Error Handling](#error-handling)
7. [Examples](#examples)

---

## Quick Start

### Service Ports & Base URLs

```
Auth Service:          http://localhost:8081/api/auth
User Service:          http://localhost:8082/api/users, /api/profiles
Organization Service:  http://localhost:8083/api/organizations, /api/projects
Task Service:          http://localhost:8084/api/tasks, /api/projects, /api/sprints
S3 Storage:            http://localhost:8085/s3
Notification Service:  http://localhost:8090/notifications
```

### Environment Variables (per CLAUDE.md)

Each service requires:
- `DB_URL` — PostgreSQL connection
- `DB_USERNAME`, `DB_PASSWORD` — database credentials
- `JWT_SECRET` — **shared** across all services for token validation
- `KAFKA_BOOTSTRAP_SERVERS` — Kafka bootstrap address
- `SERVER_PORT` — service port

---

## Authentication

### JWT Flow

1. **Register** → POST `/api/auth/register` (returns `accessToken` + `refreshToken`)
2. **Login** → POST `/api/auth/login` (returns tokens)
3. **Use access token** → Include in all requests: `Authorization: Bearer <accessToken>`
4. **Token expires?** → POST `/api/auth/refresh` with `refreshToken` to get new `accessToken`
5. **Validate token** → GET `/api/auth/validate` (internal, for gateway/middleware)

### Token Details

- **Access Token:** 15-minute TTL
- **Refresh Token:** 7-day TTL
- **Algorithm:** JWT (symmetric, uses shared `JWT_SECRET`)
- **Required in all endpoints** except `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`

### Headers

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

---

## Service Overview

### Auth Service (Port 8081)
**Purpose:** JWT token management, user authentication, password management  
**Key Entities:** User credentials, roles, password history  
**Integration:** Calls User Service via REST to create/retrieve user records  

### User Service (Port 8082)
**Purpose:** User profiles, profile management, user lookup  
**Key Entities:** User, Profile  
**Kafka Consumer:** Subscribes to `user-created` topic from Auth Service  
**Consumer Group:** `user-service-group`

### Organization Service (Port 8083)
**Purpose:** Organizations, projects, team management, sprint planning, kanban boards  
**Key Entities:** Organization, Project, Employee, Sprint, Goal, Kanban Column, Tag, Status, Invitation  
**Relationships:**
```
Organization
  ├── Employees
  ├── Projects
  │   ├── Tasks (via Task Service)
  │   ├── Employees
  │   ├── Sprints
  │   ├── Goals
  │   ├── Kanban Columns
  │   ├── Task Statuses
  │   └── Tags
  └── Tags
```

### Task Service (Port 8084)
**Purpose:** Task lifecycle, assignment, workflow transitions, history, comments  
**Key Entities:** Task, TaskHistory, Comment  
**Scoping:** Tasks belong to Projects (via `projectId` in URL path)  
**Event Flow:** Publishes task events to Kafka (NotificationService subscribes)

### S3 Cloud Storage Service (Port 8085)
**Purpose:** Presigned URLs for file upload/download, Redis caching  
**Patterns:**
- Single file: `/s3/upload-url` (POST), `/s3/download-url` (POST)
- Batch files: `/s3/upload-urls` (POST), `/s3/download-urls` (POST)

### Notification Service (Port 8090)
**Purpose:** Email + Telegram notifications  
**Kafka Consumer:** Subscribes to task events, user events  
**Requirements:** SMTP credentials, Telegram bot token

---

## Endpoints by Service

### Auth Service

#### Register User

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe"
}

# Response (201 Created)
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 900
}
```

#### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}

# Response (200 OK)
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 900
}
```

#### Refresh Token

```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

# Response (200 OK)
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 900
}
```

#### Validate Token

```http
GET /api/auth/validate
Authorization: Bearer <token>

# Response (200 OK)
{
  "valid": true,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com"
}
```

#### Change Password

```http
PUT /api/auth/users/{userId}/password
Authorization: Bearer <token>
Content-Type: application/json

{
  "oldPassword": "currentPassword123",
  "newPassword": "newPassword456"
}

# Response (204 No Content)
```

#### Update User Role

```http
PUT /api/auth/users/{userId}/role
Authorization: Bearer <token>
Content-Type: application/json

{
  "role": "ADMIN"  // or "USER", "MANAGER", etc.
}

# Response (200 OK)
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "role": "ADMIN"
}
```

#### Delete User

```http
DELETE /api/auth/users/{userId}
Authorization: Bearer <token>

# Response (204 No Content)
```

---

### User Service

#### Check Registration Eligibility

```http
POST /api/users/can-register
Content-Type: application/json

{
  "email": "user@example.com"
}

# Response (200 OK)
{
  "canRegister": true
}
```

#### Register User Profile

```http
POST /api/users/register
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "profilePicture": "https://..."
}

# Response (201 Created)
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Get User Info

```http
GET /api/users/{userId}
Authorization: Bearer <token>

# Response (200 OK)
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "createdAt": "2026-05-01T10:00:00Z"
}
```

#### Check User Exists

```http
GET /api/users/{userId}/exists
Authorization: Bearer <token>

# Response (200 OK)
{
  "exists": true
}
```

#### Find User by Login

```http
POST /api/users/findUser/login/{login}
Authorization: Bearer <token>

# Response (200 OK)
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "login": "john.doe",
  "email": "john@example.com"
}
```

#### Find User by Email

```http
POST /api/users/findUser/email/{email}
Authorization: Bearer <token>

# Response (200 OK)
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "firstName": "John"
}
```

#### Get Extended User Info

```http
GET /api/users/{userId}/extended
Authorization: Bearer <token>

# Response (200 OK)
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "activeProfile": {
    "profileId": "650e8400-e29b-41d4-a716-446655440001",
    "profileName": "Work Profile"
  },
  "lastLogin": "2026-05-08T15:30:00Z"
}
```

#### Get Current User Extended Info

```http
GET /api/users/me/extended
Authorization: Bearer <token>

# Response (200 OK)
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "activeProfile": {
    "profileId": "650e8400-e29b-41d4-a716-446655440001",
    "profileName": "Work Profile"
  }
}
```

#### Update Last Active Profile

```http
PUT /api/users/{userId}/last-profile/{profileId}
Authorization: Bearer <token>

# Response (200 OK)
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "activeProfileId": "650e8400-e29b-41d4-a716-446655440001"
}
```

#### Create Profile

```http
POST /api/profiles
Authorization: Bearer <token>
Content-Type: application/json

{
  "profileName": "Work Profile",
  "description": "Main work profile",
  "settings": {
    "timezone": "UTC",
    "notifications": true
  }
}

# Response (201 Created)
{
  "profileId": "650e8400-e29b-41d4-a716-446655440001",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "profileName": "Work Profile"
}
```

#### Get All Profiles

```http
GET /api/profiles
Authorization: Bearer <token>

# Response (200 OK)
[
  {
    "profileId": "650e8400-e29b-41d4-a716-446655440001",
    "profileName": "Work Profile",
    "isActive": true
  },
  {
    "profileId": "750e8400-e29b-41d4-a716-446655440002",
    "profileName": "Personal Profile",
    "isActive": false
  }
]
```

#### Get User Profiles

```http
GET /api/profiles/user/{userId}
Authorization: Bearer <token>

# Response (200 OK)
[...]  # same as Get All Profiles
```

#### Update Profile

```http
PUT /api/profiles/{profileId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "profileName": "Updated Work Profile",
  "settings": {
    "timezone": "EST"
  }
}

# Response (200 OK)
{
  "profileId": "650e8400-e29b-41d4-a716-446655440001",
  "profileName": "Updated Work Profile"
}
```

#### Activate Profile

```http
PUT /api/profiles/{profileId}/activate
Authorization: Bearer <token>

# Response (200 OK)
{
  "profileId": "650e8400-e29b-41d4-a716-446655440001",
  "isActive": true
}
```

---

### Organization Service

#### List Organizations

```http
GET /api/organizations
Authorization: Bearer <token>

# Query Parameters:
# - page=0 (default)
# - size=20 (default)

# Response (200 OK)
{
  "content": [
    {
      "organizationId": "100e8400-e29b-41d4-a716-446655440000",
      "name": "Acme Corp",
      "description": "Leading task management company",
      "createdAt": "2026-01-01T00:00:00Z"
    }
  ],
  "totalElements": 1,
  "page": 0,
  "size": 20
}
```

#### Get Organization by ID

```http
GET /api/organizations/{orgId}
Authorization: Bearer <token>

# Response (200 OK)
{
  "organizationId": "100e8400-e29b-41d4-a716-446655440000",
  "name": "Acme Corp",
  "description": "Leading task management company",
  "createdAt": "2026-01-01T00:00:00Z"
}
```

#### Create Organization

```http
POST /api/organizations
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "New Company",
  "description": "My new organization"
}

# Response (201 Created)
{
  "organizationId": "200e8400-e29b-41d4-a716-446655440001",
  "name": "New Company"
}
```

#### Update Organization

```http
PUT /api/organizations/{orgId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Updated Company Name",
  "description": "Updated description"
}

# Response (200 OK)
{
  "organizationId": "100e8400-e29b-41d4-a716-446655440000",
  "name": "Updated Company Name"
}
```

#### Deactivate Organization

```http
DELETE /api/organizations/{orgId}
Authorization: Bearer <token>

# Response (204 No Content)
```

#### Get User Organization Info

```http
GET /api/organizations/user/{userId}/info
Authorization: Bearer <token>

# Response (200 OK)
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "organizations": [
    {
      "organizationId": "100e8400-e29b-41d4-a716-446655440000",
      "name": "Acme Corp",
      "role": "ADMIN"  // or "MEMBER", "MANAGER"
    }
  ]
}
```

---

#### Employees

```http
# List Employees
GET /api/organizations/{orgId}/employees
Authorization: Bearer <token>

# Response (200 OK)
[
  {
    "employeeId": "300e8400-e29b-41d4-a716-446655440002",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john@example.com",
    "role": "DEVELOPER",
    "joinedAt": "2026-02-01T10:00:00Z"
  }
]

# Add Employee
POST /api/organizations/{orgId}/employees
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "role": "DEVELOPER"
}

# Response (201 Created)
{
  "employeeId": "300e8400-e29b-41d4-a716-446655440002",
  "organizationId": "100e8400-e29b-41d4-a716-446655440000",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}

# Update Employee
PUT /api/organizations/{orgId}/employees/{employeeId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "role": "SENIOR_DEVELOPER"
}

# Response (200 OK)

# Remove Employee
DELETE /api/organizations/{orgId}/employees/{employeeId}
Authorization: Bearer <token>

# Response (204 No Content)
```

---

### Projects (in Organization)

```http
# List Projects by Organization
GET /api/organizations/{orgId}/projects
Authorization: Bearer <token>

# Response (200 OK)
[
  {
    "projectId": "400e8400-e29b-41d4-a716-446655440003",
    "name": "Mobile App v2.0",
    "description": "Redesigned mobile app",
    "status": "ACTIVE"
  }
]

# Create Project
POST /api/organizations/{orgId}/projects
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Mobile App v2.0",
  "description": "Redesigned mobile app"
}

# Response (201 Created)
{
  "projectId": "400e8400-e29b-41d4-a716-446655440003",
  "organizationId": "100e8400-e29b-41d4-a716-446655440000",
  "name": "Mobile App v2.0"
}

# Get Project
GET /api/projects/{projectId}
Authorization: Bearer <token>

# Response (200 OK)
{
  "projectId": "400e8400-e29b-41d4-a716-446655440003",
  "organizationId": "100e8400-e29b-41d4-a716-446655440000",
  "name": "Mobile App v2.0",
  "status": "ACTIVE"
}

# Update Project
PUT /api/projects/{projectId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Updated Project Name",
  "description": "Updated description"
}

# Response (200 OK)

# Deactivate Project
DELETE /api/projects/{projectId}
Authorization: Bearer <token>

# Response (204 No Content)
```

---

#### Project Employees

```http
# List Project Employees
GET /api/projects/{projectId}/employees
Authorization: Bearer <token>

# Add to Project
POST /api/projects/{projectId}/employees
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "role": "DEVELOPER"
}

# Update Project Employee
PUT /api/projects/{projectId}/employees/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "role": "SENIOR_DEVELOPER"
}

# Remove from Project
DELETE /api/projects/{projectId}/employees/{id}
Authorization: Bearer <token>

# Response (204 No Content)
```

---

#### Project Tags

```http
# List Tags
GET /api/projects/{projectId}/tags
Authorization: Bearer <token>

# Create Tag
POST /api/projects/{projectId}/tags
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "bug",
  "color": "#FF0000"
}

# Update Tag
PUT /api/projects/{projectId}/tags/{tagId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "critical-bug",
  "color": "#FF6600"
}

# Delete Tag
DELETE /api/projects/{projectId}/tags/{tagId}
Authorization: Bearer <token>

# Response (204 No Content)
```

---

#### Project Statuses

```http
# List Statuses
GET /api/projects/{projectId}/statuses
Authorization: Bearer <token>

# Response (200 OK)
[
  {
    "statusId": "500e8400-e29b-41d4-a716-446655440004",
    "name": "TODO",
    "category": "BACKLOG",
    "sequence": 1
  },
  {
    "statusId": "510e8400-e29b-41d4-a716-446655440005",
    "name": "IN_PROGRESS",
    "category": "IN_PROGRESS",
    "sequence": 2
  }
]

# Create Status
POST /api/projects/{projectId}/statuses
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "DONE",
  "category": "DONE",
  "sequence": 3
}

# Update Status
PUT /api/projects/{projectId}/statuses/{statusId}
Authorization: Bearer <token>

# Delete Status
DELETE /api/projects/{projectId}/statuses/{statusId}
Authorization: Bearer <token>

# Response (204 No Content)
```

---

#### Kanban Columns

```http
# List Columns
GET /api/projects/{projectId}/kanban/columns
Authorization: Bearer <token>

# Response (200 OK)
[
  {
    "columnId": "600e8400-e29b-41d4-a716-446655440006",
    "title": "TODO",
    "order": 0,
    "taskCount": 5
  }
]

# Create Column
POST /api/projects/{projectId}/kanban/columns
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "IN_PROGRESS"
}

# Update Column
PUT /api/projects/{projectId}/kanban/columns/{columnId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Updated Title"
}

# Reorder Columns
PUT /api/projects/{projectId}/kanban/columns/reorder
Authorization: Bearer <token>
Content-Type: application/json

{
  "columnOrder": ["600e8400-e29b-41d4-a716-446655440006", "610e8400-e29b-41d4-a716-446655440007"]
}

# Deactivate Column
DELETE /api/projects/{projectId}/kanban/columns/{columnId}
Authorization: Bearer <token>

# Response (204 No Content)

# Get Positions (task positions in columns)
GET /api/projects/{projectId}/kanban/positions
Authorization: Bearer <token>

# Update Positions
PUT /api/projects/{projectId}/kanban/positions
Authorization: Bearer <token>
Content-Type: application/json

{
  "positions": [
    {
      "taskId": "700e8400-e29b-41d4-a716-446655440008",
      "columnId": "600e8400-e29b-41d4-a716-446655440006",
      "order": 0
    }
  ]
}
```

---

#### Goals

```http
# List Goals
GET /api/projects/{projectId}/goals
Authorization: Bearer <token>

# Create Goal
POST /api/projects/{projectId}/goals
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Complete mobile redesign",
  "description": "Redesign mobile UI/UX",
  "targetDate": "2026-08-31"
}

# Update Goal
PUT /api/projects/{projectId}/goals/{goalId}
Authorization: Bearer <token>

# Delete Goal
DELETE /api/projects/{projectId}/goals/{goalId}
Authorization: Bearer <token>

# Response (204 No Content)
```

---

#### Sprints

```http
# List Sprints
GET /api/projects/{projectId}/sprints
Authorization: Bearer <token>

# Create Sprint
POST /api/projects/{projectId}/sprints
Authorization: Bearer <token>
Content-Type: application/json

{
  "sprintName": "Sprint 1",
  "startDate": "2026-05-12",
  "endDate": "2026-05-26",
  "description": "First sprint"
}

# Update Sprint
PUT /api/projects/{projectId}/sprints/{sprintId}
Authorization: Bearer <token>

# Activate Sprint
PUT /api/projects/{projectId}/sprints/{sprintId}/activate
Authorization: Bearer <token>

# Response (200 OK)

# Complete Sprint
PUT /api/projects/{projectId}/sprints/{sprintId}/complete
Authorization: Bearer <token>

# Get Sprint Dashboard
GET /api/sprints/{sprintId}/dashboard
Authorization: Bearer <token>

# Response (200 OK)
{
  "sprintId": "800e8400-e29b-41d4-a716-446655440009",
  "sprintName": "Sprint 1",
  "taskStats": {
    "total": 10,
    "completed": 5,
    "inProgress": 3,
    "todo": 2
  },
  "velocity": 25,
  "burndown": [...]
}
```

---

#### Invitations

```http
# Send Invitation
POST /api/organizations/{orgId}/invitations
Authorization: Bearer <token>
Content-Type: application/json

{
  "email": "newuser@example.com",
  "role": "MEMBER"
}

# Response (201 Created)
{
  "invitationId": "900e8400-e29b-41d4-a716-446655440010",
  "email": "newuser@example.com",
  "status": "PENDING"
}

# List Invitations
GET /api/organizations/{orgId}/invitations
Authorization: Bearer <token>

# Accept Invitation
PUT /api/invitations/{id}/accept
Authorization: Bearer <token>

# Response (200 OK)
{
  "invitationId": "900e8400-e29b-41d4-a716-446655440010",
  "status": "ACCEPTED"
}

# Decline Invitation
PUT /api/invitations/{id}/decline
Authorization: Bearer <token>

# Response (200 OK)
{
  "invitationId": "900e8400-e29b-41d4-a716-446655440010",
  "status": "DECLINED"
}
```

---

#### Organization Tags

```http
# Similar to Project Tags
GET /api/organizations/{orgId}/tags
POST /api/organizations/{orgId}/tags
PUT /api/organizations/{orgId}/tags/{tagId}
DELETE /api/organizations/{orgId}/tags/{tagId}
```

---

#### Project History

```http
# List Project Changes
GET /api/projects/{projectId}/history
Authorization: Bearer <token>

# Response (200 OK)
[
  {
    "historyId": "a00e8400-e29b-41d4-a716-446655440011",
    "projectId": "400e8400-e29b-41d4-a716-446655440003",
    "action": "CREATED",
    "changedBy": "550e8400-e29b-41d4-a716-446655440000",
    "changedAt": "2026-05-01T10:00:00Z"
  }
]
```

---

### Task Service

#### Task CRUD

```http
# Create Task
POST /api/tasks
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Fix login bug",
  "description": "Users can't login with special characters",
  "priority": "HIGH",
  "dueDate": "2026-05-15",
  "assigneeId": "550e8400-e29b-41d4-a716-446655440000"
}

# Response (201 Created)
{
  "taskId": "b00e8400-e29b-41d4-a716-446655440012",
  "title": "Fix login bug",
  "status": "TODO",
  "priority": "HIGH"
}

# Get Task
GET /api/tasks/{id}
Authorization: Bearer <token>

# Get All Tasks
GET /api/tasks
Authorization: Bearer <token>

# Query Parameters:
# - page=0, size=20 (pagination)
# - status=TODO,IN_PROGRESS (filter)
# - priority=HIGH (filter)

# Search Tasks
GET /api/tasks/search
Authorization: Bearer <token>

# Query Parameters:
# - query=login (free text search)
# - projectId=... (filter)

# Text Search
GET /api/tasks/search-text
Authorization: Bearer <token>

# Query Parameters:
# - q=bug (full-text search)

# Update Task
PUT /api/tasks/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Updated title",
  "description": "Updated description",
  "priority": "CRITICAL"
}

# Response (200 OK)

# Delete Task
DELETE /api/tasks/{id}
Authorization: Bearer <token>

# Response (204 No Content)
```

---

#### Task Assignment & Workflow

```http
# Get My Tasks
GET /api/tasks/my
Authorization: Bearer <token>

# Response (200 OK)
[
  {
    "taskId": "b00e8400-e29b-41d4-a716-446655440012",
    "title": "Fix login bug",
    "assignedTo": "550e8400-e29b-41d4-a716-446655440000"
  }
]

# Assign Task
POST /api/tasks/{taskId}/assign
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}

# Response (200 OK)

# Unassign Task
POST /api/tasks/{taskId}/unassign
Authorization: Bearer <token>

# Response (200 OK)

# Add Watcher
POST /api/tasks/{taskId}/watchers
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}

# Remove Watcher
DELETE /api/tasks/{taskId}/watchers/{watcherId}
Authorization: Bearer <token>

# Response (204 No Content)

# Transition Status
POST /api/tasks/{taskId}/transition
Authorization: Bearer <token>
Content-Type: application/json

{
  "newStatus": "IN_PROGRESS"
}

# Response (200 OK)
{
  "taskId": "b00e8400-e29b-41d4-a716-446655440012",
  "status": "IN_PROGRESS"
}

# Add Comment
POST /api/tasks/{taskId}/comments
Authorization: Bearer <token>
Content-Type: application/json

{
  "text": "Found root cause in auth module"
}

# Response (201 Created)
{
  "commentId": "c00e8400-e29b-41d4-a716-446655440013",
  "taskId": "b00e8400-e29b-41d4-a716-446655440012",
  "text": "Found root cause in auth module",
  "createdAt": "2026-05-09T14:30:00Z"
}
```

---

#### Project-Scoped Tasks

```http
# Create Task in Project
POST /api/projects/{projectId}/tasks
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Implement login UI",
  "priority": "MEDIUM"
}

# List Tasks in Project
GET /api/projects/{projectId}/tasks
Authorization: Bearer <token>

# Get Task in Project
GET /api/projects/{projectId}/tasks/{taskId}
Authorization: Bearer <token>

# Update Task in Project
PUT /api/projects/{projectId}/tasks/{taskId}
Authorization: Bearer <token>

# Delete Task in Project
DELETE /api/projects/{projectId}/tasks/{taskId}
Authorization: Bearer <token>

# Get Project Dashboard
GET /api/projects/{projectId}/tasks/dashboard
Authorization: Bearer <token>

# Response (200 OK)
{
  "projectId": "400e8400-e29b-41d4-a716-446655440003",
  "totalTasks": 20,
  "tasksByStatus": {
    "TODO": 8,
    "IN_PROGRESS": 7,
    "DONE": 5
  },
  "tasksByPriority": {
    "CRITICAL": 2,
    "HIGH": 5,
    "MEDIUM": 8,
    "LOW": 5
  },
  "overdueTasks": 2,
  "completionRate": 25
}
```

---

#### Task History

```http
# Get Task History
GET /api/tasks/{taskId}/history
Authorization: Bearer <token>

# Response (200 OK)
[
  {
    "historyId": "d00e8400-e29b-41d4-a716-446655440014",
    "taskId": "b00e8400-e29b-41d4-a716-446655440012",
    "action": "CREATED",
    "changedBy": "550e8400-e29b-41d4-a716-446655440000",
    "details": {
      "title": "Fix login bug"
    },
    "changedAt": "2026-05-09T10:00:00Z"
  }
]

# Get History Entry
GET /api/tasks/history/{historyId}
Authorization: Bearer <token>

# Get User's Task History
GET /api/tasks/history/by-user/{userId}
Authorization: Bearer <token>
```

---

### S3 Cloud Storage Service

#### File Upload URLs

```http
# Single File Upload
POST /s3/upload-url
Authorization: Bearer <token>
Content-Type: application/json

{
  "fileName": "avatar.jpg",
  "fileSize": 102400,
  "contentType": "image/jpeg"
}

# Response (200 OK)
{
  "uploadUrl": "https://s3.amazonaws.com/bucket/path?X-Amz-Signature=...",
  "fileUrl": "https://s3.amazonaws.com/bucket/path/avatar.jpg"
}

# Batch File Upload
POST /s3/upload-urls
Authorization: Bearer <token>
Content-Type: application/json

{
  "files": [
    {
      "fileName": "file1.pdf",
      "fileSize": 512000,
      "contentType": "application/pdf"
    },
    {
      "fileName": "file2.jpg",
      "fileSize": 1024000,
      "contentType": "image/jpeg"
    }
  ]
}

# Response (200 OK)
{
  "uploadUrls": [
    {
      "fileName": "file1.pdf",
      "uploadUrl": "https://...",
      "fileUrl": "https://..."
    }
  ]
}
```

#### File Download URLs

```http
# Single File Download
POST /s3/download-url
Authorization: Bearer <token>
Content-Type: application/json

{
  "fileUrl": "https://s3.amazonaws.com/bucket/path/document.pdf"
}

# Response (200 OK)
{
  "downloadUrl": "https://s3.amazonaws.com/bucket/path?X-Amz-Signature=...",
  "expiresIn": 3600
}

# Batch File Downloads
POST /s3/download-urls
Authorization: Bearer <token>
Content-Type: application/json

{
  "fileUrls": [
    "https://s3.amazonaws.com/bucket/file1.pdf",
    "https://s3.amazonaws.com/bucket/file2.jpg"
  ]
}

# Response (200 OK)
{
  "downloadUrls": [
    {
      "fileUrl": "https://...",
      "downloadUrl": "https://..."
    }
  ]
}
```

---

### Notification Service

#### Send Notification

```http
POST /notifications
Authorization: Bearer <token>
Content-Type: application/json

{
  "recipientId": "550e8400-e29b-41d4-a716-446655440000",
  "type": "TASK_ASSIGNED",  // or "TASK_COMMENTED", "TASK_COMPLETED", etc.
  "title": "Task Assigned",
  "message": "You've been assigned to 'Fix login bug'",
  "channel": "EMAIL",  // or "TELEGRAM"
  "metadata": {
    "taskId": "b00e8400-e29b-41d4-a716-446655440012",
    "priority": "HIGH"
  }
}

# Response (200 OK)
{
  "notificationId": "e00e8400-e29b-41d4-a716-446655440015",
  "recipientId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "SENT"
}
```

**Note:** Notifications are typically triggered automatically by Kafka events from Auth, User, and Task services.

---

## Common Patterns

### Pagination

List endpoints support pagination via query parameters:

```
GET /api/organizations?page=0&size=20&sort=name,asc
```

**Response structure:**
```json
{
  "content": [...],
  "totalElements": 150,
  "totalPages": 8,
  "page": 0,
  "size": 20,
  "hasNext": true,
  "hasPrevious": false
}
```

### Soft Deletion

Endpoints with `DELETE` operations typically perform **soft deletes** (mark as inactive), not hard deletes:

```
DELETE /api/organizations/{id}  → sets `isActive: false`
```

Soft-deleted resources may still be queryable with filters:

```
GET /api/organizations?includeInactive=false  (default)
GET /api/organizations?includeInactive=true
```

### UUIDs

All resource IDs are UUID v4:
```
550e8400-e29b-41d4-a716-446655440000
```

### Timestamps

All timestamps are ISO 8601 format (UTC):
```
2026-05-09T14:30:00Z
```

### Status/State Enums

**Task Status:** `TODO`, `IN_PROGRESS`, `REVIEW`, `DONE`, `BLOCKED`  
**Task Priority:** `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`  
**Employee Role:** `OWNER`, `ADMIN`, `MANAGER`, `DEVELOPER`, `VIEWER`  
**Invitation Status:** `PENDING`, `ACCEPTED`, `DECLINED`, `EXPIRED`

---

## Error Handling

### Standard Error Response

```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "statusCode": 401,
  "timestamp": "2026-05-09T14:30:00Z"
}
```

### Common HTTP Status Codes

| Code | Meaning | Example |
|---|---|---|
| 200 | OK | Successful GET, PUT |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Malformed JSON, missing required field |
| 401 | Unauthorized | Missing/invalid JWT token |
| 403 | Forbidden | User lacks permission |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource, invalid state transition |
| 500 | Server Error | Internal service error |

---

## Examples

### Complete User Registration & Login Flow

```javascript
// 1. Register with Auth Service
const registerResponse = await fetch('http://localhost:8081/api/auth/register', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'john@example.com',
    password: 'SecurePass123!',
    firstName: 'John',
    lastName: 'Doe'
  })
});

const { accessToken, refreshToken, userId } = await registerResponse.json();
localStorage.setItem('accessToken', accessToken);
localStorage.setItem('refreshToken', refreshToken);

// 2. Complete user profile (User Service)
const profileResponse = await fetch('http://localhost:8082/api/users/register', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`
  },
  body: JSON.stringify({
    firstName: 'John',
    lastName: 'Doe',
    profilePicture: 'https://example.com/avatar.jpg'
  })
});

// 3. Validate token (periodic check)
const validateResponse = await fetch('http://localhost:8081/api/auth/validate', {
  method: 'GET',
  headers: { 'Authorization': `Bearer ${accessToken}` }
});

// 4. Refresh token when expired
if (!validateResponse.ok) {
  const refreshResponse = await fetch('http://localhost:8081/api/auth/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken })
  });

  const { accessToken: newAccessToken } = await refreshResponse.json();
  localStorage.setItem('accessToken', newAccessToken);
}
```

### Complete Task Workflow

```javascript
// 1. Create task in project
const createResponse = await fetch('http://localhost:8084/api/projects/400e8400-e29b-41d4-a716-446655440003/tasks', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`
  },
  body: JSON.stringify({
    title: 'Implement user authentication',
    description: 'Add JWT-based auth to all services',
    priority: 'CRITICAL',
    dueDate: '2026-05-20'
  })
});

const { taskId } = await createResponse.json();

// 2. Assign task
await fetch(`http://localhost:8084/api/tasks/${taskId}/assign`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`
  },
  body: JSON.stringify({ userId: '550e8400-e29b-41d4-a716-446655440000' })
});

// 3. Add watchers
await fetch(`http://localhost:8084/api/tasks/${taskId}/watchers`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`
  },
  body: JSON.stringify({ userId: '560e8400-e29b-41d4-a716-446655440001' })
});

// 4. Add comment
await fetch(`http://localhost:8084/api/tasks/${taskId}/comments`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`
  },
  body: JSON.stringify({ text: 'Starting implementation' })
});

// 5. Update status
await fetch(`http://localhost:8084/api/tasks/${taskId}/transition`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`
  },
  body: JSON.stringify({ newStatus: 'IN_PROGRESS' })
});

// 6. Get project dashboard
const dashboardResponse = await fetch('http://localhost:8084/api/projects/400e8400-e29b-41d4-a716-446655440003/tasks/dashboard', {
  method: 'GET',
  headers: { 'Authorization': `Bearer ${accessToken}` }
});

const dashboard = await dashboardResponse.json();
// { totalTasks: 25, tasksByStatus: { TODO: 8, IN_PROGRESS: 10, DONE: 7 }, completionRate: 28 }
```

---

## Quick Reference by Use Case

### For Frontend Auth
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/auth/validate`

### For User Profiles
- `POST /api/users/register` (complete profile after auth)
- `GET /api/users/me/extended`
- `POST /api/profiles`
- `PUT /api/profiles/{profileId}/activate`

### For Organization Setup
- `POST /api/organizations`
- `POST /api/organizations/{orgId}/invitations`
- `PUT /api/invitations/{id}/accept`
- `POST /api/organizations/{orgId}/employees`

### For Project Management
- `POST /api/organizations/{orgId}/projects`
- `POST /api/projects/{projectId}/sprints`
- `PUT /api/projects/{projectId}/sprints/{sprintId}/activate`
- `GET /api/projects/{projectId}/tasks/dashboard`

### For Task Management
- `POST /api/projects/{projectId}/tasks`
- `POST /api/tasks/{taskId}/assign`
- `POST /api/tasks/{taskId}/transition`
- `POST /api/tasks/{taskId}/comments`
- `GET /api/tasks/my`

### For File Handling
- `POST /s3/upload-url` (get presigned upload URL)
- `POST /s3/download-url` (get presigned download URL)

---

**Last Updated:** 2026-05-09  
**Status:** Complete API Reference  
**Maintainer:** Frontend Team
