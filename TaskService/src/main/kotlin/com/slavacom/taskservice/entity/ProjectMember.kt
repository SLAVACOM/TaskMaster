package com.slavacom.taskservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "project_members", uniqueConstraints = [
	UniqueConstraint(name = "uk_project_user", columnNames = ["project_id", "user_id"])
])
class ProjectMember(
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	var id: UUID? = null,

	@Column(name = "project_id", nullable = false)
	var projectId: UUID? = null,

	@Column(name = "user_id", nullable = false)
	var userId: UUID? = null,

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	var createdAt: Instant? = null,
)
