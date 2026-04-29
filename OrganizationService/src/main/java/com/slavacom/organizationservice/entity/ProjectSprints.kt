package com.slavacom.organizationservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "project_sprints")
open class ProjectSprints {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(name = "id", nullable = false)
    open var id: UUID? = null

    @field:Column(name = "project_id", nullable = false)
    open var projectId: UUID? = null

    @field:Column(name = "name", nullable = false)
    open var name: String? = null

    @field:Column(name = "description")
    open var description: String? = null

    @field:Column(name = "start_date")
    open var startDate: Instant? = null

    @field:Column(name = "end_date")
    open var endDate: Instant? = null

    @field:Column(name = "status", nullable = false)
    open var status: String = "PLANNED"

    @field:Column(name = "is_active", nullable = false)
    open var isActive: Boolean = false

    @field:CreationTimestamp
    @field:Column(name = "created_at", nullable = false, updatable = false)
    open var createdAt: Instant? = null

    @field:UpdateTimestamp
    @field:Column(name = "updated_at")
    open var updatedAt: Instant? = null
}

