package com.slavacom.taskservice.repository

import com.slavacom.taskservice.entity.TaskComment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TaskCommentRepository : JpaRepository<TaskComment, UUID> {
	fun findByTaskIdOrderByCreatedAtDesc(taskId: UUID): List<TaskComment>
	fun findByTaskIdAndParentCommentIdIsNull(taskId: UUID): List<TaskComment>
}
