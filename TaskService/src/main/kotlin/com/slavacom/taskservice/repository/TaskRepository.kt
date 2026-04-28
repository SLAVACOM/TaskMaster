package com.slavacom.taskservice.repository

import com.slavacom.taskservice.entity.Task
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TaskRepository : JpaRepository<Task, UUID>, TaskCriteriaRepository {
    fun findByExecutorAndIsActiveTrueOrderByCreatedAtDesc(executor: UUID): List<Task>
    fun findBySprintIdAndIsActiveTrueOrderByCreatedAtDesc(sprintId: UUID): List<Task>
	fun findByIdAndIsActiveTrue(id: UUID): Task?
	fun findByIsActiveTrueOrderByCreatedAtDesc(): List<Task>
}

