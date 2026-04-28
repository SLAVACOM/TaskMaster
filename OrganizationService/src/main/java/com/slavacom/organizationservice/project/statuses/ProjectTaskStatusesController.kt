package com.slavacom.organizationservice.project.statuses

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/projects/{projectId}/statuses")
class ProjectTaskStatusesController(private val service: ProjectTaskStatusesService) {

    @GetMapping
    fun list(@PathVariable projectId: UUID): List<TaskStatusResponse> = service.list(projectId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@PathVariable projectId: UUID, @RequestBody request: CreateTaskStatusRequest): TaskStatusResponse =
        service.create(projectId, request)

    @PutMapping("/{statusId}")
    fun update(
        @PathVariable projectId: UUID,
        @PathVariable statusId: UUID,
        @RequestBody request: UpdateTaskStatusRequest
    ): TaskStatusResponse = service.update(projectId, statusId, request)

    @DeleteMapping("/{statusId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable projectId: UUID, @PathVariable statusId: UUID) = service.delete(projectId, statusId)
}
