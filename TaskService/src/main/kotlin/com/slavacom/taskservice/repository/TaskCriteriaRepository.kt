package com.slavacom.taskservice.repository

import com.slavacom.taskservice.dto.TaskSearchRequest
import com.slavacom.taskservice.entity.Task
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TaskCriteriaRepository {
    fun searchTasks(filter: TaskSearchRequest, pageable: Pageable): Page<Task>
}

