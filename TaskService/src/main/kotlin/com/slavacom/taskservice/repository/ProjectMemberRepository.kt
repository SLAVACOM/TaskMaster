package com.slavacom.taskservice.repository

import com.slavacom.taskservice.entity.ProjectMember
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface ProjectMemberRepository : JpaRepository<ProjectMember, UUID> {
	fun findByProjectId(projectId: UUID): List<ProjectMember>
	fun findByUserId(userId: UUID): List<ProjectMember>
	fun findByProjectIdAndUserId(projectId: UUID, userId: UUID): Optional<ProjectMember>
}
