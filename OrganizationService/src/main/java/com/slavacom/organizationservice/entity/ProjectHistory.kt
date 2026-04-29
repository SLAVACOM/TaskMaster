package com.slavacom.organizationservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "project_history")
open class ProjectHistory {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.UUID)
    @field:Column(name = "id", nullable = false)
    open var id: UUID? = null

    @field:Column(name = "project_id", nullable = false)
    open var projectId: UUID? = null

    @field:Column(name = "changed_by")
    open var changedBy: UUID? = null

    @field:Column(name = "action", nullable = false)
    open var action: String? = null

    @field:Column(name = "changes", columnDefinition = "TEXT")
    open var changes: String? = null

    @field:CreationTimestamp
    @field:Column(name = "changed_at", nullable = false, updatable = false)
    open var changedAt: Instant? = null
}

