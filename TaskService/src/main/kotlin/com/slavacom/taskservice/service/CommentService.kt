package com.slavacom.taskservice.service

import com.slavacom.taskservice.dto.AttachmentResponse
import com.slavacom.taskservice.dto.CommentResponse
import com.slavacom.taskservice.dto.CreateCommentRequest
import com.slavacom.taskservice.entity.OrganizationComment
import com.slavacom.taskservice.entity.ProjectComment
import com.slavacom.taskservice.entity.TaskComment
import com.slavacom.taskservice.mapper.AttachmentMapper
import com.slavacom.taskservice.mapper.CommentMapper
import com.slavacom.taskservice.repository.AttachmentRepository
import com.slavacom.taskservice.repository.CommentRepository
import com.slavacom.taskservice.repository.OrganizationCommentRepository
import com.slavacom.taskservice.repository.ProjectCommentRepository
import com.slavacom.taskservice.repository.TaskCommentRepository
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class CommentService(
	private val commentRepository: CommentRepository,
	private val taskCommentRepository: TaskCommentRepository,
	private val projectCommentRepository: ProjectCommentRepository,
	private val organizationCommentRepository: OrganizationCommentRepository,
	private val attachmentRepository: AttachmentRepository,
	private val commentMapper: CommentMapper,
	private val attachmentMapper: AttachmentMapper,
) {

	@Transactional
	fun addTaskComment(taskId: UUID, request: CreateCommentRequest, createdBy: UUID): CommentResponse {
		logger.info { "Adding comment to task $taskId by user $createdBy" }
		val comment = TaskComment(taskId = taskId)
		comment.content = request.content
		comment.createdBy = createdBy
		comment.parentCommentId = request.parentCommentId
		val saved = taskCommentRepository.saveAndFlush(comment)
		return commentMapper.toResponse(saved)
	}

	@Transactional
	fun addProjectComment(projectId: UUID, request: CreateCommentRequest, createdBy: UUID): CommentResponse {
		logger.info { "Adding comment to project $projectId by user $createdBy" }
		val comment = ProjectComment(projectId = projectId)
		comment.content = request.content
		comment.createdBy = createdBy
		comment.parentCommentId = request.parentCommentId
		val saved = projectCommentRepository.saveAndFlush(comment)
		return commentMapper.toResponse(saved)
	}

	@Transactional
	fun addOrganizationComment(organizationId: UUID, request: CreateCommentRequest, createdBy: UUID): CommentResponse {
		logger.info { "Adding comment to organization $organizationId by user $createdBy" }
		val comment = OrganizationComment(organizationId = organizationId)
		comment.content = request.content
		comment.createdBy = createdBy
		comment.parentCommentId = request.parentCommentId
		val saved = organizationCommentRepository.saveAndFlush(comment)
		return commentMapper.toResponse(saved)
	}

	@Transactional
	fun addReply(parentCommentId: UUID, request: CreateCommentRequest, createdBy: UUID): CommentResponse {
		logger.info { "Adding reply to comment $parentCommentId by user $createdBy" }
		val parentComment = commentRepository.findById(parentCommentId)
			.orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Parent comment not found") }

		val reply = when (parentComment) {
			is TaskComment -> TaskComment(taskId = parentComment.taskId)
			is ProjectComment -> ProjectComment(projectId = parentComment.projectId)
			is OrganizationComment -> OrganizationComment(organizationId = parentComment.organizationId)
			else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown comment type")
		}
		reply.content = request.content
		reply.createdBy = createdBy
		reply.parentCommentId = parentCommentId
		val saved = commentRepository.saveAndFlush(reply)
		return commentMapper.toResponse(saved)
	}

	@Transactional(readOnly = true)
	fun getCommentWithReplies(commentId: UUID): CommentResponse {
		val comment = commentRepository.findById(commentId)
			.orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found") }
		return commentMapper.toResponseWithReplies(comment)
	}

	@Transactional(readOnly = true)
	fun getTaskComments(taskId: UUID): List<CommentResponse> {
		val comments = taskCommentRepository.findByTaskIdAndParentCommentIdIsNull(taskId)
		return comments.map { commentMapper.toResponseWithReplies(it) }
	}

	@Transactional(readOnly = true)
	fun getProjectComments(projectId: UUID): List<CommentResponse> {
		val comments = projectCommentRepository.findByProjectIdAndParentCommentIdIsNull(projectId)
		return comments.map { commentMapper.toResponseWithReplies(it) }
	}

	@Transactional(readOnly = true)
	fun getOrganizationComments(organizationId: UUID): List<CommentResponse> {
		val comments = organizationCommentRepository.findByOrganizationIdAndParentCommentIdIsNull(organizationId)
		return comments.map { commentMapper.toResponseWithReplies(it) }
	}

	@Transactional
	fun editComment(commentId: UUID, updatedContent: String, userId: UUID): CommentResponse {
		logger.info { "Editing comment $commentId by user $userId" }
		val comment = commentRepository.findById(commentId)
			.orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found") }

		if (comment.createdBy != userId) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only comment author can edit")
		}

		comment.content = updatedContent
		val saved = commentRepository.save(comment)
		return commentMapper.toResponse(saved)
	}

	@Transactional
	fun deleteComment(commentId: UUID, userId: UUID) {
		logger.info { "Deleting comment $commentId by user $userId" }
		val comment = commentRepository.findById(commentId)
			.orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found") }

		if (comment.createdBy != userId) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only comment author can delete")
		}

		attachmentRepository.deleteAll(attachmentRepository.findByCommentId(commentId))
		commentRepository.delete(comment)
	}
}
