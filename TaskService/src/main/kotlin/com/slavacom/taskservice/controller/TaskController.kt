package com.slavacom.taskservice.controller

import com.slavacom.taskservice.dto.CreateTaskRequest
import com.slavacom.taskservice.dto.TaskPageResponse
import com.slavacom.taskservice.dto.TaskResponse
import com.slavacom.taskservice.dto.TaskSearchRequest
import com.slavacom.taskservice.dto.UpdateTaskRequest
import com.slavacom.taskservice.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService,
) {

    @PostMapping
    fun create(
        @RequestHeader("X-User-Id") changedBy: UUID,
        @Valid @RequestBody request: CreateTaskRequest,
    ): ResponseEntity<TaskResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request, changedBy))

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): TaskResponse = taskService.getById(id)

    @GetMapping
    fun getAll(@RequestParam projectId: UUID? = null): List<TaskResponse> = taskService.getAll(projectId)

    @GetMapping("/search")
    fun search(@ModelAttribute filter: TaskSearchRequest): TaskPageResponse =
        taskService.search(filter)

    @PutMapping("/{id}")
    fun update(
        @RequestHeader("X-User-Id") changedBy: UUID,
        @PathVariable id: UUID,
        @RequestBody request: UpdateTaskRequest,
    ): TaskResponse = taskService.update(id, request, changedBy)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @RequestHeader("X-User-Id") changedBy: UUID,
        @PathVariable id: UUID,
    ) = taskService.delete(id, changedBy)
}

// Project-scoped task endpoints
@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
class ProjectTaskController(
    private val taskService: TaskService,
) {

    @PostMapping
    fun create(
        @PathVariable projectId: UUID,
        @RequestHeader("X-User-Id") changedBy: UUID,
        @Valid @RequestBody request: CreateTaskRequest,
    ): ResponseEntity<TaskResponse> {
        val requestWithProject = request.copy(projectId = projectId)
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(requestWithProject, changedBy))
    }

    @GetMapping
    fun listByProject(@PathVariable projectId: UUID): List<TaskResponse> = taskService.getAll(projectId)

    @GetMapping("/{taskId}")
    fun getTask(
        @PathVariable projectId: UUID,
        @PathVariable taskId: UUID,
    ): TaskResponse {
        val task = taskService.getById(taskId)
        if (task.projectId != projectId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task $taskId not found in project $projectId")
        }
        return task
    }

    @PutMapping("/{taskId}")
    fun updateTask(
        @PathVariable projectId: UUID,
        @PathVariable taskId: UUID,
        @RequestHeader("X-User-Id") changedBy: UUID,
        @RequestBody request: UpdateTaskRequest,
    ): TaskResponse {
        val task = taskService.getById(taskId)
        if (task.projectId != projectId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task $taskId not found in project $projectId")
        }
        return taskService.update(taskId, request, changedBy)
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTask(
        @PathVariable projectId: UUID,
        @PathVariable taskId: UUID,
        @RequestHeader("X-User-Id") changedBy: UUID,
    ) {
        val task = taskService.getById(taskId)
        if (task.projectId != projectId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task $taskId not found in project $projectId")
        }
        taskService.delete(taskId, changedBy)
    }
}
