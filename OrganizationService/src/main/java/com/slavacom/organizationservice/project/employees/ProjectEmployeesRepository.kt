package com.slavacom.organizationservice.project.employees

import com.slavacom.organizationservice.entity.ProjectEmployees
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface ProjectEmployeesRepository : JpaRepository<ProjectEmployees, UUID> {
    fun findAllByProjectIdAndIsActiveTrue(projectId: UUID): List<ProjectEmployees>
    fun findByIdAndProjectId(id: UUID, projectId: UUID): Optional<ProjectEmployees>
    fun existsByProjectIdAndProfileIdAndIsActiveTrue(projectId: UUID, profileId: UUID): Boolean
    fun findByUserIdAndProjectIdAndIsActiveTrue(userId: UUID, projectId: UUID): Optional<ProjectEmployees>
}
