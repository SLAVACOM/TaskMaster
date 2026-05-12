package com.slavacom.taskservice.repository

import com.slavacom.taskservice.entity.ProjectComment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProjectCommentRepository : JpaRepository<ProjectComment, UUID> {
	fun findByProjectIdOrderByCreatedAtDesc(projectId: UUID): List<ProjectComment>
	fun findByProjectIdAndParentCommentIdIsNull(projectId: UUID): List<ProjectComment>
}
