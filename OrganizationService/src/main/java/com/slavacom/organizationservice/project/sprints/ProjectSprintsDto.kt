package com.slavacom.organizationservice.project.sprints

import java.time.Instant
import java.util.UUID

data class SprintResponse(
    val id: UUID,
    val projectId: UUID,
    val name: String,
    val description: String?,
    val startDate: Instant?,
    val endDate: Instant?,
    val status: String,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant?
)

data class CreateSprintRequest(
    val name: String,
    val description: String? = null,
    val startDate: Instant? = null,
    val endDate: Instant? = null
)

data class UpdateSprintRequest(
    val name: String? = null,
    val description: String? = null,
    val startDate: Instant? = null,
    val endDate: Instant? = null
)
