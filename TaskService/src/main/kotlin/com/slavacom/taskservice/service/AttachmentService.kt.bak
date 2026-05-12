package com.slavacom.taskservice.service

import com.slavacom.taskservice.dto.AttachmentResponse
import com.slavacom.taskservice.entity.Attachment
import com.slavacom.taskservice.mapper.AttachmentMapper
import com.slavacom.taskservice.repository.AttachmentRepository
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class AttachmentService(
	private val attachmentRepository: AttachmentRepository,
	private val attachmentMapper: AttachmentMapper,
) {

	@Transactional
	fun addAttachment(
		commentId: UUID,
		fileName: String,
		fileUrl: String,
		fileSize: Long? = null,
		mimeType: String? = null,
	): AttachmentResponse {
		logger.info { "Adding attachment to comment $commentId: $fileName" }
		val attachment = Attachment(
			commentId = commentId,
			fileName = fileName,
			fileUrl = fileUrl,
			fileSize = fileSize,
			mimeType = mimeType,
		)
		val saved = attachmentRepository.save(attachment)
		return attachmentMapper.toResponse(saved)
	}

	@Transactional(readOnly = true)
	fun getAttachments(commentId: UUID): List<AttachmentResponse> {
		val attachments = attachmentRepository.findByCommentId(commentId)
		return attachments.map(attachmentMapper::toResponse)
	}

	@Transactional
	fun deleteAttachment(attachmentId: UUID) {
		logger.info { "Deleting attachment $attachmentId" }
		val attachment = attachmentRepository.findById(attachmentId)
			.orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found") }
		attachmentRepository.delete(attachment)
	}
}
