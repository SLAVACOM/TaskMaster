package com.slavacom.organizationservice.orgtags

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OrganizationTagsService(
    private val repository: OrganizationTagsRepository,
    private val mapper: OrganizationTagsMapper
) {

    fun list(orgId: UUID): List<OrgTagResponse> =
        repository.findAllByOrganizationId(orgId).map { mapper.toResponse(it) }

    fun create(orgId: UUID, request: CreateOrgTagRequest): OrgTagResponse {
        val tag = mapper.fromCreateRequest(request)
        tag.organizationId = orgId
        return mapper.toResponse(repository.save(tag))
    }

    fun update(orgId: UUID, tagId: UUID, request: UpdateOrgTagRequest): OrgTagResponse {
        val tag = repository.findByIdAndOrganizationId(tagId, orgId)
            .orElseThrow { NoSuchElementException("Tag $tagId not found in organization $orgId") }
        request.name?.let { tag.name = it }
        request.color?.let { tag.color = it }
        request.description?.let { tag.description = it }
        return mapper.toResponse(repository.save(tag))
    }

    fun delete(orgId: UUID, tagId: UUID) {
        val tag = repository.findByIdAndOrganizationId(tagId, orgId)
            .orElseThrow { NoSuchElementException("Tag $tagId not found in organization $orgId") }
        repository.delete(tag)
    }
}
