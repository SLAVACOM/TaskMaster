package com.slavacom.taskservice.dto

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.slavacom.taskservice.entity.enums.TaskPriority
import com.slavacom.taskservice.entity.enums.TaskStatus
import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.UUID

data class CreateTaskRequest(
    @field:NotBlank
    val name: String,
    val description: String? = null,
    @param:JsonSetter(nulls = Nulls.AS_EMPTY)
    val files: List<String> = emptyList(),
    @param:JsonSetter(nulls = Nulls.AS_EMPTY)
    val depends: List<UUID> = emptyList(),
    @param:JsonSetter(nulls = Nulls.SKIP)
    val status: TaskStatus = TaskStatus.TODO,
    val responsible: UUID? = null,
    val executor: UUID? = null,
    @param:JsonSetter(nulls = Nulls.AS_EMPTY)
    val observers: List<UUID> = emptyList(),
    @param:JsonSetter(nulls = Nulls.SKIP)
    val priority: TaskPriority = TaskPriority.MEDIUM,
    @param:JsonSetter(nulls = Nulls.AS_EMPTY)
    val tags: List<String> = emptyList(),
    val start: Instant? = null,
    val end: Instant? = null,
    val deadline: Instant? = null,
    val sprintId: UUID? = null,
    val storyPoint: Int? = null,
)

