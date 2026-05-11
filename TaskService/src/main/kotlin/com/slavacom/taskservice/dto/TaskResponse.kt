package com.slavacom.taskservice.dto

import com.slavacom.taskservice.entity.enums.TaskPriority
import java.time.Instant
import java.util.UUID

data class TaskResponse(
    val id: UUID,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    val isActive: Boolean,
    val name: String,
    val description: String?,
    val files: List<String>,
    val depends: List<UUID>,
    val status: String,
    val responsible: UUID?,
    val executor: UUID?,
    val observers: List<UUID>,
    val watchers: List<UUID>,
    val priority: TaskPriority,
    val tags: List<String>,
    val start: Instant?,
    val end: Instant?,
    val deadline: Instant?,
    val sprintId: UUID?,
    val projectId: UUID?,
    val storyPoint: Int?,
)

