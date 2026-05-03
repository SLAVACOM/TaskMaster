package com.slavacom.taskservice.dto

import com.slavacom.taskservice.entity.enums.TaskPriority
import com.slavacom.taskservice.entity.enums.TaskStatus
import java.time.Instant
import java.util.UUID

// Task summary for dashboard view
data class TaskDashboardResponse(
    val id: UUID,
    val name: String,
    val projectId: UUID,
    val sprintId: UUID?,
    val status: TaskStatus,
    val executor: UUID?,
    val priority: TaskPriority,
    val createdAt: Instant,
    val deadline: Instant?,
)

// Project dashboard overview
data class ProjectDashboardResponse(
    val projectId: UUID,
    val projectName: String,
    val totalTasks: Long,
    val todoCount: Long,
    val inProgressCount: Long,
    val doneCount: Long,
    val tasksByPriority: Map<TaskPriority, Long>,
    val latestTasks: List<TaskDashboardResponse>,
    val activeTasks: List<TaskDashboardResponse>,
)

// Sprint dashboard with burndown
data class SprintDashboardResponse(
    val sprintId: UUID,
    val sprintName: String,
    val totalTasks: Long,
    val completedTasks: Long,
    val completionPercentage: Double,
    val tasksByStatus: Map<TaskStatus, Long>,
    val tasksByPriority: Map<TaskPriority, Long>,
    val remainingTasks: List<TaskDashboardResponse>,
    val overdueTasks: List<TaskDashboardResponse>,
)

// Task search/filter response
data class TaskFilterResponse(
    val tasks: List<TaskDashboardResponse>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
)
