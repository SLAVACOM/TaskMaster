package com.slavacom.organizationservice.project.tags

import java.time.Instant
import java.util.UUID

data class ProjectTagResponse(
    val id: UUID,
    val projectId: UUID,
    val organizationId: UUID?,
    val name: String,
    val color: String?,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant?
)

data class CreateProjectTagRequest(
    val name: String,
    val color: String? = null,
    val description: String? = null
)

data class UpdateProjectTagRequest(
    val name: String? = null,
    val color: String? = null,
    val description: String? = null
)
