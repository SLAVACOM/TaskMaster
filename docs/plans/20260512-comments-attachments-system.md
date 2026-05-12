# Comments and Attachments System for Projects, Organizations, and Tasks

## Overview

Implement a polymorphic comment and attachment system supporting projects, organizations, and tasks. Users can:
- Add comments to any entity (with optional nested replies/threads)
- Attach file references (URLs/paths) to comments
- View comment history and edit/delete comments

**Architecture:**
- Polymorphic inheritance: Base `Comment` entity with subclasses `TaskComment`, `ProjectComment`, `OrganizationComment`
- Nested comment support via `parentCommentId` field (self-referencing)
- Attachments stored as separate entities with file URLs/paths (references only, no binary data)
- Timestamps: `createdAt`, `updatedAt`
- Author tracking via `createdBy` (userId)

## Context

**Files to create/modify:**
- Comment entities: `Comment.kt`, `TaskComment.kt`, `ProjectComment.kt`, `OrganizationComment.kt`
- Attachment entity: `Attachment.kt`
- Repositories: `CommentRepository`, `TaskCommentRepository`, `ProjectCommentRepository`, `OrganizationCommentRepository`, `AttachmentRepository`
- DTOs: `CreateCommentRequest.kt`, `CommentResponse.kt`, `AttachmentResponse.kt`
- Services: `CommentService.kt`, `AttachmentService.kt`
- Controllers: `CommentController.kt`
- Database migrations: Liquibase changesets for new tables

**Related patterns:**
- Spring Data JPA with inheritance strategy (JOINED)
- Polymorphic queries
- Self-referencing foreign keys (for nested comments)
- MapStruct DTOs

## Development Approach

- **testing approach:** Manual testing only
- Complete each task fully before moving to next
- Make small, focused changes
- Maintain backward compatibility
- Update plan if scope changes

## Solution Overview

**Architecture:**
1. **Comment Inheritance Hierarchy:**
   - Base `Comment` entity (abstract, JPA inheritance)
   - Subclasses: `TaskComment`, `ProjectComment`, `OrganizationComment`
   - Each subclass references its parent entity via ID field

2. **Threading Support:**
   - `Comment.parentCommentId` field (nullable)
   - Self-referencing relationship
   - Can query child comments by parentCommentId

3. **Attachments:**
   - Separate `Attachment` entity (owns file reference)
   - Links to `Comment` via foreign key
   - Stores: filename, file URL/path, size (optional), mime type (optional)

4. **REST API:**
   - `POST /api/tasks/{taskId}/comments` — add comment to task
   - `POST /api/projects/{projectId}/comments` — add comment to project
   - `POST /api/organizations/{orgId}/comments` — add comment to organization
   - `GET /api/comments/{commentId}` — get comment with replies
   - `POST /api/comments/{commentId}/replies` — add nested reply
   - `POST /api/comments/{commentId}/attachments` — add attachment to comment
   - `PUT /api/comments/{commentId}` — edit comment (only by author)
   - `DELETE /api/comments/{commentId}` — delete comment (only by author)

## Technical Details

**Comment Table Structure (Base):**
- `id` (UUID, PK)
- `dtype` (discriminator column for inheritance)
- `content` (TEXT)
- `created_by` (UUID)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)
- `parent_comment_id` (UUID, nullable, FK to comment)

**Task Comment Table:**
- `task_id` (UUID, FK)

**Project Comment Table:**
- `project_id` (UUID, FK)

**Organization Comment Table:**
- `organization_id` (UUID, FK)

**Attachment Table:**
- `id` (UUID, PK)
- `comment_id` (UUID, FK to comment)
- `file_name` (VARCHAR)
- `file_url` (TEXT) — URL or S3 presigned URL
- `file_size` (BIGINT, optional)
- `mime_type` (VARCHAR, optional)
- `created_at` (TIMESTAMP)

## What Goes Where

**Implementation Steps:** Create entities, repositories, services, controllers, DTOs, and migrations
**Post-Completion:** Manual testing, verify comment threading and attachments work

## Implementation Steps

### Task 1: Create base Comment entity and inheritance hierarchy

**Files:**
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/entity/Comment.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/entity/TaskComment.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/entity/ProjectComment.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/entity/OrganizationComment.kt`

- [x] Create abstract base `Comment` entity with:
  - [x] `@Entity @Inheritance(strategy = InheritanceType.JOINED)`
  - [x] `@DiscriminatorColumn(name = "dtype")`
  - [x] Fields: id (UUID, PK), content (TEXT), createdBy (UUID), createdAt, updatedAt, parentCommentId (UUID, nullable)
- [x] Create `TaskComment` extends Comment with taskId field
- [x] Create `ProjectComment` extends Comment with projectId field
- [x] Create `OrganizationComment` extends Comment with organizationId field

### Task 2: Create Attachment entity

**Files:**
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/entity/Attachment.kt`

- [x] Create `Attachment` entity with:
  - [x] `id` (UUID, PK)
  - [x] `commentId` (UUID, FK to Comment)
  - [x] `fileName` (VARCHAR, not null)
  - [x] `fileUrl` (TEXT, not null) — stores S3 URL or file path
  - [x] `fileSize` (BIGINT, nullable)
  - [x] `mimeType` (VARCHAR, nullable)
  - [x] `createdAt` (TIMESTAMP)

### Task 3: Create Liquibase migrations for new tables

**Files:**
- Modify: `TaskService/src/main/resources/db/changelog/initial.xml.yaml`

- [x] Add changeSet for `comment` table (base table for inheritance)
- [x] Add changeSet for `task_comment` table
- [x] Add changeSet for `project_comment` table
- [x] Add changeSet for `organization_comment` table
- [x] Add changeSet for `attachment` table with FK to comment
- [x] Add indexes on foreign key columns (task_id, project_id, organization_id, comment_id, parent_comment_id)
- [x] Code compiles successfully

### Task 4: Create DTOs for requests and responses

**Files:**
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/CreateCommentRequest.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/CommentResponse.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/AttachmentResponse.kt`

- [x] Create `CreateCommentRequest` with: content (String, not blank), parentCommentId (UUID, optional)
- [x] Create `CommentResponse` with: id, content, createdBy, createdAt, updatedAt, parentCommentId, replies (list of child comments), attachments (list)
- [x] Create `AttachmentResponse` with: id, fileName, fileUrl, fileSize, mimeType, createdAt

### Task 5: Create repositories for polymorphic queries

**Files:**
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/repository/CommentRepository.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/repository/TaskCommentRepository.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/repository/ProjectCommentRepository.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/repository/OrganizationCommentRepository.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/repository/AttachmentRepository.kt`

- [x] Create `CommentRepository` extending `JpaRepository<Comment, UUID>`
- [x] Create `TaskCommentRepository` with queries for task-specific comments
- [x] Create `ProjectCommentRepository` with queries for project-specific comments
- [x] Create `OrganizationCommentRepository` with queries for org-specific comments
- [x] Create `AttachmentRepository` with query by commentId

### Task 6: Create CommentService with business logic

**Files:**
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/CommentService.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/AttachmentService.kt`

- [x] Create `CommentService` with methods for all comment operations:
  - [x] `addTaskComment()`, `addProjectComment()`, `addOrganizationComment()`
  - [x] `addReply()` — polymorphic reply creation
  - [x] `getCommentWithReplies()` — recursive reply retrieval
  - [x] `getTaskComments()`, `getProjectComments()`, `getOrganizationComments()`
  - [x] `editComment()` — with author authorization check
  - [x] `deleteComment()` — with author authorization and cascade delete
- [x] Create `AttachmentService` with attachment operations
- [x] Authorization checks implemented (only author can edit/delete)

### Task 7: Create CommentController with REST endpoints

**Files:**
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/CommentController.kt`

- [x] Create all 12 REST endpoints:
  - [x] `POST /api/tasks/{taskId}/comments` — add comment to task
  - [x] `POST /api/projects/{projectId}/comments` — add comment to project
  - [x] `POST /api/organizations/{orgId}/comments` — add comment to organization
  - [x] `GET /api/comments/{commentId}` — get comment with replies tree
  - [x] `POST /api/comments/{commentId}/replies` — add nested reply
  - [x] `POST /api/comments/{commentId}/attachments` — add attachment
  - [x] `GET /api/tasks/{taskId}/comments` — list top-level comments for task
  - [x] `GET /api/projects/{projectId}/comments` — list top-level comments for project
  - [x] `GET /api/organizations/{orgId}/comments` — list top-level comments for org
  - [x] `PUT /api/comments/{commentId}` — edit comment (auth check)
  - [x] `DELETE /api/comments/{commentId}` — delete comment (auth check)
  - [x] `DELETE /api/attachments/{attachmentId}` — delete attachment
- [x] All endpoints require X-User-Id header
- [x] Correct HTTP status codes (201, 204, 403, 404)

### Task 8: Create MapStruct mappers

**Files:**
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/mapper/CommentMapper.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/mapper/AttachmentMapper.kt`

- [x] Create `AttachmentMapper` for simple mapping
- [x] Create `CommentMapper` with recursive reply population
  - [x] `toResponse()` — basic mapping without replies
  - [x] `toResponseWithReplies()` — recursive mapping with nested replies and attachments
- [x] Code compiles successfully

### Task 9: Manual testing of comment system

- [ ] Start infrastructure and TaskService
- [ ] Test Scenario 1: Add top-level comment to task
  - POST `/api/tasks/{taskId}/comments` with content
  - Verify stored with createdBy, timestamps
- [ ] Test Scenario 2: Add nested reply to comment
  - POST `/api/comments/{commentId}/replies` 
  - Verify parentCommentId set correctly
- [ ] Test Scenario 3: Get comment with replies
  - GET `/api/comments/{commentId}`
  - Verify replies tree populated
- [ ] Test Scenario 4: List all top-level comments
  - GET `/api/tasks/{taskId}/comments`
  - Verify only parentCommentId IS NULL returned
- [ ] Test Scenario 5: Edit comment (only by author)
  - PUT `/api/comments/{commentId}` as author — success
  - PUT `/api/comments/{commentId}` as non-author — 403 Forbidden
- [ ] Test Scenario 6: Add attachment to comment
  - POST `/api/comments/{commentId}/attachments` with file URL
  - Verify stored with fileName, fileUrl
- [ ] Test Scenario 7: Delete comment and cascade
  - DELETE `/api/comments/{commentId}`
  - Verify attachments also deleted (if ON DELETE CASCADE set)

### Task 10: Verify all requirements implemented

- [ ] Verify polymorphic comment inheritance working
- [ ] Verify nested replies/threading works
- [ ] Verify attachments store file references (URLs)
- [ ] Verify authorization checks (edit/delete only by author)
- [ ] Verify all REST endpoints functioning
- [ ] Verify mappers recursively populate nested structure

## Post-Completion

**Verification:**
- Comments and attachments visible via REST API
- Nested replies structure works correctly
- Authorization enforced (edit/delete only by creator)
- Timestamps accurate (createdAt, updatedAt)
- Cascade delete works (comment deletion cascades to attachments)

**Future enhancements (not in scope):**
- Comment reactions/likes
- Rich text editor support (markdown/HTML)
- File upload endpoint (currently just URL references)
- Comment mentions/notifications
- Edit history
