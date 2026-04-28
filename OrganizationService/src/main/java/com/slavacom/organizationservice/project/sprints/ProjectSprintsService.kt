package com.slavacom.organizationservice.project.sprints

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProjectSprintsService(
    private val repository: ProjectSprintsRepository,
    private val mapper: ProjectSprintsMapper
) {

    fun list(projectId: UUID): List<SprintResponse> =
        repository.findAllByProjectId(projectId).map { mapper.toResponse(it) }

    fun create(projectId: UUID, request: CreateSprintRequest): SprintResponse {
        val sprint = mapper.fromCreateRequest(request)
        sprint.projectId = projectId
        return mapper.toResponse(repository.save(sprint))
    }

    fun update(projectId: UUID, sprintId: UUID, request: UpdateSprintRequest): SprintResponse {
        val sprint = repository.findByIdAndProjectId(sprintId, projectId)
            .orElseThrow { NoSuchElementException("Sprint $sprintId not found in project $projectId") }
        request.name?.let { sprint.name = it }
        request.description?.let { sprint.description = it }
        request.startDate?.let { sprint.startDate = it }
        request.endDate?.let { sprint.endDate = it }
        return mapper.toResponse(repository.save(sprint))
    }

    fun activate(projectId: UUID, sprintId: UUID) {
        repository.findAllByProjectIdAndIsActiveTrue(projectId).forEach { active ->
            active.isActive = false
            active.status = "COMPLETED"
            repository.save(active)
        }
        val sprint = repository.findByIdAndProjectId(sprintId, projectId)
            .orElseThrow { NoSuchElementException("Sprint $sprintId not found in project $projectId") }
        sprint.isActive = true
        sprint.status = "ACTIVE"
        repository.save(sprint)
    }

    fun complete(projectId: UUID, sprintId: UUID) {
        val sprint = repository.findByIdAndProjectId(sprintId, projectId)
            .orElseThrow { NoSuchElementException("Sprint $sprintId not found in project $projectId") }
        sprint.isActive = false
        sprint.status = "COMPLETED"
        repository.save(sprint)
    }
}
