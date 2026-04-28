package com.slavacom.organizationservice.project.sprints

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/projects/{projectId}/sprints")
class ProjectSprintsController(private val service: ProjectSprintsService) {

    @GetMapping
    fun list(@PathVariable projectId: UUID): List<SprintResponse> = service.list(projectId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@PathVariable projectId: UUID, @RequestBody request: CreateSprintRequest): SprintResponse =
        service.create(projectId, request)

    @PutMapping("/{sprintId}")
    fun update(
        @PathVariable projectId: UUID,
        @PathVariable sprintId: UUID,
        @RequestBody request: UpdateSprintRequest
    ): SprintResponse = service.update(projectId, sprintId, request)

    @PutMapping("/{sprintId}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun activate(@PathVariable projectId: UUID, @PathVariable sprintId: UUID) =
        service.activate(projectId, sprintId)

    @PutMapping("/{sprintId}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun complete(@PathVariable projectId: UUID, @PathVariable sprintId: UUID) =
        service.complete(projectId, sprintId)
}
