package com.slavacom.organizationservice.project

import java.time.Instant
import java.util.UUID

data class ProjectResponse(
    val id: UUID,
    val organizationId: UUID,
    val name: String,
    val description: String?,
    val isActive: Boolean,
    val responsible: UUID,
    val createdAt: Instant,
    val updatedAt: Instant?
)

data class CreateProjectRequest(
    val name: String,
    val description: String? = null,
    val responsible: UUID?
)

data class UpdateProjectRequest(
    val name: String? = null,
    val description: String? = null,
    val responsible: UUID? = null,
    val isActive: Boolean? = null
)
