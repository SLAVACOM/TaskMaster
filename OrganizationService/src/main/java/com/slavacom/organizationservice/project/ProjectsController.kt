package com.slavacom.organizationservice.project

import org.osgi.annotation.bundle.Headers
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
class ProjectsController(private val service: ProjectsService) {

    @GetMapping("/api/organizations/{orgId}/projects")
    fun listByOrg(
        @PathVariable orgId: UUID,
        @RequestParam(defaultValue = "true") isActive: Boolean
    ): List<ProjectResponse> = service.listByOrg(orgId, isActive)

    @PostMapping("/api/organizations/{orgId}/projects")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable orgId: UUID,
        @RequestBody request: CreateProjectRequest,
        @RequestHeader("X-User-Id") userId: UUID
    ): ProjectResponse =
        service.create(orgId, userId, request)

    @GetMapping("/api/projects/{id}")
    fun getById(@PathVariable id: UUID): ProjectResponse = service.getById(id)

    @PutMapping("/api/projects/{id}")
    fun update(@PathVariable id: UUID, @RequestBody request: UpdateProjectRequest): ProjectResponse =
        service.update(id, request)

    @DeleteMapping("/api/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deactivate(@PathVariable id: UUID) = service.deactivate(id)
}
