package com.slavacom.organizationservice.invitation

import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
class InvitationController(private val service: InvitationService) {

    @PostMapping("/api/organizations/{orgId}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    fun invite(
        @PathVariable orgId: UUID,
        @RequestBody request: CreateInvitationRequest
    ): InvitationResponse = service.invite(orgId, request, currentUserId())

    @GetMapping("/api/organizations/{orgId}/invitations")
    fun list(@PathVariable orgId: UUID): List<InvitationResponse> =
        service.list(orgId)

    @PutMapping("/api/invitations/{id}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun accept(@PathVariable id: UUID) = service.accept(id)

    @PutMapping("/api/invitations/{id}/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun decline(@PathVariable id: UUID) = service.decline(id)

    private fun currentUserId(): UUID =
        UUID.fromString(SecurityContextHolder.getContext().authentication.principal as String)
}
