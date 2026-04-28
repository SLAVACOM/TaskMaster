package com.slavacom.taskservice.controller

import com.slavacom.taskservice.dto.TaskHistoryResponse
import com.slavacom.taskservice.entity.enums.HistoryAction
import com.slavacom.taskservice.service.TaskHistoryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/tasks")
class TaskHistoryController(
    private val taskHistoryService: TaskHistoryService,
) {

    /**
     * История изменений конкретного задания.
     * Опциональный query-параметр ?action=CREATED|UPDATED|DELETED
     *
     * GET /api/tasks/{taskId}/history
     * GET /api/tasks/{taskId}/history?action=UPDATED
     */
    @GetMapping("/{taskId}/history")
    fun getByTask(
        @PathVariable taskId: UUID,
        @RequestParam(required = false) action: HistoryAction?,
    ): List<TaskHistoryResponse> = taskHistoryService.getByTask(taskId, action)

    /**
     * Одна запись истории по её собственному id.
     *
     * GET /api/tasks/history/{historyId}
     */
    @GetMapping("/history/{historyId}")
    fun getById(@PathVariable historyId: UUID): TaskHistoryResponse =
        taskHistoryService.getById(historyId)

    /**
     * Все изменения, внесённые конкретным пользователем.
     *
     * GET /api/tasks/history/by-user/{userId}
     */
    @GetMapping("/history/by-user/{userId}")
    fun getByUser(@PathVariable userId: UUID): List<TaskHistoryResponse> =
        taskHistoryService.getByUser(userId)
}

