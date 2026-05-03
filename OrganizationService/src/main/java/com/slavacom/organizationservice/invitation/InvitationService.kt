package com.slavacom.organizationservice.invitation

import com.slavacom.organizationservice.employees.AddEmployeeRequest
import com.slavacom.organizationservice.employees.EmployeesService
import com.slavacom.organizationservice.entity.InvitationStatus
import com.slavacom.organizationservice.exception.InvitationAlreadyProcessedException
import com.slavacom.organizationservice.exception.InvitationExpiredException
import com.slavacom.organizationservice.exception.InvitationNotFoundException
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class InvitationService(
    private val invitationRepository: InvitationRepository,
    private val invitationMapper: InvitationMapper,
    private val employeesService: EmployeesService
) {

    fun invite(orgId: UUID, request: CreateInvitationRequest, invitedBy: UUID): InvitationResponse {
        val invitation = invitationMapper.fromCreateRequest(request)
        invitation.organizationId = orgId
        invitation.invitedByUserId = invitedBy
        invitation.status = InvitationStatus.PENDING
        return invitationMapper.toResponse(invitationRepository.save(invitation))
    }

    fun list(orgId: UUID): List<InvitationResponse> =
        invitationRepository.findAllByOrganizationId(orgId)
            .map { invitationMapper.toResponse(it) }

    fun accept(invitationId: UUID) {
        val invitation = invitationRepository.findById(invitationId)
            .orElseThrow { InvitationNotFoundException(invitationId) }

        if (invitation.status != InvitationStatus.PENDING) {
            throw InvitationAlreadyProcessedException("Invitation $invitationId is already ${invitation.status}")
        }
        if (invitation.expiresAt!!.isBefore(Instant.now())) {
            throw InvitationExpiredException("Invitation $invitationId has expired")
        }

        invitation.status = InvitationStatus.ACCEPTED
        invitation.respondedAt = Instant.now()
        invitationRepository.save(invitation)

        employeesService.add(
            invitation.organizationId!!,
            AddEmployeeRequest(
                userId = invitation.invitedUserId!!,
                role = com.slavacom.organizationservice.entity.EmployeeRole.valueOf(invitation.role!!),
                permissions = invitation.permissions
            )
        )
    }

    fun decline(invitationId: UUID) {
        val invitation = invitationRepository.findById(invitationId)
            .orElseThrow { InvitationNotFoundException(invitationId) }

        if (invitation.status != InvitationStatus.PENDING) {
            throw InvitationAlreadyProcessedException("Invitation $invitationId is already ${invitation.status}")
        }

        invitation.status = InvitationStatus.DECLINED
        invitation.respondedAt = Instant.now()
        invitationRepository.save(invitation)
    }
}
