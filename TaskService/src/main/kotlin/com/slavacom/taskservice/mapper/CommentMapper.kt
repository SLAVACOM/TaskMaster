package com.slavacom.taskservice.mapper

import com.slavacom.taskservice.dto.CommentResponse
import com.slavacom.taskservice.entity.Comment
import com.slavacom.taskservice.repository.AttachmentRepository
import com.slavacom.taskservice.repository.CommentRepository
import org.springframework.stereotype.Component

@Component
class CommentMapper(
	private val commentRepository: CommentRepository,
	private val attachmentRepository: AttachmentRepository,
	private val attachmentMapper: AttachmentMapper,
) {

	fun toResponse(comment: Comment): CommentResponse {
		val attachments = attachmentRepository.findByCommentId(comment.id!!)
			.map(attachmentMapper::toResponse)

		return CommentResponse(
			id = comment.id!!,
			content = comment.content,
			createdBy = comment.createdBy!!,
			createdAt = comment.createdAt!!,
			updatedAt = comment.updatedAt,
			parentCommentId = comment.parentCommentId,
			replies = emptyList(),
			attachments = attachments,
		)
	}

	fun toResponseWithReplies(comment: Comment): CommentResponse {
		val attachments = attachmentRepository.findByCommentId(comment.id!!)
			.map(attachmentMapper::toResponse)

		val replies = commentRepository.findByParentCommentIdOrderByCreatedAtDesc(comment.id!!)
			.map { toResponseWithReplies(it) }

		return CommentResponse(
			id = comment.id!!,
			content = comment.content,
			createdBy = comment.createdBy!!,
			createdAt = comment.createdAt!!,
			updatedAt = comment.updatedAt,
			parentCommentId = comment.parentCommentId,
			replies = replies,
			attachments = attachments,
		)
	}
}
