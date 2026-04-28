package com.slavacom.taskservice.repository

import com.slavacom.taskservice.entity.TaskHistory
import com.slavacom.taskservice.entity.enums.HistoryAction
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TaskHistoryRepository : JpaRepository<TaskHistory, UUID> {
    fun findByTaskIdOrderByChangedAtDesc(taskId: UUID): List<TaskHistory>
    fun findByTaskIdAndActionOrderByChangedAtDesc(taskId: UUID, action: HistoryAction): List<TaskHistory>
    fun findByChangedByOrderByChangedAtDesc(changedBy: UUID): List<TaskHistory>
}

