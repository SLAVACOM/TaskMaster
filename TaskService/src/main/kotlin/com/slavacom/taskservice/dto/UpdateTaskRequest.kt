package com.slavacom.taskservice.dto

import com.slavacom.taskservice.entity.enums.TaskPriority
import com.slavacom.taskservice.entity.enums.TaskStatus
import java.time.Instant
import java.util.UUID

data class UpdateTaskRequest(
    val name: String? = null,
    val description: String? = null,
    val files: List<String>? = null,
    val depends: List<UUID>? = null,
    val status: TaskStatus? = null,
    val responsible: UUID? = null,
    val executor: UUID? = null,
    val observers: List<UUID>? = null,
    val priority: TaskPriority? = null,
    val tags: List<String>? = null,
    val start: Instant? = null,
    val end: Instant? = null,
    val deadline: Instant? = null,
    val sprintId: UUID? = null,
    val storyPoint: Int? = null,
    val isActive: Boolean? = null,
)

