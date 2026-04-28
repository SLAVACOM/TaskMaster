package com.slavacom.organizationservice.project.history

import com.slavacom.organizationservice.entity.ProjectHistory
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProjectHistoryRepository : JpaRepository<ProjectHistory, UUID> {
    fun findAllByProjectIdOrderByChangedAtDesc(projectId: UUID): List<ProjectHistory>
}
