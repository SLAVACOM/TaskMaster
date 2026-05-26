package com.slavacom.taskservice.controller

import com.slavacom.taskservice.dto.CreateTaskRequest
import com.slavacom.taskservice.dto.TaskHistoryResponse
import com.slavacom.taskservice.dto.TaskPageResponse
import com.slavacom.taskservice.dto.TaskResponse
import com.slavacom.taskservice.dto.TaskSearchRequest
import com.slavacom.taskservice.dto.UpdateTaskRequest
import com.slavacom.taskservice.mapper.TaskMapper
import com.slavacom.taskservice.service.TaskFilteringService
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
    private val taskFilteringService: TaskFilteringService,
    private val taskMapper: TaskMapper,
) {

    @PostMapping
    fun create(
        @RequestHeader("X-User-Id") changedBy: UUID,
        @RequestHeader("X-Organization-Id", required = false) organizationId: UUID?,
        @Valid @RequestBody request: CreateTaskRequest,
    ): ResponseEntity<TaskResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request, changedBy, organizationId))

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): TaskResponse = taskService.getById(id)

    @GetMapping
    fun getAll(
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestHeader("X-Organization-Id", required = false) organizationId: UUID?,
        @ModelAttribute filter: TaskSearchRequest,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDir: String,
    ): TaskPageResponse = taskFilteringService.getAccessibleTasks(userId, organizationId, filter, page, size, sortBy, sortDir)

    @GetMapping("/search")
    @Deprecated("Use GET /api/tasks with filters instead")
    fun search(
        @RequestHeader("X-Organization-Id") organizationId: UUID,
        @ModelAttribute filter: TaskSearchRequest,
    ): TaskPageResponse = taskService.searchByOrgAndProject(filter, organizationId)

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

    // ===== PHASE 1: Dashboard & Analytics =====

    @GetMapping("/my")
    fun getMyTasks(@RequestHeader("X-User-Id") userId: UUID): List<TaskResponse> =
        taskService.getMyTasks(userId)

    @GetMapping("/search-text")
    fun searchTasks(
        @RequestParam projectId: UUID,
        @RequestParam q: String,
    ): List<TaskResponse> = taskService.searchTasks(projectId, q)

    // ===== PHASE 2: Task Assignment & Workflow =====

    @PostMapping("/{taskId}/assign")
    fun assignTask(
        @PathVariable taskId: UUID,
        @RequestHeader("X-User-Id") changedBy: UUID,
        @RequestBody request: Map<String, UUID>,
    ): TaskResponse {
        val assigneeId = request["assigneeId"] ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing assigneeId")
        return taskService.assignTask(taskId, assigneeId, changedBy)
    }

    @PostMapping("/{taskId}/unassign")
    fun unassignTask(
        @PathVariable taskId: UUID,
        @RequestHeader("X-User-Id") changedBy: UUID,
    ): TaskResponse = taskService.unassignTask(taskId, changedBy)

    @PostMapping("/{taskId}/watchers")
    fun addWatcher(
        @PathVariable taskId: UUID,
        @RequestHeader("X-User-Id") changedBy: UUID,
        @RequestBody request: Map<String, UUID>,
    ): TaskResponse {
        val watcherId = request["watcherId"] ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing watcherId")
        return taskService.addWatcher(taskId, watcherId, changedBy)
    }

    @DeleteMapping("/{taskId}/watchers/{watcherId}")
    fun removeWatcher(
        @PathVariable taskId: UUID,
        @PathVariable watcherId: UUID,
        @RequestHeader("X-User-Id") changedBy: UUID,
    ): TaskResponse = taskService.removeWatcher(taskId, watcherId, changedBy)

    @PostMapping("/{taskId}/transition")
    fun transitionStatus(
        @PathVariable taskId: UUID,
        @RequestHeader("X-User-Id") changedBy: UUID,
        @RequestBody request: Map<String, String>,
    ): TaskResponse {
        val statusStr = request["status"] ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing status")
        return taskService.transitionStatus(taskId, statusStr, changedBy)
    }

}

// Project-scoped task endpoints
@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
class ProjectTaskController(
    private val taskService: TaskService,
    private val taskFilteringService: TaskFilteringService,
    private val taskRepository: TaskRepository,
    private val taskMapper: TaskMapper,
) {

    @PostMapping
    fun create(
        @PathVariable projectId: UUID,
        @RequestHeader("X-User-Id") changedBy: UUID,
        @RequestHeader("X-Organization-Id", required = false) organizationId: UUID?,
        @Valid @RequestBody request: CreateTaskRequest,
    ): ResponseEntity<TaskResponse> {
        val requestWithProject = request.copy(projectId = projectId)
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(requestWithProject, changedBy, organizationId))
    }

    @GetMapping
    fun listByProject(
        @PathVariable projectId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDir: String,
    ): TaskPageResponse {
        val sort = if (sortDir.equals("asc", ignoreCase = true)) Sort.by(sortBy).ascending()
                   else Sort.by(sortBy).descending()
        val pageable = PageRequest.of(page, size, sort)
        val taskPage = taskRepository.findByProjectIdAndIsActiveTrue(projectId, pageable)
        return TaskPageResponse(
            content = taskPage.content.map(taskMapper::toResponse),
            page = taskPage.number,
            size = taskPage.size,
            totalElements = taskPage.totalElements,
            totalPages = taskPage.totalPages,
            hasNext = taskPage.hasNext(),
            hasPrevious = taskPage.hasPrevious(),
        )
    }

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

    // ===== PHASE 1: Dashboard endpoint for project =====

    @GetMapping("/dashboard")
    fun projectDashboard(@PathVariable projectId: UUID): Map<String, Any> =
        taskService.getProjectDashboard(projectId)
}

// Sprint-scoped endpoints (for Phase 1 dashboard)
@RestController
@RequestMapping("/api/sprints/{sprintId}")
class SprintTaskController(
    private val taskService: TaskService,
) {

    @GetMapping("/dashboard")
    fun sprintDashboard(@PathVariable sprintId: UUID): Map<String, Any> =
        taskService.getSprintDashboard(sprintId)
}
