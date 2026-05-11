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
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

private val logger = KotlinLogging.logger {}

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
        val startTime = System.currentTimeMillis()
        logger.info { "Creating task: name=${request.name}, projectId=${request.projectId}, changedBy=$changedBy" }

        try {
            val task = Task(
                name = request.name.trim(),
                description = request.description,
                files = request.files,
                depends = request.depends,
                status = request.status,
                responsible = request.responsible,
                executor = request.executor,
                observers = request.observers,
                watchers = request.watchers,
                priority = request.priority,
                tags = request.tags,
                start = request.start,
                end = request.end,
                deadline = request.deadline,
                sprintId = request.sprintId,
                projectId = request.projectId,
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
            val duration = System.currentTimeMillis() - startTime
            logger.info { "Task created successfully: taskId=${saved.id}, duration=${duration}ms" }
            return taskMapper.toResponse(saved)
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            logger.error(e) { "Failed to create task in ${duration}ms: ${e.message}" }
            throw e
        }
    }

    @Transactional(readOnly = true)
    fun getById(id: UUID): TaskResponse {
        val startTime = System.currentTimeMillis()
        logger.debug { "Fetching task: taskId=$id" }
        return taskRepository.findByIdAndIsActiveTrue(id)?.let {
            val duration = System.currentTimeMillis() - startTime
            logger.debug { "Task fetched in ${duration}ms: taskId=$id" }
            taskMapper.toResponse(it)
        } ?: run {
            logger.warn { "Task not found: taskId=$id" }
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: $id")
        }
    }

    @Transactional(readOnly = true)
    fun getAll(projectId: UUID? = null): List<TaskResponse> {
        val startTime = System.currentTimeMillis()
        logger.debug { "Fetching all tasks: projectId=$projectId" }

        val tasks = if (projectId != null)
            taskRepository.findByProjectIdAndIsActiveTrueOrderByCreatedAtDesc(projectId)
        else
            taskRepository.findByIsActiveTrueOrderByCreatedAtDesc()
        val duration = System.currentTimeMillis() - startTime
        logger.info { "Retrieved ${tasks.size} tasks in ${duration}ms: projectId=$projectId" }
        return tasks.map(taskMapper::toResponse)
    }

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
        request.watchers?.let {
            if (it != task.watchers) changes += FieldChange("watchers", task.watchers.toString(), it.toString())
            task.watchers = it
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
        request.projectId?.let {
            if (it != task.projectId) changes += FieldChange("projectId", task.projectId?.toString(), it.toString())
            task.projectId = it
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

    // ===== PHASE 1: Dashboard & Analytics =====

    @Transactional(readOnly = true)
    fun getMyTasks(userId: UUID): List<TaskResponse> {
        val assignedTasks = taskRepository.findByExecutorAndIsActiveTrueOrderByCreatedAtDesc(userId)
        return assignedTasks.map(taskMapper::toResponse)
    }

    @Transactional(readOnly = true)
    fun getProjectDashboard(projectId: UUID): Map<String, Any> {
        val allTasks = taskRepository.findByProjectIdAndIsActiveTrue(projectId)
        val todoCount = allTasks.count { it.status == "TODO" }
        val inProgressCount = allTasks.count { it.status == "IN_PROGRESS" }
        val doneCount = allTasks.count { it.status == "DONE" }

        val tasksByPriority = allTasks.groupingBy { it.priority }.eachCount()
        val tasksByStatus = allTasks.groupingBy { it.status }.eachCount()
        val activeTasks = allTasks.filter { it.status != "DONE" }.sortedByDescending { it.createdAt }

        return mapOf(
            "projectId" to projectId,
            "totalTasks" to allTasks.size,
            "todoCount" to todoCount,
            "inProgressCount" to inProgressCount,
            "doneCount" to doneCount,
            "completionPercentage" to (if (allTasks.isNotEmpty()) (doneCount * 100) / allTasks.size else 0),
            "tasksByPriority" to tasksByPriority,
            "tasksByStatus" to tasksByStatus,
            "activeTasks" to activeTasks.take(10).map(taskMapper::toResponse),
        )
    }

    @Transactional(readOnly = true)
    fun getSprintDashboard(sprintId: UUID): Map<String, Any> {
        val allTasks = taskRepository.findBySprintIdAndIsActiveTrue(sprintId)
        val completedCount = allTasks.count { it.status == "DONE" }
        val todoCount = allTasks.count { it.status == "TODO" }
        val inProgressCount = allTasks.count { it.status == "IN_PROGRESS" }

        val tasksByPriority = allTasks.groupingBy { it.priority }.eachCount()
        val tasksByStatus = allTasks.groupingBy { it.status }.eachCount()
        val remainingTasks = allTasks.filter { it.status != "DONE" }.sortedByDescending { it.createdAt }
        val overdueTasks = remainingTasks.filter { it.deadline != null && it.deadline!! < java.time.Instant.now() }

        return mapOf(
            "sprintId" to sprintId,
            "totalTasks" to allTasks.size,
            "completedTasks" to completedCount,
            "todoTasks" to todoCount,
            "inProgressTasks" to inProgressCount,
            "completionPercentage" to (if (allTasks.isNotEmpty()) (completedCount * 100) / allTasks.size else 0),
            "tasksByPriority" to tasksByPriority,
            "tasksByStatus" to tasksByStatus,
            "remainingTasks" to remainingTasks.take(20).map(taskMapper::toResponse),
            "overdueTasks" to overdueTasks.map(taskMapper::toResponse),
        )
    }

    @Transactional(readOnly = true)
    fun searchTasks(projectId: UUID, query: String): List<TaskResponse> {
        val allProjectTasks = taskRepository.findByProjectIdAndIsActiveTrue(projectId)
        val lowerQuery = query.lowercase()
        return allProjectTasks.filter { task ->
            task.name.lowercase().contains(lowerQuery) ||
            (task.description?.lowercase()?.contains(lowerQuery) ?: false)
        }.map(taskMapper::toResponse)
    }

    // ===== PHASE 2: Task Assignment & Workflow =====

    @Transactional
    fun assignTask(taskId: UUID, assigneeId: UUID, changedBy: UUID): TaskResponse {
        val task = taskRepository.findByIdAndIsActiveTrue(taskId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: $taskId")

        val changes = mutableListOf<FieldChange>()
        if (assigneeId != task.executor) {
            changes += FieldChange("executor", task.executor?.toString(), assigneeId.toString())
            task.executor = assigneeId
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
    fun unassignTask(taskId: UUID, changedBy: UUID): TaskResponse {
        val task = taskRepository.findByIdAndIsActiveTrue(taskId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: $taskId")

        val changes = mutableListOf<FieldChange>()
        if (task.executor != null) {
            changes += FieldChange("executor", task.executor?.toString(), null)
            task.executor = null
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
    fun addWatcher(taskId: UUID, watcherId: UUID, changedBy: UUID): TaskResponse {
        val task = taskRepository.findByIdAndIsActiveTrue(taskId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: $taskId")

        val changes = mutableListOf<FieldChange>()
        if (!task.watchers.contains(watcherId)) {
            val updatedWatchers = task.watchers + watcherId
            changes += FieldChange("watchers", task.watchers.toString(), updatedWatchers.toString())
            task.watchers = updatedWatchers
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
    fun removeWatcher(taskId: UUID, watcherId: UUID, changedBy: UUID): TaskResponse {
        val task = taskRepository.findByIdAndIsActiveTrue(taskId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: $taskId")

        val changes = mutableListOf<FieldChange>()
        if (task.watchers.contains(watcherId)) {
            val updatedWatchers = task.watchers - watcherId
            changes += FieldChange("watchers", task.watchers.toString(), updatedWatchers.toString())
            task.watchers = updatedWatchers
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
    fun transitionStatus(taskId: UUID, newStatus: String, changedBy: UUID): TaskResponse {
        val task = taskRepository.findByIdAndIsActiveTrue(taskId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: $taskId")

        val changes = mutableListOf<FieldChange>()
        if (newStatus != task.status) {
            changes += FieldChange("status", task.status, newStatus)
            task.status = newStatus
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
    fun addComment(taskId: UUID, comment: String, changedBy: UUID): TaskHistoryResponse {
        val task = taskRepository.findByIdAndIsActiveTrue(taskId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: $taskId")

        val history = taskHistoryRepository.save(
            TaskHistory(
                taskId = task.id!!,
                changedBy = changedBy,
                action = HistoryAction.COMMENT,
                changes = listOf(FieldChange("comment", "", comment)),
            )
        )
        return taskHistoryMapper.toResponse(history)
    }
}
