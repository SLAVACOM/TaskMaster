package com.slavacom.organizationservice.invitation

import com.slavacom.organizationservice.client.UserServiceClient
import com.slavacom.organizationservice.employees.AddEmployeeRequest
import com.slavacom.organizationservice.employees.EmployeesService
import com.slavacom.organizationservice.entity.InvitationStatus
import com.slavacom.organizationservice.exception.InvitationAlreadyProcessedException
import com.slavacom.organizationservice.exception.InvitationExpiredException
import com.slavacom.organizationservice.exception.InvitationNotFoundException
import com.slavacom.organizationservice.notification.NotificationClient
import com.slavacom.organizationservice.repository.OrganizationRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class InvitationService(
    private val invitationRepository: InvitationRepository,
    private val invitationMapper: InvitationMapper,
    private val employeesService: EmployeesService,
    private val userServiceClient: UserServiceClient,
    private val notificationClient: NotificationClient,
    private val organizationRepository: OrganizationRepository,
    @Value("\${app-url:http://localhost:3000}")
    private val appUrl: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun invite(orgId: UUID, request: CreateInvitationRequest, invitedBy: UUID): InvitationResponse {
        val invitation = invitationMapper.fromCreateRequest(request)
        invitation.organizationId = orgId
        invitation.invitedByUserId = invitedBy
        invitation.status = InvitationStatus.PENDING
        val savedInvitation = invitationRepository.save(invitation)

        try {
            sendInvitationEmailAsync(savedInvitation)
        } catch (e: Exception) {
            logger.error("Failed to send invitation email for invitation {}", savedInvitation.id, e)
        }

        return invitationMapper.toResponse(savedInvitation)
    }

    private fun sendInvitationEmailAsync(invitation: com.slavacom.organizationservice.entity.OrganizationInvitation) {
        try {
            val invitedUser = userServiceClient.getUserInfo(invitation.invitedUserId!!)
                ?: run {
                    logger.warn("Could not fetch invited user info for userId {}", invitation.invitedUserId)
                    return
                }

            val invitedByUser = userServiceClient.getUserInfo(invitation.invitedByUserId!!)
                ?: run {
                    logger.warn("Could not fetch inviter user info for userId {}", invitation.invitedByUserId)
                    return
                }

            val organization = organizationRepository.findById(invitation.organizationId!!)
                .orElse(null)
                ?: run {
                    logger.warn("Could not fetch organization for orgId {}", invitation.organizationId)
                    return
                }

            val acceptUrl = "$appUrl/invitations/${invitation.id}/accept"
            val declineUrl = "$appUrl/invitations/${invitation.id}/decline"

            notificationClient.sendInvitationEmail(
                invitedUserName = invitedUser.firstName,
                invitedEmail = invitedUser.email,
                invitedByName = invitedByUser.firstName,
                organizationName = organization.name!!,
                role = invitation.role!!,
                message = invitation.message,
                expiresAt = invitation.expiresAt!!.toString(),
                acceptUrl = acceptUrl,
                declineUrl = declineUrl
            )

            logger.info("Invitation email sent to {} for organization {}", invitedUser.email, organization.name)
        } catch (e: Exception) {
            logger.error("Error sending invitation email for invitation {}", invitation.id, e)
        }
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
