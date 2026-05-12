package com.slavacom.taskservice.repository

import com.slavacom.taskservice.entity.Comment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CommentRepository : JpaRepository<Comment, UUID> {
	fun findByParentCommentIdOrderByCreatedAtDesc(parentCommentId: UUID): List<Comment>
}
