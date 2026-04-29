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
@Table(name = "project_goals")
open class ProjectGoals {
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

    @field:Column(name = "target_date")
    open var targetDate: Instant? = null

    @field:Column(name = "progress", nullable = false)
    open var progress: Int = 0

    @field:Column(name = "is_completed", nullable = false)
    open var isCompleted: Boolean = false

    @field:Column(name = "responsible")
    open var responsible: UUID? = null

    @field:CreationTimestamp
    @field:Column(name = "created_at", nullable = false, updatable = false)
    open var createdAt: Instant? = null

    @field:UpdateTimestamp
    @field:Column(name = "updated_at")
    open var updatedAt: Instant? = null
}

