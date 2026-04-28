package com.slavacom.organizationservice.project.statuses

import com.slavacom.organizationservice.entity.ProjectTaskStatuses
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface ProjectTaskStatusesRepository : JpaRepository<ProjectTaskStatuses, UUID> {
    fun findAllByProjectIdOrderByOrderIndexAsc(projectId: UUID): List<ProjectTaskStatuses>
    fun findByIdAndProjectId(id: UUID, projectId: UUID): Optional<ProjectTaskStatuses>
}
