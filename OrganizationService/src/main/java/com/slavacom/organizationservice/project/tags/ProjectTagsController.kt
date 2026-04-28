package com.slavacom.organizationservice.project.tags

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/projects/{projectId}/tags")
class ProjectTagsController(private val service: ProjectTagsService) {

    @GetMapping
    fun list(@PathVariable projectId: UUID): List<ProjectTagResponse> = service.list(projectId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@PathVariable projectId: UUID, @RequestBody request: CreateProjectTagRequest): ProjectTagResponse =
        service.create(projectId, request)

    @PutMapping("/{tagId}")
    fun update(
        @PathVariable projectId: UUID,
        @PathVariable tagId: UUID,
        @RequestBody request: UpdateProjectTagRequest
    ): ProjectTagResponse = service.update(projectId, tagId, request)

    @DeleteMapping("/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable projectId: UUID, @PathVariable tagId: UUID) = service.delete(projectId, tagId)
}
