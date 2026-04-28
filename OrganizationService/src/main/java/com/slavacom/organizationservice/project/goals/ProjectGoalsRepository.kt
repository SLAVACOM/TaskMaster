package com.slavacom.organizationservice.project.goals

import com.slavacom.organizationservice.entity.ProjectGoals
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface ProjectGoalsRepository : JpaRepository<ProjectGoals, UUID> {
    fun findAllByProjectId(projectId: UUID): List<ProjectGoals>
    fun findByIdAndProjectId(id: UUID, projectId: UUID): Optional<ProjectGoals>
}
