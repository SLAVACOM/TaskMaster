package com.slavacom.taskservice.repository

import com.slavacom.taskservice.dto.TaskSearchRequest
import com.slavacom.taskservice.entity.Task
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface TaskCriteriaRepository {
    fun searchTasks(filter: TaskSearchRequest, pageable: Pageable): Page<Task>

    fun searchAccessibleTasks(
        filter: TaskSearchRequest,
        userId: UUID,
        userProjectIds: Set<UUID>,
        pageable: Pageable,
    ): Page<Task>
}

