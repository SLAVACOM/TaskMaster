package com.slavacom.organizationservice.orgtags

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/organizations/{orgId}/tags")
class OrganizationTagsController(private val service: OrganizationTagsService) {

    @GetMapping
    fun list(@PathVariable orgId: UUID): List<OrgTagResponse> = service.list(orgId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable orgId: UUID,
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestBody request: CreateOrgTagRequest
    ): OrgTagResponse = service.create(orgId, userId, request)

    @PutMapping("/{tagId}")
    fun update(
        @PathVariable orgId: UUID,
        @PathVariable tagId: UUID,
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestBody request: UpdateOrgTagRequest
    ): OrgTagResponse = service.update(orgId, userId, tagId, request)

    @DeleteMapping("/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable orgId: UUID,
        @PathVariable tagId: UUID,
        @RequestHeader("X-User-Id") userId: UUID
    ) = service.delete(orgId, userId, tagId)
}
