package com.slavacom.taskservice.dto

import com.slavacom.taskservice.entity.enums.TaskPriority
import com.slavacom.taskservice.entity.enums.TaskStatus
import java.time.Instant
import java.util.UUID

data class TaskSearchRequest(
    val name: String? = null,
    val description: String? = null,
    val status: TaskStatus? = null,
    val priority: TaskPriority? = null,
    val responsible: UUID? = null,
    val executor: UUID? = null,
    val sprintId: UUID? = null,
    val isActive: Boolean? = true,
    val tag: String? = null,
    val observerId: UUID? = null,
    val startFrom: Instant? = null,
    val startTo: Instant? = null,
    val deadlineFrom: Instant? = null,
    val deadlineTo: Instant? = null,
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "createdAt",
    val sortDir: String = "desc",
)

