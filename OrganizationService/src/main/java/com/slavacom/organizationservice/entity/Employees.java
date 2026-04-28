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
@Table(name = "employees")
public class Employees {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId; // ID пользователя (для совместимости с существующей БД)

	@Column(name = "profile_id")
	private UUID profileId; // ID профиля из User Service (новое поле)

	@Column(name = "organization_id", nullable = false)
	private UUID organizationId;

	@Column(name = "role", nullable = false)
	private String role; // роль в организации (ADMIN, MANAGER, USER, etc.)

	@Column(name = "permissions", columnDefinition = "TEXT")
	private String permissions; // JSON строка с правами доступа

	@Column(name = "is_active", nullable = false)
	private boolean isActive = true;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private Instant updatedAt;
}