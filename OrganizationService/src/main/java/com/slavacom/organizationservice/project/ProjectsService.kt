package com.slavacom.organizationservice.project

import org.springframework.stereotype.Service
import java.util.*

@Service
class ProjectsService(
    private val repository: ProjectsRepository,
    private val mapper: ProjectsMapper
) {

    fun listByOrg(orgId: UUID, isActive: Boolean = true): List<ProjectResponse> {
        val projects = if (isActive) repository.findAllByOrganizationIdAndIsActiveTrue(orgId)
        else repository.findAllByOrganizationId(orgId)
        return projects.map { mapper.toResponse(it) }
    }

    fun getById(id: UUID): ProjectResponse =
        repository.findById(id)
            .map { mapper.toResponse(it) }
            .orElseThrow { NoSuchElementException("Project $id not found") }

    fun create(orgId: UUID, userId: UUID?, request: CreateProjectRequest): ProjectResponse {
        val project = mapper.fromCreateRequest(request)
        project.organizationId = orgId
        project.responsible = project.responsible ?: userId
        return mapper.toResponse(repository.save(project))
    }

    fun update(id: UUID, request: UpdateProjectRequest): ProjectResponse {
        val project = repository.findById(id)
            .orElseThrow { NoSuchElementException("Project $id not found") }
        request.name?.let { project.name = it }
        request.description?.let { project.description = it }
        request.responsible?.let { project.responsible = it }
        request.isActive?.let { project.isActive = it }
        return mapper.toResponse(repository.save(project))
    }

    fun deactivate(id: UUID) {
        val project = repository.findById(id)
            .orElseThrow { NoSuchElementException("Project $id not found") }
        project.isActive = false
        repository.save(project)
    }
}
