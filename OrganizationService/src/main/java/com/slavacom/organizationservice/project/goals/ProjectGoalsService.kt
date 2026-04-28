package com.slavacom.organizationservice.project.goals

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProjectGoalsService(
    private val repository: ProjectGoalsRepository,
    private val mapper: ProjectGoalsMapper
) {

    fun list(projectId: UUID): List<ProjectGoalResponse> =
        repository.findAllByProjectId(projectId).map { mapper.toResponse(it) }

    fun create(projectId: UUID, request: CreateProjectGoalRequest): ProjectGoalResponse {
        val goal = mapper.fromCreateRequest(request)
        goal.projectId = projectId
        return mapper.toResponse(repository.save(goal))
    }

    fun update(projectId: UUID, goalId: UUID, request: UpdateProjectGoalRequest): ProjectGoalResponse {
        val goal = repository.findByIdAndProjectId(goalId, projectId)
            .orElseThrow { NoSuchElementException("Goal $goalId not found in project $projectId") }
        request.name?.let { goal.name = it }
        request.description?.let { goal.description = it }
        request.targetDate?.let { goal.targetDate = it }
        request.progress?.let { goal.progress = it }
        request.isCompleted?.let { goal.isCompleted = it }
        request.responsible?.let { goal.responsible = it }
        return mapper.toResponse(repository.save(goal))
    }

    fun delete(projectId: UUID, goalId: UUID) {
        val goal = repository.findByIdAndProjectId(goalId, projectId)
            .orElseThrow { NoSuchElementException("Goal $goalId not found in project $projectId") }
        repository.delete(goal)
    }
}
