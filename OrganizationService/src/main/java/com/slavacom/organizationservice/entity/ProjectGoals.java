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
@Table(name = "project_goals")
public class ProjectGoals {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "project_id", nullable = false)
	private UUID projectId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "target_date")
	private Instant targetDate; // планируемая дата достижения

	@Column(name = "progress", nullable = false)
	private Integer progress = 0; // прогресс в процентах (0-100)

	@Column(name = "is_completed", nullable = false)
	private boolean isCompleted = false;

	@Column(name = "responsible")
	private UUID responsible; // ответственный за цель

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private Instant updatedAt;
}
