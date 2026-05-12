package com.slavacom.taskservice.repository

import com.slavacom.taskservice.entity.Attachment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AttachmentRepository : JpaRepository<Attachment, UUID> {
	fun findByCommentId(commentId: UUID): List<Attachment>
}
