package com.slavacom.organizationservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "kanban_task_positions")
open class KanbanTaskPositions {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(name = "id", nullable = false)
    open var id: UUID? = null

    @field:Column(name = "project_id", nullable = false)
    open var projectId: UUID? = null

    @field:Column(name = "kanban_column_id", nullable = false)
    open var kanbanColumnId: UUID? = null

    @field:Column(name = "task_id", nullable = false)
    open var taskId: UUID? = null

    @field:Column(name = "order_index", nullable = false)
    open var orderIndex: Int? = null

    @field:UpdateTimestamp
    @field:Column(name = "updated_at")
    open var updatedAt: Instant? = null
}

