package com.slavacom.organizationservice.project.kanban

import java.time.Instant
import java.util.UUID

data class KanbanColumnResponse(
    val id: UUID,
    val projectId: UUID,
    val name: String,
    val orderIndex: Int,
    val color: String?,
    val wipLimit: Int?,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant?
)

data class CreateKanbanColumnRequest(
    val name: String,
    val orderIndex: Int,
    val color: String? = null,
    val wipLimit: Int? = null
)

data class UpdateKanbanColumnRequest(
    val name: String? = null,
    val orderIndex: Int? = null,
    val color: String? = null,
    val wipLimit: Int? = null
)

data class ReorderItem(val id: UUID, val orderIndex: Int)
data class ReorderColumnsRequest(val columns: List<ReorderItem>)

data class TaskPositionResponse(
    val id: UUID,
    val projectId: UUID,
    val kanbanColumnId: UUID,
    val taskId: UUID,
    val orderIndex: Int,
    val updatedAt: Instant?
)

data class TaskPositionItem(
    val taskId: UUID,
    val kanbanColumnId: UUID,
    val orderIndex: Int
)
data class BulkUpdatePositionsRequest(val positions: List<TaskPositionItem>)
