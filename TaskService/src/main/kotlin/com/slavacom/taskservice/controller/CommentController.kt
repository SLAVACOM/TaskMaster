package com.slavacom.taskservice.controller

import com.slavacom.taskservice.dto.AttachmentResponse
import com.slavacom.taskservice.dto.CommentResponse
import com.slavacom.taskservice.dto.CreateCommentRequest
import com.slavacom.taskservice.service.AttachmentService
import com.slavacom.taskservice.service.CommentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class CommentController(
	private val commentService: CommentService,
	private val attachmentService: AttachmentService,
) {

	@PostMapping("/api/tasks/{taskId}/comments")
	fun addTaskComment(
		@PathVariable taskId: UUID,
		@RequestHeader("X-User-Id") userId: UUID,
		@Valid @RequestBody request: CreateCommentRequest,
	): ResponseEntity<CommentResponse> =
		ResponseEntity.status(HttpStatus.CREATED)
			.body(commentService.addTaskComment(taskId, request, userId))

	@PostMapping("/api/projects/{projectId}/comments")
	fun addProjectComment(
		@PathVariable projectId: UUID,
		@RequestHeader("X-User-Id") userId: UUID,
		@Valid @RequestBody request: CreateCommentRequest,
	): ResponseEntity<CommentResponse> =
		ResponseEntity.status(HttpStatus.CREATED)
			.body(commentService.addProjectComment(projectId, request, userId))

	@PostMapping("/api/organizations/{organizationId}/comments")
	fun addOrganizationComment(
		@PathVariable organizationId: UUID,
		@RequestHeader("X-User-Id") userId: UUID,
		@Valid @RequestBody request: CreateCommentRequest,
	): ResponseEntity<CommentResponse> =
		ResponseEntity.status(HttpStatus.CREATED)
			.body(commentService.addOrganizationComment(organizationId, request, userId))

	@GetMapping("/api/comments/{commentId}")
	fun getComment(@PathVariable commentId: UUID): CommentResponse =
		commentService.getCommentWithReplies(commentId)

	@PostMapping("/api/comments/{commentId}/replies")
	fun addReply(
		@PathVariable commentId: UUID,
		@RequestHeader("X-User-Id") userId: UUID,
		@Valid @RequestBody request: CreateCommentRequest,
	): ResponseEntity<CommentResponse> =
		ResponseEntity.status(HttpStatus.CREATED)
			.body(commentService.addReply(commentId, request, userId))

	@GetMapping("/api/tasks/{taskId}/comments")
	fun getTaskComments(@PathVariable taskId: UUID): List<CommentResponse> =
		commentService.getTaskComments(taskId)

	@GetMapping("/api/projects/{projectId}/comments")
	fun getProjectComments(@PathVariable projectId: UUID): List<CommentResponse> =
		commentService.getProjectComments(projectId)

	@GetMapping("/api/organizations/{organizationId}/comments")
	fun getOrganizationComments(@PathVariable organizationId: UUID): List<CommentResponse> =
		commentService.getOrganizationComments(organizationId)

	@PutMapping("/api/comments/{commentId}")
	fun editComment(
		@PathVariable commentId: UUID,
		@RequestHeader("X-User-Id") userId: UUID,
		@RequestBody request: CreateCommentRequest,
	): CommentResponse =
		commentService.editComment(commentId, request.content, userId)

	@DeleteMapping("/api/comments/{commentId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun deleteComment(
		@PathVariable commentId: UUID,
		@RequestHeader("X-User-Id") userId: UUID,
	) {
		commentService.deleteComment(commentId, userId)
	}

	@PostMapping("/api/comments/{commentId}/attachments")
	fun addAttachment(
		@PathVariable commentId: UUID,
		@RequestParam fileName: String,
		@RequestParam fileUrl: String,
		@RequestParam fileSize: Long? = null,
		@RequestParam mimeType: String? = null,
	): ResponseEntity<AttachmentResponse> =
		ResponseEntity.status(HttpStatus.CREATED)
			.body(attachmentService.addAttachment(commentId, fileName, fileUrl, fileSize, mimeType))

	@DeleteMapping("/api/attachments/{attachmentId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	fun deleteAttachment(@PathVariable attachmentId: UUID) {
		attachmentService.deleteAttachment(attachmentId)
	}
}
