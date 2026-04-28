package com.slavacom.organizationservice.project.goals

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/projects/{projectId}/goals")
class ProjectGoalsController(private val service: ProjectGoalsService) {

    @GetMapping
    fun list(@PathVariable projectId: UUID): List<ProjectGoalResponse> = service.list(projectId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@PathVariable projectId: UUID, @RequestBody request: CreateProjectGoalRequest): ProjectGoalResponse =
        service.create(projectId, request)

    @PutMapping("/{goalId}")
    fun update(
        @PathVariable projectId: UUID,
        @PathVariable goalId: UUID,
        @RequestBody request: UpdateProjectGoalRequest
    ): ProjectGoalResponse = service.update(projectId, goalId, request)

    @DeleteMapping("/{goalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable projectId: UUID, @PathVariable goalId: UUID) = service.delete(projectId, goalId)
}
