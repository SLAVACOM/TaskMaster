package com.slavacom.taskservice.service

import com.slavacom.taskservice.dto.TaskHistoryResponse
import com.slavacom.taskservice.entity.enums.HistoryAction
import com.slavacom.taskservice.mapper.TaskHistoryMapper
import com.slavacom.taskservice.repository.TaskHistoryRepository
import com.slavacom.taskservice.repository.TaskRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
@Transactional(readOnly = true)
class TaskHistoryService(
    private val taskHistoryRepository: TaskHistoryRepository,
    private val taskRepository: TaskRepository,
    private val taskHistoryMapper: TaskHistoryMapper,
) {

    /** Вся история задания, опционально фильтр по типу действия */
    fun getByTask(taskId: UUID, action: HistoryAction? = null): List<TaskHistoryResponse> {
        taskRepository.findById(taskId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: $taskId") }

        val entries = if (action != null)
            taskHistoryRepository.findByTaskIdAndActionOrderByChangedAtDesc(taskId, action)
        else
            taskHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId)

        return entries.map(taskHistoryMapper::toResponse)
    }

    /** Одна запись истории по её id */
    fun getById(historyId: UUID): TaskHistoryResponse =
        taskHistoryRepository.findById(historyId)
            .map(taskHistoryMapper::toResponse)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "History entry not found: $historyId") }

    /** Все изменения, внесённые конкретным пользователем */
    fun getByUser(changedBy: UUID): List<TaskHistoryResponse> =
        taskHistoryRepository.findByChangedByOrderByChangedAtDesc(changedBy)
            .map(taskHistoryMapper::toResponse)
}

