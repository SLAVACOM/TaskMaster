package com.slavacom.organizationservice.project.statuses

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProjectTaskStatusesService(
    private val repository: ProjectTaskStatusesRepository,
    private val mapper: ProjectTaskStatusesMapper
) {

    fun list(projectId: UUID): List<TaskStatusResponse> =
        repository.findAllByProjectIdOrderByOrderIndexAsc(projectId).map { mapper.toResponse(it) }

    fun create(projectId: UUID, request: CreateTaskStatusRequest): TaskStatusResponse {
        val status = mapper.fromCreateRequest(request)
        status.projectId = projectId
        return mapper.toResponse(repository.save(status))
    }

    fun update(projectId: UUID, statusId: UUID, request: UpdateTaskStatusRequest): TaskStatusResponse {
        val status = repository.findByIdAndProjectId(statusId, projectId)
            .orElseThrow { NoSuchElementException("Status $statusId not found in project $projectId") }
        request.name?.let { status.name = it }
        request.color?.let { status.color = it }
        request.orderIndex?.let { status.orderIndex = it }
        request.isInitial?.let { status.isInitial = it }
        request.isFinal?.let { status.isFinal = it }
        request.isActive?.let { status.isActive = it }
        return mapper.toResponse(repository.save(status))
    }

    fun delete(projectId: UUID, statusId: UUID) {
        val status = repository.findByIdAndProjectId(statusId, projectId)
            .orElseThrow { NoSuchElementException("Status $statusId not found in project $projectId") }
        status.isActive = false
        repository.save(status)
    }
}
