package com.slavacom.organizationservice.project.statuses

import java.time.Instant
import java.util.UUID

data class TaskStatusResponse(
    val id: UUID,
    val projectId: UUID,
    val name: String,
    val color: String?,
    val orderIndex: Int,
    val isInitial: Boolean,
    val isFinal: Boolean,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant?
)

data class CreateTaskStatusRequest(
    val name: String,
    val color: String? = null,
    val orderIndex: Int,
    val isInitial: Boolean = false,
    val isFinal: Boolean = false
)

data class UpdateTaskStatusRequest(
    val name: String? = null,
    val color: String? = null,
    val orderIndex: Int? = null,
    val isInitial: Boolean? = null,
    val isFinal: Boolean? = null,
    val isActive: Boolean? = null
)
