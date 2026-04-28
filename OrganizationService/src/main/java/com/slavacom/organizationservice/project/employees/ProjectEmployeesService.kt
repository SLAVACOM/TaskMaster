package com.slavacom.organizationservice.project.employees

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProjectEmployeesService(
    private val repository: ProjectEmployeesRepository,
    private val mapper: ProjectEmployeesMapper
) {

    fun list(projectId: UUID): List<ProjectEmployeeResponse> =
        repository.findAllByProjectIdAndIsActiveTrue(projectId).map { mapper.toResponse(it) }

    fun add(projectId: UUID, request: AddProjectEmployeeRequest): ProjectEmployeeResponse {
        val employee = mapper.fromAddRequest(request)
        employee.projectId = projectId
        return mapper.toResponse(repository.save(employee))
    }

    fun update(projectId: UUID, id: UUID, request: UpdateProjectEmployeeRequest): ProjectEmployeeResponse {
        val employee = repository.findByIdAndProjectId(id, projectId)
            .orElseThrow { NoSuchElementException("Employee $id not found in project $projectId") }
        request.role?.let { employee.role = it }
        request.permissions?.let { employee.permissions = it }
        return mapper.toResponse(repository.save(employee))
    }

    fun remove(projectId: UUID, id: UUID) {
        val employee = repository.findByIdAndProjectId(id, projectId)
            .orElseThrow { NoSuchElementException("Employee $id not found in project $projectId") }
        employee.isActive = false
        repository.save(employee)
    }
}
