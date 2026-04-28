package com.slavacom.organizationservice.project.employees

import java.time.Instant
import java.util.UUID

data class ProjectEmployeeResponse(
    val id: UUID,
    val projectId: UUID,
    val userId: UUID,
    val profileId: UUID,
    val role: String,
    val permissions: String?,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant?
)

data class AddProjectEmployeeRequest(
    val userId: UUID,
    val profileId: UUID,
    val role: String,
    val permissions: String? = null
)

data class UpdateProjectEmployeeRequest(
    val role: String? = null,
    val permissions: String? = null
)
