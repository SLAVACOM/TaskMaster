package com.slavacom.taskservice.service

import com.slavacom.taskservice.dto.CreateTaskRequest
import com.slavacom.taskservice.dto.TaskHistoryResponse
import com.slavacom.taskservice.dto.TaskPageResponse
import com.slavacom.taskservice.dto.TaskResponse
import com.slavacom.taskservice.dto.TaskSearchRequest
import com.slavacom.taskservice.dto.UpdateTaskRequest
import com.slavacom.taskservice.entity.FieldChange
import com.slavacom.taskservice.entity.Task
import com.slavacom.taskservice.entity.TaskHistory
import com.slavacom.taskservice.entity.enums.HistoryAction
import com.slavacom.taskservice.mapper.TaskHistoryMapper
import com.slavacom.taskservice.mapper.TaskMapper
import com.slavacom.taskservice.repository.TaskHistoryRepository
import com.slavacom.taskservice.repository.TaskRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val taskMapper: TaskMapper,
    private val taskHistoryRepository: TaskHistoryRepository,
    private val taskHistoryMapper: TaskHistoryMapper,
) {

    private val allowedSortFields = setOf(
        "createdAt", "updatedAt", "name", "priority", "status", "deadline", "start"
    )

    @Transactional
    fun create(request: CreateTaskRequest, changedBy: UUID): TaskResponse {
        val task = Task(
            name = request.name.trim(),
            description = request.description,
            files = request.files,
            depends = request.depends,
            status = request.status,
            responsible = request.responsible,
            executor = request.executor,
            observers = request.observers,
            priority = request.priority,
            tags = request.tags,
            start = request.start,
            end = request.end,
            deadline = request.deadline,
            sprintId = request.sprintId,
            storyPoint = request.storyPoint,
        )
        val saved = taskRepository.save(task)
        taskHistoryRepository.save(
            TaskHistory(
                taskId = saved.id!!,
                changedBy = changedBy,
                action = HistoryAction.CREATED,
                changes = emptyList(),
            )
        )
        return taskMapper.toResponse(saved)
    }

    @Transactional(readOnly = true)
    fun getById(id: UUID): TaskResponse =
        taskRepository.findByIdAndIsActiveTrue(id)?.let(taskMapper::toResponse)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: $id")

    @Transactional(readOnly = true)
    fun getAll(): List<TaskResponse> =
        taskRepository.findByIsActiveTrueOrderByCreatedAtDesc().map(taskMapper::toResponse)

    @Transactional(readOnly = true)
    fun search(filter: TaskSearchRequest): TaskPageResponse {
        val normalizedSortBy = if (filter.sortBy in allowedSortFields) filter.sortBy else "createdAt"
        val direction = if (filter.sortDir.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(
            filter.page.coerceAtLeast(0),
            filter.size.coerceIn(1, 200),
            Sort.by(direction, normalizedSortBy),
        )
        val result = taskRepository.searchTasks(filter, pageable)
        return TaskPageResponse(
            content = result.content.map(taskMapper::toResponse),
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            hasNext = result.hasNext(),
            hasPrevious = result.hasPrevious(),
        )
    }

    @Transactional(readOnly = true)
    fun getAllBySprintId(sprintId: UUID): List<TaskResponse> =
        taskRepository.findBySprintIdAndIsActiveTrueOrderByCreatedAtDesc(sprintId).map(taskMapper::toResponse)

    @Transactional(readOnly = true)
    fun getAllByExecutor(executor: UUID): List<TaskResponse> =
        taskRepository.findByExecutorAndIsActiveTrueOrderByCreatedAtDesc(executor).map(taskMapper::toResponse)

    @Transactional
    fun update(id: UUID, request: UpdateTaskRequest, changedBy: UUID): TaskResponse {
        val task = taskRepository.findByIdAndIsActiveTrue(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: $id")

        val changes = mutableListOf<FieldChange>()

        request.name?.let {
            if (it.isBlank()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Task name cannot be blank")
            val v = it.trim()
            if (v != task.name) changes += FieldChange("name", task.name, v)
            task.name = v
        }
        request.description?.let {
            if (it != task.description) changes += FieldChange("description", task.description, it)
            task.description = it
        }
        request.files?.let {
            if (it != task.files) changes += FieldChange("files", task.files.toString(), it.toString())
            task.files = it
        }
        request.depends?.let {
            if (it != task.depends) changes += FieldChange("depends", task.depends.toString(), it.toString())
            task.depends = it
        }
        request.status?.let {
            if (it != task.status) changes += FieldChange("status", task.status.name, it.name)
            task.status = it
        }
        request.responsible?.let {
            if (it != task.responsible) changes += FieldChange("responsible", task.responsible?.toString(), it.toString())
            task.responsible = it
        }
        request.executor?.let {
            if (it != task.executor) changes += FieldChange("executor", task.executor?.toString(), it.toString())
            task.executor = it
        }
        request.observers?.let {
            if (it != task.observers) changes += FieldChange("observers", task.observers.toString(), it.toString())
            task.observers = it
        }
        request.priority?.let {
            if (it != task.priority) changes += FieldChange("priority", task.priority.name, it.name)
            task.priority = it
        }
        request.tags?.let {
            if (it != task.tags) changes += FieldChange("tags", task.tags.toString(), it.toString())
            task.tags = it
        }
        request.start?.let {
            if (it != task.start) changes += FieldChange("start", task.start?.toString(), it.toString())
            task.start = it
        }
        request.end?.let {
            if (it != task.end) changes += FieldChange("end", task.end?.toString(), it.toString())
            task.end = it
        }
        request.deadline?.let {
            if (it != task.deadline) changes += FieldChange("deadline", task.deadline?.toString(), it.toString())
            task.deadline = it
        }
        request.sprintId?.let {
            if (it != task.sprintId) changes += FieldChange("sprintId", task.sprintId?.toString(), it.toString())
            task.sprintId = it
        }
        request.storyPoint?.let {
            if (it != task.storyPoint) changes += FieldChange("storyPoint", task.storyPoint?.toString(), it.toString())
            task.storyPoint = it
        }
        request.isActive?.let {
            if (it != task.isActive) changes += FieldChange("isActive", task.isActive.toString(), it.toString())
            task.isActive = it
        }

        val saved = taskRepository.save(task)

        if (changes.isNotEmpty()) {
            taskHistoryRepository.save(
                TaskHistory(
                    taskId = saved.id!!,
                    changedBy = changedBy,
                    action = HistoryAction.UPDATED,
                    changes = changes,
                )
            )
        }
        return taskMapper.toResponse(saved)
    }

    @Transactional
    fun delete(id: UUID, changedBy: UUID) {
        val task = taskRepository.findByIdAndIsActiveTrue(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: $id")
        task.isActive = false
        taskRepository.save(task)
        taskHistoryRepository.save(
            TaskHistory(
                taskId = id,
                changedBy = changedBy,
                action = HistoryAction.DELETED,
                changes = emptyList(),
            )
        )
    }

    @Transactional(readOnly = true)
    fun getHistory(taskId: UUID): List<TaskHistoryResponse> {
        taskRepository.findById(taskId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: $taskId") }
        return taskHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId)
            .map(taskHistoryMapper::toResponse)
    }
}
