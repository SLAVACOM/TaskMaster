package com.slavacom.organizationservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "project_employees")
public class ProjectEmployees {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "project_id", nullable = false)
	private UUID projectId;

	@Column(name = "user_id", nullable = false)
	private UUID userId; // legacy ID пользователя для совместимости с текущей схемой БД

	@Column(name = "profile_id", nullable = false)
	private UUID profileId; // ID профиля из User Service

	@Column(name = "role", nullable = false)
	private String role; // роль в проекте (LEAD, DEVELOPER, TESTER, etc.)

	@Column(name = "permissions", columnDefinition = "TEXT")
	private String permissions; // JSON строка с правами доступа в проекте

	@Column(name = "is_active", nullable = false)
	private boolean isActive = true;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private Instant updatedAt;
}
