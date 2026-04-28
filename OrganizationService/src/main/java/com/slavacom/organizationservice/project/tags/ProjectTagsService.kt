package com.slavacom.organizationservice.project.tags

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProjectTagsService(
    private val repository: ProjectTagsRepository,
    private val mapper: ProjectTagsMapper
) {

    fun list(projectId: UUID): List<ProjectTagResponse> =
        repository.findAllByProjectId(projectId).map { mapper.toResponse(it) }

    fun create(projectId: UUID, request: CreateProjectTagRequest): ProjectTagResponse {
        val tag = mapper.fromCreateRequest(request)
        tag.projectId = projectId
        return mapper.toResponse(repository.save(tag))
    }

    fun update(projectId: UUID, tagId: UUID, request: UpdateProjectTagRequest): ProjectTagResponse {
        val tag = repository.findByIdAndProjectId(tagId, projectId)
            .orElseThrow { NoSuchElementException("Tag $tagId not found in project $projectId") }
        request.name?.let { tag.name = it }
        request.color?.let { tag.color = it }
        request.description?.let { tag.description = it }
        return mapper.toResponse(repository.save(tag))
    }

    fun delete(projectId: UUID, tagId: UUID) {
        val tag = repository.findByIdAndProjectId(tagId, projectId)
            .orElseThrow { NoSuchElementException("Tag $tagId not found in project $projectId") }
        repository.delete(tag)
    }
}
