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

- [ ] Create abstract base `Comment` entity with:
  - `@Entity @Inheritance(strategy = InheritanceType.JOINED)`
  - `@DiscriminatorColumn(name = "dtype")`
  - Fields: id (UUID, PK), content (TEXT), createdBy (UUID), createdAt, updatedAt, parentCommentId (UUID, nullable)
- [ ] Create `TaskComment` extends Comment with taskId field
- [ ] Create `ProjectComment` extends Comment with projectId field
- [ ] Create `OrganizationComment` extends Comment with organizationId field
- [ ] Add `@OneToMany` self-referencing relationship on Comment for child comments (optional, for convenience)

### Task 2: Create Attachment entity

**Files:**
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/entity/Attachment.kt`

- [ ] Create `Attachment` entity with:
  - `id` (UUID, PK)
  - `commentId` (UUID, FK to Comment)
  - `fileName` (VARCHAR, not null)
  - `fileUrl` (TEXT, not null) — stores S3 URL or file path
  - `fileSize` (BIGINT, nullable)
  - `mimeType` (VARCHAR, nullable)
  - `createdAt` (TIMESTAMP)
- [ ] Add foreign key constraint on commentId

### Task 3: Create Liquibase migrations for new tables

**Files:**
- Modify: `TaskService/src/main/resources/db/changelog/initial.xml.yaml`

- [ ] Add changeSet for `comment` table (base table for inheritance)
- [ ] Add changeSet for `task_comment` table
- [ ] Add changeSet for `project_comment` table
- [ ] Add changeSet for `organization_comment` table
- [ ] Add changeSet for `attachment` table with FK to comment
- [ ] Add indexes on foreign key columns (task_id, project_id, organization_id, comment_id, parent_comment_id)

### Task 4: Create DTOs for requests and responses

**Files:**
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/CreateCommentRequest.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/CommentResponse.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/dto/AttachmentResponse.kt`

- [ ] Create `CreateCommentRequest` with: content (String, not blank), parentCommentId (UUID, optional)
- [ ] Create `CommentResponse` with: id, content, createdBy, createdAt, updatedAt, parentCommentId, replies (list of child comments), attachments (list)
- [ ] Create `AttachmentResponse` with: id, fileName, fileUrl, fileSize, mimeType, createdAt

### Task 5: Create repositories for polymorphic queries

**Files:**
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/repository/CommentRepository.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/repository/TaskCommentRepository.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/repository/ProjectCommentRepository.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/repository/OrganizationCommentRepository.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/repository/AttachmentRepository.kt`

- [ ] Create `CommentRepository` extending `JpaRepository<Comment, UUID>`
  - Query method: `fun findByParentCommentIdOrderByCreatedAtDesc(parentId: UUID): List<Comment>`
- [ ] Create `TaskCommentRepository` extending `JpaRepository<TaskComment, UUID>`
  - Query method: `fun findByTaskIdOrderByCreatedAtDesc(taskId: UUID): List<TaskComment>`
  - Query method: `fun findByTaskIdAndParentCommentIdIsNull(taskId: UUID): List<TaskComment>`
- [ ] Create `ProjectCommentRepository` extending `JpaRepository<ProjectComment, UUID>`
  - Query method: `fun findByProjectIdOrderByCreatedAtDesc(projectId: UUID): List<ProjectComment>`
  - Query method: `fun findByProjectIdAndParentCommentIdIsNull(projectId: UUID): List<ProjectComment>`
- [ ] Create `OrganizationCommentRepository` extending `JpaRepository<OrganizationComment, UUID>`
  - Query method: `fun findByOrganizationIdOrderByCreatedAtDesc(orgId: UUID): List<OrganizationComment>`
  - Query method: `fun findByOrganizationIdAndParentCommentIdIsNull(orgId: UUID): List<OrganizationComment>`
- [ ] Create `AttachmentRepository` extending `JpaRepository<Attachment, UUID>`
  - Query method: `fun findByCommentId(commentId: UUID): List<Attachment>`

### Task 6: Create CommentService with business logic

**Files:**
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/CommentService.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/service/AttachmentService.kt`

- [ ] Create `CommentService` with methods:
  - `fun addTaskComment(taskId: UUID, request: CreateCommentRequest, createdBy: UUID): CommentResponse`
  - `fun addProjectComment(projectId: UUID, request: CreateCommentRequest, createdBy: UUID): CommentResponse`
  - `fun addOrganizationComment(orgId: UUID, request: CreateCommentRequest, createdBy: UUID): CommentResponse`
  - `fun addReply(parentCommentId: UUID, request: CreateCommentRequest, createdBy: UUID): CommentResponse`
  - `fun getCommentWithReplies(commentId: UUID): CommentResponse`
  - `fun getTaskComments(taskId: UUID): List<CommentResponse>` (top-level only)
  - `fun getProjectComments(projectId: UUID): List<CommentResponse>` (top-level only)
  - `fun getOrganizationComments(orgId: UUID): List<CommentResponse>` (top-level only)
  - `fun editComment(commentId: UUID, updatedContent: String, userId: UUID): CommentResponse` (auth check)
  - `fun deleteComment(commentId: UUID, userId: UUID): void` (auth check)
- [ ] Create `AttachmentService` with methods:
  - `fun addAttachment(commentId: UUID, fileName: String, fileUrl: String, fileSize: Long?, mimeType: String?): AttachmentResponse`
  - `fun getAttachments(commentId: UUID): List<AttachmentResponse>`
  - `fun deleteAttachment(attachmentId: UUID): void`

### Task 7: Create CommentController with REST endpoints

**Files:**
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/controller/CommentController.kt`

- [ ] Create endpoints:
  - `POST /api/tasks/{taskId}/comments` — add comment to task
  - `POST /api/projects/{projectId}/comments` — add comment to project
  - `POST /api/organizations/{orgId}/comments` — add comment to organization
  - `GET /api/comments/{commentId}` — get comment with replies tree
  - `POST /api/comments/{commentId}/replies` — add nested reply
  - `POST /api/comments/{commentId}/attachments` — add attachment
  - `GET /api/tasks/{taskId}/comments` — list top-level comments for task
  - `GET /api/projects/{projectId}/comments` — list top-level comments for project
  - `GET /api/organizations/{orgId}/comments` — list top-level comments for org
  - `PUT /api/comments/{commentId}` — edit comment (X-User-Id must match createdBy)
  - `DELETE /api/comments/{commentId}` — delete comment (X-User-Id must match createdBy)
- [ ] All endpoints require X-User-Id header (author context)
- [ ] Return appropriate HTTP status codes (201 for creation, 403 for auth errors, 404 for not found)

### Task 8: Create MapStruct mappers

**Files:**
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/mapper/CommentMapper.kt`
- Create: `TaskService/src/main/kotlin/com/slavacom/taskservice/mapper/AttachmentMapper.kt`

- [ ] Create `CommentMapper` with `toResponse(Comment): CommentResponse`
- [ ] Create `AttachmentMapper` with `toResponse(Attachment): AttachmentResponse`
- [ ] Mappers should recursively populate replies

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
