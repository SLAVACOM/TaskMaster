package com.slavacom.organizationservice.invitation

import com.slavacom.organizationservice.entity.InvitationStatus
import java.time.Instant
import java.util.UUID

data class InvitationResponse(
    val id: UUID,
    val organizationId: UUID,
    val invitedUserId: UUID,
    val invitedByUserId: UUID,
    val identifier: String,
    val role: String,
    val permissions: String?,
    val status: InvitationStatus,
    val message: String?,
    val expiresAt: Instant,
    val respondedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant?
)

data class CreateInvitationRequest(
    val invitedUserId: UUID,
    val identifier: String,
    val role: String,
    val message: String? = null,
    val permissions: String? = null,
    val expiresAt: Instant
)
