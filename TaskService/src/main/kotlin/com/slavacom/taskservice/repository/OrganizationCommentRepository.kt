package com.slavacom.taskservice.repository

import com.slavacom.taskservice.entity.OrganizationComment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrganizationCommentRepository : JpaRepository<OrganizationComment, UUID> {
	fun findByOrganizationIdOrderByCreatedAtDesc(organizationId: UUID): List<OrganizationComment>
	fun findByOrganizationIdAndParentCommentIdIsNull(organizationId: UUID): List<OrganizationComment>
}
