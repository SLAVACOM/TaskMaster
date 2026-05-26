package com.slavacom.organizationservice.invitation

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
class InvitationController(private val service: InvitationService) {

    @PostMapping("/api/organizations/{orgId}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    fun invite(
        @PathVariable orgId: UUID,
        @RequestHeader("X-User-Id") userId: UUID,
        @Valid @RequestBody request: CreateInvitationRequest
    ): InvitationResponse = service.invite(orgId, request, userId)

    @GetMapping("/api/organizations/{orgId}/invitations")
    fun list(@PathVariable orgId: UUID): List<InvitationResponse> =
        service.list(orgId)

    @GetMapping("/api/invitations/my")
    fun listMy(@RequestHeader("X-User-Id") userId: UUID): List<InvitationResponse> =
        service.listMy(userId)

    @PutMapping("/api/invitations/{id}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun accept(@PathVariable id: UUID) = service.accept(id)

    @PutMapping("/api/invitations/{id}/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun decline(@PathVariable id: UUID) = service.decline(id)

}
