package com.slavacom.taskservice.repository

import com.slavacom.taskservice.entity.Task
import com.slavacom.taskservice.entity.enums.TaskStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TaskRepository : JpaRepository<Task, UUID>, TaskCriteriaRepository {
    fun findByExecutorAndIsActiveTrueOrderByCreatedAtDesc(executor: UUID): List<Task>
    fun findBySprintIdAndIsActiveTrueOrderByCreatedAtDesc(sprintId: UUID): List<Task>
    fun findByProjectIdAndIsActiveTrueOrderByCreatedAtDesc(projectId: UUID): List<Task>
	fun findByIdAndIsActiveTrue(id: UUID): Task?
	fun findByIsActiveTrueOrderByCreatedAtDesc(): List<Task>

	// Dashboard & Filtering queries (Phase 1)
	fun findByProjectIdAndStatusAndIsActiveTrueOrderByCreatedAtDesc(projectId: UUID, status: TaskStatus): List<Task>
	fun findByExecutorAndStatusAndIsActiveTrueOrderByDeadlineAscCreatedAtDesc(executor: UUID, status: TaskStatus): List<Task>
	fun findBySprintIdAndStatusAndIsActiveTrueOrderByPriorityDescCreatedAtDesc(sprintId: UUID, status: TaskStatus): List<Task>
	fun findByProjectIdAndIsActiveTrue(projectId: UUID): List<Task>
	fun findBySprintIdAndIsActiveTrue(sprintId: UUID): List<Task>
	fun countByProjectIdAndStatusAndIsActiveTrue(projectId: UUID, status: TaskStatus): Long
	fun countBySprintIdAndStatusAndIsActiveTrue(sprintId: UUID, status: TaskStatus): Long
}

