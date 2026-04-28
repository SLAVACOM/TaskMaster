package com.slavacom.organizationservice.project.employees

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/projects/{projectId}/employees")
class ProjectEmployeesController(private val service: ProjectEmployeesService) {

    @GetMapping
    fun list(@PathVariable projectId: UUID): List<ProjectEmployeeResponse> = service.list(projectId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun add(@PathVariable projectId: UUID, @RequestBody request: AddProjectEmployeeRequest): ProjectEmployeeResponse =
        service.add(projectId, request)

    @PutMapping("/{id}")
    fun update(
        @PathVariable projectId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: UpdateProjectEmployeeRequest
    ): ProjectEmployeeResponse = service.update(projectId, id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remove(@PathVariable projectId: UUID, @PathVariable id: UUID) = service.remove(projectId, id)
}
