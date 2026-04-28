package com.slavacom.taskservice.controller

import com.slavacom.taskservice.dto.CreateTaskRequest
import com.slavacom.taskservice.dto.TaskPageResponse
import com.slavacom.taskservice.dto.TaskResponse
import com.slavacom.taskservice.dto.TaskSearchRequest
import com.slavacom.taskservice.dto.UpdateTaskRequest
import com.slavacom.taskservice.security.JwtTokenProvider
import com.slavacom.taskservice.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService,
    private val jwtTokenProvider: JwtTokenProvider,
) {

    @PostMapping
    fun create(
        @RequestHeader("Authorization") token: String,
        @Valid @RequestBody request: CreateTaskRequest,
    ): ResponseEntity<TaskResponse> {
        val changedBy = jwtTokenProvider.extractUserId(token)
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request, changedBy))
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): TaskResponse = taskService.getById(id)

    @GetMapping
    fun getAll(): List<TaskResponse> = taskService.getAll()

    @GetMapping("/search")
    fun search(@ModelAttribute filter: TaskSearchRequest): TaskPageResponse =
        taskService.search(filter)

    @PutMapping("/{id}")
    fun update(
        @RequestHeader("Authorization") token: String,
        @PathVariable id: UUID,
        @RequestBody request: UpdateTaskRequest,
    ): TaskResponse {
        val changedBy = jwtTokenProvider.extractUserId(token)
        return taskService.update(id, request, changedBy)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @RequestHeader("Authorization") token: String,
        @PathVariable id: UUID,
    ) {
        val changedBy = jwtTokenProvider.extractUserId(token)
        taskService.delete(id, changedBy)
    }
}
