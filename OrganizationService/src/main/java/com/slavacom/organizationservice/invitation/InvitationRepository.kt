package com.slavacom.organizationservice.invitation

import com.slavacom.organizationservice.entity.InvitationStatus
import com.slavacom.organizationservice.entity.OrganizationInvitation
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface InvitationRepository : JpaRepository<OrganizationInvitation, UUID> {
    fun findAllByOrganizationId(organizationId: UUID): List<OrganizationInvitation>
    fun findByIdAndStatus(id: UUID, status: InvitationStatus): Optional<OrganizationInvitation>
    fun findAllByInvitedUserId(invitedUserId: UUID): List<OrganizationInvitation>
}
