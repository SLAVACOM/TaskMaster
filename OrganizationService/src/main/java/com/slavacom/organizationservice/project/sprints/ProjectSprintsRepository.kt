package com.slavacom.organizationservice.project.sprints

import com.slavacom.organizationservice.entity.ProjectSprints
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface ProjectSprintsRepository : JpaRepository<ProjectSprints, UUID> {
    fun findAllByProjectId(projectId: UUID): List<ProjectSprints>
    fun findByIdAndProjectId(id: UUID, projectId: UUID): Optional<ProjectSprints>
    fun findAllByProjectIdAndIsActiveTrue(projectId: UUID): List<ProjectSprints>
}
