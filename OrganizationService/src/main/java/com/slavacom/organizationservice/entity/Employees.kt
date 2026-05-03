package com.slavacom.organizationservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "employees")
open class Employees {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(name = "id", nullable = false)
    open var id: UUID? = null

    @field:Column(name = "user_id", nullable = false)
    open var userId: UUID? = null

    @field:Column(name = "profile_id")
    open var profileId: UUID? = null

    @field:Column(name = "organization_id", nullable = false)
    open var organizationId: UUID? = null

    @field:Enumerated(EnumType.STRING)
    @field:Column(name = "role", nullable = false)
    open var role: EmployeeRole? = null

    @field:Column(name = "permissions", columnDefinition = "TEXT")
    open var permissions: String? = null

    @field:Column(name = "is_active", nullable = false)
    open var isActive: Boolean = true

    @field:CreationTimestamp
    @field:Column(name = "created_at", nullable = false, updatable = false)
    open var createdAt: Instant? = null

    @field:UpdateTimestamp
    @field:Column(name = "updated_at")
    open var updatedAt: Instant? = null
}

