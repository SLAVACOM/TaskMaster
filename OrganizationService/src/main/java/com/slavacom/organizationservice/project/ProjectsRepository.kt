package com.slavacom.organizationservice.project

import com.slavacom.organizationservice.entity.Projects
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProjectsRepository : JpaRepository<Projects, UUID> {
    fun findAllByOrganizationIdAndIsActiveTrue(organizationId: UUID): List<Projects>
    fun findAllByOrganizationId(organizationId: UUID): List<Projects>
}
