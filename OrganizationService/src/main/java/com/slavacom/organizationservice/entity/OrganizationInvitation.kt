package com.slavacom.organizationservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "organization_invitations")
open class OrganizationInvitation {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(name = "id", nullable = false)
    open var id: UUID? = null

    @field:Column(name = "organization_id", nullable = false)
    open var organizationId: UUID? = null

    @field:Column(name = "invited_user_id", nullable = false)
    open var invitedUserId: UUID? = null

    @field:Column(name = "invited_by_user_id", nullable = false)
    open var invitedByUserId: UUID? = null

    @field:Column(name = "identifier", nullable = false)
    open var identifier: String? = null

    @field:Column(name = "role", nullable = false)
    open var role: String? = null

    @field:Column(name = "permissions", columnDefinition = "TEXT")
    open var permissions: String? = null

    @field:Enumerated(EnumType.STRING)
    @field:Column(name = "status", nullable = false)
    open var status: InvitationStatus? = null

    @field:Column(name = "message")
    open var message: String? = null

    @field:Column(name = "expires_at", nullable = false)
    open var expiresAt: Instant? = null

    @field:Column(name = "responded_at")
    open var respondedAt: Instant? = null

    @field:CreationTimestamp
    @field:Column(name = "created_at", nullable = false, updatable = false)
    open var createdAt: Instant? = null

    @field:UpdateTimestamp
    @field:Column(name = "updated_at")
    open var updatedAt: Instant? = null
}

