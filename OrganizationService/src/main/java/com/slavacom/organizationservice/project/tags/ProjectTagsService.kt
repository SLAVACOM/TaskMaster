package com.slavacom.organizationservice.project.tags

import com.slavacom.organizationservice.util.AuthorizationHelper
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProjectTagsService(
    private val repository: ProjectTagsRepository,
    private val mapper: ProjectTagsMapper,
    private val authorizationHelper: AuthorizationHelper
) {

    fun list(projectId: UUID): List<ProjectTagResponse> =
        repository.findAllByProjectId(projectId).map { mapper.toResponse(it) }

    fun create(projectId: UUID, request: CreateProjectTagRequest): ProjectTagResponse {
        val tag = mapper.fromCreateRequest(request)
        tag.projectId = projectId
        return mapper.toResponse(repository.save(tag))
    }

    fun create(projectId: UUID, userId: UUID, request: CreateProjectTagRequest): ProjectTagResponse {
        authorizationHelper.checkProjectTagCreatePermission(userId, projectId)
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

    fun update(projectId: UUID, userId: UUID, tagId: UUID, request: UpdateProjectTagRequest): ProjectTagResponse {
        authorizationHelper.checkProjectTagCreatePermission(userId, projectId)
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

    fun delete(projectId: UUID, userId: UUID, tagId: UUID) {
        authorizationHelper.checkProjectTagCreatePermission(userId, projectId)
        val tag = repository.findByIdAndProjectId(tagId, projectId)
            .orElseThrow { NoSuchElementException("Tag $tagId not found in project $projectId") }
        repository.delete(tag)
    }
}
