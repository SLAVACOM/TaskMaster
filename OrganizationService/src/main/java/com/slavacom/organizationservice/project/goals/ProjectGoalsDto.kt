package com.slavacom.organizationservice.project.goals

import java.time.Instant
import java.util.UUID

data class ProjectGoalResponse(
    val id: UUID,
    val projectId: UUID,
    val name: String,
    val description: String?,
    val targetDate: Instant?,
    val progress: Int,
    val isCompleted: Boolean,
    val responsible: UUID?,
    val createdAt: Instant,
    val updatedAt: Instant?
)

data class CreateProjectGoalRequest(
    val name: String,
    val description: String? = null,
    val targetDate: Instant? = null,
    val responsible: UUID? = null
)

data class UpdateProjectGoalRequest(
    val name: String? = null,
    val description: String? = null,
    val targetDate: Instant? = null,
    val progress: Int? = null,
    val isCompleted: Boolean? = null,
    val responsible: UUID? = null
)
