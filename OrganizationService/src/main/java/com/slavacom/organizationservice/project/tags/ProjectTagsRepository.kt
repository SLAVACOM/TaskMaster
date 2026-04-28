package com.slavacom.organizationservice.project.tags

import com.slavacom.organizationservice.entity.ProjectTags
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface ProjectTagsRepository : JpaRepository<ProjectTags, UUID> {
    fun findAllByProjectId(projectId: UUID): List<ProjectTags>
    fun findByIdAndProjectId(id: UUID, projectId: UUID): Optional<ProjectTags>
}
